package com.xxx.demo.service.impl;

import com.xxx.demo.service.HelloService;
import com.xxx.framework.annotation.TService;

import java.text.SimpleDateFormat;
import java.util.Date;

@TService
public class HelloServiceImpl implements HelloService {

    public String sayHello(String name) {
        return "my name is " + name;
    }

    public String getDataTime(String name) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date = format.format(new Date());
        System.out.println("获取当前时间:" + date + ",name=" + name);
//        int i =1/0;     //测试切面异常和返回异常model
        return date;
    }
}
