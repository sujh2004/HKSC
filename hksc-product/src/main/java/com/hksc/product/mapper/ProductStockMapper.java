package com.hksc.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hksc.product.entity.ProductStock;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 商品库存Mapper
 */
@Mapper
public interface ProductStockMapper extends BaseMapper<ProductStock> {

    /**
     * 使用乐观锁扣减库存
     * @param productId 商品ID
     * @param count 扣减数量
     * @param version 当前版本号
     * @return 影响行数（1表示成功，0表示失败）
     */
    @Update("UPDATE product_stock SET available_stock = available_stock - #{count}, " +
            "version = version + 1 WHERE product_id = #{productId} " +
            "AND available_stock >= #{count} AND version = #{version}")
    int reduceStockWithVersion(@Param("productId") Long productId,
                                @Param("count") Integer count,
                                @Param("version") Integer version);

    /**
     * 锁定库存（下单未支付）
     * @param productId 商品ID
     * @param count 锁定数量
     * @return 影响行数
     */
    @Update("UPDATE product_stock SET available_stock = available_stock - #{count}, " +
            "locked_stock = locked_stock + #{count} WHERE product_id = #{productId} " +
            "AND available_stock >= #{count}")
    int lockStock(@Param("productId") Long productId, @Param("count") Integer count);

    /**
     * 释放锁定的库存（订单取消）
     * @param productId 商品ID
     * @param count 释放数量
     * @return 影响行数
     */
    @Update("UPDATE product_stock SET available_stock = available_stock + #{count}, " +
            "locked_stock = locked_stock - #{count} WHERE product_id = #{productId}")
    int unlockStock(@Param("productId") Long productId, @Param("count") Integer count);

    /**
     * 确认扣减库存（支付成功）
     * @param productId 商品ID
     * @param count 扣减数量
     * @return 影响行数
     */
    @Update("UPDATE product_stock SET total_stock = total_stock - #{count}, " +
            "locked_stock = locked_stock - #{count} WHERE product_id = #{productId}")
    int confirmReduceStock(@Param("productId") Long productId, @Param("count") Integer count);
}
