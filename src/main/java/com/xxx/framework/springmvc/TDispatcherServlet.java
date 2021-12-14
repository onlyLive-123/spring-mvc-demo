package com.xxx.framework.springmvc;

import com.xxx.framework.Model.TModelAndView;
import com.xxx.framework.annotation.TController;
import com.xxx.framework.annotation.TRequestMapping;
import com.xxx.framework.spring.TestApplicationContext;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class TDispatcherServlet extends HttpServlet {

    TestApplicationContext context;

    //请求路径对应的Controller spring 实际是list 这里简化用map
    Map<String, Handler> handlerMapping = new HashMap<String, Handler>();
    TView view;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            //调用
            doDispatcher(req, resp);
        } catch (Exception e) {
            try {
                if ("404".equals(e.getMessage())) {
                    this.view.render(null, "404", resp);
                } else {
                    Map<String, Object> modelMap = new HashMap<String, Object>();
                    modelMap.put("msg", e.getMessage());
                    modelMap.put("error", Arrays.toString(e.getStackTrace()));
                    this.view.render(modelMap, "500", resp);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }

        }
    }

    private void doDispatcher(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        //根据请求路径 得到存储Controller类的handler
        Handler handler = getHandler(req, resp);
        //解析参数
        Object[] objects = getParam(req, resp, handler);

        //反射调用
        Object result = handler.getMethod().invoke(handler.getTarget(), objects);

        if (null == result || result instanceof Void) return;
        //返回页面的
        if (result instanceof TModelAndView) {
            TModelAndView mv = (TModelAndView) result;
            this.view.render(mv.getModel(), mv.getHtmlName(), resp);
        } else {
            //解决前端页面显示中文乱码
            resp.setHeader("Content-Type", "text/html; charset=utf-8");
            resp.getWriter().write(result.toString());
        }
    }

    private Object[] getParam(HttpServletRequest req, HttpServletResponse resp, Handler handler) {
        //参数类型和参数的下标位置
        Class<?>[] parameterTypes = handler.getParameterTypes();
        Map<String, Integer> mapIndex = handler.getMapIndex();

        //传入参数
        Map<String, String[]> parameterMap = req.getParameterMap();

        Object[] params = new Object[mapIndex.size()];
        //根据下标位置获取参数
        for (Map.Entry<String, Integer> entry : mapIndex.entrySet()) {
            String key = entry.getKey();
            Integer index = entry.getValue();
            if (key.equals(HttpServletRequest.class.getName())) {
                params[index] = req;
            }
            if (key.equals(HttpServletResponse.class.getName())) {
                params[index] = resp;
            }
            if (parameterMap.containsKey(key)) {
                String value = Arrays.toString(parameterMap.get(key)).replaceAll("\\[|\\]", "");
                params[index] = converter(value, parameterTypes[index]);
            }
        }
        return params;
    }

    private Object converter(String value, Class<?> type) {
        if (type == Integer.class) {
            return Integer.valueOf(value);
        } else if (type == Double.class) {
            return Double.valueOf(value);
        }
        return value;
    }

    private Handler getHandler(HttpServletRequest req, HttpServletResponse resp) {
        String uri = req.getRequestURI();

        if (this.handlerMapping.containsKey(uri)) {
            return this.handlerMapping.get(uri);
        }
        throw new RuntimeException("404");
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        //IOC DI阶段 直接调用上下文
        this.context = new TestApplicationContext(
                config.getInitParameter("contextConfigLocation"));

        //初始化HandlerMapping & 参数解析
        initHandlerMapping();

        //初始化视图转换器
        initViewResolvers(context.getContextConfig().getProperty("htmlRootTemplate"));

        System.out.println("spring mvc is init");
    }

    private void initViewResolvers(String htmlRootTemplate) {
        //加载html页面目录
        URL url = this.getClass().getClassLoader().getResource(htmlRootTemplate);

        //封装成view 在view中提供渲染方法
        this.view = new TView(url.getFile());
    }

    private void initHandlerMapping() {
        //加载Controller层
        for (Map.Entry<String, Object> entry : this.context.getIoc().entrySet()) {
            Object instance = entry.getValue();
            Class<?> clazz = instance.getClass();
            if (!clazz.isAnnotationPresent(TController.class)) continue;

            //获取根路径
            String baseUrl = "";
            if (clazz.isAnnotationPresent(TRequestMapping.class)) {
                TRequestMapping annotation = clazz.getAnnotation(TRequestMapping.class);
                baseUrl = annotation.value();
            }
            //获取所有方法上的路径
            Method[] methods = clazz.getMethods();
            for (Method method : methods) {
                if (!method.isAnnotationPresent(TRequestMapping.class)) continue;

                TRequestMapping annotation = method.getAnnotation(TRequestMapping.class);
                String reqUrl = (baseUrl + "/" + annotation.value()).replaceAll("/+", "/");
                if (this.handlerMapping.containsKey(reqUrl)) {
                    throw new RuntimeException("[" + reqUrl + "] the handlerMapping is exists");
                }

                this.handlerMapping.put(reqUrl, new Handler(instance, method, reqUrl));
                System.out.println("mapping添加: " + reqUrl);
            }
        }
    }
}
