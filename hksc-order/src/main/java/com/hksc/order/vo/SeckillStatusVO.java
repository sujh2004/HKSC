package com.hksc.order.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 秒杀状态视图对象
 */
@Data
public class SeckillStatusVO implements Serializable {

    /**
     * 状态码：200成功，201失败，202排队中
     */
    private Integer code;

    /**
     * 状态消息
     */
    private String message;

    /**
     * 订单ID（抢购成功时返回）
     */
    private String orderId;
}
