package com.github.resolver;


/**
 * TransObjResolver: 解析包装对象，获取需要翻译的对象
 *
 * @author zhangxiaoxiang
 * @since 2025/7/27
 */
public interface TransObjResolver {

    boolean support(Object obj);

    /**
     * 解析包装对象，获取需要翻译的对象
     *
     * @param obj 原包装对象
     * @return 需要翻译的对象
     */
    Object resolveTransObj(Object obj);

}
