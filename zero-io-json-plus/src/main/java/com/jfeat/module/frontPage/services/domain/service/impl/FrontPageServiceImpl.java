package com.jfeat.module.frontPage.services.domain.service.impl;

import com.jfeat.module.frontPage.services.domain.service.FrontPageService;
import com.jfeat.module.frontPage.services.gen.crud.service.impl.CRUDFrontPageServiceImpl;
import org.springframework.stereotype.Service;


/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author admin
 * @since 2017-10-16
 */

@Service("frontPageService")
public class FrontPageServiceImpl extends CRUDFrontPageServiceImpl implements FrontPageService {

    @Override
    protected String entityName() {
        return "FrontPage";
    }





}
