package com.hksc.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hksc.product.entity.Product;

public interface ProductService extends IService<Product> {
    // 1. 外部方法：负责锁的控制
    boolean doDeduction(Long productId, Integer count);

    // 2. 内部方法：负责事务和数据库操作
    void deductStockInternal(Long productId, Integer count);

    // 新增方法定义
    boolean restoreStock(Long productId, Integer count);
}
