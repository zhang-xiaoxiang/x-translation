package com.github.xtranslation.starter.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * AutoTrans: 自动翻译注解
 *
 * @author zhangxiaoxiang
 * @since 2025/7/27
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface AutoTrans {

}
