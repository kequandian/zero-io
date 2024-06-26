package com.jfeat.excel.properties;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Created on 2020/7/20.
 *
 * @author Wen Hao
 */
@Configuration
@ConfigurationProperties(prefix = "io")
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ExcelProperties {

    Boolean https = false;

    /**
     * 模版文件目录
     */
    String excelTemplateDir;

    /**
     * 导出最大长度
     */
    private Integer excelExportMaxRows;

    /**
     * excel 导出
     */
    // Map<String, ExcelPojo> exportMap = new HashMap<>();

    /**
     * excel 导入
     */
    // Map<String, ExcelPojo> importMap = new HashMap<>();

    /*@Data
    @Component
    public static class ExcelPojo {

        private String api;

        private String templateName;
    }*/
}
