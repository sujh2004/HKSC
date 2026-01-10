/*
 * Nacos 2.2.3 MySQL 初始化脚本 (全量版)
 * 作用：创建 nacos_config 库并初始化所有表
 */

-- 1. 创建数据库 (如果不存在)
CREATE DATABASE IF NOT EXISTS `nacos_config`
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_general_ci;

-- 2. 切换到该数据库
USE `nacos_config`;

-- 3. 开始建表 ==============================

/******************************************/
/*   1. config_info (核心配置表)           */
/******************************************/
CREATE TABLE IF NOT EXISTS `config_info` (
                                             `id` bigint(20) NOT NULL AUTO_INCREMENT,
                                             `data_id` varchar(255) NOT NULL COMMENT 'data_id',
                                             `group_id` varchar(255) DEFAULT NULL,
                                             `content` longtext NOT NULL COMMENT 'content',
                                             `md5` varchar(32) DEFAULT NULL COMMENT 'md5',
                                             `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                             `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
                                             `src_user` text COMMENT 'source user',
                                             `src_ip` varchar(50) DEFAULT NULL COMMENT 'source ip',
                                             `app_name` varchar(128) DEFAULT NULL,
                                             `tenant_id` varchar(128) DEFAULT '' COMMENT '租户字段',
                                             `c_desc` varchar(256) DEFAULT NULL,
                                             `c_use` varchar(64) DEFAULT NULL,
                                             `effect` varchar(64) DEFAULT NULL,
                                             `type` varchar(64) DEFAULT NULL,
                                             `c_schema` text,
                                             `encrypted_data_key` text NOT NULL COMMENT '秘钥',
                                             PRIMARY KEY (`id`),
                                             UNIQUE KEY `uk_configinfo_datagrouptenant` (`data_id`,`group_id`,`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='config_info';

/******************************************/
/*   2. config_info_aggr (聚合表)          */
/******************************************/
CREATE TABLE IF NOT EXISTS `config_info_aggr` (
                                                  `id` bigint(20) NOT NULL AUTO_INCREMENT,
                                                  `data_id` varchar(255) NOT NULL COMMENT 'data_id',
                                                  `group_id` varchar(255) NOT NULL COMMENT 'group_id',
                                                  `datum_id` varchar(255) NOT NULL COMMENT 'datum_id',
                                                  `content` longtext NOT NULL COMMENT '内容',
                                                  `gmt_modified` datetime NOT NULL COMMENT '修改时间',
                                                  `app_name` varchar(128) DEFAULT NULL,
                                                  `tenant_id` varchar(128) DEFAULT '' COMMENT '租户字段',
                                                  PRIMARY KEY (`id`),
                                                  UNIQUE KEY `uk_configinfoaggr_datagrouptenantdatum` (`data_id`,`group_id`,`tenant_id`,`datum_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='增加租户字段';

/******************************************/
/*   3. config_info_beta (Beta测试表)      */
/******************************************/
CREATE TABLE IF NOT EXISTS `config_info_beta` (
                                                  `id` bigint(20) NOT NULL AUTO_INCREMENT,
                                                  `data_id` varchar(255) NOT NULL COMMENT 'data_id',
                                                  `group_id` varchar(255) DEFAULT NULL,
                                                  `app_name` varchar(128) DEFAULT NULL COMMENT 'app_name',
                                                  `content` longtext NOT NULL COMMENT 'content',
                                                  `beta_ips` varchar(1024) DEFAULT NULL COMMENT 'betaIps',
                                                  `md5` varchar(32) DEFAULT NULL COMMENT 'md5',
                                                  `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                                  `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
                                                  `src_user` text COMMENT 'source user',
                                                  `src_ip` varchar(50) DEFAULT NULL COMMENT 'source ip',
                                                  `tenant_id` varchar(128) DEFAULT '' COMMENT '租户字段',
                                                  `encrypted_data_key` text NOT NULL COMMENT '秘钥',
                                                  PRIMARY KEY (`id`),
                                                  UNIQUE KEY `uk_configinfobeta_datagrouptenant` (`data_id`,`group_id`,`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='config_info_beta';

/******************************************/
/*   4. config_info_tag (标签配置表)       */
/******************************************/
CREATE TABLE IF NOT EXISTS `config_info_tag` (
                                                 `id` bigint(20) NOT NULL AUTO_INCREMENT,
                                                 `data_id` varchar(255) NOT NULL COMMENT 'data_id',
                                                 `group_id` varchar(255) DEFAULT NULL,
                                                 `tenant_id` varchar(128) DEFAULT '' COMMENT 'tenant_id',
                                                 `tag_id` varchar(128) NOT NULL COMMENT 'tag_id',
                                                 `app_name` varchar(128) DEFAULT NULL COMMENT 'app_name',
                                                 `content` longtext NOT NULL COMMENT 'content',
                                                 `md5` varchar(32) DEFAULT NULL COMMENT 'md5',
                                                 `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                                 `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
                                                 `src_user` text COMMENT 'source user',
                                                 `src_ip` varchar(50) DEFAULT NULL COMMENT 'source ip',
                                                 PRIMARY KEY (`id`),
                                                 UNIQUE KEY `uk_configinfotag_datagrouptenanttag` (`data_id`,`group_id`,`tenant_id`,`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='config_info_tag';

/******************************************/
/*   5. config_tags_relation (标签关系表)  */
/******************************************/
CREATE TABLE IF NOT EXISTS `config_tags_relation` (
                                                      `id` bigint(20) NOT NULL COMMENT 'id',
                                                      `tag_name` varchar(128) NOT NULL COMMENT 'tag_name',
                                                      `tag_type` varchar(64) DEFAULT NULL COMMENT 'tag_type',
                                                      `data_id` varchar(255) NOT NULL COMMENT 'data_id',
                                                      `group_id` varchar(255) NOT NULL COMMENT 'group_id',
                                                      `tenant_id` varchar(128) DEFAULT '' COMMENT 'tenant_id',
                                                      `nid` bigint(20) NOT NULL AUTO_INCREMENT,
                                                      PRIMARY KEY (`nid`),
                                                      UNIQUE KEY `uk_configtagrelation_configidtag` (`id`,`tag_name`,`tag_type`),
                                                      KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='config_tag_relation';

/******************************************/
/*   6. group_capacity (分组容量表)        */
/******************************************/
CREATE TABLE IF NOT EXISTS `group_capacity` (
                                                `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
                                                `group_id` varchar(128) NOT NULL DEFAULT '',
                                                `quota` int(10) unsigned NOT NULL DEFAULT '0',
                                                `usage` int(10) unsigned NOT NULL DEFAULT '0',
                                                `max_size` int(10) unsigned NOT NULL DEFAULT '0',
                                                `max_aggr_count` int(10) unsigned NOT NULL DEFAULT '0',
                                                `max_aggr_size` int(10) unsigned NOT NULL DEFAULT '0',
                                                `max_history_count` int(10) unsigned NOT NULL DEFAULT '0',
                                                `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                                `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                                PRIMARY KEY (`id`),
                                                UNIQUE KEY `uk_group_id` (`group_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='集群、各Group容量信息表';

/******************************************/
/*   7. tenant_capacity (租户容量表)       */
/******************************************/
CREATE TABLE IF NOT EXISTS `tenant_capacity` (
                                                 `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
                                                 `tenant_id` varchar(128) NOT NULL DEFAULT '',
                                                 `quota` int(10) unsigned NOT NULL DEFAULT '0',
                                                 `usage` int(10) unsigned NOT NULL DEFAULT '0',
                                                 `max_size` int(10) unsigned NOT NULL DEFAULT '0',
                                                 `max_aggr_count` int(10) unsigned NOT NULL DEFAULT '0',
                                                 `max_aggr_size` int(10) unsigned NOT NULL DEFAULT '0',
                                                 `max_history_count` int(10) unsigned NOT NULL DEFAULT '0',
                                                 `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                                 `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                                 PRIMARY KEY (`id`),
                                                 UNIQUE KEY `uk_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='租户容量信息表';

/******************************************/
/*   8. tenant_info (租户信息表)           */
/******************************************/
CREATE TABLE IF NOT EXISTS `tenant_info` (
                                             `id` bigint(20) NOT NULL AUTO_INCREMENT,
                                             `kp` varchar(128) NOT NULL COMMENT 'kp',
                                             `tenant_id` varchar(128) DEFAULT '' COMMENT 'tenant_id',
                                             `tenant_name` varchar(128) DEFAULT '' COMMENT 'tenant_name',
                                             `tenant_desc` varchar(256) DEFAULT NULL COMMENT 'tenant_desc',
                                             `create_source` varchar(32) DEFAULT NULL COMMENT 'create_source',
                                             `gmt_create` bigint(20) NOT NULL COMMENT '创建时间',
                                             `gmt_modified` bigint(20) NOT NULL COMMENT '修改时间',
                                             PRIMARY KEY (`id`),
                                             UNIQUE KEY `uk_tenant_info_kptenantid` (`kp`,`tenant_id`),
                                             KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='tenant_info';

/******************************************/
/*   9. users (用户表)                     */
/******************************************/
CREATE TABLE IF NOT EXISTS `users` (
                                       `username` varchar(50) NOT NULL,
                                       `password` varchar(500) NOT NULL,
                                       `enabled` boolean NOT NULL,
                                       PRIMARY KEY (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

/******************************************/
/*   10. roles (角色表)                    */
/******************************************/
CREATE TABLE IF NOT EXISTS `roles` (
                                       `username` varchar(50) NOT NULL,
                                       `role` varchar(50) NOT NULL,
                                       UNIQUE INDEX `idx_user_role` (`username` ASC, `role` ASC) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

/******************************************/
/*   11. permissions (权限表)              */
/******************************************/
CREATE TABLE IF NOT EXISTS `permissions` (
                                             `role` varchar(50) NOT NULL,
                                             `resource` varchar(255) NOT NULL,
                                             `action` varchar(8) NOT NULL,
                                             UNIQUE INDEX `uk_role_permission` (`role`,`resource`,`action`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

/******************************************/
/*   12. his_config_info (历史记录表)      */
/*   ⚠️ 必须要有，否则无法发布配置          */
/******************************************/
CREATE TABLE IF NOT EXISTS `his_config_info` (
                                                 `id` bigint(20) unsigned NOT NULL,
                                                 `nid` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
                                                 `data_id` varchar(255) NOT NULL,
                                                 `group_id` varchar(255) NOT NULL,
                                                 `app_name` varchar(128) DEFAULT NULL COMMENT 'app_name',
                                                 `content` longtext NOT NULL,
                                                 `md5` varchar(32) DEFAULT NULL,
                                                 `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                                 `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                                 `src_user` text,
                                                 `src_ip` varchar(50) DEFAULT NULL,
                                                 `op_type` char(10) DEFAULT NULL,
                                                 `tenant_id` varchar(128) DEFAULT '' COMMENT '租户字段',
                                                 `encrypted_data_key` text NOT NULL COMMENT '秘钥',
                                                 PRIMARY KEY (`nid`),
                                                 KEY `idx_gmt_create` (`gmt_create`),
                                                 KEY `idx_gmt_modified` (`gmt_modified`),
                                                 KEY `idx_did` (`data_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='多租户改造';

-- 4. 初始化默认账号 (账号: nacos, 密码: nacos)
INSERT INTO users (username, password, enabled) VALUES ('nacos', '$2a$10$EuWPZHzz32dJN7jexM34MOeYirDdFAZm2mDJp72/5Q1E2xcVt9Cq2', TRUE);
INSERT INTO roles (username, role) VALUES ('nacos', 'ROLE_ADMIN');