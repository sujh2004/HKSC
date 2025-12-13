package com.hksc.product.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hksc.product.entity.Product;
import com.hksc.product.mapper.ProductMapper;
import com.hksc.product.service.ProductService;
import jakarta.annotation.Resource;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Service
public class ProductImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

    // 声明 RedissonClient 字段
    // 通过 @Autowired 让 Spring 自动将 RedissonClient 实例注入进来
    @Resource
    private RedissonClient redissonClient;

    // 👇👇👇 加上 @Lazy，打破循环依赖 👇👇👇
    @Lazy
    @Resource
    private ProductService selfProxy;

    @Override
    public boolean doDeduction(Long productId, Integer count) {
        String lockKey = "lock:product:" + productId;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            // 2. 尝试加锁 (等待5秒，自动过期10秒)
            boolean isLocked = lock.tryLock(5, 10, TimeUnit.SECONDS);
            if (!isLocked) {
                // 加锁失败，直接返回或抛出系统繁忙异常
                throw new RuntimeException("系统繁忙，请稍后再试");
            }

            // 锁已获取，调用内部的事务方法执行数据库操作
            // 必须通过 selfProxy 调用，才能保证 @Transactional 生效
            selfProxy.deductStockInternal(productId, count);

            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // 重新设置中断标志
            throw new RuntimeException("系统异常", e);
        } catch (RuntimeException e) {
            // 捕获内部方法抛出的业务异常（如库存不足）
            throw e;
        } finally {
            // 4. 释放锁
            // 此时，内部方法已经执行完毕，如果成功，事务已经提交！
            // 此时释放锁，保证了锁释放时，数据在数据库中已经是最新状态。
            if (lock != null && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deductStockInternal(Long productId, Integer count) {
        // 3. 查库 -> 判断 -> 扣减
        Product product = baseMapper.selectById(productId);
        if (product == null) {
            // 抛出运行时异常，Spring 事务会自动标记回滚
            throw new RuntimeException("商品不存在");
        }
        if (product.getStock() < count) {
            throw new RuntimeException("库存不足");
        }

        product.setStock(product.getStock() - count);
        baseMapper.updateById(product);

        // 方法返回后，Spring 事务提交。
        // 提交成功后，外部方法才能继续执行 finally 释放锁。
    }

    @Override
    public boolean restoreStock(Long productId, Integer count) {
        Product product = baseMapper.selectById(productId);
        if (product != null) {
            // 简单粗暴：库存 + count
            product.setStock(product.getStock() + count);
            baseMapper.updateById(product);
            return true;
        }
        return false;
    }
}
