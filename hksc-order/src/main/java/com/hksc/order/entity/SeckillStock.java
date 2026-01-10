package com.hksc.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 秒杀库存副本表（订单服务本地扣减，避免跨库事务）
 * 改名自SeckillSku，更符合命名规范
 */
@Data
@TableName("seckill_stock")
public class SeckillStock implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 秒杀活动ID
     */
    private Long activityId;

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 秒杀价格
     */
    private BigDecimal seckillPrice;

    /**
     * 可用库存
     */
    private Integer availableStock;

    /**
     * 乐观锁版本号
     */
    @Version
    private Integer version;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}