### 测试用例

1. 创建api, 提交 api名称，sql语句, sql 内容保存到 apis.dosql.path 目录下，同时在数据库中创建一条记录，sql保存后，初始化路径字段
2. 调用api, 提交参数, 从数据库中查询sql, 执行sql, 返回结果
3. 查询所有 apis分页列表, 但无需提供 sql 内容
4. 查询单个 api 详情, 需提供 api 名称, 返回 api 名称, sql 内容, 路径等信息
5. 提供初始化接口，检查 apis.dosql.path 目录下是否存在 sql 文件，历遍所有文件，创建对应的 DoSqlField 记录，初始化 sqlFilePath 字段
6. 新建api时，如果没有提交sql内容，即检查 apis.dosql.path 目录下是否存在对应的 sql 文件，如果不存在，即抛出业务异常，如果存在，即更新 DoSqlField 记录的 sqlFilePath 字段；如果有提交sql内容，即直接全量替换至apis.dosql.path目录下的对应的 sql 文件中。

---

### SQL 与 params 配置使用说明

**概述**
- 模块通过 SQL 文件定义接口。在后台管理端维护 SQL 与元数据，使用端按 URL 执行/查询 SQL。
- 元数据表：`lc.t_apis_dosql`，字段含义见下文“数据库字段说明”。

**配置项**
- `apis.dosql.path`：后台管理服务读写 SQL 文件的目录（生成并保存 `sqlFilePath` 的相对路径基准）。
- `apis.dosql.path`：使用端读取 SQL 文件的目录（`/api/apis/{sqlFile}` 调用时使用）。
- 建议两者指向同一目录以避免路径不一致：例如都配置为 `apis`。

**数据库字段说明（t_apis_dosql）**
- `id`：主键。
- `appid`：区分不同应用（可选使用）。
- `api_name`：接口服务名称（与文件名相关）。
- `sql_file_path`：SQL 文件的“项目相对路径”，例如 `apis/testget.sql`。
- `params`：记录 SQL 中使用的参数占位符列表（逗号分隔）。
- `note`：备注。

**SQL 参数占位符约定**
- 值替换：`#{param}` → 用请求参数值替换，并自动加引号（例如 `'value'`）。
- 原样替换：`${param}` → 不加引号，适用于表名、列名等标识符。
- 多语句：以分号加换行分隔（`;
\n` 或 `;\n`）；`--` 开头的行会被移除。

**params 字段的生成与用途**
- 生成时机：
  - 创建 API 时，解析 SQL 内容中的占位符，保存到 `params`（逗号分隔）。
  - 更新 API 且传入新的 SQL 内容时，重新解析并更新 `params`。
- 当前解析实现：
  - 记录 `#{...}` 的参数名；`${...}` 虽参与替换但默认不记录到 `params`。
  - 解析位置：`DoSqlFieldServiceImpl.parseParams(sql)`。
- 用途：
  - 后台分页查询支持按 `params` 模糊匹配（`LIKE`），便于检索使用了某参数的 API。
  - 执行端实际替换基于请求参数与 SQL 文件内容，不依赖 `params` 字段。

**管理端接口（/api/adm/apis）**
- 创建：`POST /api/adm/apis`
  - 校验 SQL → 解析参数 → 在 `${apis.dosql.path}/${apiName}.sql` 写入文件。
  - 将完整项目相对路径保存到 `sqlFilePath`，入库一条 `DoSqlField` 记录（包含解析出的 `params`）。
- 分页查询：`GET /api/adm/apis`
  - 支持 `apiName`、`sqlFilePath`、`params`、`note` 等条件；按需排序。
- 查看详情：`GET /api/adm/apis/{id}`
  - 默认返回 SQL 文件内容（`sql` 字段）；如文件缺失，库内会将 `sqlFilePath` 置为 `null` 并返回。
- 更新：`PUT /api/adm/apis/{id}`
  - 若有新的 SQL 内容，重新解析 `params` 并写入文件。
  - 若 `apiName` 变更，重命名文件为 `${apis.dosql.path}/${newApiName}.sql`，同步更新 `sqlFilePath`。
- 删除：`DELETE /api/adm/apis/{id}`
  - 仅删除数据库记录；不删除 `apis` 目录中的对应 SQL 文件。
- 初始化：`POST /api/adm/apis/init`
  - 遍历 `${apis.dosql.path}` 下所有 `.sql` 文件；按文件名（不含扩展名）作为 `apiName`，记录项目相对路径到 `sqlFilePath`，解析并记录 `params`。
  - 若数据库中已存在同名 `apiName`，则跳过（不更新现有记录）；仅新增缺失项。
  - 统计返回：`scanned/created/skipped/errors`。

**使用端接口（执行 SQL）**
- 查询：`GET /api/apis/{sqlFile}` → 返回查询结果。
  - `sqlFile` 通常为文件名（可带或不带 `.sql` 扩展名），目录由 `apis.dosql.path` 控制。
  - 将请求参数按上文约定替换占位符后执行。
- 执行：`POST /api/apis/{sqlFile}` → 返回影响行数。

**示例**
- SQL 文件 `apis/testget.sql`：
  - `SELECT * FROM users WHERE name=#{name} AND status=${status};`
  - `params` 将记录为 `name`（当前不包含 `status`）。
- 创建请求体（简化示例）：
  - `{ "apiName": "testget", "sql": "SELECT ..." }`
  - 服务将写入到 `${apis.dosql.path}/testget.sql`，并持久化 `sqlFilePath="apis/testget.sql"` 与 `params`。

**安全建议**
- `${param}` 为原样替换，请仅用于可信的标识符（表名、列名），必要时做白名单校验或枚举限制。
- `#{param}` 的值会加引号但未做转义，如值包含 `'` 可能引起语法或安全问题；建议前置过滤或转义处理。
- 避免将不可信输入拼接进 SQL，尽量在 SQL 文件中定义完整结构，仅传入必要参数。

**常见问题**
- 文件缺失：详情接口会将库中 `sqlFilePath` 置为 `null`；建议通过重新上传或更新恢复文件。
- 参数过多：`params` 默认长度为 `varchar(100)`，如不够用可调整为更长（例如 `varchar(500)`）。
- 目录不一致：确保 `apis.dosql.path` 与 `apis.dosql.path` 指向相同目录，避免管理端与使用端读取的不是同一位置。