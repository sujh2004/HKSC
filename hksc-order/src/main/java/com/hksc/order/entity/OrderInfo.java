package com.hksc.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data; // 👈 必须有
import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("order_info")
public class OrderInfo {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;


    private String orderSn;
    private BigDecimal totalAmount;
    private BigDecimal payAmount;
    private Integer status;
    private Date createTime;
    private String note;
}