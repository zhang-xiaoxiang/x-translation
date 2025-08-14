package com.github.util;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
/**
 * CollectionUtils: 集合工具类，提供一些集合操作的工具方法。
 *
 * @author zhangxiaoxiang
 * @since 2025/7/27
 */
public class CollectionUtils {

    public static boolean isEmpty(Collection<?> collection) {
        return (collection == null || collection.isEmpty());
    }

    public static boolean isNotEmpty(Collection<?> collection) {
        return !isEmpty(collection);
    }

    public static boolean isEmpty(Map<?, ?> map) {
        return (map == null || map.isEmpty());
    }

    public static boolean isNotEmpty(Map<?, ?> map) {
        return !isEmpty(map);
    }

    /**
     * 将对象转换为List类型</br>
     * List<String> stringList = Arrays.asList("张三", "李四", "王五");</br>
     * List<Object> result = CollectionUtils.objToList(stringList);</br>
     * result 包含: ["张三", "李四", "王五"]</br>
     * String[] stringArray = {"张三", "李四", "王五"};</br>
     * List<Object> result = CollectionUtils.objToList(stringArray);</br>
     * result 包含: ["张三", "李四", "王五"]</br>
     * String singleObject = "张三";</br>
     * List<Object> result = CollectionUtils.objToList(singleObject);</br>
     * result 包含: ["张三"]</br>
     *
     * @param obj 需要转换的对象[实现了Iterable接口的如集合,Map,或者数组[],单个对象等
     * @return 转换后的List对象
     */
    public static List<Object> objToList(Object obj) {
        List<Object> objList;
        if (obj instanceof Iterable<?>) {
            Iterable<?> iterable = (Iterable<?>) obj;
            objList = StreamSupport.stream(iterable.spliterator(), false).collect(Collectors.toList());
        } else if (obj.getClass().isArray()) {
            objList = Arrays.stream((Object[]) obj).collect(Collectors.toList());
        } else {
            objList = Collections.singletonList(obj);
        }
        return objList;
    }

}
