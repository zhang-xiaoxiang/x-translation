package com.github.util;


import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.util.*;

/**
 * ReflectUtils:反射工具类
 *
 * @author zhangxiaoxiang
 * @since 2025/7/27
 */
public class ReflectUtils {


    /**
     * 获取指定类及其所有父类的所有字段
     *
     * @param clazz 需要获取字段的类
     * @return 包含所有字段的列表
     */
    public static List<Field> getAllField(Class<?> clazz) {
        Field[] fields;
        List<Field> result = new ArrayList<>();
        for (; clazz != Object.class; clazz = clazz.getSuperclass()) {
            try {
                fields = clazz.getDeclaredFields();
                result.addAll(Arrays.asList(fields));
            } catch (Exception ignored) {
            }
        }
        return result;
    }

    /**
     * 通过反射调用注解的方法，并返回其返回值
     *
     * @param annotationType 注解的类型
     * @param annotation     注解实例
     * @param methodName     要调用的方法名
     * @return 方法的返回值，如果调用失败则返回null
     */
    public static Object invokeAnnotation(Class<? extends Annotation> annotationType, Annotation annotation, String methodName) {
        try {
            return annotationType.getMethod(methodName).invoke(annotation);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取对象的字段值
     *
     * @param obj   目标对象
     * @param field 目标字段
     * @return 字段的值，如果字段为null或无法访问，则返回null
     */
    public static Object getFieldValue(Object obj, Field field) {
        if (null == field) {
            return null;
        } else {
            if (obj instanceof Class) {
                obj = null;
            }

            setAccessible(field);

            try {
                return field.get(obj);
            } catch (IllegalAccessException e) {
                return null;
            }
        }
    }

    /**
     * 设置对象的可访问性
     *
     * @param accessibleObject 需要设置可访问性的对象
     * @param <T>              对象类型，必须是AccessibleObject的子类
     * @throws SecurityException 如果安全管理器存在且不允许设置可访问性，则抛出该异常
     */
    public static <T extends AccessibleObject> void setAccessible(T accessibleObject) throws SecurityException {
        if (null != accessibleObject && !accessibleObject.isAccessible()) {
            accessibleObject.setAccessible(true);
        }
    }

    /**
     * 设置对象的字段值
     *
     * @param obj        目标对象
     * @param field      目标字段
     * @param fieldValue 要设置的字段值
     */
    public static void setFieldValue(Object obj, Field field, Object fieldValue) {
        setAccessible(field);
        try {
            field.set(obj, fieldValue);
        } catch (IllegalAccessException ignored) {
        }
    }


    /**
     * 将JavaBean对象转换为Map对象
     *
     * @param bean JavaBean对象
     * @return Map对象，其中键为字段名，值为字段值
     */
    public static Map<?, ?> beanToMap(Object bean) {
        if (bean instanceof Map) {
            return (Map<?, ?>) bean;
        }
        List<Field> fields = getAllField(bean.getClass());
        Map<String, Object> map = new HashMap<>(fields.size());
        for (Field field : fields) {
            map.put(field.getName(), getFieldValue(bean, field));
        }
        return map;
    }
}
