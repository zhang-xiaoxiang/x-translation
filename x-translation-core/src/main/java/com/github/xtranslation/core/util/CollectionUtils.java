package com.github.xtranslation.core.util;

import com.sun.org.apache.bcel.internal.generic.NEW;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import sun.applet.Main;

import javax.security.auth.Subject;
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
     * Convert an object to a List type
     * <p>
     * Example 1:
     * List&amp;lt;String&amp;gt; stringList = Arrays.asList("张三", "李四", "王五");
     * List&amp;lt;Object&amp;gt; result = CollectionUtils.objToList(stringList);
     * result contains: ["张三", "李四", "王五"]
     * <p>
     * Example 2:
     * String[] stringArray = {"张三", "李四", "王五"};
     * List&amp;lt;Object&amp;gt; result = CollectionUtils.objToList(stringArray);
     * result contains: ["张三", "李四", "王五"]
     * <p>
     * Example 3:
     * String singleObject = "张三";
     * List&amp;lt;Object&amp;gt; result = CollectionUtils.objToList(singleObject);
     * result contains: ["张三"]
     *
     * @param obj the object to be converted [objects that implement the Iterable interface such as collections, Map, or arrays[], single objects, etc.
     * @return the converted List object
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
