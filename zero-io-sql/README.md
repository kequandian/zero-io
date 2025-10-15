# zero-io-sql

基于 SQL 文件快速定义 API 的模块：在后台维护 SQL 与元数据，在使用端按 URL 执行/查询 SQL。

**核心目录**
- `apis/`：SQL 文件目录（示例已提供 `testget.sql`、`testpost.sql`、`fields.sql`、`tables.sql`）。
- `test/dosql-test.http`：HTTP 测试用例。

**必要配置**
- `apis.dosql.path`：SQL 文件所在路径（支持绝对或相对路径）。示例：`apis.dosql.path=apis`。

**SQL 约定**
- 参数占位：
  - `#{param}` → 用请求参数值替换，并自动加引号（`'value'`）。
  - `${param}` → 原样替换（不加引号），适用于表名等标识符。
- 多语句：以分号加换行分隔（`;\r\n` 或 `;\n`），会按条执行；`--` 开头注释会被移除。
- 查询与执行：`SELECT`/`SHOW` 作为查询；其他语句按更新执行。

**使用端接口（执行 SQL）**
- `GET /api/apis/{sqlFile}`：查询 SQL，返回查询结果。
  - `sqlFile` 为文件名（可带或不带 `.sql` 扩展名）。
  - 请求参数通过查询串或表单提交传入，用于替换 SQL 中的占位符。
  - 返回：
    - 单条查询语句 → 直接返回结果数组（每个元素为一行记录的键值对）。
    - 多条查询语句 → 返回二维数组，每一项是一个查询结果集。
- `POST /api/apis/{sqlFile}`：执行 SQL（INSERT/UPDATE/DELETE 等），返回影响行数。
- 示例：
  - `GET /api/apis/testget?name1=foo&name2=bar`
  - `GET /api/apis/fields?tableName=lc_do_sql`（`fields.sql` 示例，使用 `${tableName}` 占位符）。

**后台配置接口（管理 SQL 与元数据）**

1) `/api/adm/apis`（完整管理：读写 SQL 文件 + 元数据）
- `POST /api/adm/apis`：创建 API。校验 SQL、解析参数、在 `${apis.dosql.path}/${apiName}.sql` 写入文件，并将完整项目相对路径保存到 `sqlFilePath` 字段，入库一条 `DoSqlField` 记录。
- `GET /api/adm/apis`：分页查询元数据。调用地址可由 `sqlFilePath` 推导为 `/api/apis/{fileName}`（作为提示，执行端仍按文件名调用）。
- `GET /api/adm/apis/{id}`：查看单条元数据。
- `PUT /api/adm/apis/{id}`：更新 API，同步修改 SQL 文件内容与库记录。
- `DELETE /api/adm/apis/{id}`：删除定义。
- `GET /api/adm/apis/{id}/detail`：查看详情（包含 SQL 文件内容，不返回 `apiUrl`）。


**两类后台接口的区别**
- `/api/adm/apis`：既管理库表记录，又直接读写 SQL 文件（不返回 `apiUrl`）。
（已合并）管理功能统一由 `/api/adm/apis` 提供，`/api/adm/apis/fields` 已移除。

**测试用例**
- 使用 `test/dosql-test.http` 可直接发起 GET/POST 测试请求。
- 包含对使用端接口与后台接口的示例调用。

**注意事项**
- 通过 `/api/adm/apis` 创建时，默认路径为 `${apis.dosql.path}/${apiName}.sql`，并保存至 `sqlFilePath`（项目相对路径）。
- `/api/adm/apis/{id}/detail` 读取 SQL 时直接按 `sqlFilePath` 路径访问；若文件缺失会将库内 `sqlFilePath` 置为 `null`。
- 使用 `${param}` 替换标识符时请注意安全（避免将不可信输入用于表名、列名）。
- SQL 文件路径由 `apis.dosql.path` 控制，确保可读写权限与路径正确。
