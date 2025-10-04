package com.jfeat.module.apis.services.gen.crud.service.impl;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jfeat.crud.plus.impl.CRUDServiceOnlyImpl;
import com.jfeat.module.apis.services.gen.crud.service.CRUDDoSqlFieldService;
import com.jfeat.module.apis.services.gen.persistence.dao.DoSqlFieldMapper;
import com.jfeat.module.apis.services.gen.persistence.model.DoSqlField;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * <p>
 *  implementation
 * </p>
 *CRUDDoSqlFieldService
 * @author Code generator
 * @since 2022-09-16
 */

@Service
public class CRUDDoSqlFieldServiceImpl extends CRUDServiceOnlyImpl<DoSqlField> implements CRUDDoSqlFieldService {

        @Resource
        protected DoSqlFieldMapper devDevelopMapper;

        @Override
        protected BaseMapper<DoSqlField> getMasterMapper() {
                return devDevelopMapper;
        }

}


