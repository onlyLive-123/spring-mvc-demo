package com.xxx.framework.Model;

import lombok.Data;

import java.lang.reflect.Method;

@Data
public class JoinPoint {

    Object target;
    Method method;
    Object[] args;
    Object result;
    String throwName;

    public Object proceed() throws Exception {
        return method.invoke(target,this);
    }
}
