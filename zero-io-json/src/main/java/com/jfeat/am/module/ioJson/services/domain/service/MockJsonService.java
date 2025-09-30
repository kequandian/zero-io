package com.jfeat.am.module.ioJson.services.domain.service;

import com.alibaba.fastjson.JSONObject;
// import java.util.Map;

/**
 *
 */
public interface MockJsonService {
    JSONObject readJsonFile(String name);

    JSONObject readJsonFile(String name, String tag);

    /**
     * 保存json文件
     * @param json
     * @param name
     * @return
     */
    Integer saveJsonToFile(JSONObject json, String name);

    
    Integer saveJsonToFile(JSONObject json, String name, String tag);

    // Map<String, String> getIdMap();


    /**
     * 保存json到数据库
     * @param json
     * @param name
     * @param tag
     * @return
     */
    Integer saveJsonToDataBase(JSONObject json, String name, String tag);
    // Integer synchronizationToDataBase();
}