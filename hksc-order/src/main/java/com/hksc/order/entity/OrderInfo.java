package com.hksc.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("order_info")
public class OrderInfo {

    /**
     * 订单ID (使用雪花算法生成)
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 订单总金额
     */
    private BigDecimal totalAmount;

    /**
     * 状态: 0-待付款, 1-已付款, 2-已发货, 3-已完成, 4-已取消
     */
    private Integer status;

    /**
     * 创建时间 (数据库会自动填充，Java侧一般只需读取)
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}