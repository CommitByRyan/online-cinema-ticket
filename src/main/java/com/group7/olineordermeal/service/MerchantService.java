package com.group7.olineordermeal.service;

import com.group7.olineordermeal.dto.MerchantRegisterDTO;
import com.group7.olineordermeal.dto.MerchantUpdateDTO;
import com.group7.olineordermeal.entity.Merchant;
import com.group7.olineordermeal.entity.Merchant;
import java.util.List;
/**
 * 商家（Merchant）业务层接口
 */
public interface MerchantService {

    /**
     * 注册新商家（已经实现）
     */
    void register(MerchantRegisterDTO dto);

    /**
     * 商家登录（已经实现）
     */
    Merchant login(String username, String rawPassword);

    /**
     * 根据 ID 查询商家
     */
    Merchant getById(Long id);

    /**
     * 更新商家信息（已经实现）
     */
    void updateInfo(Long id, MerchantUpdateDTO dto);

    /**
     * 修改商家状态（BANNED / APPROVED / PENDING）
     */
    void updateStatus(Long id, String status);

    /**
     * ==============================
     * 新增：按状态查询商家列表
     * ==============================
     *
     * @param status 例如 "APPROVED"、"PENDING"、"BANNED"
     * @return 符合该状态的商家集合
     */
    List<Merchant> listByStatus(String status);
    /** 查询所有商家（管理员列表页面使用） */
    List<Merchant> listAll();
}