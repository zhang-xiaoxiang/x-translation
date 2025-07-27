package com.github.manager;


import com.github.core.TransClassMeta;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * TransClassMetaCacheManager: TransClassMeta缓存管理器，用于缓存TransClassMeta对象
 *
 * @author zhangxiaoxiang
 * @since 2025/7/27
 */
public class TransClassMetaCacheManager implements Serializable {

    private static final long serialVersionUID = 3076627700677041940L;

    private static final Map<String, TransClassMeta> CACHE = new ConcurrentHashMap<>();

    public static TransClassMeta getTransClassMeta(Class<?> clazz) {
        TransClassMeta temp = CACHE.get(clazz.getName());
        if (null == temp) {
            temp = new TransClassMeta(clazz);
            if (temp.needTrans()) {
                CACHE.put(clazz.getName(), temp);
            }
        }
        return temp;
    }
}
