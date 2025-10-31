package com.jfeat.module.frontPage.api.app;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jfeat.crud.base.exception.BusinessCode;
import com.jfeat.crud.base.exception.BusinessException;
import com.jfeat.crud.base.tips.SuccessTip;
import com.jfeat.crud.base.tips.Tip;
import com.jfeat.module.frontPage.services.domain.dao.QueryFrontPageModuleInfoDao;
import com.jfeat.module.frontPage.services.domain.model.FrontPageModuleInfoRecord;
import com.jfeat.module.frontPage.services.domain.service.FrontPageModuleInfoService;
import com.jfeat.module.frontPage.services.gen.crud.model.FrontPageModuleInfoModel;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 * api
 * </p>
 *
 * @author Code generator
 * @since 2022-09-28
 */
/**
 * 页面模块接口（AppFrontPageModuleInfoEndpoint）。
 * 提供：
 * - 页面模块信息的分页查询；
 * - 获取某类型页面模块列表；
 * - 将页面模块定义/配置与数据库记录进行同步；
 * - 批量刷新所有模块类型字段。
 *
 * 查询与同步说明：
 * - 查询接口默认会在查询前执行一次同步（可通过 {@code synchronization=false} 关闭），
 *   同步会调用 {@link FrontPageModuleInfoService#updatePageModule(String)}，
 *   将指定模块类型的页面模块定义（如配置或代码内定义）与数据库进行比对并更新，确保查询结果最新。
 */
@RestController

@Api("页面模块")
@RequestMapping("/api/frontPage/moduleInfo")
public class AppFrontPageModuleInfoEndpoint {

    @Resource
    FrontPageModuleInfoService frontPageModuleInfoService;


    @Resource
    QueryFrontPageModuleInfoDao queryFrontPageModuleInfoDao;



