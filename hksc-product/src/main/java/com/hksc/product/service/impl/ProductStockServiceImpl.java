package com.hksc.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hksc.product.entity.ProductStock;
import com.hksc.product.entity.StockLog;
import com.hksc.product.mapper.ProductStockMapper;
import com.hksc.product.mapper.StockLogMapper;
import com.hksc.product.service.ProductStockService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 商品库存Service实现
 */
@Slf4j
@Service
public class ProductStockServiceImpl extends ServiceImpl<ProductStockMapper, ProductStock> implements ProductStockService {

    @Resource
    private ProductStockMapper productStockMapper;

    @Resource
    private StockLogMapper stockLogMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean lockStock(Long productId, Integer count) {
        int rows = productStockMapper.lockStock(productId, count);
        if (rows > 0) {
            // 记录库存日志
            ProductStock stock = getByProductId(productId);
            saveStockLog(productId, null, 2, -count, stock.getAvailableStock() + count, stock.getAvailableStock(), "锁定库存");
            return true;
        }
        return false;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean unlockStock(Long productId, Integer count) {
        int rows = productStockMapper.unlockStock(productId, count);
        if (rows > 0) {
            ProductStock stock = getByProductId(productId);
            saveStockLog(productId, null, 4, count, stock.getAvailableStock() - count, stock.getAvailableStock(), "释放锁定库存");
            return true;
        }
        return false;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean confirmReduceStock(Long productId, Integer count) {
        int rows = productStockMapper.confirmReduceStock(productId, count);
        if (rows > 0) {
            ProductStock stock = getByProductId(productId);
            saveStockLog(productId, null, 3, -count, stock.getTotalStock() + count, stock.getTotalStock(), "确认扣减库存");
            return true;
        }
        return false;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean reduceStockWithOptimisticLock(Long productId, Integer count) {
        // 最多重试3次
        int maxRetries = 3;
        for (int i = 0; i < maxRetries; i++) {
            ProductStock stock = getByProductId(productId);
            if (stock == null) {
                throw new RuntimeException("商品库存不存在");
            }
            if (stock.getAvailableStock() < count) {
                throw new RuntimeException("库存不足");
            }

            int rows = productStockMapper.reduceStockWithVersion(productId, count, stock.getVersion());
            if (rows > 0) {
                // 扣减成功，记录日志
                saveStockLog(productId, null, 3, -count, stock.getAvailableStock(), stock.getAvailableStock() - count, "乐观锁扣减库存");
                return true;
            }
            // 版本号不匹配，重试
            log.warn("乐观锁扣减库存失败，正在重试... 第{}次", i + 1);
        }
        throw new RuntimeException("库存扣减失败，请重试");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean restoreStock(Long productId, Integer count) {
        ProductStock stock = getByProductId(productId);
        if (stock != null) {
            stock.setTotalStock(stock.getTotalStock() + count);
            stock.setAvailableStock(stock.getAvailableStock() + count);
            productStockMapper.updateById(stock);
            saveStockLog(productId, null, 5, count, stock.getAvailableStock() - count, stock.getAvailableStock(), "退货恢复库存");
            return true;
        }
        return false;
    }

    /**
     * 根据商品ID查询库存
     */
    private ProductStock getByProductId(Long productId) {
        LambdaQueryWrapper<ProductStock> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductStock::getProductId, productId);
        return productStockMapper.selectOne(wrapper);
    }

    /**
     * 保存库存变更日志
     */
    private void saveStockLog(Long productId, String orderSn, Integer changeType, Integer changeAmount,
                               Integer beforeStock, Integer afterStock, String remark) {
        StockLog log = new StockLog();
        log.setProductId(productId);
        log.setOrderSn(orderSn);
        log.setChangeType(changeType);
        log.setChangeAmount(changeAmount);
        log.setBeforeStock(beforeStock);
        log.setAfterStock(afterStock);
        log.setRemark(remark);
        log.setCreateTime(LocalDateTime.now());
        stockLogMapper.insert(log);
    }
}
