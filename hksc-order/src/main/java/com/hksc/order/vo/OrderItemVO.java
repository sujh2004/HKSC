package com.hksc.order.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 订单明细视图对象
 */
@Data
public class OrderItemVO implements Serializable {

    private Long id;

    private Long orderId;

    private Long productId;

    private Long seckillId;

    private String productName;

    private String productImage;

    private BigDecimal productPrice;

    private Integer buyCount;

    /**
     * 小计金额
     */
    private BigDecimal subtotal;
}
