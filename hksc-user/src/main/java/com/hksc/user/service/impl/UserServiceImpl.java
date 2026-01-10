package com.hksc.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hksc.common.result.Result;
import com.hksc.common.utils.JwtUtils;
import com.hksc.user.dto.UserLoginDTO;
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
    public Result<Map<String, String>> login(UserLoginDTO dto) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getPhone, dto.getPhone()));
        if (user == null) {
            return Result.error("用户不存在");
        }

        String inputPwd = DigestUtils.md5DigestAsHex(dto.getPassword().getBytes(StandardCharsets.UTF_8));
        if (!inputPwd.equals(user.getPassword())) {
            return Result.error("密码错误");
        }

        // 生成Access Token和Refresh Token
        String accessToken = JwtUtils.createToken(user.getId(), user.getPhone());
        String refreshToken = JwtUtils.createRefreshToken(user.getId(), user.getPhone());

        // 存储Access Token（15分钟）
        stringRedisTemplate.opsForValue().set(
            "user:token:" + user.getId(),
            accessToken,
            15,
            TimeUnit.MINUTES
        );

        // 存储Refresh Token（7天）
        stringRedisTemplate.opsForValue().set(
            "user:refresh:" + user.getId(),
            refreshToken,
            7,
            TimeUnit.DAYS
        );

        Map<String, String> map = new HashMap<>();
        map.put("accessToken", accessToken);
        map.put("refreshToken", refreshToken);
        map.put("nickname", user.getNickname());

        return Result.success(map);
    }

    @Override
    public Result<String> logout(Long userId) {
        // 企业标准：清除 Redis 中的 Token，实现强制下线
        stringRedisTemplate.delete("user:token:" + userId);
        stringRedisTemplate.delete("user:refresh:" + userId);
        return Result.success("退出登录成功");
    }

    /**
     * 刷新Token
     */
    @Override
    public Result<Map<String, String>> refreshToken(String refreshToken) {
        // 1. 验证Refresh Token
        try {
            JwtUtils.validateToken(refreshToken);
        } catch (Exception e) {
            return Result.error("Refresh Token无效或已过期");
        }

        // 2. 检查Token类型
        String type = JwtUtils.getTokenType(refreshToken);
        if (!"refresh".equals(type)) {
            return Result.error("Token类型错误，请使用Refresh Token");
        }

        // 3. 获取用户ID
        Long userId = JwtUtils.getUserId(refreshToken);
        if (userId == null) {
            return Result.error("无法解析用户ID");
        }

        // 4. 验证Redis中的Refresh Token是否一致
        String cachedRefreshToken = stringRedisTemplate.opsForValue().get("user:refresh:" + userId);
        if (!refreshToken.equals(cachedRefreshToken)) {
            return Result.error("Refresh Token已失效或在其他设备登录");
        }

        // 5. 查询用户信息
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }

        // 6. 生成新的Access Token
        String newAccessToken = JwtUtils.createToken(user.getId(), user.getPhone());

        // 7. 更新Redis中的Access Token
        stringRedisTemplate.opsForValue().set(
            "user:token:" + user.getId(),
            newAccessToken,
            15,
            TimeUnit.MINUTES
        );

        Map<String, String> map = new HashMap<>();
        map.put("accessToken", newAccessToken);
        map.put("refreshToken", refreshToken);  // Refresh Token不变

        return Result.success(map);
    }

    /**
     * 获取用户信息
     */
    @Override
    public Result<Map<String, Object>> getUserInfo(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }

        Map<String, Object> info = new HashMap<>();
        info.put("userId", user.getId());
        info.put("phone", user.getPhone());
        info.put("nickname", user.getNickname());
        info.put("createTime", user.getCreateTime());

        return Result.success(info);
    }
}