package com.hksc.user.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户信息视图对象
 */
@Data
public class UserVO implements Serializable {

    private Long id;

    private String phone;

    private String nickname;

    private String avatar;

    private Integer status;

    private String statusText;

    private LocalDateTime createTime;
}
