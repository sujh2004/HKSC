/*
 * HKSC 电商平台 - 秒杀模块数据库脚本
 * 更新时间: 2025-12-16
 * 包含: 订单表结构更新(适配秒杀字段) + 秒杀库存表新建
 * 注意: 执行前请确认库名，开发环境建议重建表结构
 */

-- =======================================================
-- PART 1: 订单数据库 (hksc_order)
-- 作用: 负责订单落库、秒杀库存扣减
-- =======================================================
USE `hksc_order`;

-- 1. 订单主表 (order_info)
-- 变更点: 增加了 order_sn (防重), note, pay_amount
DROP TABLE IF EXISTS `order_info`;
CREATE TABLE `order_info` (
                              `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '订单ID',
                              `user_id` BIGINT NOT NULL COMMENT '用户ID',
                              `order_sn` VARCHAR(64) DEFAULT NULL COMMENT '订单唯一序列号(关键:用于防重/幂等)',
                              `total_amount` DECIMAL(10,2) DEFAULT NULL COMMENT '订单总金额',
                              `pay_amount` DECIMAL(10,2) DEFAULT NULL COMMENT '应付金额(实际支付)',
                              `status` INT DEFAULT 0 COMMENT '订单状态: 0->待付款, 1->已付款, 2->已发货, 3->已完成, 4->已关闭',
                              `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                              `note` VARCHAR(255) DEFAULT NULL COMMENT '订单备注',
                              PRIMARY KEY (`id`),
                              UNIQUE KEY `uk_order_sn` (`order_sn`), -- 加上唯一索引，数据库层面防止重复下单
                              KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单主表';

-- 2. 订单明细表 (order_item)
-- 变更点: 增加了 sku_id (秒杀规格), buy_count, order_sn
DROP TABLE IF EXISTS `order_item`;
CREATE TABLE `order_item` (
                              `id` BIGINT NOT NULL AUTO_INCREMENT,
                              `order_id` BIGINT NOT NULL COMMENT '关联的主订单ID',
                              `order_sn` VARCHAR(64) DEFAULT NULL COMMENT '冗余订单号',
                              `product_id` BIGINT NOT NULL COMMENT '商品SPU ID',
                              `sku_id` BIGINT DEFAULT NULL COMMENT '商品SKU ID (秒杀必须)',
                              `product_name` VARCHAR(255) DEFAULT NULL COMMENT '商品名称快照',
                              `product_price` DECIMAL(10,2) DEFAULT NULL COMMENT '购买时的单价',
                              `buy_count` INT DEFAULT 1 COMMENT '购买数量',
                              PRIMARY KEY (`id`),
                              KEY `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单明细表';

-- 3. 秒杀库存副本表 (seckill_sku)
-- 作用: 订单服务本地扣减库存，避免跨库事务
DROP TABLE IF EXISTS `seckill_sku`;
CREATE TABLE `seckill_sku` (
                               `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '秒杀ID',
                               `product_id` BIGINT NOT NULL COMMENT '关联的原始商品ID',
                               `seckill_price` DECIMAL(10,2) NOT NULL COMMENT '秒杀价格',
                               `seckill_stock` INT NOT NULL COMMENT '秒杀库存数量',
                               `seckill_limit` INT NOT NULL DEFAULT 1 COMMENT '每人限购数量',
                               `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
                               PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='秒杀库存表(订单库副本)';

-- 初始化测试数据: ID=1 的秒杀商品，库存 10 个
INSERT INTO `seckill_sku` (`id`, `product_id`, `seckill_price`, `seckill_stock`, `seckill_limit`)
VALUES (1, 1, 999.00, 10, 1);


-- =======================================================
-- PART 2: 业务主数据库 (hksc_db)
-- 作用: 商品服务管理秒杀活动、Redis预热的数据源
-- =======================================================
USE `hksc_db`;

-- 1. 秒杀商品主表 (seckill_sku)
-- 注意: 如果已经存在，请根据情况决定是否 Drop
DROP TABLE IF EXISTS `seckill_sku`;
CREATE TABLE `seckill_sku` (
                               `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '秒杀ID',
                               `product_id` BIGINT NOT NULL COMMENT '关联的原始商品ID',
                               `seckill_price` DECIMAL(10,2) NOT NULL COMMENT '秒杀价格',
                               `seckill_stock` INT NOT NULL COMMENT '秒杀库存数量',
                               `seckill_limit` INT NOT NULL DEFAULT 1 COMMENT '每人限购数量',
                               `start_time` DATETIME NOT NULL COMMENT '秒杀开始时间',
                               `end_time` DATETIME NOT NULL COMMENT '秒杀结束时间',
                               `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
                               PRIMARY KEY (`id`),
                               KEY `idx_product_id` (`product_id`),
                               KEY `idx_start_time` (`start_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='秒杀商品主表(管理端配置用)';

-- 初始化测试数据
INSERT INTO `seckill_sku` (`id`, `product_id`, `seckill_price`, `seckill_stock`, `seckill_limit`, `start_time`, `end_time`)
VALUES (1, 1, 999.00, 10, 1, NOW(), DATE_ADD(NOW(), INTERVAL 2 DAY));