    /**
     * 分页查询页面模块信息。
     *
     * 支持筛选字段：{@code id}, {@code pageId}, {@code itemModuleName}, {@code frontPageId}, {@code title}, {@code moduleType}；
     * 支持模糊搜索参数：{@code search}；
     * 支持排序：{@code orderBy} + {@code sort}（仅允许 ASC/DESC，不区分大小写）。
     *
     * 同步行为：
     * - 默认会在查询前执行一次 {@link FrontPageModuleInfoService#updatePageModule(String)} 以保证数据最新；
     * - 传入 {@code synchronization=false} 可跳过同步。
     *
     * @param page 分页对象（由框架注入）
     * @param pageNum 页码，默认 1
     * @param pageSize 每页数量，默认 10
     * @param search 模糊搜索关键字
     * @param id 精确匹配记录主键
     * @param pageId 页面 ID（关联页面）
     * @param itemModuleName 模块项名称（用于筛选）
     * @param moduleType 模块类型（用于筛选与同步范围）
     * @param title 模块标题（用于筛选）
     * @param frontPageId 前端页面 ID（用于筛选）
     * @param orderBy 排序字段名（会被包装为 `字段` 并与 sort 组合）
     * @param sort 排序方向（ASC/DESC，不区分大小写）
     * @param synchronization 是否在查询前自动同步（默认 true）
     * @return 分页数据，记录类型为 {@link FrontPageModuleInfoRecord}
     */
    @ApiOperation(value = "FrontPageModuleInfo 列表信息", response = FrontPageModuleInfoRecord.class)
    @GetMapping
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pageNum", dataType = "Integer"),
            @ApiImplicitParam(name = "pageSize", dataType = "Integer"),
            @ApiImplicitParam(name = "search", dataType = "String"),
            @ApiImplicitParam(name = "id", dataType = "Long"),
            @ApiImplicitParam(name = "pageId", dataType = "Long"),
            @ApiImplicitParam(name = "moduleName", dataType = "String"),
            @ApiImplicitParam(name = "moduleJson", dataType = "String"),
            @ApiImplicitParam(name = "frontPageId", dataType = "Long"),
            @ApiImplicitParam(name = "orderBy", dataType = "String"),
            @ApiImplicitParam(name = "sort", dataType = "String")
    })
    public Tip queryFrontPageModuleInfos(Page<FrontPageModuleInfoRecord> page,
                                         @RequestParam(name = "pageNum", required = false, defaultValue = "1") Integer pageNum,
                                         @RequestParam(name = "pageSize", required = false, defaultValue = "10") Integer pageSize,
                                         @RequestParam(name = "search", required = false) String search,
                                         @RequestParam(name = "id", required = false) Long id,

                                         @RequestParam(name = "pageId", required = false) Long pageId,

                                         @RequestParam(name = "itemModuleName", required = false) String itemModuleName,
                                         @RequestParam(name = "moduleType", required = false) String moduleType,
                                         @RequestParam(name = "title", required = false) String title,
                                         @RequestParam(name = "frontPageId", required = false) Long frontPageId,
                                         @RequestParam(name = "orderBy", required = false) String orderBy,
                                         @RequestParam(name = "sort", required = false) String sort,
                                         @RequestParam(name = "synchronization" ,defaultValue = "true")Boolean synchronization) {

        if (orderBy != null && orderBy.length() > 0) {
            if (sort != null && sort.length() > 0) {
                String pattern = "(ASC|DESC|asc|desc)";
                if (!sort.matches(pattern)) {
                    throw new BusinessException(BusinessCode.BadRequest.getCode(), "sort must be ASC or DESC");//此处异常类型根据实际情况而定
                }
            } else {
                sort = "ASC";
            }
            orderBy = "`" + orderBy + "`" + " " + sort;
        }
        page.setCurrent(pageNum);
        page.setSize(pageSize);

        FrontPageModuleInfoRecord record = new FrontPageModuleInfoRecord();
        record.setId(id);
        record.setPageId(pageId);
        record.setItemModuleName(itemModuleName);

        record.setFrontPageId(frontPageId);
        record.setTitle(title);
        record.setModuleType(moduleType);

        if(synchronization){
            Integer integer = frontPageModuleInfoService.updatePageModule(moduleType);
        }

        List<FrontPageModuleInfoRecord> frontPageModuleInfoPage = queryFrontPageModuleInfoDao.findFrontPageModuleInfoPage(page, record, search, orderBy, null, null);

        page.setRecords(frontPageModuleInfoPage);

        return SuccessTip.create(page);
    }


    /**
     * 获取某模块类型的页面模块列表（非分页）。
     *
     * 数据来源：
     * - 调用 {@link FrontPageModuleInfoService#getPageModules(String)}，返回指定 {@code moduleType} 下的页面模块定义/配置模型。
     *
     * @param moduleType 模块类型（可选；为空时返回全部或由实现决定范围）
     * @return 指定类型的 {@link FrontPageModuleInfoModel} 列表
     */
    @GetMapping("/pageList")
    public Tip getPageModule(@RequestParam(name = "moduleType", required = false) String moduleType){
        List<FrontPageModuleInfoModel> pageModules = frontPageModuleInfoService.getPageModules(moduleType);
        return SuccessTip.create(pageModules);
    }

    /**
     * 手动同步页面模块数据。
     *
     * 行为：
     * - 调用 {@link FrontPageModuleInfoService#updatePageModule(String)}，
     *   将指定 {@code moduleType} 的页面模块定义与数据库记录进行对齐更新。
     *
     * @param moduleType 模块类型（可选；为空时由实现决定同步范围）
     * @return 受影响（新增/更新）记录数
     */
    @PutMapping("/synchronizationData")
    public Tip synchronizationData(@RequestParam(name = "moduleType", required = false) String moduleType){
        Integer integer = frontPageModuleInfoService.updatePageModule(moduleType);
        return SuccessTip.create(integer);
    }

    /**
     * 批量刷新所有模块的类型字段。
     *
     * 行为：
     * - 调用 {@link FrontPageModuleInfoService#batchUpdateAllType()}，
     *   对现有记录进行类型字段的批量修正或重建。
     *
     * @return 受影响记录数
     */
    @PutMapping("/updateAllType")
    public Tip updateAllType(){
        Integer integer = frontPageModuleInfoService.batchUpdateAllType();
        return SuccessTip.create(integer);
    }


}
