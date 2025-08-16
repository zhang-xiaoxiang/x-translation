package com.github.xtranslation.core.resolver;

import java.util.ArrayList;
import java.util.List;

/**
 * TransObjResolverFactory: 转换对象解析器工厂类
 *
 * @author zhangxiaoxiang
 * @since 2025/7/27
 */
public class TransObjResolverFactory {

    private final static List<TransObjResolver> RESOLVERS = new ArrayList<>();

    /**
     * 注册一个TransObjResolver对象解析器
     *
     * @param resolver 需要注册的TransObjResolver对象解析器
     */
    public static void register(TransObjResolver resolver) {
        RESOLVERS.add(resolver);
    }

    /**
     * 获取已注册的TransObjResolver对象解析器列表
     *
     * @return 已注册的TransObjResolver对象解析器列表
     */
    public static List<TransObjResolver> getResolvers() {
        return RESOLVERS;
    }

}
