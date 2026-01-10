package com.hksc.user.converter;

import com.hksc.user.entity.User;
import com.hksc.user.vo.UserVO;

/**
 * 用户对象转换工具类
 * Entity <-> DTO <-> VO
 */
public class UserConverter {

    /**
     * User Entity -> UserVO
     */
    public static UserVO toVO(User entity) {
        if (entity == null) {
            return null;
        }
        UserVO vo = new UserVO();
        vo.setId(entity.getId());
        vo.setPhone(entity.getPhone());
        vo.setNickname(entity.getNickname());
        vo.setAvatar(entity.getAvatar());
        vo.setStatus(entity.getStatus());
        vo.setStatusText(getStatusText(entity.getStatus()));
        vo.setCreateTime(entity.getCreateTime());
        return vo;
    }

    /**
     * 获取状态文本
     */
    private static String getStatusText(Integer status) {
        if (status == null) {
            return "未知";
        }
        return status == 1 ? "正常" : "禁用";
    }
}
