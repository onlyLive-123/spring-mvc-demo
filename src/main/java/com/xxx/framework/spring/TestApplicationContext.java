package com.xxx.framework.spring;

import com.xxx.framework.annotation.TAutowired;
import com.xxx.framework.annotation.TController;
import com.xxx.framework.annotation.TService;
import com.xxx.framework.aop.TAopConfig;
import com.xxx.framework.aop.TJDKDynamicAopProxy;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * new TestApplicationContext("classpath:application.properties") 方式调用
 */
public class TestApplicationContext {

    //保存配置
    public Properties contextConfig = new Properties();
    //保存需要加载的clasName
    public List<String> classNames = new ArrayList<String>();
    //ioc容器
    public ConcurrentHashMap<String, Object> ioc = new ConcurrentHashMap<String, Object>();

    public Properties getContextConfig() {
        return contextConfig;
    }

    public ConcurrentHashMap<String, Object> getIoc() {
        return ioc;
    }

    public TestApplicationContext(String... classpath) {
        init(classpath);
    }

    public void init(String... classpath) {
        //初始化配置
        doLoadConfig(classpath[0]);

        //扫描对应的类
        doScanner(contextConfig.getProperty("packscanner"));

        //实例化对象 并加到IOC容器 加了注解的类
        doInstance();

        //依赖注入
        doAutowired();

        System.out.println("application is init");
    }

    //简化版的实现 先了解这个思路
    private void doAutowired() {
        for (Map.Entry<String, Object> entry : this.ioc.entrySet()) {
            //判断声明的方法里是否有 @TestAutowired注解的
            Object instance = entry.getValue();
            Class<?> clazz = instance.getClass();
            //所有声明的方法
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                if(!field.isAnnotationPresent(TAutowired.class)) continue;
                //默认beanName是全路径
                String name = field.getType().getName();
                //通过注解value上的value来确定beanName 等同于@Qualifier(value="xxx") 这里就简化了
                TAutowired annotation = field.getAnnotation(TAutowired.class);
                if(!"".equals(annotation.value())){
                    name = annotation.value();
                }

                Object autowiredService = this.ioc.get(name);
                if(autowiredService == null) continue;

                field.setAccessible(true);

                try {
                    field.set(instance,autowiredService);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void doInstance() {
        for (String className : this.classNames) {
            try {
                Class<?> clazz = Class.forName(className);

                //只有加了注解的类才初始化
                if (!clazz.isAnnotationPresent(TController.class) && !clazz.isAnnotationPresent(TService.class)) {
                    continue;
                }
                //如果满足AOP切面条件 此处应该生成代理对象 真实调用应该有invoke实现
                Object instance = initAopConfig(clazz);

                //保存到IOC容器 beanName -> instance 生成bean name 首字母小写
                String beanName = toFristLowerCase(clazz.getSimpleName());
                //如果是service 看是否有自定义名称
                if(clazz.isAnnotationPresent(TService.class)){
                    TService service = clazz.getAnnotation(TService.class);
                    if(!"".equals(service.value())){
                        beanName = service.value();
                    }
                }

                //比如beanName 相同的暂不考虑 主要是体现思想
                this.ioc.put(beanName,instance);

                //如果是接口实现的 需要把接口全路径对应到service
                Class<?>[] interfaces = clazz.getInterfaces();
                for (Class<?> i : interfaces) {
                    this.ioc.put(i.getName(),instance);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private Object initAopConfig(Class<?> clazz) throws Exception {
        Object instance = clazz.newInstance();

        //获取切面的切点 正则匹配全路径类名 匹配成功则生成代理类
        String pointCut = this.contextConfig.getProperty("pointCut");
        //把字符串里面的符号 替换成 可识别的正则符号
        String regxMethodStr = pointCut.replaceAll("\\.","\\\\.")
                .replaceAll("\\\\.\\*",".*")
                .replaceAll("\\(","\\\\(")
                .replaceAll("\\)","\\\\)");
        //类名是 class com.xxx.demo.service.impl.HelloServiceImp 所以匹配类名做如下处理
        //public .* com\.xxx\.demo\.service\.impl\..*
        String regxClassStr = regxMethodStr.substring(0,regxMethodStr.lastIndexOf("\\("));
        //class com\.xxx\.demo\.service\.impl\..*
        Pattern classPattern = Pattern.compile("class " + regxClassStr.substring(regxClassStr.lastIndexOf(" ")+1));
        if(classPattern.matcher(instance.getClass().toString()).matches()){
            //匹配成功就创建代理对象
            TAopConfig config = new TAopConfig();
            config.setPointCut(pointCut);
            config.setAspectClass(this.contextConfig.getProperty("aspectClass"));
            config.setAspectBefore(this.contextConfig.getProperty("aspectBefore"));
            config.setAspectAfter(this.contextConfig.getProperty("aspectAfter"));
            config.setAspectAfterThrowing(this.contextConfig.getProperty("aspectAfterThrowing"));
            //匹配方法的正则
            Pattern methodPattern = Pattern.compile(regxMethodStr);
            config.setMethodPattern(methodPattern);
            config.setTarget(instance);
            config.setTargetClass(clazz);
            //匹配上的生成代理对象
            instance = new TJDKDynamicAopProxy(config).getProxy();
        }
        return instance;
    }

    private String toFristLowerCase(String simpleName) {
        char[] chars = simpleName.toCharArray();
        chars[0] += 32;
        return new String(chars);
    }

    private void doScanner(String packscanner) {
        URL url = this.getClass().getResource("/" + packscanner.replaceAll("\\.", "/"));
        File file = new File(url.getPath());
        for (File f : file.listFiles()) {
            if (f.isDirectory()) {
                doScanner(packscanner + "." + f.getName());
            } else {
                if (!f.getName().endsWith(".class")) continue;
                this.classNames.add(packscanner + "." + f.getName().replaceAll("\\.class", ""));
            }
        }
    }

    private void doLoadConfig(String config) {
        InputStream inputStream = this.getClass().getClassLoader()
                .getResourceAsStream(config.replace("classpath:", ""));
        try {
            contextConfig.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Object getBean(Class<?> clazz) {
        return this.getBean(toFristLowerCase(clazz.getSimpleName()));
    }

    public Object getBean(String beanName) {
        return this.ioc.get(beanName);
    }

}
