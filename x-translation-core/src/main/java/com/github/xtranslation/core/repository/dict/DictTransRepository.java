package com.github.xtranslation.core.repository.dict;


import com.github.xtranslation.core.annotation.DictTrans;
import com.github.xtranslation.core.repository.TransRepository;
import io.vavr.control.Option;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;

/**
 * DictTransRepository: 字典转换仓库，用于加载字典数据
 *
 * @author zhangxiaoxiang
 * @since 2025/7/27
 */
public class DictTransRepository implements TransRepository {

    private final DictLoader dictLoader;

    public DictTransRepository(DictLoader dictLoader) {
        this.dictLoader = dictLoader;
    }

    /**
     * 获取转换值映射
     *
     * @param transIdList 转换值列表
     * @param transAnno   转换注解
     * @return 转换值映射
     */
    @Override
    public Map<Object, Object> getTransValueMap(List<Object> transIdList, Annotation transAnno) {
        // 使用Option处理dictLoader不为空且transAnno是DictTrans类型实例的情况
        return Option.of(dictLoader)
                .filter(loader -> transAnno instanceof DictTrans)
                // 使用Stream.of方法创建一个包含group的流，并收集为Map，键为group，值为通过dictLoader加载的字典
                .map(loader -> (Map<Object, Object>) new HashMap<Object, Object>(Stream.of(((DictTrans) transAnno).group()).collect(toMap(x -> x, dictLoader::loadDict)))).getOrElse(Collections.emptyMap());
    }


}
