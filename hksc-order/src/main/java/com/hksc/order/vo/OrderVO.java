package com.hksc.order.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单列表项视图对象
 */
@Data
public class OrderVO implements Serializable {

    private Long id;

    private String orderSn;

    private Long userId;

    private Integer orderType;

    private String orderTypeText;

    private BigDecimal totalAmount;

    private BigDecimal payAmount;

    private Integer status;

    private String statusText;

    private LocalDateTime createTime;

    /**
     * 订单商品数量
     */
    private Integer itemCount;

    /**
     * 第一个商品名称（用于列表展示）
     */
    private String firstProductName;

    /**
     * 第一个商品图片（用于列表展示）
     */
    private String firstProductImage;
}
