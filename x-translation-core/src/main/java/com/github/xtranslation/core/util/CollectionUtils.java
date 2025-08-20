package com.github.xtranslation.core.util;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static io.vavr.API.*;
import static io.vavr.Predicates.instanceOf;

/**
 * CollectionUtils: 集合工具类，提供一些集合操作的工具方法。
 *
 * @author zhangxiaoxiang
 * @since 2025/7/27
 */
public class CollectionUtils {
    /**
     * 将对象转换为List集合
     *
     * @param obj 对象
     * @return List集合
     */
    public static List<Object> objToList(Object obj) {
        return Match(obj).of(
                Case($(Objects::isNull), Collections.emptyList()),
                Case($(instanceOf(Collection.class)), c -> new ArrayList<>((Collection<?>) c)),
                Case($(instanceOf(Iterable.class)), i -> StreamSupport.stream(((Iterable<?>) i).spliterator(), false).collect(Collectors.toList())),
                Case($(o -> o.getClass().isArray()), arr -> Arrays.stream((Object[]) arr).collect(Collectors.toList())),
                Case($(), Collections::singletonList)
        );
    }

}
