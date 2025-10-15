CREATE TABLE `lc`.`t_apis_dosql` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `appid` varchar(50)  DEFAULT NULL COMMENT '区分不同应用',
  `api_name` varchar(50) NOT NULL COMMENT '接口名称',
  `sql_file_path` varchar(200) DEFAULT NULL COMMENT 'sql文件路径',
  `params` varchar(100) DEFAULT NULL COMMENT '参数',
  `note` varchar(100) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8
