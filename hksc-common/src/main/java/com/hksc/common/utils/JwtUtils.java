package com.hksc.common.utils;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

public class JwtUtils {

    // 1. 定义一个固定的字符串秘钥 (必须大于32个字符，否则报错)
    // 随便敲一串长的，或者用 UUID，确保 auth 和 gateway 用的是同一份代码，所以这个常量是一样的
    private static final String SECRET_STRING = "hksc-secure-key-2024-make-it-long-enough-for-sha256";

    // 2. 转换成 Key 对象
    private static final SecretKey KEY = Keys.hmacShaKeyFor(SECRET_STRING.getBytes(StandardCharsets.UTF_8));

    // Access Token：15分钟（短期，频繁过期，安全性高）
    private static final long ACCESS_TOKEN_EXPIRE = 1000 * 60 * 15;

    // Refresh Token：7天（长期，用于刷新Access Token）
    private static final long REFRESH_TOKEN_EXPIRE = 1000 * 60 * 60 * 24 * 7;

    /**
     * 生成 Access Token（短期）
     */
    public static String createToken(Long userId, String username) {
        return Jwts.builder()
                .setSubject(username)
                .claim("userId", userId)
                .claim("type", "access")  // 标记为Access Token
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRE))
                .signWith(KEY, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 生成 Refresh Token（长期）
     */
    public static String createRefreshToken(Long userId, String username) {
        return Jwts.builder()
                .setSubject(username)
                .claim("userId", userId)
                .claim("type", "refresh")  // 标记为Refresh Token
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRE))
                .signWith(KEY, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 验证Token类型
     */
    public static String getTokenType(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(KEY)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .get("type", String.class);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 这里简单写一下，能解析且不报错就是通过
     */
    public static void validateToken(String token) {
        Jwts.parserBuilder()
                .setSigningKey(KEY)
                .build()
                .parseClaimsJws(token);
    }
    public static Long getUserId(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(KEY)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .get("userId", Long.class); // 获取载荷中的 userId
        } catch (Exception e) {
            return null; // 解析失败
        }
    }
}