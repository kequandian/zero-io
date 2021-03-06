package com.jfeat.excel.services;

import com.jfeat.excel.model.ExportParam;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;

/**
 * Created on 2020/4/27.
 *
 * @author Wen Hao
 */
public interface ExcelExportService {

    /**
     * 导出Excel
     * @param exportParam
     * @return
     */
    ByteArrayInputStream export(String exportName) throws IOException;

    /**
     * 导出Excel, api 方式, 处理字节流数据
     * @param   - 导出参数
     * @return
     */
    ByteArrayInputStream exportByApi(String exportName, String api);

    /**
     * 导出 Excel, sql 方式, 处理字节流数据
     * @param exportName    - 导出名称
     * @param search        - 搜索字段
     * @return
     */
    ByteArrayInputStream exportBySql(String exportName, Map<String, String> search);
}
