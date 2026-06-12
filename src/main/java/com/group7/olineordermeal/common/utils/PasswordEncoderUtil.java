package com.group7.olineordermeal.common.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码加密工具，使用 BCrypt 算法（安全性高）
 */
public class PasswordEncoderUtil {

    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    // 加密原始密码
    public static String encode(String rawPassword) {
        return encoder.encode(rawPassword);
    }

    // 校验原始密码和加密后的密码是否匹配
    public static boolean matches(String rawPassword, String encodedPassword) {
        return encoder.matches(rawPassword, encodedPassword);
    }

    // 测试用（组长可以运行 main 方法生成加密密码）
    public static void main(String[] args) {
        System.out.println(encode("123456"));  // 生成密文
    }
}
