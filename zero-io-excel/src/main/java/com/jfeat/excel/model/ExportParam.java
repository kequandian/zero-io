package com.jfeat.excel.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import jakarta.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;

/**
 * Created on 2020/7/20.
 *
 * @author Wen Hao
 */
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ExportParam {

    // @NotNull
    //@Schema(description = "导出名称")
    //String exportName;

//    @Schema(description = "导出数据来源(api url)")
//    String api;

    //@Schema(description = "导出类型, SQL=数据库方式, API=api方式")
    //String type;

//    @Schema(description = "查询参数")
//    Map<String, String> search;

    //@Schema(description = "转换字典")
    //Map<String, Map<String, String>> dict = new HashMap<>();
}
