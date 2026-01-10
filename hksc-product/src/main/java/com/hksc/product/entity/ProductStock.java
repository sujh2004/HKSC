package com.hksc.product.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 商品库存表
 * 独立管理库存，解决高并发锁竞争问题
 */
@Data
@TableName("product_stock")
public class ProductStock implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 总库存
     */
    private Integer totalStock;

    /**
     * 可用库存（下单时扣减）
     */
    private Integer availableStock;

    /**
     * 锁定库存（下单未支付）
     */
    private Integer lockedStock;

    /**
     * 乐观锁版本号（防止超卖）
     */
    @Version
    private Integer version;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
