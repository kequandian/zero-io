CREATE TABLE ``lc`.`lc_do_sql`` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `field_name` varchar(50) NOT NULL COMMENT '服务名称',
#   `description` varchar(255) DEFAULT NULL COMMENT '描述',
  `sql_file_name` varchar(100) DEFAULT NULL COMMENT '查询文件名',
  `params` varchar(100) DEFAULT NULL COMMENT '参数',
#   `param_status` tinyint NOT NULL DEFAULT '0' COMMENT '是否需要参数 0-不需要 1-需要 默认0',
#   `sort_number` int NOT NULL DEFAULT '1' COMMENT '排序号',
#   `status` tinyint NOT NULL DEFAULT '1' COMMENT '上下架 0-下架 1-上架 默认 1',
#   `sql_version` varchar(50) NOT NULL COMMENT '数据库版本',
  `note` varchar(255) DEFAULT NULL COMMENT '备用',
#   `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
#   `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8
