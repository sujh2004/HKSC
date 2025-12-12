package com.hksc.order.service;

import com.hksc.common.result.Result;
import com.hksc.order.dto.OrderCreateDTO;

public interface OrderService {

    /**
     * 创建订单
     * @param userId 用户ID
     * @param dto 下单参数
     * @return 订单ID
     */
    Result<String> createOrder(Long userId, OrderCreateDTO dto);
}