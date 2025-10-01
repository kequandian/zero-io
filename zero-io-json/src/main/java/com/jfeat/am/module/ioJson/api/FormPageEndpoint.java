package com.jfeat.am.module.ioJson.api;


import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jfeat.am.module.ioJson.services.domain.service.MockJsonService;
import com.jfeat.crud.base.exception.BusinessCode;
import com.jfeat.crud.base.exception.BusinessException;
import com.jfeat.crud.base.tips.SuccessTip;
import com.jfeat.crud.base.tips.Tip;
import com.jfeat.module.frontPage.services.domain.dao.QueryFrontPageDao;
import com.jfeat.module.frontPage.services.domain.model.FrontPageRecord;
import com.jfeat.module.frontPage.services.gen.persistence.dao.FrontPageMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.ibatis.annotations.Param;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * <p>
 * api
 * </p>
 *
 * @author Code generator
 * @since 2022-05-19
 */
@RestController

@Api("表单用")
@RequestMapping("/form")
public class FormPageEndpoint {

    @Resource
    MockJsonService mockJsonService;

    @Resource
    FrontPageMapper frontPageMapper;
    @Resource
    QueryFrontPageDao queryFrontPageDao;



    @GetMapping("")
    @ApiOperation(value = "查看表单Json")
    public Tip getFormJson(@Param(value = "id") String id) {
        return SuccessTip.create(mockJsonService.readJsonFile(id));
    }

    @PostMapping("/{id}")
    @ApiOperation(value = "保存页面配置")
    public Tip addFormJson(@PathVariable String id, @RequestBody JSONObject json) {
        Integer integer = mockJsonService.saveJsonToFile(json, id);
        return SuccessTip.create(integer);
    }

    @PostMapping("/{name}/{tag}")
    @ApiOperation(value = "增加Json 带tag")
    public Tip addPageJsonWithTag(@PathVariable String tag, @PathVariable String name, @RequestBody JSONObject json) {
        Integer integer = mockJsonService.saveJsonToFile(json, name, tag);
        return SuccessTip.create(integer);
    }

    @GetMapping("/forms")
    public Tip getAllPages(Page<FrontPageRecord> page,
                           @RequestParam(name = "pageNum", required = false, defaultValue = "1") Integer pageNum,
                           @RequestParam(name = "pageSize", required = false, defaultValue = "10") Integer pageSize,
                           @RequestParam(name = "tag", required = false) String tag,
                           @RequestParam(name = "search", required = false) String search,
                           @RequestParam(name = "pageId", required = false) String pageId,
                           @RequestParam(name = "title", required = false) String title,
                           @RequestParam(name = "notes", required = false) String notes,
                           @RequestParam(name = "content", required = false) String content,
                           @RequestParam(name = "jsonName", required = false) String jsonName,
                           @RequestParam(name = "createTime", required = false) String createTime,
                           @RequestParam(name = "updateTime", required = false) String updateTime,
                           @RequestParam(name = "orderBy", required = false) String orderBy,
                           @RequestParam(name = "sort", required = false) String sort) {

        // sanitize empty strings -> null
        tag = sanitize(tag);
        search = sanitize(search);
        pageId = sanitize(pageId);
        title = sanitize(title);
        notes = sanitize(notes);
        content = sanitize(content);
        jsonName = sanitize(jsonName);
        orderBy = sanitize(orderBy);
        sort = sanitize(sort);

        // orderBy / sort handling
        if (orderBy != null && orderBy.length() > 0) {
            if (sort != null && sort.length() > 0) {
                String sortPattern = "(ASC|DESC|asc|desc)";
                if (!sort.matches(sortPattern)) {
                    // skip strict check: default to ASC if invalid
                    sort = "ASC";
                }
            } else {
                sort = "ASC";
            }
            orderBy = "`" + orderBy + "` " + sort;
        } else {
            // keep null when orderBy is empty to skip ordering
            orderBy = null;
        }

        // Initialize page when null (HTTP binder may not bind MyBatis Page)
        if (page == null) {
            page = new Page<>();
        }
        page.setCurrent(pageNum);
        page.setSize(pageSize);

        // parse dates from string, empty -> null
        Date createTimeDate = parseDate(createTime);
        Date updateTimeDate = parseDate(updateTime);

        FrontPageRecord record = new FrontPageRecord();
        record.setPageId(pageId);
        record.setTitle(title);
        record.setnotes(notes);
        record.setContent(content);
        record.setJsonName(jsonName);
        record.setCreateTime(createTimeDate);
        record.setUpdateTime(updateTimeDate);

        List<FrontPageRecord> frontPagePage = queryFrontPageDao.findFrontPagePage(page, record, tag, search, orderBy, null, null);
        page.setRecords(frontPagePage);

        return SuccessTip.create(page);
    }

    private String sanitize(String s) {
        return (s == null || s.trim().isEmpty()) ? null : s.trim();
    }

    private Date parseDate(String s) {
        if (s == null || s.trim().isEmpty()) return null;
        try {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(s.trim());
        } catch (ParseException e) {
            // invalid format -> treat as null to be tolerant
            return null;
        }
    }
}
