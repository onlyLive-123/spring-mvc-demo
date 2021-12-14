package com.xxx.framework.aop;

import lombok.Data;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

@Data
public class TAopConfig {
    //切面信息保存
    String pointCut;
    String aspectClass;
    String aspectBefore;
    String aspectAfter;
    String aspectAfterThrowing;

    Pattern methodPattern;

    //原始实例和clazz信息
    Object target;
    Class<?> targetClass;

    Map<Method,Map<String,TAdvice>> adviceCacheMap = new HashMap<Method, Map<String, TAdvice>>();

    public void setTargetClass(Class<?> targetClass) {
        this.targetClass = targetClass;
        parse();
    }

    //把切面信息组成一个map 给invoker调用
    /**
     * 初始化切面类
     * 获取对应方法存入map
     * 通过
     */
    private void parse() {
        try {
            Class<?> clazz = Class.forName(this.aspectClass);
            Method[] methods = clazz.getMethods();
            Map<String,Method> aspectMap = new HashMap<String, Method>();
            for (Method method : methods) {
                aspectMap.put(method.getName(),method);
            }
            Map<String,TAdvice> adviceMap = new HashMap<String, TAdvice>();
            Object aspectInstance = clazz.newInstance();
            for (Method method : this.targetClass.getMethods()) {
                String methodName = method.toString();
                if(methodName.contains("throws")){
                    methodName = methodName.substring(0,methodName.lastIndexOf("throws")).trim();
                }
                //如果方法满足切点
                if(methodPattern.matcher(methodName).matches()){
                    if(null != this.aspectBefore){
                        adviceMap.put("before",new TAdvice(aspectInstance,aspectMap.get(this.aspectBefore)));
                    }

                    if(null != this.aspectAfter){
                        adviceMap.put("after",new TAdvice(aspectInstance,aspectMap.get(this.aspectAfter)));
                    }

                    if(null != this.aspectAfterThrowing){
                        adviceMap.put("afterThrow",new TAdvice(aspectInstance,aspectMap.get(this.aspectAfterThrowing)));
                    }
                    this.adviceCacheMap.put(method,adviceMap);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //method 实际为代理对象的方法 这里需要去原targetClass拿到原始方法 才能在Map中找出映射关系
    public  Map<String, TAdvice> getAdviceMap(Method method) throws NoSuchMethodException {

            Map<String, TAdvice> adviceMap = adviceCacheMap.get(method);
            if(adviceMap == null){
                Method method1 = this.targetClass.getMethod(method.getName(), method.getParameterTypes());
                adviceMap = adviceCacheMap.get(method1);
                this.adviceCacheMap.put(method,adviceMap);
            }
            if(adviceMap == null){
                return new HashMap<String, TAdvice>();
            }
            return adviceMap;
    }
}
