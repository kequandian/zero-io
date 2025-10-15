package com.jfeat.module.apis.services.domain.service;

import com.jfeat.module.apis.services.gen.crud.service.CRUDDoSqlFieldService;
import com.jfeat.module.apis.services.gen.persistence.model.DoSqlField;

/**
 * Created by vincent on 2017/10/19.
 */
public interface DoSqlFieldService extends CRUDDoSqlFieldService {

    /**
     * 创建
     * @param doSqlField
     */
    void saveAndWriteFIle(DoSqlField doSqlField);


    /**
     * 获取详情，包活sql文件内容
     * @param id
     * @return
     */
    DoSqlField getDetail(Long id);

    /**
     * 更新，包含sql文件内容
     * @param doSqlField
     * @return
     */
    Integer updateIncludeSqlFIle(DoSqlField doSqlField);

    /**
     * 遍历 apis.dosql.path 路径下的所有 .sql 文件，初始化/更新数据库记录
     * @return 统计结果
     */
    java.util.Map<String, Object> initializeFromSqlFiles();
}