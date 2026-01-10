package com.hksc.product.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 商品分类视图对象
 */
@Data
public class CategoryVO implements Serializable {

    private Long id;

    private String name;

    private Long parentId;

    private Integer level;

    private Integer sort;

    private String icon;

    /**
     * 子分类列表（用于树形结构）
     */
    private List<CategoryVO> children;
}
