package com.hksc.user.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 登录响应视图对象
 */
@Data
public class LoginVO implements Serializable {

    /**
     * 访问令牌
     */
    private String token;

    /**
     * 刷新令牌
     */
    private String refreshToken;

    /**
     * 用户信息
     */
    private UserVO userInfo;
}
