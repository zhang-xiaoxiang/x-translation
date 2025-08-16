package com.github.xtranslation.starter.aop;


import com.github.xtranslation.starter.util.TransUtil;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;

/**
 * AutoTransAspect: 自动事务切面
 *
 * @author zhangxiaoxiang
 * @since 2025/7/27
 */
@Aspect
public class AutoTransAspect {

    /**
     * 后置返回通知方法，在目标方法成功执行并返回结果后调用
     * 该方法通过AOP切面拦截带有@AutoTrans注解的方法，对返回结果进行自动转换处理
     *
     * @param methodResult 目标方法的返回结果对象
     * @return 处理后的返回结果对象
     */
    @AfterReturning(pointcut = "@annotation(com.github.xtranslation.starter.annotation.AutoTrans)", returning = "methodResult")
    public Object afterReturning(Object methodResult) {
        // 对方法返回结果进行转换处理
        TransUtil.trans(methodResult);
        return methodResult;
    }


}
