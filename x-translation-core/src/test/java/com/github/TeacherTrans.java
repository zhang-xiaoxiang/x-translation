package com.github;


import com.github.annotation.Trans;
import com.github.repository.TeacherTransRepository;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@Trans(using = TeacherTransRepository.class)
public @interface TeacherTrans {

    /**
     * 需要翻译的字段
     */
    String trans() default "";

    /**
     * key 提取的字段
     */
    String key() default "";


}
