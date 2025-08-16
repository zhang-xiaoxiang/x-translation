package com.github.xtranslation.core.service;


import com.github.xtranslation.core.core.TransClassMeta;
import com.github.xtranslation.core.core.TransFieldMeta;
import com.github.xtranslation.core.core.TransModel;
import com.github.xtranslation.core.manager.TransClassMetaCacheManager;
import com.github.xtranslation.core.repository.TransRepository;
import com.github.xtranslation.core.repository.TransRepositoryFactory;
import com.github.xtranslation.core.resolver.TransObjResolver;
import com.github.xtranslation.core.resolver.TransObjResolverFactory;
import com.github.xtranslation.core.util.CollectionUtils;
import lombok.Setter;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

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
        if (this.executor == null) {
            this.executor = Executors.newCachedThreadPool(r -> new Thread(r, "trans-thread-" + r.hashCode()));
        }
        // 这个方法会将isInit设置为true，表示TransService已经初始化完成。
        isInit = true;
    }

    /**
     * @param obj 需要被翻译的对象
     * @return 是否翻译成功
     */
    public boolean trans(Object obj) {
        if (!isInit) {
            // 如果线程池没有准备好,那么就无法进行翻译操作,直接返回false
            return false;
        }
        // 获取解析对象
        obj = resolveObj(obj);
        if (obj == null) {
            // 解析的对象为空,当然不用翻译
            return false;
        }
        List<Object> objList = CollectionUtils.objToList(obj);
        if (CollectionUtils.isEmpty(objList)) {
            // 空集合当然也不用翻译
            return false;
        }
        Class<?> objClass = objList.get(0).getClass();
        if (objClass.getName().startsWith("java.")) {
            // 跳过Java内置类（如String、Integer、List等 这些类通常不包含需要翻译的业务字段,提高点新能
            return false;
        }
        // 因为是一种类型,说以这里取objClass即可
        TransClassMeta info = TransClassMetaCacheManager.getTransClassMeta(objClass);
        if (!info.needTrans()) {
            // 检查对象类是否包含需要翻译的字段
            return false;
        }
        // 执行翻译赋值的核心方法
        doTrans(objList, info.getTransFieldList());
        return true;
    }

    /**
     * 解析对象(一般就是controller层封装的Result(code/msg/data)对象)
     *
     * @param obj 需要解析的对象
     * @return 解析后的对象，如果对象无法解析或为空，则返回原对象
     */
    private Object resolveObj(Object obj) {
        if (obj == null) {
            return null;
        }
        // 通过EasyTransRegister取到使用方的包装类(可以是多个)
        List<TransObjResolver> resolvers = TransObjResolverFactory.getResolvers();
        // 判断是否需要解析
        boolean resolve = false;
        Object resolvedObj = obj;
        for (TransObjResolver resolver : resolvers) {
            if (resolver.support(obj)) {
                resolvedObj = resolver.resolveTransObj(obj);
                // for循环中一旦找到能处理当前对象的解析器就立即break跳出，因为递归调用会继续对解析后的对象进行同样的查找过程，所以每次只需要找到第一个匹配的解析器即可，不需要遍历所有解析器。
                resolve = true;
                break;
            }
        }
        if (resolve) {
            // 处理嵌套翻译,例如 Result<Page<UserDto>> result = new Result<>();
            // 解析完Result继续解析Page,就是这个流程递归
            resolvedObj = resolveObj(resolvedObj);
        }
        return resolvedObj;
    }

    /**
     * 执行转换操作
     *
     * @param objList            待转换的对象列表
     * @param transFieldMetaList 包含转换字段信息的列表
     */
    private void doTrans(List<Object> objList, List<TransFieldMeta> transFieldMetaList) {
        // 将转换字段信息按仓库类分组
        Map<? extends Class<? extends TransRepository>, List<TransFieldMeta>> listMap = transFieldMetaList.stream().collect(Collectors.groupingBy(TransFieldMeta::getRepository));

        // 如果分组数量大于1，表示有多个仓库类需要处理
        if (listMap.size() > 1) {
            // 使用CompletableFuture并发执行多个转换操作
            CompletableFuture.allOf(
                            // 遍历每个分组
                            listMap.entrySet()
                                    .stream()
                                    .map(entry -> CompletableFuture.runAsync(() ->
                                            // 递归调用doTrans方法处理每个分组
                                            doTrans(objList, entry.getKey(), entry.getValue()), executor))
                                    .toArray(CompletableFuture[]::new))
                    .join();
        } else {
            // 如果分组数量不大于1，表示只有一个仓库类需要处理
            listMap.forEach((transClass, transFields) -> doTrans(objList, transClass, transFields));
        }
    }


    /**
     * 执行转换操作
     *
     * @param objList     待转换的对象列表
     * @param transClass  转换仓库类
     * @param transFields 包含转换字段信息的列表
     */
    private void doTrans(List<Object> objList, Class<? extends TransRepository> transClass, List<TransFieldMeta> transFields) {
        TransRepository transRepository = TransRepositoryFactory.getTransRepository(transClass);
        if (transRepository == null) {
            return;
        }
        // 获取需要被翻译的集合Map<trans, List < TransModel>>
        Map<String, List<TransModel>> toTransMap = getToTransMap(objList, transFields);
        if (CollectionUtils.isNotEmpty(toTransMap)) {
            doTrans0(transRepository, toTransMap);
        }
        // 处理嵌套翻译
        transFields.forEach(transField -> {
            if (CollectionUtils.isNotEmpty(transField.getChildren())) {
                doTrans(objList, transField.getChildren());
            }
        });
    }

    /**
     * 获取需要翻译的集合
     *
     * @param objList     需要被翻译的对象集合
     * @param toTransList 需要被翻译的属性
     * @return 需要被翻译的集合Map<trans, List < TransModel>>
     */
    private Map<String, List<TransModel>> getToTransMap(List<Object> objList, List<TransFieldMeta> toTransList) {
        // 将toTransList中的每个TransFieldMeta对象与objList中的每个对象进行映射，生成TransModel对象
        return toTransList.stream()
                // 对每个TransFieldMeta对象，将其与objList中的每个对象进行映射，生成TransModel对象
                .flatMap(x -> objList.stream().map(o -> new TransModel(o, x)))
                // 过滤出需要翻译的TransModel对象
                .filter(TransModel::needTrans)
                // 根据TransFieldMeta对象的trans属性对TransModel对象进行分组
                .collect(Collectors.groupingBy(x -> x.getTransField().getTrans()));
    }


    /**
     * 执行转换操作（具体实现）
     *
     * @param transRepository 转换仓库
     * @param toTransMap      需要转换的模型映射，键为转换标识，值为模型列表
     */
    private void doTrans0(TransRepository transRepository, Map<String, List<TransModel>> toTransMap) {
        // 分组查询
        if (toTransMap.size() > 1) {
            // 说明有多个实体，异步查询
            CompletableFuture<?>[] futures = toTransMap.values()
                    .stream()
                    .map(transModels -> CompletableFuture.runAsync(() -> doTrans(transRepository, transModels), executor))
                    .toArray(CompletableFuture[]::new);
            CompletableFuture.allOf(futures).join();

        } else {
            // 直接查询
            toTransMap.values().forEach(transModels -> doTrans(transRepository, transModels));
        }
    }

    /**
     * 执行转换操作
     *
     * @param transRepository 转换仓库
     * @param transModels     包含转换模型的列表
     */
    private void doTrans(TransRepository transRepository, List<TransModel> transModels) {
        // 获取所有转换模型中需要转换的值，去重后存入List
        List<Object> transValues = transModels.stream()
                .map(TransModel::getMultipleTransVal)
                .flatMap(Collection::stream)
                .distinct()
                .collect(Collectors.toList());

        // 获取转换注解
        Annotation transAnno = transModels.get(0).getTransField().getTransAnno();

        // 获取转换值映射(使用者提供的数据源) userId -> userDO(数据库实体的对象)
        Map<Object, Object> valueMap = transRepository.getTransValueMap(transValues, transAnno);

        // 如果转换值映射不为空
        if (CollectionUtils.isNotEmpty(valueMap)) {
            // 遍历转换模型，设置转换后的值
            transModels.forEach(transModel -> transModel.setValue(valueMap));
        }
    }


}
