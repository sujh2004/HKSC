package com.hksc.user.dto;

import lombok.Data;

@Data
public class UserLoginDTO {

    /**
     * 手机号
     */
    private String phone;

    /**
     * 密码
     */
    private String password;
}