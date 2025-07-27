package com.github.annotation;


import com.github.core.TransModel;
import com.github.repository.dict.DictTransRepository;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * DictTrans:字典翻译注解
 *
 * @author zhangxiaoxiang
 * @since 2025/7/27
 */
@Trans(using = DictTransRepository.class, key = TransModel.VAL_EXTRACT)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface DictTrans {


    /**
     * @return 需要翻译的字段
     */
    String trans();

    /**
     * @return 字典分组
     */
    String group();

}
