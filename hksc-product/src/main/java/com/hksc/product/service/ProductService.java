package com.hksc.product.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hksc.product.dto.ProductDTO;
import com.hksc.product.dto.ProductQueryDTO;
import com.hksc.product.entity.Product;
import com.hksc.product.vo.ProductDetailVO;
import com.hksc.product.vo.ProductVO;

public interface ProductService extends IService<Product> {
    // 1. 外部方法：负责锁的控制
    boolean doDeduction(Long productId, Integer count);

    // 2. 内部方法：负责事务和数据库操作
    void deductStockInternal(Long productId, Integer count);

    // 新增方法定义
    boolean restoreStock(Long productId, Integer count);

    /**
     * 获取商品详情（返回VO）
     */
    ProductDetailVO getProductDetail(Long id);

    /**
     * 获取带库存信息的商品列表（返回VO）
     */
    Page<ProductVO> getProductListWithStock(ProductQueryDTO queryDTO);

    /**
     * 根据ID获取商品DTO（用于Feign调用）
     */
    ProductDTO getProductDTO(Long id);
}
