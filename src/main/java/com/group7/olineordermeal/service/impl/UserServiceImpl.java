package com.group7.olineordermeal.service.impl;

import com.group7.olineordermeal.common.exception.BusinessException;
import com.group7.olineordermeal.common.utils.PasswordEncoderUtil;
import com.group7.olineordermeal.dto.RegisterDTO;
import com.group7.olineordermeal.dto.PasswordUpdateDTO;
import com.group7.olineordermeal.entity.User;
import com.group7.olineordermeal.mapper.UserMapper;
import com.group7.olineordermeal.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

    @Service
    public class UserServiceImpl implements UserService {

        @Autowired
        private UserMapper userMapper;

        @Override
        @Transactional
        public User register(RegisterDTO dto) {
            // 1. 检查用户名是否已存在
            if (userMapper.selectByUsername(dto.getUsername()) != null) {
                throw new BusinessException("用户名已存在");
            }
            // 2. 检查手机号是否已存在（可选，根据需求）
            if (userMapper.selectByPhone(dto.getPhone()) != null) {
                throw new BusinessException("手机号已被注册");
            }
            // 3. 创建用户对象
            User user = new User();
            user.setUsername(dto.getUsername());
            // 密码加密
            user.setPassword(PasswordEncoderUtil.encode(dto.getPassword()));
            user.setPhone(dto.getPhone());
            user.setStatus("ACTIVE");
            user.setRole("USER");
            // 4. 插入数据库
            userMapper.insert(user);
            return user;
        }

        @Override
        public User login(String loginName, String rawPassword) {
            // 1. 先按用户名查询
            User user = userMapper.selectByUsername(loginName);
            // 2. 如果查不到，再按手机号查询
            if (user == null) {
                user = userMapper.selectByPhone(loginName);
            }
            // 3. 用户不存在
            if (user == null) {
                return null;  // 让 Controller 决定跳转到注册页
            }
            // 4. 密码校验
            if (!PasswordEncoderUtil.matches(rawPassword, user.getPassword())) {
                throw new BusinessException("密码错误");
            }
            // 5. 检查账号状态
            if (!"ACTIVE".equals(user.getStatus())) {
                throw new BusinessException("账号已被封禁");
            }
            return user;
        }

        @Override
        public void updatePassword(Long userId, String oldPassword, String newPassword) {
            User user = userMapper.selectById(userId);
            if (user == null) {
                throw new BusinessException("用户不存在");
            }
            if (!PasswordEncoderUtil.matches(oldPassword, user.getPassword())) {
                throw new BusinessException("原密码错误");
            }
            user.setPassword(PasswordEncoderUtil.encode(newPassword));
            userMapper.updateById(user);
        }

        @Override
        public void updatePhone(Long userId, String newPhone) {
            // 检查新手机号是否已被其他用户占用
            User exist = userMapper.selectByPhone(newPhone);
            if (exist != null && !exist.getId().equals(userId)) {
                throw new BusinessException("手机号已被其他用户绑定");
            }
            User user = userMapper.selectById(userId);
            user.setPhone(newPhone);
            userMapper.updateById(user);
        }

        @Override
        public User getUserById(Long userId) {
            return userMapper.selectById(userId);
        }
    }
