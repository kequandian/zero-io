package com.jfeat.module.apis.services.domain.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jfeat.crud.plus.CRUD;
import com.jfeat.module.apis.services.domain.service.ParseSql;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ParseSqlImp implements ParseSql {

    protected final Log logger = LogFactory.getLog(getClass());

    @Resource
    private JdbcTemplate jdbcTemplate;

    @Value("${apis.dosql.path}")
    private String sqlFilePath; // sql文件路径

    private static final String sqlFileType = ".sql"; // 文件类型

    @Override
    public String readSqlFile(String fileName) {
        // String file = "devops/".concat(fileName).concat(".sql");
        // updated in 2024-12-23
        // 文件名处理
        if (fileName.contains(".")) {
            fileName = fileName.substring(0, fileName.lastIndexOf("."));
        }
            fileName += sqlFileType;
        // 读取文件
        Path filePath = Paths.get(sqlFilePath).resolve(fileName);
        try {
            logger.info("读取文件: " + filePath.toAbsolutePath());
            return new String(Files.readAllBytes(filePath));
        }catch (IOException e){
            logger.error(e);
            logger.error(filePath.toAbsolutePath().toString());
        }

        return null;
    }

    @Override
    public String sqlParameters(String sql, Map<String, String> parameter) {

//        遍历键值对 替换sql文件中的参数
        for(Map.Entry<String, String> entry : parameter.entrySet()){
            String mapKey = entry.getKey();
            String mapValue = entry.getValue();
//            匹配sql文件中的参数 进行全部替换
            String regex = "".concat("#\\{").concat(mapKey).concat("}");
            sql =  sql.replaceAll(regex,"".concat("'").concat(mapValue).concat("'"));
            // added in 2024-12-26 增加表名的替换，表名标识 ${}
            String tableRegex = "".concat("\\$\\{").concat(mapKey).concat("}");
            sql = sql.replaceAll(tableRegex, mapValue);
        }
        return sql;
    }

    @Override
    public int executeSql(String sql) {
        List<String> sqlList = loadSql(sql);
        int affect = 0;
        for (String s:sqlList){
            try {
                logger.info(s);
                affect+=jdbcTemplate.update(s);
            }catch (Exception e){
                logger.error(e);
                logger.error("sql:"+s);
            }
        }
        return affect;
    }



    @Override
    public JSONObject querySql(String sql) {
        JSONObject result = new JSONObject();

        List<String> sqlList = loadSql(sql);
        if (sqlList != null && sqlList.size() == 1) {
            String executeSql = sqlList.get(0);
            if (executeSql.toUpperCase().startsWith("SELECT") || executeSql.toUpperCase().startsWith("SHOW")) {
                try {
                    List<Map<String, Object>> list = jdbcTemplate.queryForList(executeSql);
                    // 将查询结果转换为jsonArray
                    // JSONObject jsonObject = new JSONObject();
                    result.put(CRUD.ITEMS, list);
                    // result = jsonObject.getJSONArray(CRUD.ITEMS);
                } catch (Exception e) {
                    logger.error("sql执行异常：" + executeSql);
                }
            }
        } else if (sqlList != null && sqlList.size() > 1) {
            // 分页识别：第一条为 COUNT(1) AS total，第二条为列表查询
            String first = sqlList.get(0).trim();
            String second = sqlList.get(1).trim();
            String firstUpper = first.toUpperCase();

            boolean firstIsCountTotal = firstUpper.startsWith("SELECT ") && firstUpper.contains("COUNT(") && firstUpper.contains("AS TOTAL");
            boolean secondIsQuery = second.toUpperCase().startsWith("SELECT") || second.toUpperCase().startsWith("SHOW");

            if (firstIsCountTotal && secondIsQuery) {
                // 识别为分页查询：返回 { total, records }
                Number totalNumber = 0;
                try {
                    List<Map<String, Object>> countRows = jdbcTemplate.queryForList(first);
                    if (countRows != null && !countRows.isEmpty()) {
                        Map<String, Object> row = countRows.get(0);
                        Object totalObj = row.get("total");
                        if (totalObj == null) totalObj = row.get("TOTAL");
                        if (totalObj instanceof Number) {
                            totalNumber = (Number) totalObj;
                        } else if (totalObj != null) {
                            try {
                                totalNumber = Long.parseLong(totalObj.toString());
                            } catch (Exception ignore) {
                                // 保持默认 0
                            }
                        }
                    }
                } catch (Exception e) {
                    logger.error("统计总数执行异常：" + first);
                }

                List<Map<String, Object>> records = new ArrayList<>();
                try {
                    records = jdbcTemplate.queryForList(second);
                } catch (Exception e) {
                    logger.error("列表查询执行异常：" + second);
                }

                result.put("total", totalNumber == null ? 0 : totalNumber);
                result.put("records", records);
                return result;
            }

            // 默认行为：多条查询返回二维数组（每条语句一个结果集）
            JSONArray arrays = new JSONArray();
            for (String s : sqlList) {
                logger.info(s);
                String up = s.trim().toUpperCase();
                if (up.startsWith("SELECT") || up.startsWith("SHOW")) {
                    try {
                        List<Map<String, Object>> list = jdbcTemplate.queryForList(s);
                        arrays.add(list);
                    } catch (Exception e) {
                        logger.error("sql执行异常：" + s);
                    }
                }
            }
            result.put(CRUD.ITEMS, arrays);
        }

        return result;
    }


    /**
     * 将sql语句 分成一条一条语句列表
     * @param sql
     * @return
     */
    public  List<String> loadSql(String sql) {
        List<String> sqlList = new ArrayList<String>();
        try {
            // Windows 下换行是 \r\n, Linux 下是 \n
            String[] sqlArr = sql.split("(;\\s*\\r\\n)|(;\\s*\\n)");
            for (int i = 0; i < sqlArr.length; i++) {
//                sql中的注释
                String sqlRow = sqlArr[i].replaceAll("--.*", "").trim();
                if (!sqlRow.equals("")) {
                    sqlList.add(sqlRow);
                }
            }
            return sqlList;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
