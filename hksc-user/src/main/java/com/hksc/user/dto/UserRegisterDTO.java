package com.hksc.user.dto;

import lombok.Data;

@Data
public class UserRegisterDTO {
    private String phone;
    private String password;
    private String code; // 短信验证码
}