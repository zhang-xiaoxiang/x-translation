package com.github.xtranslation.core.annotation;


import com.github.xtranslation.core.repository.TransRepository;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Trans:翻译注解
 *
 * @author zhangxiaoxiang
 * @since 2025/7/27
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.ANNOTATION_TYPE})
public @interface Trans {

    /**
     * transKey属性名常量
     */
    String TRANS_KEY_ATTR = "transKey";

    /**
     * transField属性名常量
     */
    String TRANS_FIELD_ATTR = "transField";

    /**
     * @return 待翻译的数据对应的主键key(例如:部门表主键字段deptId)
     */
    String transKey() default "";

    /**
     * @return 待翻译的字段对应的字段(例如:部门名称deptName)
     */
    String transField() default "";

    /**
     * @return 翻译数据获取仓库
     */
    Class<? extends TransRepository> repository();

}
