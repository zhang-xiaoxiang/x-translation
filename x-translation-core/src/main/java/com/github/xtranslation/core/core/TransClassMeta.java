package com.github.xtranslation.core.core;


import com.github.xtranslation.core.annotation.Trans;
import com.github.xtranslation.core.repository.TransRepository;
import com.github.xtranslation.core.util.ReflectUtils;
import com.github.xtranslation.core.util.StringUtils;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

/**
 * TransClassMeta: 翻译类元数据信息
 * <p>
 * 该类用于存储和解析需要进行翻译操作的类的元数据信息。它会分析类中的字段，
 * 识别带有 @Trans 注解的字段，并构建这些字段之间的层级关系树，为后续的翻译
 * 处理提供结构化的数据支持。
 * </p>
 *
 * @author zhangxiaoxiang
 * @since 2025/7/27
 */
public class TransClassMeta implements Serializable {

    private static final long serialVersionUID = -8211850528694193388L;

    /**
     * 需要翻译的目标类
     * <p>
     * 这是包含需要翻译字段的原始类，通过反射分析该类的所有字段来识别
     * 带有 @Trans 注解的字段。
     * </p>
     */
    private final Class<?> clazz;

    /**
     * 需要翻译的字段集合
     * <p>
     * 存储所有解析后的 TransFieldMeta 对象，这些对象包含了翻译所需的所有信息，
     * 包括源字段、目标字段、翻译键值、翻译仓库等。该列表以树形结构组织，支持
     * 嵌套翻译场景。
     * </p>
     */
    private List<TransFieldMeta> transFieldMetaList = new ArrayList<>();


    /**
     * TransClassMeta的构造函数
     * <p>
     * 在创建 TransClassMeta 实例时会立即解析传入类中的所有 @Trans 注解字段，
     * 并构建字段间的层级关系树。这种"创建即用"的设计确保了对象状态的完整性。
     * </p>
     *
     * @param clazz 要解析的类，该类应包含需要翻译的字段
     */
    public TransClassMeta(Class<?> clazz) {
        // 保存传入的类对象
        this.clazz = clazz;
        // 解析带有Trans注解的字段(带有递归遍历树节点等操作)
        parseTransField();
    }

    /**
     * 递归查找并设置子节点
     * <p>
     * 通过递归方式为字段节点设置子节点，构建完整的树形结构。
     * 当某个字段的名称与另一个字段的trans值匹配时，就形成父子关系。
     * </p>
     *
     * @param root    根节点列表
     * @param tempMap 临时映射表，用于存储字段名到TransFieldMeta列表的映射关系
     */
    public static void findChildren(List<TransFieldMeta> root, Map<String, List<TransFieldMeta>> tempMap) {
        root.stream()
                .filter(x -> tempMap.containsKey(x.getField().getName()))
                .forEach(x -> {
                    List<TransFieldMeta> children = tempMap.get(x.getField().getName());
                    x.setChildren(children);
                    findChildren(children, tempMap);
                });
    }

    /**
     * 获取需要翻译的字段列表
     * <p>
     * 返回已解析的字段元数据列表，该列表以树形结构组织，每个节点代表一个翻译字段，
     * 可能包含子节点表示嵌套的翻译关系。
     * </p>
     *
     * @return 字段集合，以树形结构组织的 TransFieldMeta 列表
     */
    public List<TransFieldMeta> getTransFieldList() {
        return this.transFieldMetaList;
    }

