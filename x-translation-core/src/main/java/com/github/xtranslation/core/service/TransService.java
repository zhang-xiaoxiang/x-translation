package com.github.xtranslation.core.service;


import cn.hutool.core.collection.CollUtil;
import com.github.xtranslation.core.core.TransFieldMeta;
import com.github.xtranslation.core.core.TransModel;
import com.github.xtranslation.core.manager.TransClassMetaCacheManager;
import com.github.xtranslation.core.repository.TransRepository;
import com.github.xtranslation.core.repository.TransRepositoryFactory;
import com.github.xtranslation.core.resolver.TransObjResolver;
import com.github.xtranslation.core.resolver.TransObjResolverFactory;
import com.github.xtranslation.core.util.CollectionUtils;
import io.vavr.Tuple;
import io.vavr.control.Option;
import lombok.Setter;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static io.vavr.API.*;


/**
 * TransService: 翻译服务类，用于将对象中的字段值翻译成其他格式。
 *
 * @author zhangxiaoxiang
 * @since 2025/7/27
 */
public class TransService {

    @Setter
    private ExecutorService executor;

    // 在Java中，volatile关键字用于多线程环境下的变量可见性控制
    private volatile boolean isInit = false;

    /**
     * 初始化方法
     * 如果executor为空，则创建一个新的线程池，并使用指定的线程工厂来创建线程。
     * 设置线程名称为"trans-thread-"加上任务对象的hashCode值。
     * 最后，将isInit标记为true，表示已经初始化。
     */
    public void init() {
        // 使用Option处理executor为null的情况
        Option.of(this.executor).onEmpty(() -> this.executor = Executors.newCachedThreadPool(r -> new Thread(r, "trans-thread-" + r.hashCode())));
        // 这个方法会将isInit设置为true，表示TransService已经初始化完成。
        isInit = true;
    }

    /**
     * @param obj 需要被翻译的对象
     * @return 是否翻译成功
     */
    public boolean trans(Object obj) {
        // 检查线程池是否准备好，并且对象解析后不为空
        return Option.of(obj)
                // 线程池是否准备好
                .filter(o -> isInit)
                // 解析对象
                .map(this::resolveObj)
                // 解析后的对象不为空
                .filter(Objects::nonNull)
                // 转换为列表
                .map(CollectionUtils::objToList)
                // 列表不为空
                .filter(CollUtil::isNotEmpty)
                // 不是Java内置类(排除几乎不可能翻译的类型,例如String,Integer等)
                .filter(list -> !list.get(0).getClass().getName().startsWith("java."))
                // 获取元数据信息
                .map(list -> Tuple.of(list, TransClassMetaCacheManager.getTransClassMeta(list.get(0).getClass())))
                // 检查是否需要翻译
                .filter(tuple -> tuple._2.needTrans())
                .map(tuple -> {
                    // 执行翻译赋值的核心方法
                    this.doTrans(tuple._1, tuple._2.getTransFieldList());
                    return true;
                })
                // 默认返回false
                .getOrElse(false);
    }

    /**
     * 解析对象(一般就是controller层封装的Result(code/msg/data)对象)
     *
     * @param obj 需要解析的对象
     * @return 解析后的对象，如果对象无法解析或为空，则返回原对象
     */
    private Object resolveObj(Object obj) {
        // 使用Option处理null值检查
        return Option.of(obj)
                .map(o -> {
                    // 通过EasyTransRegister取到使用方的包装类(可以是多个)
                    List<TransObjResolver> resolvers = TransObjResolverFactory.getResolvers();

                    // 查找第一个支持该对象的解析器并处理
                    return resolvers.stream()
                            .filter(resolver -> resolver.support(o))
                            .findFirst()
                            .map(resolver -> {
                                // 解析对象
                                Object resolvedObj = resolver.resolveTransObj(o);
                                // 递归处理嵌套翻译
                                return resolveObj(resolvedObj);
                            })
                            // 如果没有找到支持的解析器，返回原对象
                            .orElse(o);
                })
                .getOrNull();
    }

    /**
     * 执行转换操作
     *
     * @param needTransVOList    需要转换的VO对象列表
     * @param transFieldMetaList 对应翻译元字段列表
     */
    private void doTrans(List<Object> needTransVOList, List<TransFieldMeta> transFieldMetaList) {
        // 将转换字段信息按仓库类分组
        Map<? extends Class<? extends TransRepository>, List<TransFieldMeta>> listMap = transFieldMetaList.stream().collect(Collectors.groupingBy(TransFieldMeta::getRepository));

        // 使用 Match 替代 if-else
        Match(listMap.size() > 1).of(
                Case($(true), () -> {
                    // 使用CompletableFuture并发执行多个转换操作
                    CompletableFuture.allOf(listMap.entrySet().stream().map(entry -> CompletableFuture.runAsync(() ->
                                    // 递归调用doTrans方法处理每个分组
                                    this.doTrans(needTransVOList, entry.getKey(), entry.getValue()), executor)).toArray(CompletableFuture[]::new))
                            .join();
                    return null;
                }),
                Case($(), () -> {
                    // 如果分组数量不大于1，表示只有一个仓库类需要处理
                    listMap.forEach((transClass, transFields) -> this.doTrans(needTransVOList, transClass, transFields));
                    return null;
                })
        );
    }


