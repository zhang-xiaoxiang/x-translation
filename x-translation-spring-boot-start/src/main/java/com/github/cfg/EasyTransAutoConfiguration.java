package com.github.cfg;

import com.github.aop.AutoTransAspect;
import com.github.register.EasyTransRegister;
import com.github.repository.dict.DictLoader;
import com.github.repository.dict.DictTransRepository;
import com.github.service.TransService;
import com.github.util.TransUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * EasyTransAutoConfiguration: 自动配置类
 * <p>
 * 这是 x-translation Spring Boot Starter 的核心自动配置类，负责初始化框架所需的所有核心组件。
 * 采用 Spring Boot Starter 标准做法，通过 @Configuration 注解集中管理所有 Bean 的创建和配置。
 * </p>
 *
 * @author zhangxiaoxiang
 * @since 2025/7/27
 */
@Configuration
@Slf4j
public class EasyTransAutoConfiguration {

    /**
     * 创建并初始化翻译服务 Bean
     * <p>
     * 这是框架的核心服务组件，负责实际的翻译处理逻辑。使用 @ConditionalOnMissingBean 注解确保
     * 用户可以自定义替换默认的 TransService 实现。在创建实例后调用 init() 方法进行必要的初始化操作，
     * 如加载翻译规则、初始化缓存等。
     * </p>
     *
     * @return 初始化完成的 TransService 实例
     */
    @Bean
    @ConditionalOnMissingBean
    public TransService transService() {
        TransService transService = new TransService();
        // 初始化翻译服务，加载必要的配置和数据
        transService.init();
        log.warn("================== x-translation 启动啦 ======================");
        return transService;
    }

    /**
     * 创建字典翻译仓库 Bean
     * <p>
     * 该 Bean 依赖于用户提供的 DictLoader 实现，只有当 Spring 容器中存在 DictLoader Bean 时才会创建。
     * 负责管理字典类型的翻译数据，作为 TransService 的数据源之一。
     * </p>
     *
     * @param dictLoader 用户提供的字典加载器实现
     * @return 字典翻译仓库实例
     */
    @Bean
    @ConditionalOnBean(DictLoader.class)
    public DictTransRepository dictTransRepository(DictLoader dictLoader) {
        return new DictTransRepository(dictLoader);
    }

    /**
     * 创建翻译注册器 Bean
     * <p>
     * 负责注册和管理各种翻译处理器，维护翻译规则的注册表，是框架扩展性的基础组件。
     * </p>
     *
     * @return 翻译注册器实例
     */
    @Bean
    public EasyTransRegister easyTransRegister() {
        return new EasyTransRegister();
    }

    /**
     * 创建自动翻译切面 Bean
     * <p>
     * 实现基于注解的自动翻译功能，通过 AOP 拦截带有 @AutoTrans 注解的方法，
     * 对方法返回值自动进行翻译处理。
     * </p>
     *
     * @return 自动翻译切面实例
     */
    @Bean
    public AutoTransAspect autoTransAspect() {
        // 切面类和普通类一样可以注入,只上告诉有@Aspect注解,水喷淋额外处理切面即可,本质还是一个普通类,正常注入即可
        return new AutoTransAspect();
    }

    /**
     * 创建翻译工具类 Bean
     * <p>
     * 虽然 TransUtil 主要提供静态方法访问，但仍需要通过 Spring 容器管理，
     * 以便实现 ApplicationContextAware 接口，获取 Spring 上下文环境。
     * </p>
     *
     * @return 翻译工具类实例
     */
    @Bean
    public TransUtil transUtil() {
        return new TransUtil();
    }

}
