package com.jfeat.module.apis.services.domain.service.impl;

import com.jfeat.module.apis.services.domain.service.DoSqlFieldService;
import com.jfeat.module.apis.services.gen.crud.service.impl.CRUDDoSqlFieldServiceImpl;
import com.jfeat.module.apis.services.gen.persistence.dao.DoSqlFieldMapper;
import com.jfeat.module.apis.services.gen.persistence.model.DoSqlField;
import com.jfeat.crud.base.util.FileUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author admin
 * @since 2017-10-16
 */

@Service("DoSqlFieldService")
public class DoSqlFieldServiceImpl extends CRUDDoSqlFieldServiceImpl implements DoSqlFieldService {
    @Value("${lowcode.dosql.path}")
    private String sqlFolderPath; // sql文件夹路径

    @Override
    protected String entityName() {
        return "DoSqlField";
    }

    @Resource
    private DoSqlFieldMapper doSqlFieldMapper; // dosql mapper

    private static final String EXECUTE_API_PREFIX_PATH = "/api/lc/apis"; // 执行api的前缀路径
    private static final String SELECT = "SELECT";
    private static final String INSERT = "INSERT";
    private static final String SHOW = "SHOW";
    private static final String SQL_FILE_EXPAND_NAME = ".sql"; // sql文件拓展名
    private static final String SQL_PARAM_REGEX = "#\\{(.*?)}|\\$\\{(.*?)}"; // 匹配sql中参数的正则表达式 #{} ${}这是参数的标识
    private static final String PARAM_SEPARATOR = ","; // 参数记录分隔符


    /**
     * 创建
     *
     * @param doSqlField
     */
    @Override
    public void saveAndWriteFIle(DoSqlField doSqlField) {
        String sql = doSqlField.getSql();
        validateSQL(sql); // 验证sql
        // 解析参数
        String params = parseParams(sql);
        doSqlField.setParams(params);
        // 写入文件
        String fileName = doSqlField.getFieldName() + SQL_FILE_EXPAND_NAME;
        try {
            FileUtil.writeStringToFile(Paths.get(sqlFolderPath), fileName, doSqlField.getSql(), true);
        } catch (IOException e) {
            throw new RuntimeException(String.format("sql文件写入失败: path: %s. fileName: %s, fileContent: %s", sqlFolderPath, fileName, doSqlField.getSql()), e);
        }
        // 文件写入成功，记录入库
        doSqlField.setSqlFileName(fileName);
        createMaster(doSqlField);
    }

    /**
     * 计算apiUrl
     *
     * @param doSqlFields
     */
    @Override
    public void calculateApiUrl(List<DoSqlField> doSqlFields) {
        if (doSqlFields == null || doSqlFields.isEmpty()) {
            return;
        }
        for (DoSqlField doSqlField : doSqlFields) {
            String baseName = doSqlField.getSqlFileName();
            if (baseName != null && !baseName.isBlank()) {
                doSqlField.setApiUrl(calculateApiUrl(baseName));
            }
        }
    }

    /**
     * 计算apiUrl
     * @param fileName
     * @return
     */
    private String calculateApiUrl(String fileName) {
        // 大于1，避免开头就是.导致的截取了空串
        if (fileName.lastIndexOf(".") > 1) {
            fileName = fileName.substring(0, fileName.lastIndexOf("."));
        }
        return EXECUTE_API_PREFIX_PATH + "/" + fileName;
    }

    /**
     * 获取详情，包活sql文件内容
     *
     * @param id
     * @return
     */
    @Override
    public DoSqlField getDetail(Long id) {
        DoSqlField doSqlField = doSqlFieldMapper.selectById(id);
        if (doSqlField == null) {
            return null;
        }
        // 读取文件内容
        Path path = Paths.get(sqlFolderPath, doSqlField.getSqlFileName());
        String fileContent = null;
        try {
            fileContent = FileUtil.readString(path);
        } catch (IOException e) {
            throw new RuntimeException("文件内容读取失败: " + path, e);
        }
        doSqlField.setSql(fileContent);
        // 计算apiUrl
        doSqlField.setApiUrl(calculateApiUrl(doSqlField.getSqlFileName()));
        return doSqlField;
    }

    /**
     * 更新，包含sql文件内容
     *
     * @param doSqlField
     * @return
     */
    @Override
    public Integer updateIncludeSqlFIle(DoSqlField doSqlField) {
        if (doSqlField == null || doSqlField.getId() == null) {
            return 0;
        }
        DoSqlField oldDoSqlField = doSqlFieldMapper.selectById(doSqlField.getId());
        if (oldDoSqlField == null) {
            throw new RuntimeException("没有找到id: " + doSqlField.getId() + "的sql记录");
        }
        String newSql = doSqlField.getSql();
        if (newSql != null && !newSql.isBlank()) {
            validateSQL(newSql);
            String params = parseParams(newSql);
            doSqlField.setParams(params);
            // 更新sql文件内容,不读取参数中的实体，以查询出来的为主，避免参数中的被修改
            try {
                FileUtil.writeString(Paths.get(sqlFolderPath, oldDoSqlField.getSqlFileName()), newSql);
            } catch (IOException e) {
                throw new RuntimeException("文件内容更新失败");
            }
        }
        // 判断是否需要更新名称
        String oldFieldName = oldDoSqlField.getFieldName();
        String newFieldName = doSqlField.getFieldName();
        if (!oldFieldName.equals(newFieldName)) {
            Path oldPath = Paths.get(sqlFolderPath, oldDoSqlField.getFieldName() + SQL_FILE_EXPAND_NAME); // 旧文件
            Path newPath = Paths.get(sqlFolderPath, doSqlField.getFieldName() + SQL_FILE_EXPAND_NAME); // 新文件
            try {
                FileUtil.rename(oldPath, newPath);
            } catch (IOException e) {
                throw new RuntimeException("文件名修改失败：旧文件名：" + oldPath + " 新文件名：" + newPath, e);
            }
            doSqlField.setSqlFileName(doSqlField.getFieldName() + SQL_FILE_EXPAND_NAME);
        }
        // 更新记录
        return patchMaster(doSqlField);
    }

    /**
     * 验证字符串是否是有效的 SQL 语句
     *
     * @param sql 字符串内容
     * @throws IllegalArgumentException 如果不是有效的 SQL 语句抛出异常
     */
    private void validateSQL(String sql) throws IllegalArgumentException {
        if (sql == null || sql.isBlank()) {
            throw new IllegalArgumentException("SQL 语句不能为空");
        }
        // 简单验证SQL语句开头是否是常见的合法关键字
        String lowerSql = sql.trim().toLowerCase();
        if (!(lowerSql.toUpperCase().startsWith(SELECT) ||
                lowerSql.toUpperCase().startsWith(INSERT) ||
                lowerSql.toUpperCase().startsWith(SHOW))) {
            throw new IllegalArgumentException("无效的SQL语句");
        }
    }

    /**
     * 解析参数，sql中支持定义参数，同一格式通过#{}定义
     *
     * @param sql
     * @return
     */
    private String parseParams(String sql) {
        // 使用正则表达式匹配 #{} 中的内容
        Pattern pattern = Pattern.compile(SQL_PARAM_REGEX);
        Matcher matcher = pattern.matcher(sql);
        List<String> parameters = new ArrayList<>();
        // 循环遍历所有匹配到的内容
        while (matcher.find()) {
            parameters.add(matcher.group(1));  // group(1) 获取第一个捕获组的内容
        }
        // 指定分隔符将数组转为一个String对象
        return String.join(PARAM_SEPARATOR, parameters);
    }
}
