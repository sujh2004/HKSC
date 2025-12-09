package com.hksc.user.controller;

import com.hksc.common.result.Result;
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
    public Result<Map<String, String>> login(@RequestParam String phone, @RequestParam String password) {
        return userService.login(phone, password);
    }
}