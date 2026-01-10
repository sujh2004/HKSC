package com.hksc.user.controller;

import com.hksc.common.result.Result;
import com.hksc.user.dto.UserLoginDTO;
import com.hksc.user.dto.UserRegisterDTO;
import com.hksc.user.service.UserService; // 引入接口
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService; // 注入接口，Spring 会自动找到 UserServiceImpl

    /**
     * 1. 发送验证码
     * 请求路径: POST /user/code?phone=138xxxx
     */
    @PostMapping("/code")
    public Result<String> sendCode(@RequestParam String phone) {
        return userService.sendCode(phone);
    }

    /**
     * 2. 注册
     * 请求路径: POST /user/register
     */
    @PostMapping("/register")
    public Result<String> register(@RequestBody UserRegisterDTO dto) {
        return userService.register(dto);
    }

    /**
     * 3. 登录
     * 请求路径: POST /user/login?phone=...&password=...
     */
    @PostMapping("/login")
    public Result<Map<String, String>> login(@RequestBody UserLoginDTO dto) { // 必须加 @RequestBody
        return userService.login(dto);
    }

    /**
     * 4. 退出登录
     * 请求路径: POST /user/logout
     * 企业标准：从网关传递的 X-User-Id 获取用户ID
     */
    @PostMapping("/logout")
    public Result<String> logout(@RequestHeader("X-User-Id") Long userId) {
        return userService.logout(userId);
    }

    /**
     * 5. 刷新Token
     * 请求路径: POST /user/refresh
     * 请求头: Refresh-Token: xxx
     */
    @PostMapping("/refresh")
    public Result<Map<String, String>> refresh(@RequestHeader("Refresh-Token") String refreshToken) {
        return userService.refreshToken(refreshToken);
    }

    /**
     * 6. 获取用户信息
     * 请求路径: GET /user/info
     * 企业标准：从网关传递的 X-User-Id 获取用户ID
     */
    @GetMapping("/info")
    public Result<Map<String, Object>> getUserInfo(@RequestHeader("X-User-Id") Long userId) {
        return userService.getUserInfo(userId);
    }
}