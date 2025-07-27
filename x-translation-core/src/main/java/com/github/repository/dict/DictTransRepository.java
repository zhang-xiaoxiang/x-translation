package com.github.repository.dict;


import com.github.annotation.DictTrans;
import com.github.repository.TransRepository;

import java.lang.annotation.Annotation;
import java.util.Collections;
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
     * @param transValues 转换值列表
     * @param transAnno   转换注解
     * @return 转换值映射
     */
    @Override
    public Map<Object, Object> getTransValueMap(List<Object> transValues, Annotation transAnno) {
        // 如果dictLoader不为空，并且transAnno是DictTrans类型的实例
        if (dictLoader != null && transAnno instanceof DictTrans) {
            DictTrans dictTrans = (DictTrans) transAnno;
            String group = dictTrans.group();
            // 使用Stream.of方法创建一个包含group的流，并收集为Map，键为group，值为通过dictLoader加载的字典
            return Stream.of(group).collect(toMap(x -> x, dictLoader::loadDict));
        }
        // 如果不满足条件，则返回空的Map
        return Collections.emptyMap();
    }


}
