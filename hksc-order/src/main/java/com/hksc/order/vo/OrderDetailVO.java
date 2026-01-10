package com.hksc.order.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单详情视图对象
 */
@Data
public class OrderDetailVO implements Serializable {

    private Long id;

    private String orderSn;

    private Long userId;

    private Integer orderType;

    private String orderTypeText;

    private BigDecimal totalAmount;

    private BigDecimal payAmount;

    private BigDecimal freightAmount;

    private BigDecimal couponAmount;

    private Integer paymentType;

    private String paymentTypeText;

    private Integer status;

    private String statusText;

    private String deliveryInfo;

    private String note;

    private String cancelReason;

    private LocalDateTime createTime;

    private LocalDateTime payTime;

    private LocalDateTime deliveryTime;

    private LocalDateTime finishTime;

    private LocalDateTime cancelTime;

    /**
     * 订单明细列表
     */
    private List<OrderItemVO> items;
}
