package com.hksc.gateway.config;

import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.BlockRequestHandler;
import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.GatewayCallbackManager;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerResponse;

import jakarta.annotation.PostConstruct;


@Configuration
public class GatewayConfiguration {

    @PostConstruct
    public void doInit() {
        // 自定义限流后的返回内容
        BlockRequestHandler blockRequestHandler = (serverWebExchange, throwable) -> {
            String msg = "{\"code\": 429, \"message\": \"服务器拥挤，请稍后再试 (Gateway限流)\"}";

            return ServerResponse.status(HttpStatus.TOO_MANY_REQUESTS)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(msg));
        };

        // 注册进去
        GatewayCallbackManager.setBlockHandler(blockRequestHandler);
    }
}