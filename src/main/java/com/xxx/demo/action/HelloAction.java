package com.xxx.demo.action;


import com.xxx.demo.service.HelloService;
import com.xxx.framework.Model.TModelAndView;
import com.xxx.framework.annotation.TAutowired;
import com.xxx.framework.annotation.TController;
import com.xxx.framework.annotation.TRequestMapping;
import com.xxx.framework.annotation.TRequestParam;

import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@TController
@TRequestMapping("/mvc")
public class HelloAction {

    @TAutowired
    HelloService helloService;

    public String hello(String name) {
        return helloService.sayHello(name);
    }


    @TRequestMapping("/hello1")
    public void hello1(@TRequestParam(name = "name") String name, HttpServletResponse response) {
        try {
            response.setHeader("Content-Type","text/html; charset=utf-8");
            response.getWriter().write(helloService.sayHello(name));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @TRequestMapping("/hello2")
    public String hello2(@TRequestParam(name = "a") Integer a, @TRequestParam(name = "b") Integer b) {
        return "a + b = " + (a + b);
//        return null; //测试null返回
    }


    @TRequestMapping("/index.html")
    public TModelAndView index(@TRequestParam(name = "name") String name) {
        Map<String, Object> modelMap;
        try {
            String time = helloService.getDataTime(name);
            modelMap = new HashMap<String, Object>();
            modelMap.put("name", name);
            modelMap.put("date", time);
            modelMap.put("token", UUID.randomUUID());

            return new TModelAndView("index", modelMap);
        } catch (Exception e) {
            modelMap = new HashMap<String, Object>();
            modelMap.put("msg", "服务器请求异常");
            modelMap.put("error", Arrays.toString(e.getStackTrace()));
            return new TModelAndView("500", modelMap);
        }
    }

}
