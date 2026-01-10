package com.hksc.cart.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 更新购物车数量DTO
 */
@Data
public class CartUpdateDTO implements Serializable {

    private Long productId;

    private Integer count;
}
