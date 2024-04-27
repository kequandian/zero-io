SET FOREIGN_KEY_CHECKS=0;

DROP TABLE IF EXISTS `t_zero_io_icon`;
CREATE TABLE `t_zero_io_icon`(
`id` BIGINT(20) auto_increment,
`name` VARCHAR(255) NULL COMMENT '名称',
`title` VARCHAR(50) NULL COMMENT '标题',
`note` VARCHAR(200) NULL COMMENT '描述',
`path` VARCHAR(255) NOT NULL COMMENT '路径',
`check_sum` VARCHAR(255) NULL COMMENT '摘要',
`flag` TINYINT(4) DEFAULT 0 COMMENT '是否逻辑删除',
`create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
`update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
UNIQUE(`path`),
PRIMARY KEY(`id`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8;