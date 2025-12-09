package com.hksc.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("user") // 对应数据库表名
public class User {

    @TableId(type = IdType.AUTO) // 对应数据库的自增主键
    private Long id;

    private String phone;

    private String password; // 存加密后的密码

    private String nickname;

    private String avatar;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}