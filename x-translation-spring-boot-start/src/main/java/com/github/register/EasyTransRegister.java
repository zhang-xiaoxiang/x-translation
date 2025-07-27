package com.github.register;


import com.github.repository.TransRepository;
import com.github.repository.TransRepositoryFactory;
import com.github.resolver.TransObjResolver;
import com.github.resolver.TransObjResolverFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

/**
 * EasyTransRegister: 注册TransRepository和TransObjResolver
 *
 * @author zhangxiaoxiang
 * @since 2025/7/27
 */
@Slf4j
public class EasyTransRegister implements BeanPostProcessor {

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof TransRepository) {
            TransRepositoryFactory.register((TransRepository) bean);
            log.info("TransRepository: {} 数据仓库已注册", beanName);
        } else if (bean instanceof TransObjResolver) {
            TransObjResolverFactory.register((TransObjResolver) bean);
        log.info("TransObjResolver: {} 包装器已注册", beanName);
        }
        return bean;
    }

}
