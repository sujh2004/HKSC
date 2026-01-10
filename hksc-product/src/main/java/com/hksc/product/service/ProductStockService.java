package com.hksc.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hksc.product.entity.ProductStock;

/**
 * 商品库存Service
 */
public interface ProductStockService extends IService<ProductStock> {

    /**
     * 锁定库存（下单时调用）
     * @param productId 商品ID
     * @param count 数量
     * @return 是否成功
     */
    boolean lockStock(Long productId, Integer count);

    /**
     * 释放锁定的库存（订单取消）
     * @param productId 商品ID
     * @param count 数量
     * @return 是否成功
     */
    boolean unlockStock(Long productId, Integer count);

    /**
     * 确认扣减库存（支付成功）
     * @param productId 商品ID
     * @param count 数量
     * @return 是否成功
     */
    boolean confirmReduceStock(Long productId, Integer count);

    /**
     * 使用乐观锁扣减库存
     * @param productId 商品ID
     * @param count 数量
     * @return 是否成功
     */
    boolean reduceStockWithOptimisticLock(Long productId, Integer count);

    /**
     * 恢复库存
     * @param productId 商品ID
     * @param count 数量
     * @return 是否成功
     */
    boolean restoreStock(Long productId, Integer count);
}
