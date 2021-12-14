package com.xxx.framework.springmvc;

import lombok.Data;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.RandomAccessFile;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
public class TView {

    String htmlPath;

    public TView(String htmlPath) {
        this.htmlPath = htmlPath;
    }


    public void render(Map<String, Object> modelMap, String htmlName, HttpServletResponse resp) throws Exception {
        StringBuilder builder = new StringBuilder();
        //获取html的路径
        File viewFile = new File(this.htmlPath + "/" + htmlName
                + (htmlName.endsWith(".html") ? "" : ".html"));
        //读行
        RandomAccessFile accessFile = new RandomAccessFile(viewFile, "r");
        String line;
        while (null != (line = accessFile.readLine())) {
            line = new String(line.getBytes("ISO-8859-1"), "utf-8");
            //正则匹配#{}的字段
            Pattern pattern = Pattern.compile("#\\{[^\\}]+\\}", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(line);
            while (matcher.find()) {
                String paramName = matcher.group();
                //#{name}
                paramName = paramName.replaceAll("#\\{|\\}", "");
                //从map中取出并替换
                Object paramValue = modelMap.get(paramName);
                if (paramValue != null) {
                    line = matcher.replaceFirst(makeStringForRegExp(paramValue.toString()));
                } else {
                    line = matcher.replaceFirst("null");
                }
                matcher = pattern.matcher(line);
            }
            builder.append(line);
        }
        resp.setCharacterEncoding("utf-8");
        resp.getWriter().write(builder.toString());
    }

    //处理特殊字符
    public static String makeStringForRegExp(String str) {
        return str.replace("\\", "\\\\").replace("*", "\\*")
                .replace("+", "\\+").replace("|", "\\|")
                .replace("{", "\\{").replace("}", "\\}")
                .replace("(", "\\(").replace(")", "\\)")
                .replace("^", "\\^").replace("$", "\\$")
                .replace("[", "\\[").replace("]", "\\]")
                .replace("?", "\\?").replace(",", "\\,")
                .replace(".", "\\.").replace("&", "\\&");
    }
}
