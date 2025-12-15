package com.hksc.user.service;

import com.hksc.common.result.Result;
import com.hksc.user.dto.UserLoginDTO;
import com.hksc.user.dto.UserRegisterDTO;
import java.util.Map;

/**
 * 用户服务接口
 * 只定义"做什么"，不定义"怎么做"
 */
public interface UserService {

    /**
     * 发送验证码
     */
    Result<String> sendCode(String phone);

    /**
     * 用户注册
     */
    Result<String> register(UserRegisterDTO dto);

    /**
     * 用户登录
     */
    Result<Map<String, String>> login(UserLoginDTO dto);
}