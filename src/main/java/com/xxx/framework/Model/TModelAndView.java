package com.xxx.framework.Model;

import lombok.Data;

import java.util.Map;

@Data
public class TModelAndView {

    String htmlName;
    Map<String,Object> model;

    public TModelAndView(String htmlName) {
        this.htmlName = htmlName;
    }

    public TModelAndView(String htmlName, Map<String, Object> model) {
        this.htmlName = htmlName;
        this.model = model;
    }
}
