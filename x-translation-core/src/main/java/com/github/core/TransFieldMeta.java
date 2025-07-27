package com.github.core;


import com.github.repository.TransRepository;
import lombok.Getter;
import lombok.Setter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.List;

/**
 * TransFieldMeta: Trans字段元数据
 *
 * @author zhangxiaoxiang
 * @since 2025/7/27
 */
@Getter
public class TransFieldMeta {

    /**
     * 需要被翻译的属性名称
     */
    private final String trans;

    /**
     * 需要被翻译的属性
     */
    private final Field transField;

    /**
     * 需要翻译的属性
     */
    private final Field field;

    /**
     * 提取字段的key
     */
    private final String key;

    private final Annotation transAnno;

    /**
     * 翻译仓库
     */
    private final Class<? extends TransRepository> repository;

    /**
     * 子属性
     */
    @Setter
    private List<TransFieldMeta> children;

    /**
     * TransFieldMeta的构造函数
     *
     * @param field      目标字段
     * @param transField 转换字段
     * @param key        字段的key
     * @param repository 转换仓库类
     * @param transAnno  Trans注解
     */
    public TransFieldMeta(Field field, Field transField, String key, Class<? extends TransRepository> repository, Annotation transAnno) {
        this.field = field;
        this.transField = transField;
        this.trans = transField.getName();
        this.key = key;
        this.repository = repository;
        this.transAnno = transAnno;
    }

}
