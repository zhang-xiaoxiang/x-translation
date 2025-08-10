package com.github.core;


import com.github.util.CollectionUtils;
import com.github.util.ReflectUtils;
import lombok.Getter;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

/**
 * TransModel: 属性翻译模型
 * <p>
 * 这是翻译框架的核心执行类，负责封装单个字段翻译的所有上下文信息和执行逻辑。
 * 该类采用高内聚、低耦合的设计原则，将翻译操作所需的数据和行为封装在一起，
 * 提供了统一的翻译执行接口。
 * </p>
 * <p>
 * 核心优势：
 * 1. 上下文封装：将翻译所需的所有信息（源对象、字段元数据、原始值等）封装在一起
 * 2. 类型兼容：支持单值、集合、数组等多种数据类型
 * 3. 灵活提取：支持普通字段提取和完整对象提取两种模式
 * 4. 智能处理：自动处理空值、类型转换等边界情况
 * </p>
 *
 * @author zhangxiaoxiang
 * @since 2025/7/27
 */
public class TransModel {

    /**
     * object value 提取标识
     * <p>
     * 用于指定提取翻译数据中的完整对象，而非特定字段。
     * </p>
     * <p>
     * 使用示例：
     * // 假设翻译数据为: {1: {status: 1, desc: "正常"}}
     * // transVal = 1 (原始值)
     * // key = "desc" (指定要提取的字段)
     * // 结果: objValue = "正常" (提取desc字段的值)
     * <p>
     * // 假设翻译数据为: {1001: {id: 1001, name: "张三", age: 25}}
     * // transVal = 1001 (原始值)
     * // key = "#val" (表示提取整个值)
     * // 结果: objValue = {id: 1001, name: "张三", age: 25} (提取整个对象)
     */
    public final static String VAL_EXTRACT = "#val";

    /**
     * 需要被翻译的属性元数据
     * <p>
     * 包含翻译字段的所有元信息，如源字段、目标字段、翻译键值、翻译仓库等。
     * 这是翻译操作的核心配置信息。
     * </p>
     */
    private final TransFieldMeta transFieldMeta;

    /**
     * 需要被翻译的属性值（源值）
     * <p>
     * 从源字段中提取的实际值，用于在翻译数据中查找对应的翻译结果。
     * 例如：userId=1001，将用于在用户翻译数据中查找id为1001的用户信息。
     * </p>
     */
    @Getter
    private final Object transVal;

    /**
     * 当前正在处理的对象实例
     * <p>
     * 这是包含需要翻译字段的原始对象实例，翻译完成后会将结果设置到该对象的
     * 目标字段中。
     * </p>
     */
    @Getter
    private final Object obj;

    /**
     * 是否是多值类型标识
     * <p>
     * 用于标识源字段是否为集合或数组类型，决定后续处理逻辑。
     * 预先计算并缓存该值可以提高性能，避免重复类型检查。
     * </p>
     * -- GETTER --
     * 判断是否为多值类型
     * true表示字段为集合或数组类型，false表示为单值类型
     */
    @Getter
    private final boolean isMultiple;

    /**
     * 是否是值提取模式标识
     * <p>
     * 当key等于VAL_EXTRACT时为true，表示需要提取翻译数据中的完整对象，
     * 而不是特定字段的值。
     * </p>
     * -- GETTER --
     * 判断是否为值提取模式
     * true表示为值提取模式，false表示为普通字段提取模式
     */
    @Getter
    private final boolean isValExtract;

    /**
     * TransModel构造函数
     * <p>
     * 在创建实例时初始化所有必要的上下文信息，包括：
     * 1. 判断字段类型（单值/多值）
     * 2. 提取源字段值
     * 3. 判断提取模式（普通/值提取）
     * </p>
     *
     * @param obj   需要进行翻译的对象实例
     * @param field 翻译字段的元数据信息
     */
    public TransModel(Object obj, TransFieldMeta field) {
        this.transFieldMeta = field;
        this.obj = obj;
        Field transField = field.getTransField();
        Class<?> type = transField.getType();
        // 预先判断是否为多值类型，提高后续处理性能
        this.isMultiple = (Iterable.class).isAssignableFrom(type) || type.isArray();
        // 提取源字段的实际值
        this.transVal = ReflectUtils.getFieldValue(this.obj, transField);
        // 判断是否为值提取模式
        this.isValExtract = VAL_EXTRACT.equals(this.transFieldMeta.getKey());
    }

