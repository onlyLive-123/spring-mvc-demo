package com.xxx.framework.spring;

import com.xxx.demo.action.HelloAction;

public class Test {

    public static void main(String[] args) {
        TestApplicationContext context = new TestApplicationContext("classpath:application.properties");
        HelloAction action = (HelloAction) context.getBean(HelloAction.class);
        String hello = action.hello("张三");
        System.out.println(hello);
    }

}
