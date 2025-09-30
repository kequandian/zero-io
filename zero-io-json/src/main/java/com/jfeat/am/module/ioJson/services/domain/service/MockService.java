package com.jfeat.am.module.ioJson.services.domain.service;

import com.alibaba.fastjson.JSONObject;
// import java.util.Map;

public interface MockService {

    /**
     *  从文件中读取mock数据
     * @param name
     * @param dir 可以为空，为空时表示从默认目录读取
     * @return
     */
    JSONObject readJsonFile(String name, String dir);

    /**
     *  保存mock数据到文件
     * @param json
     * @param name
     * @param dir 可以为空，为空时表示保存到默认目录
     * @return
     */
    Integer saveJsonToFile(JSONObject json, String name, String dir);


    // /**
    //  *  获取指定目录下的所有mock数据文件名
    //  * @param dir
    //  * @return
    //  */
    // Map<String, String> getJsonNameMap(String dir);

}
