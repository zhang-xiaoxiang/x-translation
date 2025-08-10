package com.github.util;

import com.github.service.TransService;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * TransUtil: 翻译工具类
 * <p>
 * 这是一个工具类，提供静态方法来访问翻译服务。该类实现了 ApplicationContextAware 接口，
 * 以便在 Spring 容器启动时获取 ApplicationContext，从而能够获取 Spring 管理的 Bean。
 * </p>
 * <p>
 * 设计模式体现：
 * 1. 外观模式（Facade Pattern）：为复杂的翻译子系统提供简化的统一访问接口
 * 2. 单例模式（Singleton Pattern）：通过内部静态类确保 TransService 实例的单例性
 * 3. 依赖注入适配：实现 ApplicationContextAware 接口，适配 Spring 的依赖注入机制
 * </p>
 *
 * @author zhangxiaoxiang
 * @since 2025/7/27
 */
public class TransUtil implements ApplicationContextAware {

    /**
     * Spring 应用程序上下文，用于获取 Spring 管理的 Bean
     * <p>
     * 通过实现 ApplicationContextAware 接口，在 Spring 容器初始化时自动注入
     * </p>
     */
    private static ApplicationContext applicationContext;

    /**
     * 翻译工具方法（外观模式的体现，统一访问入口）
     * <p>
     * 为调用者提供简化的翻译接口，隐藏了获取 TransService 实例和具体翻译实现的复杂性。
     * 调用者只需传入需要翻译的对象，即可完成翻译操作。
     * </p>
     *
     * @param obj 需要翻译的对象，可以是任何支持翻译的数据结构
     * @return boolean 翻译是否成功，true 表示翻译成功，false 表示翻译失败
     */
    public static boolean trans(Object obj) {
        return TransServiceHolder.get().trans(obj);
    }

    /**
     * 设置Spring应用程序上下文（依赖注入适配的体现）
     * <p>
     * 实现 ApplicationContextAware 接口的方法，在 Spring 容器启动时自动调用，
     * 用于获取 Spring 的 ApplicationContext，以便后续从中获取所需的 Bean。
     * </p>
     *
     * @param applicationContext Spring应用程序上下文
     * @throws BeansException 如果在设置上下文过程中发生错误，则抛出BeansException异常
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        TransUtil.applicationContext = applicationContext;
    }

    /**
     * TransService 持有者（单例模式的体现）<p>
     * 已经考虑过的3点
     * <p>
     * 1并发风险低：
     * TransServiceHolder 使用静态内部类单例，JVM类加载机制已经保证了线程安全
     * 即使在极端情况下多个线程同时访问，也不会造成严重问题
     * <p>
     * 2性能考虑：
     * synchronized 会带来不必要的性能开销
     * 翻译操作本身可能比同步开销更大，加锁反而可能成为瓶颈
     * <p>
     * 3简单性优先：
     * 作为开源项目，简单清晰的代码更易于理解和维护
     * 过度设计反而会增加复杂性和潜在bug
     * <p>
     * 使用静态内部类实现单例模式，利用 JVM 类加载机制保证线程安全和延迟初始化。
     * 只有在第一次调用 get() 方法时才会创建 TransService 实例。
     * </p>
     */
    static class TransServiceHolder {

        /**
         * TransService 单例实例
         * <p>
         * 通过 applicationContext.getBean(TransService.class) 从 Spring 容器中获取，
         * 确保与 Spring 的生命周期管理保持一致。
         * </p>
         */
        private static final TransService INSTANCE = applicationContext.getBean(TransService.class);

        /**
         * 获取 TransService 单例实例
         * <p>
         * 静态工厂方法，提供对单例实例的访问点
         * </p>
         *
         * @return TransService 单例实例
         */
        public static TransService get() {
            return INSTANCE;
        }
    }

}
