package com.github.resolver;


/**
 * TransObjResolver: 解析包装对象，获取需要翻译的对象
 *
 * @author zhangxiaoxiang
 * @since 2025/7/27
 */
public interface TransObjResolver {

    /**
     * 判断是否支持此对象
     *
     * @param obj 原包装对象
     * @return 是否支持此对象
     */
    boolean support(Object obj);

    /**
     * 解析包装对象，获取需要翻译的对象
     *
     * @param obj 原包装对象 如:Result<UserVO>
     * @return 需要翻译的对象 如:UserVO
     */
    Object resolveTransObj(Object obj);

}
