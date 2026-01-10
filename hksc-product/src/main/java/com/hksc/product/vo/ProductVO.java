package com.hksc.product.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品列表视图对象
 */
@Data
public class ProductVO implements Serializable {

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
     * 库存
     */
    private Integer stock;

    /**
     * 状态文本
     */
    private String statusText;

    /**
     * 秒杀相关字段
     */
    private BigDecimal seckillPrice;
    private Integer seckillStock;
    private Integer seckillLimit;
    private LocalDateTime seckillStartTime;
    private LocalDateTime seckillEndTime;
    private Integer seckillStatus;
    private String seckillStatusText;
}