    /**
     * 解析带有Trans注解的字段
     * <p>
     * 这是核心解析方法，负责分析类中的所有字段，识别带有 @Trans 注解的字段，
     * 提取翻译所需的元数据信息，并创建对应的 TransFieldMeta 对象。该方法支持：
     * 1. 忽略 static、final、volatile、transient 等特殊修饰符的字段
     * 2. 直接在字段上使用 @Trans 注解
     * 3. 通过其他注解间接使用 @Trans 注解（组合注解模式）
     * 4. 构建字段间的层级关系树
     * </p>
     */
    private void parseTransField() {
        // 获取类的所有字段，包括父类中的字段
        List<Field> declaredFields = ReflectUtils.getAllField(this.clazz);
        // 创建一个字段名称到字段对象的映射(如果出现重复键，默认保留旧值。)
        Map<String, Field> fieldNameMap = declaredFields.stream().collect(Collectors.toMap(Field::getName, x -> x, (o, n) -> o));
        int mod;
        List<TransFieldMeta> transFieldMetas = new ArrayList<>();
        // 循环遍历所有的属性进行判断
        for (Field field : declaredFields) {
            mod = field.getModifiers();
            // 如果是 static, final, volatile, transient 的字段，则直接跳过
            if (Modifier.isStatic(mod) || Modifier.isFinal(mod) || Modifier.isVolatile(mod) || Modifier.isTransient(mod)) {
                continue;
            }

            Trans transAnno = field.getAnnotation(Trans.class);
            String trans = null;
            String key = null;
            Class<? extends TransRepository> repository = null;
            Annotation transAnnotation = transAnno;
            // 解析字段上的 Trans 注解，如果字段没有直接标注 Trans 注解，则检查其所有注解是否包含 Trans 注解
            if (transAnno == null) {
                // 获取字段上所有的直接注解
                Annotation[] annotations = field.getDeclaredAnnotations();
                // 处理嵌套注解的情况，即一个字段上可能有多个注解，其中一个或多个注解可能包含 @Trans 注解
                for (Annotation annotation : annotations) {
                    // 获取注解的类型
                    Class<? extends Annotation> annotationType = annotation.annotationType();
                    // 检查这个注解类型是否被 @Trans 注解标记
                    transAnno = annotationType.getAnnotation(Trans.class);
                    if (transAnno != null) {
                        // 如果找到了被 @Trans 标记的注解，则提取相关属性
                        repository = transAnno.repository();
                        // 处理 trans 属性值，优先使用注解直接定义的值，否则通过反射获取
                        trans = StringUtils.isNotEmpty(transAnno.transKey()) ? transAnno.transKey() : (String) ReflectUtils.invokeAnnotation(annotationType, annotation,  Trans.TRANS_KEY_ATTR);
                        // 处理 key 属性值，同上
                        key = StringUtils.isNotEmpty(transAnno.transField()) ? transAnno.transField() : (String) ReflectUtils.invokeAnnotation(annotationType, annotation,  Trans.TRANS_FIELD_ATTR);
                        // 保存实际的注解实例（可能是组合注解）
                        transAnnotation = annotation;
                        break; // 找到第一个匹配的就退出循环(因为最多只有1个)
                    }
                }
            } else {
                // 如果字段有 Trans 注解，直接获取 trans、key 和 repository 属性
                repository = transAnno.repository();
                trans = transAnno.transKey();
                key = transAnno.transField();
            }
            if (StringUtils.isEmpty(trans)) {
                // 没有翻译字段继续递归
                continue;
            }
            if (!fieldNameMap.containsKey(trans)) {
                //  如果翻译字段不存在 则跳过当前字段
                continue;
            }
            if (StringUtils.isEmpty(key)) {
                // 约定优于配置：提供合理的默认行为，如果key为空则使用字段名作为键值
                // 例如   @Trans(trans = "userName", key = "userId", using = UserTransRepository.class)
                // 等价于 @Trans(trans = "userName", using = UserTransRepository.class),若果不是userId,key可以直接省略,不是uId,那么就手动指定即可
                key = field.getName();
            }
            // 将解析到的Trans字段信息添加到列表中
            transFieldMetas.add(new TransFieldMeta(field, fieldNameMap.get(trans), key, repository, transAnnotation));
        }
        // 构建Trans字段的解析树,也是处理嵌套翻译场景
        this.transFieldMetaList = buildTransTree(transFieldMetas);
    }

    /**
     * 构建Trans字段的解析树
     * <p>
     * 将扁平的字段元数据列表转换为树形结构，用于支持嵌套翻译场景。
     * 例如，字段A的翻译结果可能被字段B使用，这种依赖关系通过树形结构表示。
     * 算法思路：
     * 1. 构建字段名到字段列表的映射关系
     * 2. 识别根节点（其trans值不指向其他字段的字段）
     * 3. 为每个根节点递归查找并设置子节点
     * </p>
     *
     * @param transFieldMetas Trans字段信息列表
     * @return 构建好的Trans字段解析树
     */
    private List<TransFieldMeta> buildTransTree(List<TransFieldMeta> transFieldMetas) {
        Map<String, List<TransFieldMeta>> transMap = transFieldMetas.stream().collect(Collectors.groupingBy(TransFieldMeta::getTrans));
        Map<String, List<TransFieldMeta>> filedNameMap = transFieldMetas.stream().collect(Collectors.groupingBy(x -> x.getField().getName()));

        return transFieldMetas
                .stream()
                .filter(m -> !filedNameMap.containsKey(m.getTrans()))
                .peek(m -> findChildren(Collections.singletonList(m), transMap))
                .collect(toList());
    }

    /**
     * 判断当前类是否需要进行翻译处理
     * <p>
     * 通过检查是否存在需要翻译的字段来判断，如果 transFieldMetaList 不为空，
     * 则表示该类包含需要翻译的字段，需要进行翻译处理。
     * </p>
     *
     * @return boolean 是否需要翻译，true表示需要翻译，false表示不需要翻译
     */
    public boolean needTrans() {
        return !transFieldMetaList.isEmpty();
    }

}
