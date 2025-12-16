package com.hksc.product.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;

@Configuration
public class RedisScriptConfig {

    @Bean
    public DefaultRedisScript<Long> seckillScript() {
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        // 指向 resources/lua/seckill_stock.lua
        redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/seckill_stock.lua")));
        // 设置脚本的返回值类型 (我们在脚本里返回了 -1, 0, 1, 2，都是 Long)
        redisScript.setResultType(Long.class);
        return redisScript;
    }
}