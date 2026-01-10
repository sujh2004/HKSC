package com.hksc.gateway.filter;

import com.hksc.common.utils.JwtUtils;
import jakarta.annotation.Resource;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.MediaType;
import java.nio.charset.StandardCharsets;

import java.util.List;

/**
 * 网关全局鉴权过滤器
 * 作用：拦截所有请求，校验 Token
 * 企业标准：JWT签名验证 + Redis状态验证（双重验证）
 */
@Component
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    private final AntPathMatcher antPathMatcher = new AntPathMatcher();

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // 1. 白名单放行 (登录、注册、验证码接口不需要 Token)
        if (isWhiteList(path)) {
            return chain.filter(exchange);
        }

        // 2. 获取 Token (约定放在 Header 的 "token" 字段中)
        String token = request.getHeaders().getFirst("token");

        // 3. 校验 Token 是否存在
        if (token == null || token.isEmpty()) {
            return out(exchange, HttpStatus.UNAUTHORIZED, "Token缺失");
        }

        try {
            // 4. 验证 JWT 签名和过期时间
            JwtUtils.validateToken(token);

            // 5. 从 JWT 中提取 userId
            Long userId = JwtUtils.getUserId(token);
            if (userId == null) {
                return out(exchange, HttpStatus.UNAUTHORIZED, "Token无效");
            }

            // 6. 企业标准：验证 Redis 中的 Token（防止已注销的Token仍被使用）
            String cachedToken = stringRedisTemplate.opsForValue().get("user:token:" + userId);
            if (cachedToken == null || !cachedToken.equals(token)) {
                return out(exchange, HttpStatus.UNAUTHORIZED, "Token已失效或已在其他设备登录");
            }

            // 7. 将 userId 传递给下游服务（避免下游服务重复解析JWT）
            ServerHttpRequest newRequest = request.mutate()
                    .header("X-User-Id", userId.toString())
                    .build();

            // 8. 校验通过，放行
            return chain.filter(exchange.mutate().request(newRequest).build());

        } catch (Exception e) {
            return out(exchange, HttpStatus.UNAUTHORIZED, "Token验证失败");
        }
    }

    // 定义白名单
    private boolean isWhiteList(String path) {
        List<String> whiteList = List.of(
                "/api/user/login",
                "/api/user/register",
                "/api/user/code",
                "/api/product/list", // (可选) 如果你想让商品列表不登录也能看，就加这里
                "/api/auth/**"
        );
        for (String pattern : whiteList) {
            if (antPathMatcher.match(pattern, path)) {
                return true;
            }
        }
        return false;
    }

    // 返回错误响应
    private Mono<Void> out(ServerWebExchange exchange, HttpStatus status, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        // 构造一个 JSON 字符串
        String jsonMessage = "{\"code\": " + status.value() + ", \"message\": \"" + message + "\", \"data\": null}";
        byte[] bytes = jsonMessage.getBytes(StandardCharsets.UTF_8);

        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        // 把 JSON 写回给前端
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return 0; // 优先级，越小越先执行
    }
}