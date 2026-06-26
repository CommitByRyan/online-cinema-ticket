package com.group7.olineordermeal.mapper;

import com.group7.olineordermeal.entity.Merchant;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商家（Merchant）Mapper
 * ① 业务中已经使用的 CRUD、按状态查询等方法
 * ② 新增的 “查询全部商家” 方法（管理员列表页面使用）
 */
@Mapper
public interface MerchantMapper {

    /** 插入新商家（注册） */
    int insert(Merchant merchant);

    /** 根据主键 ID 查询 */
    Merchant selectById(Long id);

    /** 根据用户名查询（登录） */
    Merchant selectByUsername(String username);

    /** 根据状态查询（PENDING / APPROVED / BANNED） */
    List<Merchant> selectByStatus(@Param("status") String status);

    /** 更新（包括状态、店铺信息） */
    int updateById(Merchant merchant);

    // ------------------- 新增 -------------------
    /** 查询全部商家（管理员列表页面） */
    List<Merchant> selectAll();
}
