package com.hksc.order.converter;

import com.hksc.order.entity.OrderInfo;
import com.hksc.order.entity.OrderItem;
import com.hksc.order.vo.OrderDetailVO;
import com.hksc.order.vo.OrderItemVO;
import com.hksc.order.vo.OrderVO;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 订单对象转换工具类
 * Entity <-> DTO <-> VO
 */
public class OrderConverter {

    // ===================== OrderInfo Entity -> VO ======================

    /**
     * Entity -> OrderVO（订单列表项）
     */
    public static OrderVO toVO(OrderInfo entity) {
        if (entity == null) {
            return null;
        }
        OrderVO vo = new OrderVO();
        vo.setId(entity.getId());
        vo.setOrderSn(entity.getOrderSn());
        vo.setUserId(entity.getUserId());
        vo.setOrderType(entity.getOrderType());
        vo.setOrderTypeText(getOrderTypeText(entity.getOrderType()));
        vo.setTotalAmount(entity.getTotalAmount());
        vo.setPayAmount(entity.getPayAmount());
        vo.setStatus(entity.getStatus());
        vo.setStatusText(getOrderStatusText(entity.getStatus()));
        vo.setCreateTime(entity.getCreateTime());
        return vo;
    }

    /**
     * Entity -> OrderDetailVO（订单详情）
     */
    public static OrderDetailVO toDetailVO(OrderInfo entity) {
        if (entity == null) {
            return null;
        }
        OrderDetailVO vo = new OrderDetailVO();
        vo.setId(entity.getId());
        vo.setOrderSn(entity.getOrderSn());
        vo.setUserId(entity.getUserId());
        vo.setOrderType(entity.getOrderType());
        vo.setOrderTypeText(getOrderTypeText(entity.getOrderType()));
        vo.setTotalAmount(entity.getTotalAmount());
        vo.setPayAmount(entity.getPayAmount());
        vo.setFreightAmount(entity.getFreightAmount());
        vo.setCouponAmount(entity.getCouponAmount());
        vo.setPaymentType(entity.getPaymentType());
        vo.setPaymentTypeText(getPaymentTypeText(entity.getPaymentType()));
        vo.setStatus(entity.getStatus());
        vo.setStatusText(getOrderStatusText(entity.getStatus()));
        vo.setDeliveryInfo(entity.getDeliveryInfo());
        vo.setNote(entity.getNote());
        vo.setCancelReason(entity.getCancelReason());
        vo.setCreateTime(entity.getCreateTime());
        vo.setPayTime(entity.getPayTime());
        vo.setDeliveryTime(entity.getDeliveryTime());
        vo.setFinishTime(entity.getFinishTime());
        vo.setCancelTime(entity.getCancelTime());
        return vo;
    }

    /**
     * Entity List -> VO List
     */
    public static List<OrderVO> toVOList(List<OrderInfo> entities) {
        if (entities == null) {
            return null;
        }
        List<OrderVO> voList = new ArrayList<>();
        for (OrderInfo entity : entities) {
            voList.add(toVO(entity));
        }
        return voList;
    }

    // ===================== OrderItem Entity -> VO ======================

    /**
     * OrderItem Entity -> VO
     */
    public static OrderItemVO itemToVO(OrderItem entity) {
        if (entity == null) {
            return null;
        }
        OrderItemVO vo = new OrderItemVO();
        vo.setId(entity.getId());
        vo.setOrderId(entity.getOrderId());
        vo.setProductId(entity.getProductId());
        vo.setSeckillId(entity.getSeckillId());
        vo.setProductName(entity.getProductName());
        vo.setProductImage(entity.getProductImage());
        vo.setProductPrice(entity.getProductPrice());
        vo.setBuyCount(entity.getBuyCount());

        // 计算小计
        if (entity.getProductPrice() != null && entity.getBuyCount() != null) {
            vo.setSubtotal(entity.getProductPrice().multiply(BigDecimal.valueOf(entity.getBuyCount())));
        }

        return vo;
    }

    /**
     * OrderItem Entity List -> VO List
     */
    public static List<OrderItemVO> itemToVOList(List<OrderItem> entities) {
        if (entities == null) {
            return null;
        }
        List<OrderItemVO> voList = new ArrayList<>();
        for (OrderItem entity : entities) {
            voList.add(itemToVO(entity));
        }
        return voList;
    }

    // ===================== 辅助方法 ======================

    /**
     * 获取订单类型文本
     */
    private static String getOrderTypeText(Integer orderType) {
        if (orderType == null) {
            return "未知";
        }
        return orderType == 1 ? "普通订单" : orderType == 2 ? "秒杀订单" : "未知";
    }

    /**
     * 获取订单状态文本
     */
    private static String getOrderStatusText(Integer status) {
        if (status == null) {
            return "未知";
        }
        switch (status) {
            case 0:
                return "待付款";
            case 1:
                return "已付款";
            case 2:
                return "已发货";
            case 3:
                return "已完成";
            case 4:
                return "已取消";
            case 5:
                return "已关闭";
            default:
                return "未知";
        }
    }

    /**
     * 获取支付方式文本
     */
    private static String getPaymentTypeText(Integer paymentType) {
        if (paymentType == null) {
            return "未支付";
        }
        return paymentType == 1 ? "微信支付" : paymentType == 2 ? "支付宝" : "未知";
    }
}
