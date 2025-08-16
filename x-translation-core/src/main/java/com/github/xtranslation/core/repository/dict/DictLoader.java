package com.github.xtranslation.core.repository.dict;


import java.util.Map;

/**
 * DictLoader: 字典加载器，用于加载字典数据
 *
 * @author zhangxiaoxiang
 * @since 2025/7/27
 */
@FunctionalInterface
public interface DictLoader {

    Map<String, String> loadDict(String dictGroup);

}
