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
 *
 * @author zhangxiaoxiang
 * @since 2025/7/27
 */
public class TransModel {

    /**
     * object value 提取标识
     */
    public final static String VAL_EXTRACT = "#val";

    /**
     * 需要被翻译的属性
     */
    private final TransFieldMeta transFieldMeta;

    /**
     * 需要被翻译的属性值
     */
    @Getter
    private final Object transVal;

    /**
     * 当前对象
     */
    @Getter
    private final Object obj;

    /**
     * 是否是多值
     */
    private final boolean isMultiple;

    /**
     * 是否是值提取
     */
    private final boolean isValExtract;

    public TransModel(Object obj, TransFieldMeta field) {
        this.transFieldMeta = field;
        this.obj = obj;
        Field transField = field.getTransField();
        Class<?> type = transField.getType();
        this.isMultiple = (Iterable.class).isAssignableFrom(type) || type.isArray();
        this.transVal = ReflectUtils.getFieldValue(this.obj, transField);
        this.isValExtract = VAL_EXTRACT.equals(this.transFieldMeta.getKey());
    }

    /**
     * 设置对象字段的值
     *
     * @param transValMap 包含转换值和对象值的映射
     */
    public void setValue(Map<Object, Object> transValMap) {
        // 将传入的映射转换为对象值映射
        Map<Object, ? extends Map<?, ?>> objValMap = transValMap.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> ReflectUtils.beanToMap(entry.getValue())));

        Object objValue = null;

        if (this.isMultiple) {
            // 获取多个转换值
            List<Object> multipleTransVal = getMultipleTransVal();
            // 获取对象值
            objValue = getObjValue(multipleTransVal);

            if (objValue instanceof Collection) {
                @SuppressWarnings("unchecked")
                // 转换为集合类型
                Collection<Object> objCollection = (Collection<Object>) objValue;
                // 遍历多个转换值
                multipleTransVal.forEach(val -> {
                    if (this.isValExtract) {
                        // 如果是提取所有值
                        for (Map<?, ?> objMap : objValMap.values()) {
                            objCollection.add(objMap.get(val));
                        }
                    } else {
                        // 否则根据转换值获取对应的对象值
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
                        // 如果是提取所有值
                        for (Map<?, ?> objMap : objValMap.values()) {
                            objArray[i] = objMap.get(multipleTransVal.get(i));
                        }
                    } else {
                        // 否则根据转换值获取对应的对象值
                        Map<?, ?> objMap = objValMap.get(multipleTransVal.get(i));
                        if (objMap != null) {
                            objArray[i] = objMap.get(this.transFieldMeta.getKey());
                        }
                    }
                }
            }
        } else {
            // 如果不是多个转换值
            if (this.isValExtract) {
                // 如果是提取所有值
                for (Map<?, ?> value : objValMap.values()) {
                    objValue = value.get(this.transVal);
                }
            } else {
                // 否则根据转换值获取对应的对象值
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
     * 根据多个转换值获取对象值
     *
     * @param multipleTransVal 多个转换值
     * @return 对象值
     */
    private Object getObjValue(List<Object> multipleTransVal) {
        // 获取对象字段的值
        Object objValue = ReflectUtils.getFieldValue(this.obj, this.transFieldMeta.getField());

        // 如果对象字段的值为空
        if (objValue == null) {
            // 获取对象字段的类型
            Class<?> type = this.transFieldMeta.getField().getType();

            // 如果字段类型是List
            if ((List.class).isAssignableFrom(type)) {
                objValue = new ArrayList<>();
            }
            // 如果字段类型是Set
            else if ((Set.class).isAssignableFrom(type)) {
                objValue = new HashSet<>();
            }
            // 如果字段类型是数组
            else if (type.isArray()) {
                // 创建一个新的数组实例，大小为多个转换值的大小
                objValue = Array.newInstance(type.getComponentType(), multipleTransVal.size());
            }
        }
        return objValue;
    }


    public TransFieldMeta getTransField() {
        return transFieldMeta;
    }

    public boolean isMultiple() {
        return isMultiple;
    }

    public List<Object> getMultipleTransVal() {
        return CollectionUtils.objToList(this.transVal);
    }

    public boolean needTrans() {
        return transVal != null;
    }

    public boolean isValExtract() {
        return isValExtract;
    }

}