    /**
     * 设置对象字段的值（核心翻译执行方法）
     * <p>
     * 根据提供的翻译数据映射，执行实际的翻译操作，将翻译结果设置到目标字段中。
     * 该方法支持多种复杂场景：
     * 1. 单值翻译 vs 多值翻译
     * 2. 普通字段提取 vs 完整对象提取
     * 3. 集合类型 vs 数组类型 vs 普通类型
     * </p>
     *
     * @param transValMap 包含转换值和对象值的映射，键为源值，值为翻译数据对象
     */
    public void setValue(Map<Object, Object> transValMap) {
        // 将传入的映射转换为对象值映射，便于后续字段提取
        // 这一步将翻译数据对象转换为Map格式，提高字段访问效率
        Map<Object, ? extends Map<?, ?>> objValMap = transValMap.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> ReflectUtils.beanToMap(entry.getValue())));

        Object objValue = null;

        // 根据是否为多值类型采用不同的处理逻辑
        if (this.isMultiple) {
            // 处理多值场景（集合或数组）
            // 获取多个转换值
            List<Object> multipleTransVal = getMultipleTransVal();
            // 获取对象值（根据目标字段类型创建合适的容器）
            objValue = getObjValue(multipleTransVal);

            // 根据目标字段的具体类型进行处理
            if (objValue instanceof Collection) {
                @SuppressWarnings("unchecked")
                // 转换为集合类型
                Collection<Object> objCollection = (Collection<Object>) objValue;
                // 遍历多个转换值，为每个值执行翻译操作
                multipleTransVal.forEach(val -> {
                    if (this.isValExtract) {
                        // 如果是提取所有值（值提取模式）
                        for (Map<?, ?> objMap : objValMap.values()) {
                            objCollection.add(objMap.get(val));
                        }
                    } else {
                        // 否则根据转换值获取对应的对象值（普通字段提取）
                        Map<?, ?> objMap = objValMap.get(val);
                        if (objMap != null) {
                            objCollection.add(objMap.get(this.transFieldMeta.getKey()));
                        }
                    }
                });
            } else if (objValue instanceof Object[]) {
                // 转换为数组类型
                Object[] objArray = (Object[]) objValue;
                // 遍历多个转换值
                for (int i = 0; i < multipleTransVal.size(); i++) {
                    if (this.isValExtract) {
                        // 如果是提取所有值（值提取模式）
                        for (Map<?, ?> objMap : objValMap.values()) {
                            objArray[i] = objMap.get(multipleTransVal.get(i));
                        }
                    } else {
                        // 否则根据转换值获取对应的对象值（普通字段提取）
                        Map<?, ?> objMap = objValMap.get(multipleTransVal.get(i));
                        if (objMap != null) {
                            objArray[i] = objMap.get(this.transFieldMeta.getKey());
                        }
                    }
                }
            }
        } else {
            // 处理单值场景
            if (this.isValExtract) {
                // 如果是提取所有值（值提取模式）
                for (Map<?, ?> value : objValMap.values()) {
                    objValue = value.get(this.transVal);
                }
            } else {
                // 否则根据转换值获取对应的对象值（普通字段提取）
                Map<?, ?> objMap = objValMap.get(this.transVal);
                if (objMap != null) {
                    objValue = objMap.get(this.transFieldMeta.getKey());
                }
            }
        }

        // 如果对象值不为空，则设置对象字段的值
        if (objValue != null) {
            // 核心逻辑：设置对象字段的值
            ReflectUtils.setFieldValue(this.obj, this.transFieldMeta.getField(), objValue);
        }
    }


    /**
     * 根据多个转换值获取对象值（目标容器创建方法）
     * <p>
     * 根据目标字段的类型创建合适的容器来存储翻译结果。
     * 如果目标字段已有值则直接使用，否则根据类型创建新的容器实例。
     * 这种设计既保证了类型兼容性，又支持了复用已有容器的场景。
     * </p>
     *
     * @param multipleTransVal 多个转换值，用于确定数组大小
     * @return 适当类型的对象值容器
     */
    private Object getObjValue(List<Object> multipleTransVal) {
        // 获取对象字段的值
        Object objValue = ReflectUtils.getFieldValue(this.obj, this.transFieldMeta.getField());

        // 如果对象字段的值为空，则根据字段类型创建合适的容器
        if (objValue == null) {
            // 获取对象字段的类型
            Class<?> type = this.transFieldMeta.getField().getType();

            // 如果字段类型是List，创建ArrayList实例
            if ((List.class).isAssignableFrom(type)) {
                objValue = new ArrayList<>();
            }
            // 如果字段类型是Set，创建HashSet实例
            else if ((Set.class).isAssignableFrom(type)) {
                objValue = new HashSet<>();
            }
            // 如果字段类型是数组，创建相应类型和大小的数组实例
            else if (type.isArray()) {
                // 创建一个新的数组实例，大小为多个转换值的大小
                objValue = Array.newInstance(type.getComponentType(), multipleTransVal.size());
            }
        }
        return objValue;
    }


    /**
     * 获取翻译字段的元数据信息
     *
     * @return 翻译字段元数据
     */
    public TransFieldMeta getTransField() {
        return transFieldMeta;
    }

    /**
     * 获取多个转换值
     * <p>
     * 使用工具类将源值转换为List格式，统一处理单值和多值场景。
     * </p>
     *
     * @return 转换值列表
     */
    public List<Object> getMultipleTransVal() {
        return CollectionUtils.objToList(this.transVal);
    }

    /**
     * 判断是否需要进行翻译
     * <p>
     * 通过检查源值是否为空来判断，为空则无需翻译。
     * </p>
     *
     * @return true表示需要翻译，false表示无需翻译
     */
    public boolean needTrans() {
        return transVal != null;
    }

}
