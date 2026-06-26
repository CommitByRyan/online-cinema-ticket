package com.group7.olineordermeal.service;

import com.group7.olineordermeal.entity.User;
import com.group7.olineordermeal.dto.RegisterDTO;
import com.group7.olineordermeal.dto.PasswordUpdateDTO;

    public interface UserService {

        /**
         * 用户注册
         * @return 注册成功的用户对象
         */
        User register(RegisterDTO dto);

        /**
         * 用户登录验证
         * @param loginName 用户名或手机号
         * @param rawPassword 明文密码
         * @return 登录成功的用户对象，失败返回 null（由 Controller 处理具体错误）
         */
        User login(String loginName, String rawPassword);

        /**
         * 修改密码
         */
        void updatePassword(Long userId, String oldPassword, String newPassword);

        /**
         * 修改手机号（简单模拟，不涉及真实短信）
         */
        void updatePhone(Long userId, String newPhone);

        /**
         * 根据ID获取用户
         */
        User getUserById(Long userId);
    }
