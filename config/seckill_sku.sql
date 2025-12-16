-- 1. 创建秒杀商品表
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='秒杀商品表';

-- 2. 插入测试数据 (假设 product_id = 1 是一个 iPhone)
-- 这一步很重要！我们假设有一个 ID=1 的商品，拿出来 10 个做秒杀，每人限购 1 个
INSERT INTO `seckill_sku` (`product_id`, `seckill_price`, `seckill_stock`, `seckill_limit`, `start_time`, `end_time`)
VALUES (1, 999.00, 10, 1, NOW(), DATE_ADD(NOW(), INTERVAL 2 DAY));