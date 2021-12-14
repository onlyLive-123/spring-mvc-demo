package com.xxx.framework.aop;

import com.xxx.framework.Model.JoinPoint;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

/**
 * 代理类走向
 */
public class TJDKDynamicAopProxy implements InvocationHandler {

    private TAopConfig config;

    public TJDKDynamicAopProxy(TAopConfig config) {
        this.config = config;
    }

    /**
     * 代理会走到这个方法
     * 1.获取aspect AopConfig配置,切面信息和原始实例类
     * 2.保存method -> list<TAopConfig>配置 到map 可能多个切面
     * 3.依次调用 invoke
     */
    public Object invoke(Object proxy, Method method, Object[] args) throws Exception {
        Map<String, TAdvice> adviceMap = new HashMap<String, TAdvice>();

        Object result = null;
        JoinPoint joinPoint = new JoinPoint();
        joinPoint.setArgs(args);
        try {
            adviceMap = config.getAdviceMap(method);
            invokeAspect(adviceMap.get("before"), joinPoint);

            result = method.invoke(config.getTarget(), args);
            joinPoint.setResult(result);
            invokeAspect(adviceMap.get("after"), joinPoint);
        } catch (Exception e) {
            joinPoint.setThrowName(e.getCause().getMessage());
            invokeAspect(adviceMap.get("afterThrow"), joinPoint);
            throw e;
        }
        return result;
    }

    private void invokeAspect(TAdvice advice, JoinPoint joinPoint) {
        try {
            if (advice == null) return;
            //正常来说是 JoinPoint.invoke 来实现，这里简化 源码后再补充
            advice.getAspectMethod().invoke(advice.getAspectClass(), joinPoint);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * JDK通过接口创建代理对象 无接口的暂时不考虑
     *
     * @return
     */
    public Object getProxy() {
        return Proxy.newProxyInstance(this.getClass().getClassLoader(),
                this.config.getTargetClass().getInterfaces(), this);
    }
}
