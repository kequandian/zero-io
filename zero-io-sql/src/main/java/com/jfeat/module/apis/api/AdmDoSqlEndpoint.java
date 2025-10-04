
package com.jfeat.module.apis.api;


import com.jfeat.module.apis.services.domain.service.DoSqlFieldService;
import com.jfeat.module.apis.services.gen.persistence.model.DoSqlField;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.dao.DuplicateKeyException;
import com.jfeat.module.apis.services.domain.dao.QueryDoSqlFieldDao;
import com.jfeat.crud.base.tips.SuccessTip;
import com.jfeat.crud.base.tips.Tip;
import com.jfeat.crud.base.annotation.BusinessLog;
import com.jfeat.crud.base.exception.BusinessCode;
import com.jfeat.crud.base.exception.BusinessException;

import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 * api
 * </p>
 *
 * @author Code generator
 * @since 2022-09-16
 */
@RestController
@Api("DoSqlField")
@RequestMapping("/api/adm/apis")
public class AdmDoSqlEndpoint {

    @Resource
    DoSqlFieldService DoSqlFieldService;

    @Resource
    QueryDoSqlFieldDao queryDoSqlFieldDao;


    @BusinessLog(name = "DoSqlField", value = "create DoSqlField")
    @PostMapping
    @ApiOperation(value = "新建 DoSqlField", response = DoSqlField.class)
    public Tip createDoSqlField(@RequestBody DoSqlField entity) {
        try {
            DoSqlFieldService.saveAndWriteFIle(entity);
        } catch (DuplicateKeyException e) {
            throw new BusinessException(BusinessCode.DuplicateKey);
        }

        return SuccessTip.create();
    }

    @GetMapping("/{id}")
    @ApiOperation(value = "查看 DoSqlField", response = DoSqlField.class)
    public Tip getDoSqlField(@PathVariable Long id) {
        return SuccessTip.create(DoSqlFieldService.queryMasterModel(queryDoSqlFieldDao, id));
    }

    @BusinessLog(name = "DoSqlField", value = "update DoSqlField")
    @PutMapping("/{id}")
    @ApiOperation(value = "修改 DoSqlField", response = DoSqlField.class)
    public Tip updateDoSqlField(@PathVariable Long id, @RequestBody DoSqlField entity) {
        entity.setId(id);
        return SuccessTip.create(DoSqlFieldService.updateIncludeSqlFIle(entity));
    }

    @BusinessLog(name = "DoSqlField", value = "delete DoSqlField")
    @DeleteMapping("/{id}")
    @ApiOperation("删除 DoSqlField")
    public Tip deleteDoSqlField(@PathVariable Long id) {
        return SuccessTip.create(DoSqlFieldService.deleteMaster(id));
    }

    @ApiOperation(value = "DoSqlField 列表信息", response = DoSqlField.class)
    @GetMapping
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pageNum", dataType = "Integer"),
            @ApiImplicitParam(name = "pageSize", dataType = "Integer"),
            @ApiImplicitParam(name = "search", dataType = "String"),
            @ApiImplicitParam(name = "id", dataType = "Long"),
            @ApiImplicitParam(name = "title", dataType = "String"),
            @ApiImplicitParam(name = "description", dataType = "String"),
            @ApiImplicitParam(name = "queryFileName", dataType = "String"),
            @ApiImplicitParam(name = "executionFileName", dataType = "String"),
            @ApiImplicitParam(name = "paramStatus", dataType = "Integer"),
            @ApiImplicitParam(name = "sqlVersion", dataType = "String"),
            @ApiImplicitParam(name = "note", dataType = "String"),
            @ApiImplicitParam(name = "createTime", dataType = "Date"),
            @ApiImplicitParam(name = "updateTime", dataType = "Date"),
            @ApiImplicitParam(name = "orderBy", dataType = "String"),
            @ApiImplicitParam(name = "sort", dataType = "String")
    })
    public Tip queryDoSqlFieldPage(Page<DoSqlField> page,
                                   @RequestParam(name = "pageNum", required = false, defaultValue = "1") Integer pageNum,
                                   @RequestParam(name = "pageSize", required = false, defaultValue = "10") Integer pageSize,
                                   // for tag feature query
                                   @RequestParam(name = "tag", required = false) String tag,
                                   // end tag
                                   @RequestParam(name = "search", required = false) String search,

                                   @RequestParam(name = "fieldName", required = false) String fieldName,
                                   @RequestParam(name = "sqlFileName", required = false) String sqlFileName,
                                   @RequestParam(name = "params", required = false) String params,
                                   @RequestParam(name = "note", required = false) String note,

                                   @RequestParam(name = "orderBy", required = false) String orderBy,
                                   @RequestParam(name = "sort", required = false) String sort) {

        if (orderBy != null && orderBy.length() > 0) {
            if (sort != null && sort.length() > 0) {
                String sortPattern = "(ASC|DESC|asc|desc)";
                if (!sort.matches(sortPattern)) {
                    throw new BusinessException(BusinessCode.BadRequest.getCode(), "sort must be ASC or DESC");//此处异常类型根据实际情况而定
                }
            } else {
                sort = "ASC";
            }
            orderBy = "`" + orderBy + "`" + " " + sort;
        }
        page.setCurrent(pageNum);
        page.setSize(pageSize);

        DoSqlField record = new DoSqlField();
        record.setFieldName(fieldName);
        record.setSqlFileName(sqlFileName);
        record.setParams(params);
        record.setNote(note);


        List<DoSqlField> devDevelopPage = queryDoSqlFieldDao.findDoSqlFieldPage(page, record, tag, search, orderBy, null, null);
        // 计算apiUrl
        DoSqlFieldService.calculateApiUrl(devDevelopPage);

        page.setRecords(devDevelopPage);

        return SuccessTip.create(page);
    }

    /**
     * 查询详情，包括sql文件内容
     * @param id
     * @return
     */
    @GetMapping("/{id}/detail")
    public Tip queryDetail(@PathVariable("id") Long id) {
        return SuccessTip.create(DoSqlFieldService.getDetail(id));
    }
}

