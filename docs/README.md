# 文档持续更新中

> 张老头儿把代码写完后,空了文档就会更新哈,还在准备中.  
> 如果他少打点篮球或者游戏,可能顺带出个视频之类的.
> 可以临时参考 https://gitee.com/zhang-xiao-xiang/x-translation

# x-translation

一款性能中等儿偏下的java翻译插件

<!-- PROJECT SHIELDS -->

<!-- PROJECT LOGO -->
<br />

<p align="center">
  <a href="https://gitee.com/zhang-xiao-xiang/x-translation">
    <img src="logo.png" alt="Logo" width="300" height="70">
  </a>

<h3 align="center">x-translation</h3>
  <p align="center">
    一款高性能的Java翻译插件
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
- [二、优点](#二优点)
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
    - [复杂嵌套结构翻译](#复杂嵌套结构翻译)
    - [树形结构翻译](#树形结构翻译)
    - [复杂综合嵌套结构翻译](#复杂综合嵌套结构翻译)

- [六、自定义翻译仓库](#六自定义翻译仓库)
- [七、性能说明](#七性能说明)
- [八、贡献](#八贡献)
- [九、许可证](#九许可证)

## 一、基本介绍

x-translation是一个轻量级的Java翻译框架，主要用于解决业务开发中常见的数据翻译问题，如将ID翻译为名称、将编码翻译为描述等。该框架通过注解驱动的方式，简化了翻译逻辑的实现，使开发者能够专注于业务逻辑而非繁琐的数据转换。

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

## 二、优点

1. 核心源码简单，仅几百行，依赖项少（io.vavr、cn.hutool.core、lombok）
2. 高度可拓展，拓展逻辑仅需实现TransRepository接口
3. 支持数据库翻译、字典翻译、集合翻译、嵌套翻译等多种场景
4. 并行翻译，不同字段的翻译任务并行执行，性能优异
5. 零侵入性，通过注解方式实现翻译，不影响原有代码结构

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
// 1. 定义DTO对象
public class UserDto {
    private Long id;
    private String name;
    private Long teacherId;
    
    @Trans(transKey = "teacherId", transField = "name", repository = TeacherTransRepository.class)
    private String teacherName;

    // getters and setters...
}

// 2. 实现翻译仓库
public class TeacherTransRepository implements TransRepository {
    @Override 
    public Map<Object, Object> getTransValueMap(List<Object> transIdList, Annotation transAnno) {
        // 从数据库或其他数据源获取翻译数据
        return teacherList.stream() 
                .filter(x -> transIdList.contains(x.getId()))
                .collect(Collectors.toMap(TeacherDto::getId, x -> x));
    }
}

// 3. 执行翻译
TransService transService = new TransService();
transService.init();
UserDto userDto = new UserDto(/* ... */);
transService.trans(userDto);
```
## 四、核心注解

### @Trans

用于对象翻译，将一个字段的值翻译为另一个对象的指定字段。

参数说明：
参数说明：
- [transKey](x-translation-core/src/main/java/com/github/xtranslation/core/annotation/Trans.java#L44-L44): 源字段名，即需要翻译的字段
- [transField](x-translation-core/src/main/java/com/github/xtranslation/core/annotation/Trans.java#L51-L51): 目标字段名，即翻译后要填充的字段
- [repository](x-translation-core/src/main/java/com/github/xtranslation/core/annotation/Trans.java#L59-L59): 翻译仓库类，用于提供翻译数据

### @DictTrans

用于字典翻译，将编码翻译为对应的描述信息。

参数说明：
- [transKey](x-translation-core/src/main/java/com/github/xtranslation/core/annotation/DictTrans.java#L24-L24) : 源字段名，即需要翻译的字段
- [group](x-translation-core/src/main/java/com/github/xtranslation/core/annotation/DictTrans.java#L34-L34) : 字典分组名称

## 五、使用示例

### 单对象翻译

```java
@Test
public void transSingleObjectAndList() {
    System.out.println("1测试单个对象翻译");
    // 测试单个对象翻译
    UserDto userDto = new UserDto(1L, "张三", 2L, "1", "2");
    System.out.println("翻译前：" + JSONUtil.toJsonPrettyStr(userDto));
    transService.trans(userDto);
    System.out.println("翻译后：" + JSONUtil.toJsonPrettyStr(userDto));

    // 验证翻译结果
    assertEquals("男", userDto.getSexName());
    assertEquals("生活委员", userDto.getJobName());
    assertEquals("老师2", userDto.getTeacherName());
    assertEquals("数学", userDto.getSubjectName());

    System.out.println("测试对象列表翻译");
    // 测试对象列表翻译
    List<UserDto> userDtoList = new ArrayList<>();
    UserDto userDto2 = new UserDto(2L, "李四", 1L, "2", "1");
    UserDto userDto3 = new UserDto(3L, "王五", 2L, "1", "3");
    UserDto userDto4 = new UserDto(4L, "赵六", 3L, "2", "4");
    userDtoList.add(userDto4);
    userDtoList.add(userDto3);
    userDtoList.add(userDto2);
    System.out.println("翻译前：" + JSONUtil.toJsonPrettyStr(userDtoList));
    transService.trans(userDtoList);
    System.out.println("翻译后：" + JSONUtil.toJsonPrettyStr(userDtoList));

    // 验证列表中对象的翻译结果
    assertNotNull(userDtoList);
    assertEquals(3, userDtoList.size());
    assertEquals("女", userDtoList.get(0).getSexName());
    assertEquals("男", userDtoList.get(1).getSexName());
    assertEquals("女", userDtoList.get(2).getSexName());
}
```

### 列表翻译
```java
@Test
void transObjectWithListFields() {
    System.out.println("2测试包含列表字段的对象翻译");
    // 测试包含列表字段的对象翻译
    List<Long> teacherIds = new ArrayList<>();
    teacherIds.add(1L);
    teacherIds.add(2L);
    List<String> jobIds = new ArrayList<>();
    jobIds.add("1");
    jobIds.add("2");
    UserDto2 userDto = new UserDto2(1L, "张三", teacherIds, jobIds);
    System.out.println("翻译前：" + JSONUtil.toJsonPrettyStr(userDto));
    transService.trans(userDto);
    System.out.println("翻译后：" + JSONUtil.toJsonPrettyStr(userDto));

    // 验证翻译结果
    assertEquals(2, userDto.getJobNames().size());
    assertEquals("学习委员", userDto.getJobNames().get(0));
    assertEquals("生活委员", userDto.getJobNames().get(1));

    System.out.println("测试包含列表字段的对象列表翻译");
    // 测试包含列表字段的对象列表翻译
    List<UserDto2> userDtoList = new ArrayList<>();
    UserDto2 userDto2 = new UserDto2(2L, "李四", teacherIds, jobIds);
    List<Long> teacherIds2 = new ArrayList<>();
    teacherIds2.add(3L);
    teacherIds2.add(4L);
    List<String> jobIds2 = new ArrayList<>();
    jobIds2.add("3");
    jobIds2.add("4");
    UserDto2 userDto3 = new UserDto2(3L, "王五", teacherIds2, jobIds2);
    UserDto2 userDto4 = new UserDto2(4L, "赵六", teacherIds2, jobIds2);
    userDtoList.add(userDto4);
    userDtoList.add(userDto3);
    userDtoList.add(userDto2);
    System.out.println("翻译前：" + JSONUtil.toJsonPrettyStr(userDtoList));
    transService.trans(userDtoList);
    System.out.println("翻译后：" + JSONUtil.toJsonPrettyStr(userDtoList));

    // 验证翻译结果
    assertEquals(3, userDtoList.size());
    assertEquals(2, userDtoList.get(0).getJobNames().size());
}
```

### 嵌套对象翻译
```java
@Test
void transNestedObject() {
    System.out.println("3测试嵌套对象翻译");
    // 测试嵌套对象翻译
    List<Long> teacherIds = new ArrayList<>();
    teacherIds.add(1L);
    teacherIds.add(2L);
    List<String> jobIds = new ArrayList<>();
    jobIds.add("1");
    jobIds.add("2");
    jobIds.add("3");
    UserDto2 userDto = new UserDto2(1L, "张三", teacherIds, jobIds);
    Result<UserDto2> result = new Result<>(userDto, "success");
    System.out.println("翻译前：" + JSONUtil.toJsonPrettyStr(result));
    transService.trans(result);
    System.out.println("翻译后：" + JSONUtil.toJsonPrettyStr(result));

    // 验证翻译结果
    assertNotNull(result.getData());
    assertEquals(3, result.getData().getJobNames().size());

    System.out.println("测试多层嵌套对象翻译");
    // 测试多层嵌套对象翻译
    UserDto2 userDto2 = new UserDto2(2L, "李四", teacherIds, jobIds);
    Result<UserDto2> result2 = new Result<>(userDto2, "success");
    Result<Result<UserDto2>> result3 = new Result<>(result2, "success");
    System.out.println("翻译前：" + JSONUtil.toJsonPrettyStr(result3));
    transService.trans(result3);
    System.out.println("翻译后：" + JSONUtil.toJsonPrettyStr(result3));

    // 验证翻译结果
    assertNotNull(result3.getData());
    assertNotNull(result3.getData().getData());
    assertEquals(3, result3.getData().getData().getJobNames().size());
}
```
### 复杂嵌套结构翻译
```java
 @Test
void transComplexNestedStructure() {
    System.out.println("4测试复杂嵌套结构翻译");
    // 创建复杂嵌套结构用于测试
    List<Map<String, Object>> complexListMapStructure = new ArrayList<>();

    Map<String, Object> map1 = new HashMap<>();
    map1.put("id", 1L);
    map1.put("refId", 1L);
    map1.put("name", "项目1");

    Map<String, Object> map2 = new HashMap<>();
    map2.put("id", 2L);
    map2.put("refId", 2L);
    map2.put("name", "项目2");

    complexListMapStructure.add(map1);
    complexListMapStructure.add(map2);

    Map<String, Object> complexMap = new HashMap<>();
    complexMap.put("refId", 1L);
    complexMap.put("description", "这是一个复杂Map结构");

    // 测试包含复杂嵌套结构的对象翻译
    List<String> jobIds = new ArrayList<>();
    jobIds.add("1");
    jobIds.add("2");
    UserDto3 userDto3 = new UserDto3(1L, "张三", jobIds, complexListMapStructure, complexMap, 2L);

    System.out.println("翻译前：" + JSONUtil.toJsonPrettyStr(userDto3));
    transService.trans(userDto3);
    System.out.println("翻译后：" + JSONUtil.toJsonPrettyStr(userDto3));

    // 验证基础翻译结果
    assertEquals(2, userDto3.getJobNames().size());
    assertEquals("学习委员", userDto3.getJobNames().get(0));
    assertEquals("生活委员", userDto3.getJobNames().get(1));
    assertEquals("数学", userDto3.getRefName()); // refId为2，对应数学科目

    // 验证复杂嵌套结构存在（但其中的字段不会被翻译，因为Map中的字段没有注解）
    assertNotNull(userDto3.getComplexListMapStructure());
    assertEquals(2, userDto3.getComplexListMapStructure().size());
    assertNotNull(userDto3.getComplexMap());
}
``` 
### 树形结构翻译
```java
 @Test
    void transTreeStructure() {
        System.out.println("5测试树形结构翻译");
        // 创建树形结构数据
        // 根节点：校长办公室
        DepartmentTreeDto root = new DepartmentTreeDto(1L, "校长办公室", 1L, 1L);

        // 二级节点：教务处和学生处
        DepartmentTreeDto academicAffairs = new DepartmentTreeDto(2L, "教务处", 2L, 2L);
        DepartmentTreeDto studentAffairs = new DepartmentTreeDto(3L, "学生处", 3L, 3L);

        // 三级节点：教务处下设的部门
        DepartmentTreeDto teachingDepartment = new DepartmentTreeDto(4L, "教学科", 4L, 1L);
        DepartmentTreeDto textbookDepartment = new DepartmentTreeDto(5L, "教材科", 1L, 2L);

        // 构建树形结构
        List<DepartmentTreeDto> level2Children = new ArrayList<>();
        level2Children.add(academicAffairs);
        level2Children.add(studentAffairs);
        root.setChildren(level2Children);

        List<DepartmentTreeDto> level3Children = new ArrayList<>();
        level3Children.add(teachingDepartment);
        level3Children.add(textbookDepartment);
        academicAffairs.setChildren(level3Children);

        // 创建一个包含所有节点的列表，以便框架可以处理所有节点的翻译
        List<DepartmentTreeDto> allNodes = new ArrayList<>();
        allNodes.add(root);
        allNodes.add(academicAffairs);
        allNodes.add(studentAffairs);
        allNodes.add(teachingDepartment);
        allNodes.add(textbookDepartment);

        System.out.println("翻译前：" + JSONUtil.toJsonPrettyStr(allNodes));
        transService.trans(allNodes);
        System.out.println("翻译后：" + JSONUtil.toJsonPrettyStr(allNodes));

        // 验证根节点翻译结果
        assertEquals("老师1", root.getLeaderName());
        assertEquals("语文", root.getSubjectName());

        // 验证二级节点翻译结果
        assertEquals("老师2", academicAffairs.getLeaderName());
        assertEquals("数学", academicAffairs.getSubjectName());
        assertEquals("老师3", studentAffairs.getLeaderName());
        assertEquals("英语", studentAffairs.getSubjectName());

        // 验证三级节点翻译结果
        assertEquals("老师4", teachingDepartment.getLeaderName());
        assertEquals("语文", teachingDepartment.getSubjectName());
        assertEquals("老师1", textbookDepartment.getLeaderName());
        assertEquals("数学", textbookDepartment.getSubjectName());

        // 验证树形结构仍然存在
        assertNotNull(root.getChildren());
        assertEquals(2, root.getChildren().size());
        assertNotNull(academicAffairs.getChildren());
        assertEquals(2, academicAffairs.getChildren().size());
    }
```

### 复杂综合嵌套结构翻译
```java
 @Test
    void transComplexComprehensiveStructure() {
        System.out.println("5测试复杂综合嵌套结构翻译");
        // 创建一个复杂的综合嵌套结构，模拟真实学校管理系统场景

        // 创建班级对象
        SchoolClassDto classDto = new SchoolClassDto();
        classDto.setClassId(1L);
        classDto.setClassName("高三(1)班");
        classDto.setHeadTeacherId(1L); // 班主任ID为1
        classDto.setHeadTeacherName(""); // 初始化为空字符串而不是null
        classDto.setSubjectId(2L); // 班主任所教学科ID为2（数学）
        classDto.setSubjectName(""); // 初始化为空字符串而不是null

        // 创建学生列表
        List<SchoolClassDto.StudentInfo> students = new ArrayList<>();
        SchoolClassDto.StudentInfo student1 = new SchoolClassDto.StudentInfo();
        student1.setStudentId(101L);
        student1.setStudentName("张三");
        student1.setSexCode("1");
        student1.setSexName(""); // 初始化为空字符串而不是null

        SchoolClassDto.StudentInfo student2 = new SchoolClassDto.StudentInfo();
        student2.setStudentId(102L);
        student2.setStudentName("李四");
        student2.setSexCode("2");
        student2.setSexName(""); // 初始化为空字符串而不是null

        students.add(student1);
        students.add(student2);
        classDto.setStudents(students);

        // 创建学生分组Map
        Map<String, List<SchoolClassDto.StudentInfo>> studentGroups = new HashMap<>();
        studentGroups.put("第一组", students);
        classDto.setStudentGroups(studentGroups);

        // 创建学生详细信息Map
        Map<Long, SchoolClassDto.StudentDetail> studentDetails = new HashMap<>();
        SchoolClassDto.StudentDetail detail1 = new SchoolClassDto.StudentDetail();
        detail1.setStudentId(101L);
        detail1.setAge(18);
        detail1.setAddress("北京市朝阳区");
        detail1.setFatherId(2L); // 父亲ID为2
        detail1.setFatherName(""); // 初始化为空字符串而不是null
        detail1.setMotherId(3L); // 母亲ID为3
        detail1.setMotherName(""); // 初始化为空字符串而不是null

        SchoolClassDto.StudentDetail detail2 = new SchoolClassDto.StudentDetail();
        detail2.setStudentId(102L);
        detail2.setAge(17);
        detail2.setAddress("上海市浦东新区");
        detail2.setFatherId(4L); // 父亲ID为4
        detail2.setFatherName(""); // 初始化为空字符串而不是null
        detail2.setMotherId(1L); // 母亲ID为1
        detail2.setMotherName(""); // 初始化为空字符串而不是null

        studentDetails.put(101L, detail1);
        studentDetails.put(102L, detail2);
        classDto.setStudentDetails(studentDetails);

        // 创建课程安排列表
        List<SchoolClassDto.CourseSchedule> courseSchedules = new ArrayList<>();

        // 数学课
        SchoolClassDto.CourseSchedule mathCourse = new SchoolClassDto.CourseSchedule();
        mathCourse.setCourseId(1L);
        mathCourse.setCourseName("数学");
        mathCourse.setTeacherId(1L); // 授课教师ID为1
        mathCourse.setTeacherName(""); // 初始化为空字符串而不是null
        mathCourse.setSubjectId(2L); // 学科ID为2（数学）
        mathCourse.setSubjectName(""); // 初始化为空字符串而不是null

        // 数学课时间安排
        List<SchoolClassDto.ClassTime> mathClassTimes = new ArrayList<>();
        SchoolClassDto.ClassTime mathTime1 = new SchoolClassDto.ClassTime();
        mathTime1.setWeekDay(1); // 周一
        mathTime1.setStartTime("08:00");
        mathTime1.setEndTime("09:30");
        mathTime1.setRoomId(1L); // 教室ID为1
        mathTime1.setRoomName(""); // 初始化为空字符串而不是null

        SchoolClassDto.ClassTime mathTime2 = new SchoolClassDto.ClassTime();
        mathTime2.setWeekDay(3); // 周三
        mathTime2.setStartTime("10:00");
        mathTime2.setEndTime("11:30");
        mathTime2.setRoomId(2L); // 教室ID为2
        mathTime2.setRoomName(""); // 初始化为空字符串而不是null

        mathClassTimes.add(mathTime1);
        mathClassTimes.add(mathTime2);
        mathCourse.setClassTimes(mathClassTimes);

        // 数学课选修学生
        List<SchoolClassDto.StudentInfo> mathStudents = new ArrayList<>();
        mathStudents.add(student1);
        mathCourse.setEnrolledStudents(mathStudents);

        // 语文课
        SchoolClassDto.CourseSchedule chineseCourse = new SchoolClassDto.CourseSchedule();
        chineseCourse.setCourseId(2L);
        chineseCourse.setCourseName("语文");
        chineseCourse.setTeacherId(2L); // 授课教师ID为2
        chineseCourse.setTeacherName(""); // 初始化为空字符串而不是null
        chineseCourse.setSubjectId(1L); // 学科ID为1（语文）
        chineseCourse.setSubjectName(""); // 初始化为空字符串而不是null

        // 语文课时间安排
        List<SchoolClassDto.ClassTime> chineseClassTimes = new ArrayList<>();
        SchoolClassDto.ClassTime chineseTime1 = new SchoolClassDto.ClassTime();
        chineseTime1.setWeekDay(2); // 周二
        chineseTime1.setStartTime("08:00");
        chineseTime1.setEndTime("09:30");
        chineseTime1.setRoomId(3L); // 教室ID为3
        chineseTime1.setRoomName(""); // 初始化为空字符串而不是null

        chineseClassTimes.add(chineseTime1);
        chineseCourse.setClassTimes(chineseClassTimes);

        // 语文课选修学生
        List<SchoolClassDto.StudentInfo> chineseStudents = new ArrayList<>();
        chineseStudents.add(student1);
        chineseStudents.add(student2);
        chineseCourse.setEnrolledStudents(chineseStudents);

        courseSchedules.add(mathCourse);
        courseSchedules.add(chineseCourse);
        classDto.setCourseSchedules(courseSchedules);

        // 创建一个包含所有需要翻译的对象的列表
        List<Object> allObjects = new ArrayList<>();
        allObjects.add(classDto);
        allObjects.addAll(students);
        allObjects.addAll(studentDetails.values());

        for (SchoolClassDto.CourseSchedule schedule : courseSchedules) {
            allObjects.add(schedule);
            allObjects.addAll(schedule.getClassTimes());
            allObjects.addAll(schedule.getEnrolledStudents());
        }

        System.out.println("翻译前：" + JSONUtil.toJsonPrettyStr(classDto));
        transService.trans(allObjects);
        System.out.println("翻译后：" + JSONUtil.toJsonPrettyStr(classDto));

        // 验证班级信息翻译
        assertEquals("老师1", classDto.getHeadTeacherName());
        assertEquals("数学", classDto.getSubjectName());
    }
```

## 六、自定义翻译仓库

```java
public class TeacherTransRepository implements TransRepository {
    @Override 
    public Map<Object, Object> getTransValueMap(List<Object> transIdList, Annotation transAnno) {
        return getTeachers().stream() 
                .filter(x -> transIdList.contains(x.getId()))
                .collect(Collectors.toMap(TeacherDto::getId, x -> x));
    }
    
    public List<TeacherDto> getTeachers() {
        List<TeacherDto> teachers = new ArrayList<>();
        teachers.add(new TeacherDto(1L, "老师1", 1L));
        teachers.add(new TeacherDto(2L, "老师2", 2L));
        teachers.add(new TeacherDto(3L, "老师3", 3L));
        teachers.add(new TeacherDto(4L, "老师4", 4L));
        return teachers;
    }
}
```

## 七、性能说明

x-translation采用并行处理机制，不同字段的翻译任务可以并行执行，大大提高了翻译效率。框架内部使用了以下优化策略：

1. 相同翻译仓库的数据合并查询，减少数据库访问次数
2. 多线程并行处理不同的翻译任务
3. 缓存机制避免重复翻译相同数据
4. 基于Java 8 Stream API的函数式编程，提升代码执行效率

## 八、贡献

欢迎任何形式的贡献，包括但不限于：

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

本项目采用MIT许可证，详情请见[LICENSE](https://gitee.com/zhang-xiao-xiang/x-translation/blob/master/LICENSE)文件。

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

