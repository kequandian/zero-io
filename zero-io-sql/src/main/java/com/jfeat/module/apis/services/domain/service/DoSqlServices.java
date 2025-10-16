package com.jfeat.module.apis.services.domain.service;

import javax.servlet.http.HttpServletRequest;

import com.alibaba.fastjson.JSONObject;

public interface DoSqlServices {
    JSONObject querySql(HttpServletRequest request, String sqlFile);

    int executeSql(HttpServletRequest request, String sqlFile);
}
