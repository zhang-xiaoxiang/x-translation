# x-translation

一款性能中等儿偏下的java翻译插件


<!-- PROJECT LOGO -->
<br />

<p align="center">
  <a href="https://github.com/zhang-xiaoxiang/x-translation">
    <img src="logo.png" alt="Logo" width="300" height="70">
  </a>

<h3 align="center">x-translation</h3>
  <p align="center">
    一款性能中等儿偏下的java翻译插件
    <br />
    <a href="https://github.com/zhang-xiaoxiang/x-translation"><strong>探索本项目的文档 »</strong></a>
    <br />
    <br />
    <a href="https://github.com/zhang-xiaoxiang/x-translation/blob/master/x-translation-core/src/test/java/com/github/service/TransServiceTest.java">查看Demo</a>
    ·
    <a href="https://github.com/zhang-xiaoxiang/x-translation/issues">报告Bug</a>
    ·
    <a href="https://github.com/zhang-xiaoxiang/x-translation/issues">提出新特性</a>
  </p>


## 一、基本介绍
数据库翻译等日常数据翻译

```java

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.ANNOTATION_TYPE})
public @interface Trans {

  /**
   * @return 需要翻译的字段
   */
  String trans() default "";

  /**
   * @return 提取的字段
   */
  String key() default "";

  /**
   * @return 翻译数据获取仓库
   */
  Class<? extends TransRepository> using();

}

```

## 二、优点

1、核心源码简单，仅几百行，依赖项项少(了io.vavr、cn.hutool.core、lombok),<br />
2、高度可拓展，拓展逻辑仅仅只需要实现TransRepository接口;<br />
3、支持数据库翻译、字典翻译、集合翻译、嵌套翻译等;<br />
4、并行翻译，翻译不同字段是并行翻译的，性能高<br />

## 三、基本使用

maven引入

```java
<dependency>
    <groupId>io.github.zhang-xiaoxiang</groupId>
    <artifactId>x-translation-core</artifactId>
    <version>1.0.0</version>
</dependency>
```





