# zero-io-excel

> 基于 EasyPOI 的 Excel 导入导出解决方案

## 简介

本模块提供了灵活的 Excel 导入导出功能，支持以下特性：

- **导出方式**：支持 API 方式和 SQL 方式两种数据导出
- **字段转换**：支持通过字典配置进行字段值转换
- **模板导出**：基于 EasyPOI 模板进行自定义格式导出
- **批量导入**：支持单表和多表关联导入，支持重复数据处理策略

## 技术栈

- [EasyPOI](https://gitee.com/lemur/easypoi) - Excel 处理核心框架
- Spring Boot 3.x
- Java 17

---

## 配置说明

### 1. 基础配置

在 `application.yml` 中配置模板文件目录：

```yaml
io:
  # Excel 模板文件目录（相对于 classpath 或绝对路径）
  excel-template-dir: "excel-templates"
  # 导出最大行数限制（可选）
  excel-export-max-rows: 10000
  # 是否使用 HTTPS（可选）
  https: false
```

### 2. 模板文件目录结构

```
excel-templates/
├── export/
│   ├── template1.xlsx          # API 方式导出模板
│   ├── template1.json          # 字段转换字典
│   └── template2.sql           # SQL 方式导出脚本
└── import/
    ├── import1.json            # 导入配置文件
    └── import2.json
```

---

## 导出功能

### API 接口

```
POST /api/io/excel/export/{exportName}
```

**路径参数：**

| 参数 | 说明 |
|------|------|
| exportName | 导出配置名称，对应模板文件名（不含扩展名） |

**查询参数：**

| 参数 | 说明 | 必填 |
|------|------|------|
| filename | 下载文件名 | 否 |

### 导出方式

#### 方式一：API 导出

通过调用内部 API 获取数据并导出。

**请求体参数：**

| 参数 | 类型 | 说明 |
|------|------|------|
| type | String | 固定值：`"API"` |
| api | String | 获取数据的 API 路径 |
| search | Object | 传递给目标 API 的查询参数 |

**请求示例：**

```json
{
  "type": "API",
  "api": "/api/adm/equipment/equipments",
  "search": {
    "categoryId": "",
    "activeKey": "list",
    "pageSize": 99
  }
}
```

**所需模板文件：**

1. **Excel 模板** (`{exportName}.xlsx`)：定义导出格式

基于 EasyPOI 模板语法，使用 `{{$fe: list}}` 遍历数据：

| 编号 | 名称 | 说明 |
|------|------|------|
| {{$fe: list t.id \| t.name \| t.note}} | 数据行示例 | `t` 代表数据对象，`list` 为数据集合 |

2. **字典配置** (`{exportName}.json`)：定义字段值转换规则

```json
{
  "status": {
    "IN_USE": "使用中",
    "STAND_BY": "待用"
  },
  "changeStatus": {
    "": "无",
    "SCRAPPED": "报废",
    "SEALED": "封存",
    "DISABLED": "停用"
  },
  "stockStatus": {
    "TO_STOCKTAKE": "待盘点",
    "TO_STOCK_ADJUST": "待调整"
  }
}
```

#### 方式二：SQL 导出

直接执行 SQL 查询并导出结果。

**请求体参数：**

| 参数 | 类型 | 说明 |
|------|------|------|
| type | String | 固定值：`"SQL"` |
| search | Object | SQL 模板变量替换值 |

**请求示例：**

```json
{
  "type": "SQL",
  "search": {
    "status": "IN_USE"
  }
}
```

**SQL 模板文件** (`{exportName}.sql`)：

- 使用 `#{variable}` 语法定义可替换变量
- 使用 `--` 注释动态条件，当变量有值时自动取消注释

```sql
SELECT
    (@i:=@i+1) AS "序号",
    id AS '编号',
    CASE status
        WHEN 'IN_USE' THEN '使用中'
        WHEN 'STAND_BY' THEN '待机中'
    END AS '状态'
FROM equipment, (SELECT @i:=0) t
WHERE 1=1
--AND status = '#{status}'
```

---

## 导入功能

### API 接口

```
POST /api/io/excel/import/{importName}
```

**路径参数：**

| 参数 | 说明 |
|------|------|
| importName | 导入配置名称，对应 JSON 配置文件名（不含扩展名） |

**请求体：**

| 参数 | 类型 | 说明 |
|------|------|------|
| multipartFile | File | 要导入的 Excel 文件 |

### 导入配置说明

配置文件位于 `{excel-template-dir}/import/{importName}.json`

#### 配置结构

```json
{
  "level": 1,
  "header": 1,
  "duplicate": 0,
  "overwrite": 1,
  "relationOnly": 0,
  "target": [...],
  "unique": [...],
  "convert": [...]
}
```

#### 配置参数详解

| 参数 | 类型 | 说明 |
|------|------|------|
| level | Integer | 导入级别：1=单表，2=多表关联 |
| header | Integer | Excel 是否包含表头：1=是，0=否 |
| duplicate | Integer | 是否允许完全重复的数据：1=允许，0=不允许 |
| overwrite | Integer | 重复数据是否更新：1=更新，0=跳过 |
| relationOnly | Integer | 是否仅建立关系（多表场景）：1=是，0=否 |

#### target - 导入目标配置

定义数据导入的目标表和字段映射。

```json
"target": [
  {
    "table": "equipment",
    "fields": [
      "projectCode",
      "projectName",
      "code",
      "name",
      "status"
    ],
    "valueConverterMap": {
      "status": {
        "IN_USE": "在用",
        "STAND_BY": "已下发",
        "LOST": "丢失"
      }
    },
    "values": []
  }
]
```

| 字段 | 说明 |
|------|------|
| table | 目标数据库表名 |
| fields | Excel 列对应的数据库字段名（按顺序） |
| valueConverterMap | 字段值转换映射（数据库值 → Excel显示值） |
| values | 固定值（可选） |

#### unique - 唯一性约束配置

定义用于判断重复数据的唯一字段。

```json
"unique": [
  {
    "fields": ["code"],
    "table": "equipment",
    "option": {
      "type": "UPDATE"
    }
  }
]
```

| 字段 | 说明 |
|------|------|
| fields | 唯一字段列表，多个字段组合判断唯一性 |
| table | 所属表名 |
| option.type | 处理方式：`UPDATE`=更新，`POSTFIX`=添加后缀新增 |
| option.postfix | 后缀格式（POSTFIX 模式）：`%%{1...}` 表示递增数字 |

**POSTFIX 模式示例：**

```json
"unique": [{
  "fields": ["code"],
  "table": "equipment",
  "option": {
    "type": "POSTFIX",
    "postfix": "%%{1...}"
  }
}]
```

当发现重复时，自动在 `code` 后添加 `%%1`、`%%2` 等后缀。

#### convert - 导入前数据转换

在数据入库前进行字段值转换。

```json
"convert": [
  {
    "field": "status",
    "oldValue": "使用中",
    "newValue": "IN_USE",
    "table": "equipment"
  },
  {
    "field": "status",
    "oldValue": "待用",
    "newValue": "STAND_BY",
    "table": "equipment"
  }
]
```

### 完整配置示例

```json
{
  "level": 1,
  "header": 1,
  "duplicate": 0,
  "overwrite": 1,
  "relationOnly": 0,
  "target": [
    {
      "fields": [
        "projectCode",
        "projectName",
        "code",
        "batchNumber",
        "name",
        "categoryName",
        "installationSite",
        "warehouseName",
        "status",
        "changeStatus",
        "stockStatus",
        "factory",
        "system",
        "spec",
        "material",
        "machineCode",
        "brand",
        "supplier",
        "startTime",
        "produceTime",
        "serviceLife",
        "note"
      ],
      "table": "equipment",
      "valueConverterMap": {
        "status": {
          "IN_USE": "在用",
          "STAND_BY": "已下发",
          "LOST": "丢失"
        },
        "change_status": {
          "SCRAPPED": "报废",
          "SEALED": "封存",
          "DISABLED": "停用"
        },
        "stock_status": {
          "TO_STOCKTAKE": "待盘点",
          "TO_STOCK_ADJUST": "待调整"
        }
      },
      "values": []
    }
  ],
  "unique": [
    {
      "fields": ["code"],
      "table": "equipment",
      "valueConverterMap": {},
      "option": {
        "type": "UPDATE"
      },
      "values": []
    }
  ],
  "convert": [
    {
      "field": "status",
      "newValue": "IN_USE",
      "oldValue": "使用中",
      "table": "equipment"
    },
    {
      "field": "status",
      "newValue": "STAND_BY",
      "oldValue": "待用",
      "table": "equipment"
    }
  ]
}
```

---

## 常见问题

### 1. API 导出时出现 "No route to host" 错误

可能是防火墙阻止了内部 API 调用，检查防火墙设置：

```bash
# 查看防火墙状态
firewall-cmd --state
systemctl status firewalld

# 临时关闭防火墙（测试用）
systemctl stop firewalld
```

### 2. 导入时字段不匹配

确保：
- Excel 表头顺序与 `fields` 配置顺序一致
- Excel 列名与数据库字段名映射正确
- 使用 `convert` 配置处理值转换

### 3. 模板文件找不到

检查：
- `excel-template-dir` 配置路径是否正确
- 模板文件名是否与 `exportName`/`importName` 一致
- 文件是否在 classpath 中（resources 目录下）

---

## 版本要求

- Java 17+
- Spring Boot 3.x
- Maven 3.6+
