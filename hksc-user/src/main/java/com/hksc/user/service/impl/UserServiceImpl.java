package com.hksc.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hksc.common.result.Result;
import com.hksc.common.utils.JwtUtils;
import com.hksc.user.dto.UserRegisterDTO;
import com.hksc.user.entity.User;
import com.hksc.user.mapper.UserMapper;
import com.hksc.user.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl implements UserService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result<String> sendCode(String phone) {
        String code = String.valueOf((int)((Math.random() * 9 + 1) * 100000));

        stringRedisTemplate.opsForValue().set("code:" + phone, code, 5, TimeUnit.MINUTES);

        System.out.println("【模拟短信】给手机 " + phone + " 发送验证码: " + code);
        return Result.success("验证码已发送(请看控制台): " + code);
    }

    @Override
    public Result<String> register(UserRegisterDTO dto) {
        String cacheCode = stringRedisTemplate.opsForValue().get("code:" + dto.getPhone());
        if (!StringUtils.hasText(cacheCode) || !cacheCode.equals(dto.getCode())) {
            return Result.error("验证码错误或已失效");
        }

        Long count = userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getPhone, dto.getPhone()));
        if (count > 0) {
            return Result.error("该手机号已注册");
        }

        User user = new User();
        user.setPhone(dto.getPhone());
        String encryptedPwd = DigestUtils.md5DigestAsHex(dto.getPassword().getBytes(StandardCharsets.UTF_8));
        user.setPassword(encryptedPwd);
        user.setNickname("用户" + dto.getPhone().substring(7));

        userMapper.insert(user);

        stringRedisTemplate.delete("code:" + dto.getPhone());

        return Result.success("注册成功");
    }

    @Override
    public Result<Map<String, String>> login(String phone, String password) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getPhone, phone));
        if (user == null) {
            return Result.error("用户不存在");
        }

        String inputPwd = DigestUtils.md5DigestAsHex(password.getBytes(StandardCharsets.UTF_8));
        if (!inputPwd.equals(user.getPassword())) {
            return Result.error("密码错误");
        }

        String token = JwtUtils.createToken(user.getId(), user.getPhone());

        stringRedisTemplate.opsForValue().set("login:token:" + token, user.getId().toString(), 24, TimeUnit.HOURS);

        Map<String, String> map = new HashMap<>();
        map.put("token", token);
        map.put("nickname", user.getNickname());

        return Result.success(map);
    }
}