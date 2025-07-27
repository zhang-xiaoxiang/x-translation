package com.github.uitl;


import com.github.service.TransService;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
/**
 * TransUtil: 翻译工具类
 *
 * @author zhangxiaoxiang
 * @since 2025/7/27
 */
public class TransUtil implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    /**
     * 翻译工具
     *
     * @param obj 需要翻译的对象
     * @return 是否翻译成功
     */
    public static boolean trans(Object obj) {
        return TransServiceHolder.get().trans(obj);
    }

    /**
     * 设置Spring应用程序上下文
     *
     * @param applicationContext Spring应用程序上下文
     * @throws BeansException 如果在设置上下文过程中发生错误，则抛出BeansException异常
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        TransUtil.applicationContext = applicationContext;
    }

    static class TransServiceHolder {
        private static final TransService INSTANCE = applicationContext.getBean(TransService.class);

        public static TransService get() {
            return INSTANCE;
        }
    }

}