    /**
     * 执行转换操作
     *
     * @param needTransVOList    需要转换的VO对象列表
     * @param transClass         转换仓库类
     * @param transFieldMetaList 对应翻译元字段列表
     */
    private void doTrans(List<Object> needTransVOList, Class<? extends TransRepository> transClass, List<TransFieldMeta> transFieldMetaList) {
        Option.of(TransRepositoryFactory.getTransRepository(transClass))
                .forEach(transRepository -> {
                    // 获取需要被翻译的集合Map<trans, List < TransModel>>
                    Map<String, List<TransModel>> transMap = this.getTransMap(needTransVOList, transFieldMetaList);

                    // 使用Option处理transMap非空情况
                    Option.of(transMap)
                            .filter(CollUtil::isNotEmpty)
                            .forEach(map -> doTrans0(transRepository, map));

                    // 有嵌套属性,就继续翻译
                    transFieldMetaList.forEach(transField ->
                            Option.of(transField.getChildren())
                                    .filter(CollUtil::isNotEmpty)
                                    .forEach(children -> doTrans(needTransVOList, children))
                    );
                });
    }

    /**
     * 获取需要翻译的集合
     *
     * @param needTransVOList    需要被翻译的对象集合
     * @param transFieldMetaList 需要被翻译的属性
     * @return 需要被翻译的集合Map<trans, List < TransModel>>
     */
    private Map<String, List<TransModel>> getTransMap(List<Object> needTransVOList, List<TransFieldMeta> transFieldMetaList) {
        // 将toTransList中的每个TransFieldMeta对象与objList中的每个对象进行映射，生成TransModel对象
        return transFieldMetaList.stream()
                // 对每个TransFieldMeta对象，将其与objList中的每个对象进行映射，生成TransModel对象
                .flatMap(x -> needTransVOList.stream().map(o -> new TransModel(o, x)))
                // 过滤出需要翻译的TransModel对象
                .filter(TransModel::needTrans)
                // 根据TransFieldMeta对象的trans属性对TransModel对象进行分组
                .collect(Collectors.groupingBy(x -> x.getTransField().getTrans()));
    }


    /**
     * 执行转换操作（具体实现）
     * 把需要翻译的在总数据数据仓库仅需对比,对需要翻译的仅需赋值翻译
     *
     * @param transRepository 转换仓库
     * @param transMap        需要转换的模型映射，键为转换标识，值为模型列表
     */
    private void doTrans0(TransRepository transRepository, Map<String, List<TransModel>> transMap) {
        boolean b = transMap.size() > 1;
        // 分组查询
        Match(b).of(
                Case($(b), () -> {
                    CompletableFuture<?>[] futures = transMap.values()
                            .stream()
                            .map(transModels -> CompletableFuture.runAsync(() -> doTrans(transRepository, transModels), executor))
                            .toArray(CompletableFuture[]::new);
                    CompletableFuture.allOf(futures).join();
                    return null; // Void方法需要返回null
                }),
                Case($(), () -> {
                    transMap.values().forEach(transModels -> doTrans(transRepository, transModels));
                    return null; // Void方法需要返回null
                })
        );
    }

    /**
     * 执行转换操作
     *
     * @param transRepository 转换仓库
     * @param transModels     包含转换模型的列表
     */
    private void doTrans(TransRepository transRepository, List<TransModel> transModels) {
        // 获取所有转换模型中需要转换的值，去重后存入List
        List<Object> transIdList = transModels.stream()
                .map(TransModel::getMultipleTransVal)
                .flatMap(Collection::stream)
                .distinct()
                .collect(Collectors.toList());

        // 获取转换注解
        // 它们都属于同一个字段（同一个 TransFieldMeta）
        // 它们都使用相同的翻译仓库（TransRepository）
        // 它们都使用相同的翻译注解（@Trans 或相关注解）
        Annotation transAnno = transModels.get(0).getTransField().getTransAnno();

        // 获取转换值映射(使用者提供的数据源) userId -> userDO(数据库实体的对象),相当于根据id获取到id map
        Map<Object, Object> valueMap = transRepository.getTransValueMap(transIdList, transAnno);

        // 如果转换值映射不为空
        Option.of(valueMap)
                .filter(CollUtil::isNotEmpty)
                // 遍历转换模型，设置转换后的值
                .peek(map -> transModels.forEach(transModel -> transModel.setValue(map)));

    }


}
