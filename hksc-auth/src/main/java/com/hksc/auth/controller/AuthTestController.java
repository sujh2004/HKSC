package com.hksc.auth.controller;

import com.hksc.common.utils.JwtUtils; // 引用 common 模块的工具类
import com.hksc.common.result.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证服务测试与验收接口
 */
@RestController
@RequestMapping("/auth/test")
public class AuthTestController {

    // 1. 移除 @Autowired 注入，因为 JwtUtils 是静态方法
    // @Autowired
    // private JwtUtils jwtUtils;

    /**
     * Day 2 验收点：Auth 服务能正常颁发 Token
     * 访问路径 (通过 Gateway)：http://127.0.0.1:8080/api/auth/test/token
     */
    @GetMapping("/token")
    public Result<Map<String, Object>> issueToken() {

        // 1. 模拟用户登录成功的关键信息
        Long userId = 10086L;
        String userName = "AuthTestUser";

        // 2. 直接通过类名调用静态方法！ (将 generateToken 改为你的 createToken)
        String token = JwtUtils.createToken(userId, userName);

        // 3. 封装返回结果
        Map<String, Object> tokenInfo = new HashMap<>();
        tokenInfo.put("userId", userId);
        tokenInfo.put("userName", userName);
        tokenInfo.put("token", token);

        return Result.success(tokenInfo);
    }
}