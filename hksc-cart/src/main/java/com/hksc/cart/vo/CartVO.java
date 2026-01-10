package com.hksc.cart.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 购物车视图对象
 */
@Data
public class CartVO implements Serializable {

    /**
     * 购物车项列表
     */
    private List<CartItemVO> items;

    /**
     * 总金额
     */
    private BigDecimal totalAmount;

    /**
     * 选中商品数量
     */
    private Integer checkedCount;
}
