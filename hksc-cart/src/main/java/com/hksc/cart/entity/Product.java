package com.hksc.cart.entity;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Product implements Serializable {

    private Long id;
    private String title;
    private BigDecimal price;
    private Integer stock;
    private Long categoryId;
    private String brand;
    private String image;
    private Integer status; // 1上架 0下架
    private String detail;


    private LocalDateTime createTime;


    private LocalDateTime updateTime;
}
