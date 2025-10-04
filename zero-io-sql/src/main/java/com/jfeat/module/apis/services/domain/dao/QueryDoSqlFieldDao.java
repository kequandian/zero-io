package com.jfeat.module.apis.services.domain.dao;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jfeat.crud.plus.QueryMasterDao;
import com.jfeat.module.apis.services.gen.persistence.model.DoSqlField;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * Created by Code generator on 2022-09-16
 */
public interface QueryDoSqlFieldDao extends QueryMasterDao<DoSqlField> {
   /*
    * Query entity list by page
    */
    List<DoSqlField> findDoSqlFieldPage(Page<DoSqlField> page, @Param("record") DoSqlField record,
                                            @Param("tag") String tag,
                                            @Param("search") String search, @Param("orderBy") String orderBy,
                                            @Param("startTime") Date startTime, @Param("endTime") Date endTime);

    /*
     * Query entity model for details
     */
    DoSqlField queryMasterModel(@Param("id") Long id);


    /*
     * Query entity model list for slave items
     */
    List<DoSqlField> queryMasterModelList(@Param("masterId") Object masterId);


}