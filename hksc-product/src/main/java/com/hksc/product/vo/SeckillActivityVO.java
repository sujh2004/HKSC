package com.hksc.product.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 秒杀活动视图对象
 */
@Data
public class SeckillActivityVO implements Serializable {

    private Long id;

    private Long productId;

    private String productTitle;

    private String productImage;

    private BigDecimal originalPrice;

    private BigDecimal seckillPrice;

    private Integer seckillStock;

    private Integer remainStock;

    private Integer seckillLimit;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Integer status;

    private String statusText;

    /**
     * 活动进度百分比
     */
    private Integer progress;
}
