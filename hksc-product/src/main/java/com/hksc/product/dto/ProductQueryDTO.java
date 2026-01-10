package com.hksc.product.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 商品查询参数DTO
 */
@Data
public class ProductQueryDTO implements Serializable {

    /**
     * 搜索关键词
     */
    private String keyword;

    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * 品牌
     */
    private String brand;

    /**
     * 是否秒杀商品: 1是/0否
     */
    private Integer isSeckill;

    /**
     * 页码
     */
    private Integer page = 1;

    /**
     * 每页数量
     */
    private Integer limit = 10;
}
