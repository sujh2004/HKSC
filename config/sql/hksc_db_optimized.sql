/*
 * HKSC 电商平台 - 优化后的数据库设计
 * 优化点：分离库存表、规范化字段、添加必要索引
 */

-- =======================================================
-- hksc_db (主业务库)
-- =======================================================
CREATE DATABASE IF NOT EXISTS `hksc_db` DEFAULT CHARACTER SET utf8mb4;
USE `hksc_db`;

-- 1. 用户表
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `phone` VARCHAR(20) NOT NULL COMMENT '手机号',
    `password` VARCHAR(128) NOT NULL COMMENT '加密密码',
    `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
    `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 1正常/0禁用',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除: 0未删除/1已删除',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_phone` (`phone`),  -- 手机号唯一索引，用于登录查询
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 2. 商品分类表
DROP TABLE IF EXISTS `category`;
CREATE TABLE `category` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '分类ID',
    `name` VARCHAR(50) NOT NULL COMMENT '分类名称',
    `parent_id` BIGINT DEFAULT 0 COMMENT '父分类ID(0表示一级分类)',
    `level` INT DEFAULT 1 COMMENT '分类层级(1/2/3)',
    `sort` INT DEFAULT 0 COMMENT '排序号',
    `icon` VARCHAR(255) DEFAULT NULL COMMENT '分类图标',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_parent_id` (`parent_id`),
    KEY `idx_level` (`level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品分类表';

-- 3. 商品主表 (SPU - Standard Product Unit) 标准产品单元
DROP TABLE IF EXISTS `product`;
CREATE TABLE `product` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '商品ID',
    `title` VARCHAR(255) NOT NULL COMMENT '商品标题',
    `price` DECIMAL(10,2) NOT NULL COMMENT '售价',
    `category_id` BIGINT NOT NULL COMMENT '分类ID',
    `brand` VARCHAR(100) DEFAULT NULL COMMENT '品牌',
    `image` VARCHAR(500) DEFAULT NULL COMMENT '主图URL',
    `images` TEXT DEFAULT NULL COMMENT '商品图片列表(JSON)',
    `detail` TEXT DEFAULT NULL COMMENT '商品详情描述',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 1上架/0下架',
    `is_seckill` TINYINT DEFAULT 0 COMMENT '是否参与秒杀: 1是/0否',
    `sales` INT DEFAULT 0 COMMENT '销量',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_category_id` (`category_id`),
    KEY `idx_status` (`status`),
    KEY `idx_is_seckill` (`is_seckill`),
    KEY `idx_sales` (`sales`)  -- 用于按销量排序
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品主表(SPU)';

-- 4. 库存表 (独立管理库存) 库存保存单位
DROP TABLE IF EXISTS `product_stock`;
CREATE TABLE `product_stock` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '库存ID',
    `product_id` BIGINT NOT NULL COMMENT '商品ID',
    `total_stock` INT NOT NULL DEFAULT 0 COMMENT '总库存',
    `available_stock` INT NOT NULL DEFAULT 0 COMMENT '可用库存',
    `locked_stock` INT NOT NULL DEFAULT 0 COMMENT '锁定库存(下单未支付)',
    `version` INT DEFAULT 0 COMMENT '乐观锁版本号',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_product_id` (`product_id`)  -- 一个商品一条库存记录
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品库存表';

-- 5. 库存变更日志表 (追溯库存变化)
DROP TABLE IF EXISTS `stock_log`;
CREATE TABLE `stock_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    `product_id` BIGINT NOT NULL COMMENT '商品ID',
    `order_sn` VARCHAR(64) DEFAULT NULL COMMENT '订单号',
    `change_type` TINYINT NOT NULL COMMENT '变更类型: 1入库/2锁定/3扣减/4解锁/5退货',
    `change_amount` INT NOT NULL COMMENT '变更数量(正数为增加,负数为减少)',
    `before_stock` INT NOT NULL COMMENT '变更前库存',
    `after_stock` INT NOT NULL COMMENT '变更后库存',
    `operator` VARCHAR(50) DEFAULT NULL COMMENT '操作人',
    `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_product_id` (`product_id`),
    KEY `idx_order_sn` (`order_sn`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存变更日志表';

-- 6. 秒杀活动配置表
DROP TABLE IF EXISTS `seckill_activity`;
CREATE TABLE `seckill_activity` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '秒杀活动ID',
    `product_id` BIGINT NOT NULL COMMENT '商品ID',
    `seckill_price` DECIMAL(10,2) NOT NULL COMMENT '秒杀价',
    `seckill_stock` INT NOT NULL COMMENT '秒杀总库存',
    `seckill_limit` INT DEFAULT 1 COMMENT '限购数量',
    `start_time` DATETIME NOT NULL COMMENT '开始时间',
    `end_time` DATETIME NOT NULL COMMENT '结束时间',
    `status` TINYINT DEFAULT 0 COMMENT '状态: 0未开始/1进行中/2已结束',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_product_id` (`product_id`),
    KEY `idx_start_time` (`start_time`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='秒杀活动配置表';

-- 7. 用户秒杀记录表 (防止重复秒杀)
DROP TABLE IF EXISTS `seckill_record`;
CREATE TABLE `seckill_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `activity_id` BIGINT NOT NULL COMMENT '秒杀活动ID',
    `product_id` BIGINT NOT NULL COMMENT '商品ID',
    `order_sn` VARCHAR(64) NOT NULL COMMENT '订单号',
    `buy_count` INT DEFAULT 1 COMMENT '购买数量',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_activity` (`user_id`, `activity_id`),  -- 防止重复秒杀
    KEY `idx_order_sn` (`order_sn`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户秒杀记录表';


-- =======================================================
-- hksc_order (订单库)
-- =======================================================
CREATE DATABASE IF NOT EXISTS `hksc_order` DEFAULT CHARACTER SET utf8mb4;
USE `hksc_order`;

-- 1. 订单主表
DROP TABLE IF EXISTS `order_info`;
CREATE TABLE `order_info` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '订单ID',
    `order_sn` VARCHAR(64) NOT NULL COMMENT '订单唯一编号',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `order_type` TINYINT DEFAULT 1 COMMENT '订单类型: 1普通订单/2秒杀订单',
    `total_amount` DECIMAL(10,2) NOT NULL COMMENT '订单总金额',
    `pay_amount` DECIMAL(10,2) NOT NULL COMMENT '实付金额',
    `freight_amount` DECIMAL(10,2) DEFAULT 0.00 COMMENT '运费',
    `coupon_amount` DECIMAL(10,2) DEFAULT 0.00 COMMENT '优惠券金额',
    `payment_type` TINYINT DEFAULT NULL COMMENT '支付方式: 1微信/2支付宝',
    `status` TINYINT DEFAULT 0 COMMENT '订单状态: 0待付款/1已付款/2已发货/3已完成/4已取消/5已关闭',
    `delivery_info` TEXT DEFAULT NULL COMMENT '收货信息(JSON)',
    `note` VARCHAR(500) DEFAULT NULL COMMENT '订单备注',
    `cancel_reason` VARCHAR(255) DEFAULT NULL COMMENT '取消原因',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '下单时间',
    `pay_time` DATETIME DEFAULT NULL COMMENT '支付时间',
    `delivery_time` DATETIME DEFAULT NULL COMMENT '发货时间',
    `finish_time` DATETIME DEFAULT NULL COMMENT '完成时间',
    `cancel_time` DATETIME DEFAULT NULL COMMENT '取消时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_sn` (`order_sn`),  -- 防止重复下单
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status` (`status`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单主表';

-- 2. 订单明细表
DROP TABLE IF EXISTS `order_item`;
CREATE TABLE `order_item` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '明细ID',
    `order_id` BIGINT NOT NULL COMMENT '订单ID',
    `product_id` BIGINT NOT NULL COMMENT '商品ID',
    `seckill_id` BIGINT DEFAULT NULL COMMENT '秒杀活动ID(普通订单为空)',
    `product_name` VARCHAR(255) NOT NULL COMMENT '商品名称快照',
    `product_image` VARCHAR(500) DEFAULT NULL COMMENT '商品图片快照',
    `product_price` DECIMAL(10,2) NOT NULL COMMENT '商品单价',
    `buy_count` INT NOT NULL DEFAULT 1 COMMENT '购买数量',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_order_id` (`order_id`),
    KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单明细表';

-- 3. 秒杀库存副本表 (订单服务本地扣减，避免跨库事务)
DROP TABLE IF EXISTS `seckill_stock`;
CREATE TABLE `seckill_stock` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '秒杀ID',
    `activity_id` BIGINT NOT NULL COMMENT '秒杀活动ID',
    `product_id` BIGINT NOT NULL COMMENT '商品ID',
    `seckill_price` DECIMAL(10,2) NOT NULL COMMENT '秒杀价',
    `available_stock` INT NOT NULL COMMENT '可用库存',
    `version` INT DEFAULT 0 COMMENT '乐观锁版本号',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_activity_id` (`activity_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='秒杀库存副本表';


-- =======================================================
-- 初始化测试数据
-- =======================================================
USE `hksc_db`;

-- 插入分类数据
INSERT INTO `category` (`id`, `name`, `parent_id`, `level`, `sort`) VALUES
(1, '手机数码', 0, 1, 1),
(2, '智能手机', 1, 2, 1),
(3, '笔记本电脑', 1, 2, 2);

-- 插入商品数据
INSERT INTO `product` (`id`, `title`, `price`, `category_id`, `brand`, `status`, `is_seckill`) VALUES
(1, 'iPhone 15 Pro Max 256GB', 9999.00, 2, 'Apple', 1, 1),
(2, '华为Mate 60 Pro 512GB', 7999.00, 2, '华为', 1, 0);

-- 插入库存数据
INSERT INTO `product_stock` (`product_id`, `total_stock`, `available_stock`, `locked_stock`) VALUES
(1, 100, 100, 0),
(2, 200, 200, 0);

-- 插入秒杀活动
INSERT INTO `seckill_activity` (`product_id`, `seckill_price`, `seckill_stock`, `seckill_limit`, `start_time`, `end_time`, `status`) VALUES
(1, 999.00, 10, 1, NOW(), DATE_ADD(NOW(), INTERVAL 2 DAY), 1);

USE `hksc_order`;

-- 插入秒杀库存副本 (需要与主库同步)
INSERT INTO `seckill_stock` (`activity_id`, `product_id`, `seckill_price`, `available_stock`) VALUES
(1, 1, 999.00, 10);
