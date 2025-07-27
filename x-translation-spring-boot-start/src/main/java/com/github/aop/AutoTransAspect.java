package com.github.aop;


import com.github.uitl.TransUtil;
import org.aspectj.lang.JoinPoint;
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

    @AfterReturning(pointcut = "@annotation(com.github.annotation.AutoTrans)", returning = "methodResult")
    public Object afterReturning(JoinPoint joinPoint, Object methodResult) {
        TransUtil.trans(methodResult);
        return methodResult;
    }

}
