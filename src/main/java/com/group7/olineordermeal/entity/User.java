package com.group7.olineordermeal.entity;

import lombok.Data;
import java.time.LocalDateTime;

    /**
     * 用户实体，对应数据库表 tb_user
     */
    @Data  // Lombok 自动生成 getter/setter/toString/equals/hashCode
    public class User {
        private Long id;                // 用户ID，自增主键
        private String username;        // 登录用户名，唯一
        private String password;        // 加密后的密码
        private String phone;           // 手机号
        private String status;          // ACTIVE / BANNED
        private String role;            // USER / ADMIN（如果组长加了该字段）
        private LocalDateTime createTime;
        private LocalDateTime updateTime;
    }

