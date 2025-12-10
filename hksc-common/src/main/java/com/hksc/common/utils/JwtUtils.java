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

    private static final long EXPIRE = 1000 * 60 * 60 * 24; // 24小时

    /**
     * 生成 Token
     */
    public static String createToken(Long userId, String username) {
        return Jwts.builder()
                .setSubject(username)
                .claim("userId", userId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRE))
                .signWith(KEY, SignatureAlgorithm.HS256) // 注意写法变化
                .compact();
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
}