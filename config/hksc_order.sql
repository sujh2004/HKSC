CREATE DATABASE IF NOT EXISTS `hksc_order` DEFAULT CHARACTER SET utf8mb4;

USE `hksc_order`;

-- 1. 订单主表
CREATE TABLE `order_info` (
                              `id` BIGINT NOT NULL COMMENT '订单ID (雪花算法生成)',
                              `user_id` BIGINT NOT NULL COMMENT '用户ID',
                              `total_amount` DECIMAL(10,2) NOT NULL COMMENT '订单总金额',
                              `status` INT DEFAULT 0 COMMENT '状态: 0-待付款, 1-已付款, 2-已发货, 3-已完成, 4-已取消',
                              `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
                              `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                              PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- 2. 订单明细表 (记录买了什么)
CREATE TABLE `order_item` (
                              `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
                              `order_id` BIGINT NOT NULL COMMENT '归属的订单ID',
                              `product_id` BIGINT NOT NULL COMMENT '商品ID',
                              `product_name` VARCHAR(255) COMMENT '商品快照名称',
                              `price` DECIMAL(10,2) NOT NULL COMMENT '购买单价',
                              `quantity` INT NOT NULL COMMENT '购买数量'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单明细表';