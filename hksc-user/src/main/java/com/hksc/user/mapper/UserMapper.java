package com.hksc.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hksc.user.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    // 只要继承了 BaseMapper，基础的 CRUD 就都有了
}