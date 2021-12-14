package com.xxx.framework.aop;

import lombok.Data;

import java.lang.reflect.Method;

/**
 * AOP切面调用链封装
 */
@Data
public class TAdvice {
    private Object aspectClass;
    private Method aspectMethod;
    private String throwName;

    public TAdvice(Object aspectClass, Method aspectMethod) {
        this.aspectClass = aspectClass;
        this.aspectMethod = aspectMethod;
    }
}
