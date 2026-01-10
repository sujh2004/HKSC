package com.hksc.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hksc.product.dto.ProductDTO;
import com.hksc.product.dto.ProductQueryDTO;
import com.hksc.product.entity.Category;
import com.hksc.product.entity.Product;
import com.hksc.product.entity.ProductStock;
import com.hksc.product.entity.SeckillActivity;
import com.hksc.product.mapper.CategoryMapper;
import com.hksc.product.mapper.ProductMapper;
import com.hksc.product.mapper.ProductStockMapper;
import com.hksc.product.mapper.SeckillActivityMapper;
import com.hksc.product.service.ProductService;
import com.hksc.product.vo.ProductDetailVO;
import com.hksc.product.vo.ProductVO;
import jakarta.annotation.Resource;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class ProductImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private SeckillActivityMapper seckillActivityMapper;

    @Resource
    private ProductStockMapper productStockMapper;

    @Resource
    private CategoryMapper categoryMapper;

    @Lazy
    @Resource
    private ProductService selfProxy;

    @Override
    public boolean doDeduction(Long productId, Integer count) {
        String lockKey = "lock:product:" + productId;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            boolean isLocked = lock.tryLock(5, 10, TimeUnit.SECONDS);
            if (!isLocked) {
                throw new RuntimeException("系统繁忙，请稍后再试");
            }

            selfProxy.deductStockInternal(productId, count);
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("系统异常", e);
        } catch (RuntimeException e) {
            throw e;
        } finally {
            if (lock != null && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deductStockInternal(Long productId, Integer count) {
        // 检查商品是否存在
        Product product = baseMapper.selectById(productId);
        if (product == null) {
            throw new RuntimeException("商品不存在");
        }

        // 从 product_stock 表扣减库存
        ProductStock productStock = productStockMapper.selectOne(
            new LambdaQueryWrapper<ProductStock>().eq(ProductStock::getProductId, productId)
        );

        if (productStock == null) {
            throw new RuntimeException("库存记录不存在");
        }

        if (productStock.getAvailableStock() < count) {
            throw new RuntimeException("库存不足");
        }

        // 使用乐观锁扣减库存
        boolean success = productStockMapper.update(null,
            new com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper<ProductStock>()
                .setSql("available_stock = available_stock - " + count)
                .eq("product_id", productId)
                .ge("available_stock", count)
                .eq("version", productStock.getVersion())
        ) > 0;

        if (!success) {
            throw new RuntimeException("库存扣减失败，请重试");
        }
    }

    @Override
    public boolean restoreStock(Long productId, Integer count) {
        Product product = baseMapper.selectById(productId);
        if (product != null) {
            product.setStock(product.getStock() + count);
            baseMapper.updateById(product);
            return true;
        }
        return false;
    }

    @Override
    public ProductDetailVO getProductDetail(Long id) {
        // 查询商品基本信息
        Product product = baseMapper.selectById(id);
        if (product == null) {
            return null;
        }

        // 转换为VO
        ProductDetailVO vo = com.hksc.product.converter.ProductConverter.toDetailVO(product);

        // 查询库存
        ProductStock productStock = productStockMapper.selectOne(
            new LambdaQueryWrapper<ProductStock>().eq(ProductStock::getProductId, id)
        );
        if (productStock != null) {
            vo.setStock(productStock.getAvailableStock());
        }

        // 如果是秒杀商品，填充秒杀信息
        if (product.getIsSeckill() != null && product.getIsSeckill() == 1) {
            fillSeckillInfo(vo);
        }

        return vo;
    }

    @Override
    public Page<ProductVO> getProductListWithStock(ProductQueryDTO queryDTO) {
        // 基本查询逻辑
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getStatus, 1); // 只查询上架商品

        if (queryDTO.getKeyword() != null && !queryDTO.getKeyword().isEmpty()) {
            wrapper.like(Product::getTitle, queryDTO.getKeyword());
        }

        if (queryDTO.getCategoryId() != null) {
            // 查询该分类是否是父分类（一级分类）
            Category category = categoryMapper.selectById(queryDTO.getCategoryId());
            if (category != null && category.getParentId() == 0) {
                // 是一级分类，查询所有子分类
                List<Category> children = categoryMapper.selectList(
                    new LambdaQueryWrapper<Category>()
                        .eq(Category::getParentId, queryDTO.getCategoryId())
                );
                if (!children.isEmpty()) {
                    // 提取所有子分类ID
                    List<Long> childIds = children.stream()
                        .map(Category::getId)
                        .collect(Collectors.toList());
                    // 使用IN查询
                    wrapper.in(Product::getCategoryId, childIds);
                } else {
                    // 没有子分类，直接查询（虽然可能查不到）
                    wrapper.eq(Product::getCategoryId, queryDTO.getCategoryId());
                }
            } else {
                // 是二级分类，直接查询
                wrapper.eq(Product::getCategoryId, queryDTO.getCategoryId());
            }
        }

        Page<Product> page = new Page<>(
                queryDTO.getPage() != null ? queryDTO.getPage() : 1,
                queryDTO.getLimit() != null ? queryDTO.getLimit() : 10
        );

        Page<Product> productPage = baseMapper.selectPage(page, wrapper);

        // 转换为VO并填充库存和秒杀信息
        Page<ProductVO> voPage = new Page<>();
        List<ProductVO> voList = new ArrayList<>();

        for (Product product : productPage.getRecords()) {
            ProductVO vo = com.hksc.product.converter.ProductConverter.toVO(product);

            // 查询库存
            ProductStock productStock = productStockMapper.selectOne(
                new LambdaQueryWrapper<ProductStock>().eq(ProductStock::getProductId, product.getId())
            );
            if (productStock != null) {
                vo.setStock(productStock.getAvailableStock());
            } else {
                vo.setStock(0); // 如果没有库存记录，设为0
            }

            // 如果是秒杀商品，填充秒杀信息
            if (product.getIsSeckill() != null && product.getIsSeckill() == 1) {
                fillSeckillInfoForVO(vo);
            }

            voList.add(vo);
        }

        voPage.setRecords(voList);
        voPage.setTotal(productPage.getTotal());
        voPage.setCurrent(productPage.getCurrent());
        voPage.setSize(productPage.getSize());

        return voPage;
    }

    @Override
    public ProductDTO getProductDTO(Long id) {
        Product product = baseMapper.selectById(id);
        if (product == null) {
            return null;
        }

        ProductDTO dto = com.hksc.product.converter.ProductConverter.toDTO(product);

        // 查询库存
        ProductStock productStock = productStockMapper.selectOne(
            new LambdaQueryWrapper<ProductStock>().eq(ProductStock::getProductId, id)
        );
        if (productStock != null) {
            dto.setStock(productStock.getAvailableStock());
        } else {
            dto.setStock(0);
        }

        return dto;
    }

    /**
     * 填充秒杀信息
     */
    private void fillSeckillInfo(ProductDetailVO vo) {
        if (vo == null || vo.getIsSeckill() == null || vo.getIsSeckill() != 1) {
            return;
        }

        // 查询进行中的秒杀活动
        LambdaQueryWrapper<SeckillActivity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SeckillActivity::getProductId, vo.getId());
        wrapper.eq(SeckillActivity::getStatus, 1); // 进行中

        SeckillActivity activity = seckillActivityMapper.selectOne(wrapper);

        if (activity != null) {
            vo.setSeckillPrice(activity.getSeckillPrice());

            // ✅ 从Redis读取实时库存（使用StringRedisTemplate读取字符串格式的数据）
            try {
                String stockKey = "seckill:stock:" + vo.getId();
                String stockStr = stringRedisTemplate.opsForValue().get(stockKey);

                if (stockStr != null && !stockStr.isEmpty()) {
                    vo.setSeckillStock(Integer.parseInt(stockStr));
                } else {
                    // 如果Redis中没有，使用数据库中的初始值（可能是活动还未开始）
                    vo.setSeckillStock(activity.getSeckillStock());
                }
            } catch (Exception e) {
                // Redis读取失败时，使用数据库中的初始值
                vo.setSeckillStock(activity.getSeckillStock());
            }

            vo.setSeckillLimit(activity.getSeckillLimit());
            vo.setSeckillStartTime(activity.getStartTime());
            vo.setSeckillEndTime(activity.getEndTime());
            vo.setSeckillStatus(activity.getStatus());

            // 设置状态文本
            switch (activity.getStatus()) {
                case 0:
                    vo.setSeckillStatusText("未开始");
                    break;
                case 1:
                    vo.setSeckillStatusText("进行中");
                    break;
                case 2:
                    vo.setSeckillStatusText("已结束");
                    break;
                default:
                    vo.setSeckillStatusText("未知");
            }
        }
    }

    /**
     * 填充秒杀信息（用于ProductVO）
     */
    private void fillSeckillInfoForVO(ProductVO vo) {
        if (vo == null || vo.getIsSeckill() == null || vo.getIsSeckill() != 1) {
            return;
        }

        // 查询进行中的秒杀活动
        LambdaQueryWrapper<SeckillActivity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SeckillActivity::getProductId, vo.getId());
        wrapper.eq(SeckillActivity::getStatus, 1); // 进行中

        SeckillActivity activity = seckillActivityMapper.selectOne(wrapper);

        if (activity != null) {
            vo.setSeckillPrice(activity.getSeckillPrice());

            // ✅ 从Redis读取实时库存（使用StringRedisTemplate读取字符串格式的数据）
            try {
                String stockKey = "seckill:stock:" + vo.getId();
                String stockStr = stringRedisTemplate.opsForValue().get(stockKey);

                if (stockStr != null && !stockStr.isEmpty()) {
                    vo.setSeckillStock(Integer.parseInt(stockStr));
                } else {
                    // 如果Redis中没有，使用数据库中的初始值（可能是活动还未开始）
                    vo.setSeckillStock(activity.getSeckillStock());
                }
            } catch (Exception e) {
                // Redis读取失败时，使用数据库中的初始值
                vo.setSeckillStock(activity.getSeckillStock());
            }

            vo.setSeckillLimit(activity.getSeckillLimit());
            vo.setSeckillStartTime(activity.getStartTime());
            vo.setSeckillEndTime(activity.getEndTime());
            vo.setSeckillStatus(activity.getStatus());

            // 设置状态文本
            switch (activity.getStatus()) {
                case 0:
                    vo.setSeckillStatusText("未开始");
                    break;
                case 1:
                    vo.setSeckillStatusText("进行中");
                    break;
                case 2:
                    vo.setSeckillStatusText("已结束");
                    break;
                default:
                    vo.setSeckillStatusText("未知");
            }
        }
    }
}
