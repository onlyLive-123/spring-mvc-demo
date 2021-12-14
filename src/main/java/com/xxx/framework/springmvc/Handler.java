package com.xxx.framework.springmvc;

import com.xxx.framework.annotation.TRequestParam;
import lombok.Data;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@Data
public class Handler {

    Object target;  //实例类
    Method method;  //方法
    String url;     //请求路径
    Class<?>[] parameterTypes; //参数类型
    Map<String,Integer> mapIndex;   //参数位置


    public Handler(Object target, Method method, String reqUrl) {
        this.target = target;
        this.method = method;
        this.url = reqUrl;
        this.parameterTypes = method.getParameterTypes();
        doHanlerParam(method);
    }

    //初始化参数map 记录参数key和位置
    private void doHanlerParam(Method method) {
        this.mapIndex = new HashMap<String, Integer>();
        //先处理 req 和resp的位置
        for (int i = 0; i < this.parameterTypes.length; i++) {
            Class<?> type = this.parameterTypes[i];
            if (type == HttpServletRequest.class || type == HttpServletResponse.class) {
                this.mapIndex.put(type.getName(),i);
            }
        }

        //处理参数的位置(根据注解)
        Annotation[][] pas = method.getParameterAnnotations();
        for (int k = 0; k < pas.length; k++) {  //如果用forEach 需要用Map存储参数位置
            Annotation[] pa = pas[k];
            for (int i = 0; i < pa.length; i++) {
                Annotation annotation = pa[i];
                if (annotation instanceof TRequestParam) {
                    String key = ((TRequestParam) annotation).name();
                    this. mapIndex.put(key,k);
                }
            }
        }
    }
}
