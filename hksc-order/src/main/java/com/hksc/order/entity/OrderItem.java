package com.hksc.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("order_item")
public class OrderItem {

    /**
     * 明细ID (数据库自增)
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 归属的订单ID
     */
    private Long orderId;

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 商品名称快照 (下单时的名字，防止后续商品改名)
     */
    private String productName;

    /**
     * 购买单价
     */
    private BigDecimal price;

    /**
     * 购买数量
     */
    private Integer quantity;
}