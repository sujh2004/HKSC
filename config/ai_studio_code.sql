/*
 * HKSC 电商平台初始化脚本
 * 包含：库创建、核心表结构、少量测试数据
 */

-- 1. 创建数据库
CREATE DATABASE IF NOT EXISTS `hksc_db` CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_general_ci';
USE `hksc_db`;

-- ==========================================
-- 2. 用户服务表 (hksc-user)
-- ==========================================
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` VARCHAR(64) DEFAULT NULL COMMENT '用户名',
  `password` VARCHAR(64) NOT NULL COMMENT '密码(加密)',
  `phone` VARCHAR(11) NOT NULL COMMENT '手机号',
  `nickname` VARCHAR(64) DEFAULT NULL COMMENT '昵称',
  `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像',
  `status` TINYINT DEFAULT 1 COMMENT '状态: 1启用 0禁用',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_phone` (`phone`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 插入测试用户 (密码是 123456 的 BCrypt密文，暂时先用明文占位，后面开发Auth时会讲)
INSERT INTO `user` (`username`, `password`, `phone`, `nickname`) 
VALUES ('test_user', '$2a$10$NxKkGq.0/s1.X', '13800138000', '测试用户A');


-- ==========================================
-- 3. 商品服务表 (hksc-product)
-- ==========================================
DROP TABLE IF EXISTS `category`;
CREATE TABLE `category` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(64) NOT NULL COMMENT '分类名称',
  `parent_id` BIGINT DEFAULT 0 COMMENT '父分类ID',
  `level` INT DEFAULT 1 COMMENT '层级',
  `sort` INT DEFAULT 0 COMMENT '排序',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品分类表';

INSERT INTO `category` (`id`, `name`, `parent_id`, `level`) VALUES 
(1, '数码电子', 0, 1), 
(2, '手机', 1, 2), 
(3, '笔记本', 1, 2);

DROP TABLE IF EXISTS `product`;
CREATE TABLE `product` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `title` VARCHAR(128) NOT NULL COMMENT '商品标题',
  `price` DECIMAL(10,2) NOT NULL COMMENT '价格',
  `stock` INT NOT NULL DEFAULT 0 COMMENT '库存',
  `category_id` BIGINT DEFAULT NULL COMMENT '分类ID',
  `brand` VARCHAR(64) DEFAULT NULL COMMENT '品牌',
  `image` VARCHAR(255) DEFAULT NULL COMMENT '图片URL',
  `status` TINYINT DEFAULT 1 COMMENT '状态: 1上架 0下架',
  `detail` TEXT COMMENT '详情描述(AI生成)',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

INSERT INTO `product` (`title`, `price`, `stock`, `category_id`, `brand`, `status`) VALUES 
('iPhone 15 Pro', 8999.00, 100, 2, 'Apple', 1),
('MacBook Pro M3', 14999.00, 50, 3, 'Apple', 1),
('小米14', 3999.00, 200, 2, 'Xiaomi', 1);


-- ==========================================
-- 4. 订单服务表 (hksc-order)
-- ==========================================
DROP TABLE IF EXISTS `orders`;
CREATE TABLE `orders` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `order_no` VARCHAR(64) NOT NULL COMMENT '订单号(唯一)',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `total_amount` DECIMAL(10,2) NOT NULL COMMENT '订单总金额',
  `pay_amount` DECIMAL(10,2) DEFAULT NULL COMMENT '实付金额',
  `status` TINYINT DEFAULT 0 COMMENT '0待付款 1已付款 2已发货 3已完成 4已取消',
  `pay_type` TINYINT DEFAULT NULL COMMENT '1支付宝 2微信',
  `receiver_name` VARCHAR(32) DEFAULT NULL COMMENT '收货人',
  `receiver_phone` VARCHAR(20) DEFAULT NULL COMMENT '收货电话',
  `receiver_addr` VARCHAR(255) DEFAULT NULL COMMENT '收货地址',
  `pay_time` DATETIME DEFAULT NULL COMMENT '支付时间',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单主表';

DROP TABLE IF EXISTS `order_item`;
CREATE TABLE `order_item` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `order_id` BIGINT NOT NULL COMMENT '订单ID',
  `order_no` VARCHAR(64) NOT NULL COMMENT '订单号',
  `product_id` BIGINT NOT NULL COMMENT '商品ID',
  `product_name` VARCHAR(128) NOT NULL COMMENT '商品名称',
  `product_price` DECIMAL(10,2) NOT NULL COMMENT '商品单价',
  `count` INT NOT NULL COMMENT '购买数量',
  `total_price` DECIMAL(10,2) NOT NULL COMMENT '总价',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单明细表';