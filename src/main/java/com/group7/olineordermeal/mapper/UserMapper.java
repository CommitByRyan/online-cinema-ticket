package com.group7.olineordermeal.mapper;

import com.group7.olineordermeal.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

    @Mapper
    public interface UserMapper {

        /**
         * 根据用户名查询用户（登录用）
         */
        User selectByUsername(@Param("username") String username);

        /**
         * 根据手机号查询用户（注册时检查手机号是否已存在）
         */
        User selectByPhone(@Param("phone") String phone);

        /**
         * 插入新用户
         */
        int insert(User user);

        /**
         * 更新用户信息（修改密码、手机号等）
         */
        int updateById(User user);

        /**
         * 根据ID查询用户
         */
        User selectById(@Param("id") Long id);
    }

