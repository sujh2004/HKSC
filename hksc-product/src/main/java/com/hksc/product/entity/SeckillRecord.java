package com.hksc.product.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户秒杀记录表
 * 防止用户重复秒杀同一个活动
 */
@Data
@TableName("seckill_record")
public class SeckillRecord implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 秒杀活动ID
     */
    private Long activityId;

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 订单号
     */
    private String orderSn;

    /**
     * 购买数量
     */
    private Integer buyCount;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
