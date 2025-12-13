package com.hksc.order.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProductDTO {
    private Long id;
    private String title;
    private BigDecimal price; // 订单服务最关心这个
    private Integer stock;
    // 其他字段比如 image, brand 如果不需要可以不写
}