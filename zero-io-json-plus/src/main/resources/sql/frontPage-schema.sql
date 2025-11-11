SET FOREIGN_KEY_CHECKS=0;

DROP TABLE IF EXISTS `t_front_page`;
CREATE TABLE `t_front_page` (
`id` bigint NOT NULL AUTO_INCREMENT,
`page_name` varchar(50) DEFAULT NULL,
`page_id` varchar(50) NOT NULL COMMENT '前端传的唯一数值',
`title` varchar(50) NOT NULL COMMENT '标题',
`page_descrip` varchar(255) DEFAULT NULL COMMENT '页面描述',
`content` json DEFAULT NULL COMMENT '页面配置json数据',
`user_id` bigint DEFAULT NULL COMMENT '提交人用户id',
`appid` varchar(50) DEFAULT 'DEFAULT' COMMENT 'appid',
`channel_no` varchar(50) DEFAULT NULL COMMENT '渠道编号[deprecated]',
`json_name` varchar(255) DEFAULT NULL COMMENT 'json文件名',
`json_path` varchar(255) DEFAULT NULL COMMENT 'json文件路径',
`create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
`update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
`template_status` tinyint NOT NULL DEFAULT '0' COMMENT '是否是模板 0-不是 1-是',
`tag` varchar(200) DEFAULT NULL COMMENT '唯一标识 可用于搜索',
`type` varchar(255) DEFAULT NULL COMMENT '页面主要类型（modules里面的 存在多个时优先显示autolist）',
`module_name` varchar(255) DEFAULT NULL COMMENT '页面名字多个时显示第一个',
`notes` varchar(255) DEFAULT NULL COMMENT '页面描述',
PRIMARY KEY (`id`) USING BTREE,
UNIQUE KEY `page_id` (`page_id`) USING BTREE,
UNIQUE KEY `tag` (`tag`) USING BTREE,
UNIQUE KEY `page_name` (`page_name`) USING BTREE,
KEY `idx_channel_no` (`channel_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC;

-- =====================================================================
-- t_front_page_module_info 设计说明（页面管理-模块维度快照表）
--
-- 核心作用：从 t_front_page.content(JSON) 中抽取“模块级”信息并快照化，
--          形成可索引、可检索、可统计、可审计的页面-模块明细。
--
-- 为什么不只用 t_front_page：
-- 1) 可索引与性能：对 JSON 做 JSON_EXTRACT/JSON_CONTAINS 难以高效索引；
--    拆分后可直接在 module_type、item_module_name 建索引，大幅提升查询性能。
-- 2) 一对多建模：页面天然包含多个模块，1-N 关系用子表表达最清晰，
--    便于做模块级分页、筛选、聚合与统计。
-- 3) 运营与检索：支持“按模块维度”的检索、覆盖率/热度统计、紧急下线等运营需求。
-- 4) JSON 演进解耦：将被频繁检索/统计的关键信息固化为行记录，降低上层对 JSON 结构变更的敏感度。
-- 5) 细粒度权限与审计：支持模块级审批、灰度、审计轨迹；仅用整页 JSON 粒度过粗。
-- 6) 写放大与并发：大 JSON 频繁整体更新易产生写放大/冲突；模块信息行级维护更稳健。
-- 7) 模板与复用：便于模块清单、跨页复用、模板推荐与批量替换。
--
-- 字段要点：
-- - page_id / front_page_id：双指针便于业务标识与主键双向检索；
-- - module_type / item_module_name：检索与分组关键字段，适合加索引；
-- - module_json：保留模块原子 JSON 片段，便于还原/比对；
-- - title：页面标题快照，常用展示避免联表；
-- 与 Mapper 的 batchInsert/cleanModule 等操作配合使用，形成“清空+重建”的一致性快照流程。
-- =====================================================================
DROP TABLE IF EXISTS `t_front_page_module_info`;
CREATE TABLE `t_front_page_module_info` (
`id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
`page_id` bigint DEFAULT NULL COMMENT '页面page_id（与t_front_page.page_id对应）',
`item_module_name` varchar(255) DEFAULT NULL COMMENT '模块名称（moduleData中的name）',
`module_json` json DEFAULT NULL COMMENT '模块JSON（modules或moduleData提取）',
`front_page_id` bigint DEFAULT NULL COMMENT '前端页面表主键（t_front_page.id）',
`title` varchar(255) DEFAULT NULL COMMENT '页面标题快照',
`module_type` varchar(50) DEFAULT NULL COMMENT '模块类型（modules[*].type）',
`create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
`update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
PRIMARY KEY (`id`),
KEY `idx_module_type` (`module_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC COMMENT='前端页面模块信息表';
