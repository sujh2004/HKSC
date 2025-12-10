package com.hksc.gateway.filter;

import com.hksc.common.utils.JwtUtils;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * 网关全局鉴权过滤器
 * 作用：拦截所有请求，校验 Token
 */
@Component
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    private final AntPathMatcher antPathMatcher = new AntPathMatcher();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // 1. 白名单放行 (登录、注册、验证码接口不需要 Token)
        if (isWhiteList(path)) {
            return chain.filter(exchange);
        }

        // 2. 获取 Token (约定放在 Header 的 "token" 字段中)
        // 也可以约定放在 "Authorization": "Bearer xxxx"
        String token = request.getHeaders().getFirst("token");

        // 3. 校验 Token
        if (token == null || token.isEmpty()) {
            return out(exchange, HttpStatus.UNAUTHORIZED); // 401
        }

        try {
            // 使用 Common 里的工具类校验，如果报错说明 Token 是假的或过期的
            JwtUtils.validateToken(token); // 昨天的 JwtUtils 里要补上这个方法，下面我会给代码
        } catch (Exception e) {
            return out(exchange, HttpStatus.UNAUTHORIZED); // 401
        }

        // 4. 校验通过，放行
        return chain.filter(exchange);
    }

    // 定义白名单
    private boolean isWhiteList(String path) {
        List<String> whiteList = List.of(
                "/api/user/login",
                "/api/user/register",
                "/api/user/code",
                "/api/product/list" // (可选) 如果你想让商品列表不登录也能看，就加这里
        );
        for (String pattern : whiteList) {
            if (antPathMatcher.match(pattern, path)) {
                return true;
            }
        }
        return false;
    }

    // 返回错误响应
    private Mono<Void> out(ServerWebExchange exchange, HttpStatus status) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        return response.setComplete();
    }

    @Override
    public int getOrder() {
        return 0; // 优先级，越小越先执行
    }
}