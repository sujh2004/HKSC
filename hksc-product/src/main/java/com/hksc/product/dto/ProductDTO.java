package com.hksc.product.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 商品数据传输对象（用于服务间调用）
 */
@Data
public class ProductDTO implements Serializable {

    private Long id;

    private String title;

    private BigDecimal price;

    private Long categoryId;

    private String brand;

    private String image;

    private Integer status;

    private Integer isSeckill;

    private Integer sales;

    /**
     * 库存信息（从库存表查询后填充）
     */
    private Integer stock;
}
