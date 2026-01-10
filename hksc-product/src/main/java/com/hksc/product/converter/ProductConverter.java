package com.hksc.product.converter;

import com.hksc.product.dto.ProductDTO;
import com.hksc.product.entity.Category;
import com.hksc.product.entity.Product;
import com.hksc.product.entity.SeckillActivity;
import com.hksc.product.vo.CategoryVO;
import com.hksc.product.vo.ProductDetailVO;
import com.hksc.product.vo.ProductVO;
import com.hksc.product.vo.SeckillActivityVO;

import java.util.ArrayList;
import java.util.List;

/**
 * 商品对象转换工具类
 * Entity <-> DTO <-> VO
 */
public class ProductConverter {

    // ===================== Product Entity <-> DTO ======================

    /**
     * Entity -> DTO
     */
    public static ProductDTO toDTO(Product entity) {
        if (entity == null) {
            return null;
        }
        ProductDTO dto = new ProductDTO();
        dto.setId(entity.getId());
        dto.setTitle(entity.getTitle());
        dto.setPrice(entity.getPrice());
        dto.setCategoryId(entity.getCategoryId());
        dto.setBrand(entity.getBrand());
        dto.setImage(entity.getImage());
        dto.setStatus(entity.getStatus());
        dto.setIsSeckill(entity.getIsSeckill());
        dto.setSales(entity.getSales());
        dto.setStock(entity.getStock());
        return dto;
    }

    /**
     * DTO -> Entity
     */
    public static Product toEntity(ProductDTO dto) {
        if (dto == null) {
            return null;
        }
        Product entity = new Product();
        entity.setId(dto.getId());
        entity.setTitle(dto.getTitle());
        entity.setPrice(dto.getPrice());
        entity.setCategoryId(dto.getCategoryId());
        entity.setBrand(dto.getBrand());
        entity.setImage(dto.getImage());
        entity.setStatus(dto.getStatus());
        entity.setIsSeckill(dto.getIsSeckill());
        entity.setSales(dto.getSales());
        entity.setStock(dto.getStock());
        return entity;
    }

    // ===================== Product Entity -> VO ======================

    /**
     * Entity -> ProductVO（商品列表项）
     */
    public static ProductVO toVO(Product entity) {
        if (entity == null) {
            return null;
        }
        ProductVO vo = new ProductVO();
        vo.setId(entity.getId());
        vo.setTitle(entity.getTitle());
        vo.setPrice(entity.getPrice());
        vo.setCategoryId(entity.getCategoryId());
        vo.setBrand(entity.getBrand());
        vo.setImage(entity.getImage());
        vo.setStatus(entity.getStatus());
        vo.setStatusText(getStatusText(entity.getStatus()));
        vo.setIsSeckill(entity.getIsSeckill());
        vo.setSales(entity.getSales());
        vo.setStock(entity.getStock());
        return vo;
    }

    /**
     * Entity -> ProductDetailVO（商品详情）
     */
    public static ProductDetailVO toDetailVO(Product entity) {
        if (entity == null) {
            return null;
        }
        ProductDetailVO vo = new ProductDetailVO();
        vo.setId(entity.getId());
        vo.setTitle(entity.getTitle());
        vo.setPrice(entity.getPrice());
        vo.setCategoryId(entity.getCategoryId());
        vo.setBrand(entity.getBrand());
        vo.setImage(entity.getImage());
        vo.setImages(entity.getImages());
        vo.setDetail(entity.getDetail());
        vo.setStatus(entity.getStatus());
        vo.setStatusText(getStatusText(entity.getStatus()));
        vo.setIsSeckill(entity.getIsSeckill());
        vo.setSales(entity.getSales());
        vo.setStock(entity.getStock());
        vo.setCreateTime(entity.getCreateTime());
        vo.setUpdateTime(entity.getUpdateTime());
        return vo;
    }

    /**
     * Entity List -> VO List
     */
    public static List<ProductVO> toVOList(List<Product> entities) {
        if (entities == null) {
            return null;
        }
        List<ProductVO> voList = new ArrayList<>();
        for (Product entity : entities) {
            voList.add(toVO(entity));
        }
        return voList;
    }

    // ===================== Category Entity <-> VO ======================

    /**
     * Category Entity -> VO
     */
    public static CategoryVO categoryToVO(Category entity) {
        if (entity == null) {
            return null;
        }
        CategoryVO vo = new CategoryVO();
        vo.setId(entity.getId());
        vo.setName(entity.getName());
        vo.setParentId(entity.getParentId());
        vo.setLevel(entity.getLevel());
        vo.setSort(entity.getSort());
        vo.setIcon(entity.getIcon());
        return vo;
    }

    /**
     * Category Entity List -> VO List
     */
    public static List<CategoryVO> categoryToVOList(List<Category> entities) {
        if (entities == null) {
            return null;
        }
        List<CategoryVO> voList = new ArrayList<>();
        for (Category entity : entities) {
            voList.add(categoryToVO(entity));
        }
        return voList;
    }

    // ===================== SeckillActivity Entity -> VO ======================

    /**
     * SeckillActivity Entity -> VO
     */
    public static SeckillActivityVO seckillToVO(SeckillActivity entity, Product product, Integer remainStock) {
        if (entity == null) {
            return null;
        }
        SeckillActivityVO vo = new SeckillActivityVO();
        vo.setId(entity.getId());
        vo.setProductId(entity.getProductId());
        vo.setSeckillPrice(entity.getSeckillPrice());
        vo.setSeckillStock(entity.getSeckillStock());
        vo.setRemainStock(remainStock);
        vo.setSeckillLimit(entity.getSeckillLimit());
        vo.setStartTime(entity.getStartTime());
        vo.setEndTime(entity.getEndTime());
        vo.setStatus(entity.getStatus());
        vo.setStatusText(getSeckillStatusText(entity.getStatus()));

        // 填充商品信息
        if (product != null) {
            vo.setProductTitle(product.getTitle());
            vo.setProductImage(product.getImage());
            vo.setOriginalPrice(product.getPrice());
        }

        // 计算进度
        if (entity.getSeckillStock() != null && entity.getSeckillStock() > 0) {
            int sold = entity.getSeckillStock() - (remainStock != null ? remainStock : 0);
            vo.setProgress(sold * 100 / entity.getSeckillStock());
        } else {
            vo.setProgress(0);
        }

        return vo;
    }

    // ===================== 辅助方法 ======================

    /**
     * 获取商品状态文本
     */
    private static String getStatusText(Integer status) {
        if (status == null) {
            return "未知";
        }
        return status == 1 ? "上架" : "下架";
    }

    /**
     * 获取秒杀状态文本
     */
    private static String getSeckillStatusText(Integer status) {
        if (status == null) {
            return "未知";
        }
        switch (status) {
            case 0:
                return "未开始";
            case 1:
                return "进行中";
            case 2:
                return "已结束";
            default:
                return "未知";
        }
    }
}
