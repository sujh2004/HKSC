package com.hksc.cart.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 添加购物车DTO
 */
@Data
public class CartAddDTO implements Serializable {

    private Long productId;

    private Integer count;
}
