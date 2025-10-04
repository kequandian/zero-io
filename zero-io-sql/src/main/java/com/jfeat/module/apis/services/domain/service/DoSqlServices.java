package com.jfeat.module.apis.services.domain.service;

import com.alibaba.fastjson.JSONArray;

import javax.servlet.http.HttpServletRequest;

public interface DoSqlServices {
    JSONArray querySql(HttpServletRequest request, String sqlFile);

    int executeSql(HttpServletRequest request, String sqlFile);
}
