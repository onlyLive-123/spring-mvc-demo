package com.xxx.demo.aspect;


import com.xxx.framework.Model.JoinPoint;

public class LogAspect {

    //方法之前执行before方法
    public void before(JoinPoint point) {
        System.out.println("方法之前调用:"+point.toString());
    }

    //方法之后执行after方法
    public void after(JoinPoint point) {
        System.out.println("方法之后调用:"+point.toString());
    }

    public void afterThrowing(JoinPoint point) {

        System.out.println("出现异常调用:"+point.toString());
    }
}
