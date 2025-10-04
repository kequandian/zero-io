package com.jfeat.module.apis.services.domain.service;

import com.alibaba.fastjson.JSONArray;

import java.util.List;
import java.util.Map;

public interface ParseSql {

    /**
     * 读取sql文件
     * @param fileName sql文件名 不带.sql后缀
     * @return sql语句
     */
    String readSqlFile(String fileName);

    /**
     *
     * @param sql sql语句
     * @param parameter 参数键值对
     * @return 匹配好的sql语句
     */
    String sqlParameters(String sql, Map<String,String> parameter);

    /**
     * 执行sql语句
     * @param sql
     * @return
     */
    int executeSql(String sql);

    /**
     * 查询sql
     * @param sql
     * @return 二维列表对象 每一个列表代表一个查询结果
     */
    JSONArray querySql(String sql);


    /**
     * 将sql语句 分成一条一条语句列表
     * @param sql
     * @return
     */
    List<String> loadSql(String sql);


}
