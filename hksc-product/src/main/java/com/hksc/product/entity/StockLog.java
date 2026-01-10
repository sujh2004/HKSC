package com.hksc.product.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 库存变更日志表
 * 记录每次库存变化，方便追溯和审计
 */
@Data
@TableName("stock_log")
public class StockLog implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 订单号
     */
    private String orderSn;

    /**
     * 变更类型: 1入库/2锁定/3扣减/4解锁/5退货
     */
    private Integer changeType;

    /**
     * 变更数量（正数为增加，负数为减少）
     */
    private Integer changeAmount;

    /**
     * 变更前库存
     */
    private Integer beforeStock;

    /**
     * 变更后库存
     */
    private Integer afterStock;

    /**
     * 操作人
     */
    private String operator;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
