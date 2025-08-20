package com.github.xtranslation.core.repository;

import io.vavr.control.Option;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * TransRepositoryFactory: 翻译仓库工厂类
 * 1. 静态工具类的特性
 * 静态方法和依赖注入：Spring 的依赖注入机制是基于实例的，而 TransUtil 是一个静态工具类。静态方法不能直接使用 Spring 的依赖注入功能（如构造函数注入、字段注入或 setter 注入），因为这些注入方式都是针对实例对象的。
 * 解决方案：通过 ApplicationContext 获取 Bean 可以绕过这个问题，使得静态方法也能访问到 Spring 管理的 Bean。
 * 2. 延迟加载与单例模式
 * 懒加载：TransServiceHolder 内部静态类采用了 Holder 模式，确保 TransService 实例在第一次调用时才被创建，从而实现了懒加载。这种方式不仅节省了资源，还避免了类加载时不必要的初始化。
 * 线程安全：Holder 模式是线程安全的，因为它利用了 Java 类加载机制的特性，保证了 INSTANCE 只会被初始化一次。
 * 3. 灵活性
 * 动态获取 Bean：通过 ApplicationContext 获取 Bean 提供了更大的灵活性。例如，在某些情况下，你可能需要根据不同的条件获取不同类型的 TransService 实现，或者在运行时动态替换 TransService 的实现。
 * 测试友好：在单元测试中，你可以轻松地通过设置 applicationContext 来模拟不同的 TransService 实现，而不必修改类的结构。
 * 4. 避免循环依赖
 * 防止循环依赖问题：如果直接将 TransService 注入到 TransUtil 中，可能会导致循环依赖的问题。特别是在复杂的项目中，多个类之间可能存在复杂的依赖关系，使用 ApplicationContext 可以有效避免这种情况。
 * 总结
 * 虽然直接注入 TransService 更加直观和简洁，但在静态工具类中，使用 ApplicationContext 来获取 Bean 是一种常见的做法，它解决了静态上下文中无法使用依赖注入的问题，并且提供了更好的灵活性和线程安全性。如果你不需要这些特性，也可以考虑重构为非静态类，从而直接使用依赖注入。
 *
 * @author zhangxiaoxiang
 * @since 2025/7/27
 */
public class TransRepositoryFactory {


    /**
     * 获取翻译仓库的缓存容器  key: 翻译仓库的class  value: 翻译仓库实例
     */
    private final static Map<Class<? extends TransRepository>, TransRepository> TRANS_REPOSITORY_MAP = new ConcurrentHashMap<>();

    /***
     * 获取翻译仓库实例
     * @param repository 翻译仓库的class
     * @return 翻译仓库实例
     */
    public static TransRepository getTransRepository(Class<? extends TransRepository> repository) {
        return TRANS_REPOSITORY_MAP.get(repository);
    }

    public static void register(TransRepository transRepository) {
        Option.of(transRepository).forEach(repo -> TRANS_REPOSITORY_MAP.put(repo.getClass(), repo));
    }

}
