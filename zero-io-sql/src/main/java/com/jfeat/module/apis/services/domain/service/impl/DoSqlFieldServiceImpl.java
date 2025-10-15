package com.jfeat.module.apis.services.domain.service.impl;

import com.jfeat.module.apis.services.domain.service.DoSqlFieldService;
import com.jfeat.module.apis.services.gen.crud.service.impl.CRUDDoSqlFieldServiceImpl;
import com.jfeat.module.apis.services.gen.persistence.dao.DoSqlFieldMapper;
import com.jfeat.module.apis.services.gen.persistence.model.DoSqlField;
import com.jfeat.crud.base.util.FileUtil;
import com.jfeat.crud.base.exception.BusinessException;
import com.jfeat.crud.base.exception.BusinessCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    @Value("${apis.dosql.path}")
    private String sqlFolderPath; // sql文件夹路径

    @Override
    protected String entityName() {
        return "DoSqlField";
    }

    @Resource
    private DoSqlFieldMapper doSqlFieldMapper; // dosql mapper

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
        String fileName = doSqlField.getApiName() + SQL_FILE_EXPAND_NAME;
        Path targetPath = Paths.get(sqlFolderPath, fileName);

        // 情况一：未提交 sql 内容
        if (sql == null || sql.isBlank()) {
            if (!java.nio.file.Files.exists(targetPath)) {
                throw new BusinessException(BusinessCode.BadRequest.getCode(), "新建api未提交sql，且文件不存在: " + targetPath);
            }
            // 读取已有文件以解析参数（记录 params，便于后续查询）
            String fileContent;
            try {
                fileContent = FileUtil.readString(targetPath);
            } catch (IOException e) {
                throw new RuntimeException("sql文件读取失败: " + targetPath, e);
            }
            String params = parseParams(fileContent);
            doSqlField.setParams(params);
            doSqlField.setSqlFilePath(targetPath.toString().replace('\\','/'));
            createMaster(doSqlField);
            return;
        }

        // 情况二：提交了 sql 内容
        validateSQL(sql); // 验证sql
        String params = parseParams(sql);
        doSqlField.setParams(params);
        try {
            FileUtil.writeStringToFile(Paths.get(sqlFolderPath), fileName, sql, true);
        } catch (IOException e) {
            throw new RuntimeException(String.format("sql文件写入失败: path: %s. fileName: %s, fileContent: %s", sqlFolderPath, fileName, sql), e);
        }
        doSqlField.setSqlFilePath(targetPath.toString().replace('\\','/'));
        createMaster(doSqlField);
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
        // 读取文件内容（直接使用存储的项目相对路径）
        if (doSqlField.getSqlFilePath() == null || doSqlField.getSqlFilePath().isBlank()) {
            return doSqlField;
        }
        Path path = Paths.get(doSqlField.getSqlFilePath());
        if (!java.nio.file.Files.exists(path)) {
            // 文件不存在，更新库字段为 null
            DoSqlField patch = new DoSqlField();
            patch.setId(id);
            patch.setSqlFilePath(null);
            patchMaster(patch);
            doSqlField.setSqlFilePath(null);
            doSqlField.setSql(null);
            return doSqlField;
        }
        String fileContent = null;
        try {
            fileContent = FileUtil.readString(path);
        } catch (IOException e) {
            throw new RuntimeException("文件内容读取失败: " + path, e);
        }
        doSqlField.setSql(fileContent);
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
                Path targetPath;
                if (oldDoSqlField.getSqlFilePath() != null && !oldDoSqlField.getSqlFilePath().isBlank()) {
                    targetPath = Paths.get(oldDoSqlField.getSqlFilePath());
                } else {
                    // 如果原路径为空，按规则生成路径并写入
                    String fileName = oldDoSqlField.getApiName() + SQL_FILE_EXPAND_NAME;
                    targetPath = Paths.get(sqlFolderPath, fileName);
                    // 同时更新库中的路径字段
                    doSqlField.setSqlFilePath(targetPath.toString().replace('\\','/'));
                }
                FileUtil.writeString(targetPath, newSql);
            } catch (IOException e) {
                throw new RuntimeException("文件内容更新失败");
            }
        }
        // 判断是否需要更新名称
        String oldapiName = oldDoSqlField.getApiName();
        String newapiName = doSqlField.getApiName();
        if (!oldapiName.equals(newapiName)) {
            Path oldPath = oldDoSqlField.getSqlFilePath() != null && !oldDoSqlField.getSqlFilePath().isBlank()
                    ? Paths.get(oldDoSqlField.getSqlFilePath())
                    : Paths.get(sqlFolderPath, oldDoSqlField.getApiName() + SQL_FILE_EXPAND_NAME); // 旧文件
            Path newPath = Paths.get(sqlFolderPath, doSqlField.getApiName() + SQL_FILE_EXPAND_NAME); // 新文件
            try {
                FileUtil.rename(oldPath, newPath);
            } catch (IOException e) {
                throw new RuntimeException("文件名修改失败：旧文件名：" + oldPath + " 新文件名：" + newPath, e);
            }
            doSqlField.setSqlFilePath(newPath.toString().replace('\\','/'));
        }
        // 更新记录
        return patchMaster(doSqlField);
    }

    /**
     * 遍历 apis.dosql.path 下所有 .sql 文件，初始化/更新数据库记录
     */
    @Override
    public Map<String, Object> initializeFromSqlFiles() {
        Path basePath = Paths.get(sqlFolderPath);
        if (!Files.exists(basePath) || !Files.isDirectory(basePath)) {
            throw new BusinessException(BusinessCode.BadRequest.getCode(), "SQL 目录不存在或不可访问: " + basePath);
        }

        int scanned = 0;
        int created = 0;
        int updated = 0; // 当前策略不做更新，仅计数保留为0
        int skipped = 0;
        List<String> errors = new ArrayList<>();

        try {
            for (Path p : (Iterable<Path>) Files.walk(basePath)::iterator) {
                if (!Files.isRegularFile(p) || !p.toString().toLowerCase().endsWith(SQL_FILE_EXPAND_NAME)) {
                    continue;
                }
                scanned++;
                String normalizedPath = p.toString().replace('\\','/');
                String folderNorm = Paths.get(sqlFolderPath).toString().replace('\\','/');
                int idx = normalizedPath.indexOf(folderNorm);
                String relativeProjectPath = idx >= 0 ? normalizedPath.substring(idx) : normalizedPath;

                String fileName = p.getFileName().toString();
                String apiName = fileName.endsWith(SQL_FILE_EXPAND_NAME)
                        ? fileName.substring(0, fileName.length() - SQL_FILE_EXPAND_NAME.length())
                        : fileName;

                String fileContent;
                try {
                    fileContent = FileUtil.readString(p);
                } catch (IOException e) {
                    errors.add("读取失败:" + normalizedPath + ", " + e.getMessage());
                    continue;
                }

                String params = parseParams(fileContent);

                // 查找是否已有记录（按 api_name 匹配）；存在则跳过
                com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<DoSqlField> qw = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
                qw.eq("api_name", apiName).last("limit 1");
                List<DoSqlField> exists = doSqlFieldMapper.selectList(qw);
                if (exists != null && !exists.isEmpty()) {
                    // 已存在，跳过，不更新
                    skipped++;
                } else {
                    DoSqlField record = new DoSqlField();
                    record.setApiName(apiName);
                    record.setSqlFilePath(relativeProjectPath);
                    record.setParams(params);
                    createMaster(record);
                    created++;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("遍历 SQL 目录失败: " + basePath, e);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("scanned", scanned);
        result.put("created", created);
        result.put("updated", updated);
        result.put("skipped", skipped);
        result.put("errors", errors);
        return result;
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
