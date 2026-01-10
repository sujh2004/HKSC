package com.hksc.order.feign.fallback;

import com.hksc.common.result.Result;
import com.hksc.order.feign.CartClient; // 稍后创建这个接口
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 购物车服务的“备胎”
 * 当远程调用失败时，会进入这里
 */
@Slf4j
@Component // 👈 必须加，交给 Spring 管理
public class CartClientFallback implements CartClient {

    @Override
    public Result<Boolean> deleteChecked(Long userId,List<Long> skuIds) {
        // 记录日志，方便以后排查
        log.error("⚠️ 购物车服务不可用，触发熔断降级。跳过清理购物车步骤，不影响下单。");

        // 返回“假成功”，骗过 OrderService
        return Result.success(true);
    }

    @Override
    public String clearCart() {
        // 当购物车服务挂了（熔断）时，订单服务会收到这个返回值
        return "⛔ [熔断降级] 购物车服务连接失败，清理操作已跳过";
    }
}