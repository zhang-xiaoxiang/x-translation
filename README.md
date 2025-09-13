# x-translation

一款性能中等儿偏下的java翻译插件

<!-- PROJECT SHIELDS -->

<!-- PROJECT LOGO -->
<br />

<p style="text-align: center;">
  <a href="https://gitee.com/zhang-xiao-xiang/x-translation">
    <img src="logo.png" alt="Logo" width="882" height="161">
  </a>

<h3 style="text-align: center;">x-translation</h3>
  <p style="text-align: center;">
    一款性能中等儿偏下的java翻译插件
    <br />
    <a href="https://gitee.com/zhang-xiao-xiang/x-translation"><strong>探索本项目的文档 »</strong></a>
    <br />
    <br />
    <a href="https://gitee.com/zhang-xiao-xiang/x-translation/blob/master/x-translation-core/src/test/java/com/github/xtranslation/core/service/TransServiceTest.java">查看Demo</a>
    ·
    <a href="https://gitee.com/zhang-xiao-xiang/x-translation/issues">报告Bug</a>
    ·
    <a href="https://gitee.com/zhang-xiao-xiang/x-translation/issues">提出新特性</a>
  </p>

## 目录

- [一、基本介绍](#一基本介绍)
- [二、相关说明](#二相关说明)
- [三、快速开始](#三快速开始)
    - [Maven依赖](#maven依赖)
    - [基本使用](#基本使用)
- [四、核心注解](#四核心注解)
    - [@Trans](#trans)
    - [@DictTrans](#dicttrans)
- [五、使用示例](#五使用示例)
    - [单对象翻译](#单对象翻译)
    - [列表翻译](#列表翻译)
    - [嵌套对象翻译](#嵌套对象翻译)
- [六、自定义翻译仓库](#六自定义翻译仓库)
- [七、性能说明](#七性能说明)
- [八、贡献](#八贡献)
- [九、许可证](#九许可证)

## 一、基本介绍

x-translation是一个轻量级的Java翻译框架,主要用于解决业务开发中常见的数据翻译问题,如将ID翻译为名称、将编码翻译为描述等。该框架通过注解驱动的方式,简化了翻译逻辑的实现,使开发者能够专注于业务逻辑而非繁琐的数据转换。

核心注解定义如下：

```java

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
     * @return 待翻译数据的键字段名(例如:部门表主键字段deptId)
     */
    String transKey() default "";

    /**
     * @return 待翻译字段对应的目标字段名(例如:部门名称deptName)
     */
    String transField() default "";

    /**
     * @return 翻译仓库实现类（必须指定）
     */
    Class<? extends TransRepository> repository();


}
```

## 二、相关说明

1. 本插件的设计初衷是探索对 Java 常用控制流结构（如 `if`、`try-catch` 等）的简化与抽象,尝试将其视为可复用的“函数式操作”,把它当做特殊函数对待。
2. 为验证该理念的可行性,参考相关开源项目,以数据翻译场景为切入点,构建了一个轻量级框架原型。此选择既便于快速实现,也具备较强的通用性。
3. 项目整体设计遵循“小而全”的原则：代码量适中,功能完整,依赖简洁,旨在作为 AI 读取与训练的示例工程,同时具备一丢丢的扩展性和不是很正经学习价值。
4. 当前核心能力聚焦于多场景下的数据翻译支持,包括但不限于数据库字段映射、字典编码解析、集合批量转换及嵌套对象递归处理等,力求在不侵入业务逻辑的前提下提升开发效率。
5. 未来可能发展方向：
    - **持续演进与性能优化**：在现有基础上不断迭代,提升框架的稳定性、可扩展性及运行效率。
    - **引入响应式编程与异步任务编排机制**：探索集成相关技术栈,支持非阻塞式数据处理与多线程任务调度,增强系统并发能力。
    - **构建“一键式设计模式”体系**：通过注解驱动和模板化方式,实现常见设计模式（如单例、策略、装饰器等）的声明式调用。例如,开发者可通过 `xxx.singleton()` 或 `xxx.strategy()` 等简洁语法快速启用对应模式,减少样板代码,尝试设计模式从“经验驱动”向“工具化、标准化”演进。
    - **精力有限**：无聊的时候再尝试,说不定AI的发展,直接不需要编程了,最后就还是捡废品,耕地吧 哈哈哈。

## 三、快速开始

### Maven依赖

```xml

<dependency>
    <groupId>io.github.zhang-xiaoxiang</groupId>
    <artifactId>x-translation-core</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 基本使用

```java
// 1. VO对象添加翻译字段注解
public class UserVO {
    private Long id;
    private String name;
    private Long teacherId;
    // 标记待翻译的字段 和 目标字段名, 以及翻译仓库实现类
    @Trans(transKey = "teacherId", transField = "name", repository = TeacherTransRepository.class)
    private String teacherName;
}

// 2. 实现翻译仓库
public class TeacherTransRepository implements TransRepository {
    @Override
    public Map<Object, Object> getTransValueMap(List<Object> transIdList, Annotation transAnno) {
        // 从数据库或其他数据源获取翻译数据,目标是实体主键为key,实体为 value的Map
        return teacherList.stream()
                .filter(x -> transIdList.contains(x.getId()))
                .collect(Collectors.toMap(TeacherDO::getId, x -> x));
    }
}

// 3. 执行翻译
// 方式1:(编码翻译方式)
TransService transService = new TransService();
transService.

init();

UserVO userVO = new UserVO(/* ... */);
transService.

trans(userVO);

// 方式2:(注解声明方式)
@GetMapping("/list")
@AutoTrans
public Result<List<UserVO>> list() {
    // userService.list()是数据库查询出的数据列表,此处假定已经通过数据库查询得到
    return Result.success(userService.list().getData());
}
```

## 四、核心注解

### @Trans

用于对象翻译,将一个字段的值翻译为另一个对象的指定字段。

参数说明：
参数说明：

- [transKey](x-translation-core/src/main/java/com/github/xtranslation/core/annotation/Trans.java#L44-L44): 源字段名,即需要翻译的字段
- [transField](x-translation-core/src/main/java/com/github/xtranslation/core/annotation/Trans.java#L51-L51):
  目标字段名,即翻译后要填充的字段
- [repository](x-translation-core/src/main/java/com/github/xtranslation/core/annotation/Trans.java#L59-L59):
  翻译仓库类,用于提供翻译数据

### @DictTrans

用于字典翻译,将编码翻译为对应的描述信息。

参数说明：

- [transKey](x-translation-core/src/main/java/com/github/xtranslation/core/annotation/DictTrans.java#L24-L24) :
  源字段名,即需要翻译的字段
- [group](x-translation-core/src/main/java/com/github/xtranslation/core/annotation/DictTrans.java#L34-L34) : 字典分组名称

## 五、使用示例

### 单对象翻译

```java

@Test
public void transOne() {
    //1 测试单个对象翻译
    UserVO vo = new UserVO(1L, "张三", 1L, "1", "1");
    System.out.println("翻译前：" + JSONUtil.toJsonStr(vo));
    transService.trans(vo);
    System.out.println("翻译后：" + JSONUtil.toJsonStr(vo));
    // 翻译前：{"id":1,"name":"张三","sex":"1","job":"1","teacherId":1}
    // 翻译后：{"id":1,"name":"张三","sex":"1","job":"1","teacherId":1,"subjectId":1,"sexName":"男","jobName":"1号职务","teacherName":"1号老师","subjectName":"科目1"}
}
```

### 列表翻译

```java

@Test
public void transList() {
    //2 测试列表对象翻译
    List<UserVO> userList = new ArrayList<>();
    UserVO user1 = new UserVO(1L, "张三", 1L, "1", "1");
    UserVO user2 = new UserVO(2L, "李四", 2L, "2", "2");
    userList.add(user1);
    userList.add(user2);
    System.out.println("翻译前：" + JSONUtil.toJsonStr(userList));
    transService.trans(userList);
    System.out.println("翻译后：" + JSONUtil.toJsonStr(userList));
    // 翻译前：{"id":1,"name":"张三","sex":"1","job":"1","teacherId":1}
    // 翻译后：{"id":1,"name":"张三","sex":"1","job":"1","teacherId":1,"subjectId":1,"sexName":"男","jobName":"1号职务","teacherName":"1号老师","subjectName":"科目1"}
}
```

### 嵌套对象翻译

```java

@Test
public void transNested() {
    //3 测试嵌套对象翻译
    UserVO user = new UserVO(1L, "张三", 1L, "1", "1");
    Result<UserVO> result = Result.success(user);
    // Result<List<UserVO>>列表嵌套同理
    System.out.println("翻译前：" + JSONUtil.toJsonStr(result));
    transService.trans(result);
    System.out.println("翻译后：" + JSONUtil.toJsonStr(result));
    // 翻译前：{"code":"0","message":"成功","data":{"id":1,"name":"张三","sex":"1","job":"1","teacherId":1}}
    // 翻译后：{"code":"0","message":"成功","data":{"id":1,"name":"张三","sex":"1","job":"1","teacherId":1,"subjectId":1,"sexName":"男","jobName":"1号职务","teacherName":"1号老师","subjectName":"科目1"}}
}
```

## 六、自定义翻译仓库

```java
public class TeacherTransRepository implements TransRepository {

    @Override
    public Map<Object, Object>
    getTransValueMap(List<Object> transIdList, Annotation transAnno) {
        // 模拟数据库查询到的数据
        return ListUtil.of(
                new TeacherDO(1L, "1号老师", 1L),
                new TeacherDO(2L, "2号老师", 2L),
                new TeacherDO(3L, "3号老师", 3L),
                new TeacherDO(4L, "4号老师", 4L)
        ).stream().filter(x -> transIdList.contains(x.getId())).collect(Collectors.toMap(TeacherDO::getId, x -> x));
    }
}
```

## 七、性能说明

x-translation采用并行处理机制,不同字段的翻译任务可以并行执行,性能中等儿偏下。框架内部被迫使用了以下优化策略：

1. 相同翻译仓库的数据合并查询,减少数据库访问次数
2. 多线程并行处理不同的翻译任务
3. 缓存机制避免重复翻译相同数据
4. 其他可能未列出的优化策略。

## 八、贡献

欢迎任何形式的贡献,包括但不限于：

1. 提交Bug报告
2. 提交修复代码
3. 提出功能建议
4. 完善文档说明

贡献步骤：

1. Fork本项目
2. 创建您的特性分支 (git checkout -b feature/AmazingFeature)
3. 提交您的更改 (git commit -m 'Add some AmazingFeature')
4. 推送到分支 (git push origin feature/AmazingFeature)
5. 开启一个Pull Request

## 九、许可证

本项目采用MIT许可证,详情请见[LICENSE](https://gitee.com/zhang-xiao-xiang/x-translation/blob/master/LICENSE)文件。

<!-- links -->

[contributors-shield]: https://img.shields.io/gitee/contributors/zhang-xiao-xiang/x-translation.svg?style=flat-square

[contributors-url]: https://gitee.com/zhang-xiao-xiang/x-translation/pulls

[forks-shield]: https://img.shields.io/gitee/forks/zhang-xiao-xiang/x-translation.svg?style=flat-square

[forks-url]: https://gitee.com/zhang-xiao-xiang/x-translation/pulls#

[stars-shield]: https://gitee.com/zhang-xiao-xiang/x-translation/stargazers

[stars-url]: https://gitee.com/zhang-xiao-xiang/x-translation/star

[issues-shield]: https://img.shields.io/gitee/issues/zhang-xiao-xiang/x-translation.svg?style=flat-square

[issues-url]: https://gitee.com/zhang-xiao-xiang/x-translation/issues

[license-shield]: https://img.shields.io/gitee/license/zhang-xiao-xiang/x-translation.svg?style=flat-square

[license-url]: https://gitee.com/zhang-xiao-xiang/x-translation/blob/master/LICENSE
