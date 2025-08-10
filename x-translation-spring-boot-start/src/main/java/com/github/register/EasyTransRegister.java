package com.github.register;

import com.github.repository.TransRepository;
import com.github.repository.TransRepositoryFactory;
import com.github.resolver.TransObjResolver;
import com.github.resolver.TransObjResolverFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

/**
 * EasyTransRegister: 翻译组件自动注册器
 * <p>
 * 这是一个 Spring Bean 后置处理器，负责自动发现并注册实现了特定接口的 Bean。
 * 采用 Spring Boot Starter 的标准做法，通过 BeanPostProcessor 机制实现组件的自动注册，
 * 无需用户手动配置，提高了框架的易用性和扩展性。
 * </p>
 * <p>
 * 自动注册机制：
 * 1. TransRepository 实现类：自动注册到 TransRepositoryFactory 工厂中，作为数据源提供者
 * 2. TransObjResolver 实现类：自动注册到 TransObjResolverFactory 工厂中，作为对象解析器
 * </p>
 *
 * @author zhangxiaoxiang
 * @since 2025/7/27
 */
@Slf4j
public class EasyTransRegister implements BeanPostProcessor {

    /**
     * Bean 初始化后处理方法
     * <p>
     * 在每个 Spring Bean 初始化完成后调用此方法，检查 Bean 是否实现了特定接口，
     * 如果实现则自动注册到相应的工厂类中。这是框架自动发现和注册用户自定义组件的核心机制。
     * </p>
     * <p>
     * 注意：只有被 Spring 容器管理的 Bean 才会被此方法处理，单纯的接口实现类如果没有被 Spring
     * 管理（如未添加 @Component 注解或未通过 @Bean 方法定义）将不会被注册。
     * </p>
     *
     * @param bean     Spring 容器中的 Bean 实例
     * @param beanName Bean 的名称
     * @return 处理后的 Bean 实例
     * @throws BeansException 如果处理过程中发生错误
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        // 检查是否为 TransRepository 实现类
        if (bean instanceof TransRepository) {
            // 注册到 TransRepositoryFactory 工厂中
            TransRepositoryFactory.register((TransRepository) bean);
            log.info("TransRepository: {} 数据仓库已注册", beanName);
        }
        // 检查是否为 TransObjResolver 实现类
        else if (bean instanceof TransObjResolver) {
            // 注册到 TransObjResolverFactory 工厂中
            TransObjResolverFactory.register((TransObjResolver) bean);
            log.info("TransObjResolver: {} 包装器已注册", beanName);
        }
        // 返回未修改的 Bean 实例
        return bean;
    }

}
