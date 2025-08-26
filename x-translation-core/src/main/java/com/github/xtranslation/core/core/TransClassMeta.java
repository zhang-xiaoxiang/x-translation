package com.github.xtranslation.core.core;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.github.xtranslation.core.annotation.Trans;
import com.github.xtranslation.core.repository.TransRepository;
import io.vavr.Tuple;
import io.vavr.Tuple4;
import io.vavr.control.Option;
import io.vavr.control.Try;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
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
        List<Field> declaredFields = CollUtil.toList(ReflectUtil.getFields(this.clazz));
        // 创建一个字段名称到字段对象的映射(如果出现重复键，默认保留旧值。)
        Map<String, Field> fieldNameMap = declaredFields.stream().collect(Collectors.toMap(Field::getName, x -> x, (o, n) -> o));

        // 使用函数式方式处理字段
        List<TransFieldMeta> transFieldMetas = declaredFields.stream()
                // 过滤特殊字段
                .filter(field -> !isSpecialField(field))
                // 转换为TransFieldMeta
                .map(field -> createTransFieldMeta(field, fieldNameMap))
                // 过滤掉null值
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // 构建Trans字段的解析树,也是处理嵌套翻译场景
        this.transFieldMetaList = buildTransTree(transFieldMetas);
    }

    /**
     * 判断是否为特殊字段（static, final, volatile, transient）
     *
     * @param field 字段
     * @return boolean 是否为特殊字段
     */
    private boolean isSpecialField(Field field) {
        int mod = field.getModifiers();
        return Modifier.isStatic(mod) || Modifier.isFinal(mod) || Modifier.isVolatile(mod) || Modifier.isTransient(mod);
    }

    /**
     * 根据字段创建TransFieldMeta对象
     *
     * @param field        字段
     * @param fieldNameMap 字段名称映射
     * @return TransFieldMeta对象，如果字段不符合条件则返回null
     */
    private TransFieldMeta createTransFieldMeta(Field field, Map<String, Field> fieldNameMap) {
        Trans transAnno = field.getAnnotation(Trans.class);

        // 使用Vavr处理字段注解
        Option<TransAnnotationResult> resultOption = Option.of(transAnno)
                .fold(
                        // 如果没有直接的Trans注解，则处理字段上的所有注解
                        () -> Option.of(processFieldAnnotations(field)),
                        // 如果有直接的Trans注解，则返回null（因为不需要处理嵌套注解）
                        trans -> Option.none()
                );

        // 提取注解信息 - 简化处理逻辑
        Option<Tuple4<Class<? extends TransRepository>, String, String, Annotation>> tupleOption =
                resultOption.map(result -> Tuple.of(result.repository, result.trans, result.key, result.transAnnotation));

        // 如果没有从嵌套注解中获取到信息，且字段上有直接的Trans注解
        tupleOption = tupleOption.orElse(Option.of(transAnno).map(trans -> Tuple.of(trans.repository(), trans.transKey(), trans.transField(), trans)));


        // 获取元组数据
        Tuple4<Class<? extends TransRepository>, String, String, Annotation> annotationData =
                tupleOption.getOrElse(Tuple.of(null, null, null, null));

        Class<? extends TransRepository> repository = annotationData._1;
        String trans = annotationData._2;
        String key = annotationData._3;
        Annotation transAnnotation = annotationData._4 != null ? annotationData._4 : transAnno;

        // 验证必要条件并创建TransFieldMeta对象
        return Option.of(trans)
                .filter(t -> StrUtil.isNotEmpty(t) && fieldNameMap.containsKey(t))
                .map(t -> {
                    // 设置默认key值
                    String finalKey = StrUtil.isEmpty(key) ? field.getName() : key;
                    return new TransFieldMeta(field, fieldNameMap.get(t), finalKey, repository, transAnnotation);
                })
                .getOrElse((TransFieldMeta) null);
    }

    /**
     * 处理字段上的所有注解，查找被 @Trans 注解标记的注解
     * <p>
     * 遍历字段上的所有注解，查找第一个被 @Trans 注解标记的注解，并提取相关属性
     * </p>
     *
     * @param field 要处理的字段
     * @return TransAnnotationResult 包含找到的 @Trans 注解信息，如果未找到则返回 null
     */
    private TransAnnotationResult processFieldAnnotations(Field field) {
        // 获取字段上所有的直接注解
        Annotation[] annotations = field.getDeclaredAnnotations();
        // 使用Vavr处理嵌套注解的情况
        return io.vavr.collection.List.of(annotations)
                .find(annotation -> annotation.annotationType().getAnnotation(Trans.class) != null)
                .map(annotation -> {
                    Class<? extends Annotation> annotationType = annotation.annotationType();
                    Trans transAnno = annotationType.getAnnotation(Trans.class);
                    Class<? extends TransRepository> repository = transAnno.repository();

                    // 处理 trans 属性值，优先使用注解直接定义的值，否则通过反射获取
                    String trans = Option.of(transAnno.transKey())
                            .filter(StrUtil::isNotEmpty)
                            .getOrElse(() -> Try.of(() -> annotationType.getMethod(Trans.TRANS_KEY_ATTR).invoke(annotation))
                                    .map(obj -> (String) obj)
                                    .getOrElse((String) null));

                    // 处理 key 属性值，同上
                    String key = Option.of(transAnno.transField())
                            .filter(StrUtil::isNotEmpty)
                            .getOrElse(() -> Try.of(() -> annotationType.getMethod(Trans.TRANS_FIELD_ATTR).invoke(annotation))
                                    .map(obj -> (String) obj)
                                    .getOrElse((String) null));

                    // 返回结果
                    return new TransAnnotationResult(repository, trans, key, annotation);
                })
                .getOrElse((TransAnnotationResult) null);
    }


    /**
     * 用于存储找到的 @Trans 注解信息的结果类
     */
    private static class TransAnnotationResult {
        final Class<? extends TransRepository> repository;
        final String trans;
        final String key;
        final Annotation transAnnotation;

        TransAnnotationResult(Class<? extends TransRepository> repository, String trans, String key, Annotation transAnnotation) {
            this.repository = repository;
            this.trans = trans;
            this.key = key;
            this.transAnnotation = transAnnotation;
        }
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
