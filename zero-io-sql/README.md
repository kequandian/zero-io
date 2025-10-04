# pom-dev-devops

1. sql文件放在 `resource/sqls` 下
2. test 文件下有测试用例



#### 执行查询语句

- api：GET {{endpoint}}/api/adm/apis/{fieldName}?key1=value&key2=value2
- 参数说明
   - `sqlFileName`为`sql`文件名，不带`.sql`后缀
   - key1、key2 对应 sql文件中的 #{key1} 和#{key2}，键对应的值 将会替代sql文件中的参数值，sql文件参数只能用 #{参数名} 这种格式
- 返回值 返回一个二维数组，每一列对应一条查询语句结果

#### 执行sql语句

- api：Post {{endpoint}}/api/adm/dosql/{fieldName}?key1=value&key2=value2
- 参数说明
  - sqlFIleName为sql文件名，不带.sql后缀
  - key1、key2 对应 sql文件中的 #{key1} 和#{key2}，键对应的值 将会替代sql文件中的参数值，sql文件参数只能用 #{参数名} 这种格式
- 查询sql语句无效
- 返回 影响行数
