package com.hksc.product.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品详情视图对象
 */
@Data
public class ProductDetailVO implements Serializable {

    private Long id;

    private String title;

    private BigDecimal price;

    private Long categoryId;

    private String categoryName;

    private String brand;

    private String image;

    private String images;

    private String detail;

    private Integer status;

    private String statusText;

    private Integer isSeckill;

    private Integer sales;

    /**
     * 库存信息
     */
    private Integer stock;

    /**
     * 秒杀相关信息（仅当isSeckill=1时有值）
     */
    private BigDecimal seckillPrice;

    private Integer seckillStock;

    private Integer seckillLimit;

    private LocalDateTime seckillStartTime;

    private LocalDateTime seckillEndTime;

    private Integer seckillStatus;

    private String seckillStatusText;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
