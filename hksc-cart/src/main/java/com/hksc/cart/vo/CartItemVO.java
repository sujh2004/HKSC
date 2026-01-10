package com.hksc.cart.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 购物车项视图对象
 */
@Data
public class CartItemVO implements Serializable {

    private Long productId;

    private String title;

    private BigDecimal price;

    private String image;

    private Integer count;

    private Integer stock;

    /**
     * 是否选中
     */
    private Boolean checked;

    /**
     * 小计金额
     */
    private BigDecimal subtotal;
}
