package com.hksc.order.dto;

import lombok.Data;
import java.io.Serializable;

@Data
public class OrderCreateDTO implements Serializable {

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 购买数量
     */
    private Integer count;
}