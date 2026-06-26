package com.group7.olineordermeal.service.impl;

import com.group7.olineordermeal.common.exception.BusinessException;
import com.group7.olineordermeal.common.utils.PasswordEncoderUtil;
import com.group7.olineordermeal.dto.MerchantRegisterDTO;
import com.group7.olineordermeal.dto.MerchantUpdateDTO;
import com.group7.olineordermeal.entity.Merchant;
import com.group7.olineordermeal.mapper.MerchantMapper;
import com.group7.olineordermeal.service.MerchantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MerchantServiceImpl implements MerchantService {

    @Autowired
    private MerchantMapper merchantMapper;

    // -------------------------------------------------
    // 已有的注册、登录、查询、更新等业务
    // -------------------------------------------------
    @Override
    public void register(MerchantRegisterDTO dto) {
        if (merchantMapper.selectByUsername(dto.getUsername()) != null) {
            throw new BusinessException("用户名已存在");
        }
        Merchant merchant = new Merchant();
        merchant.setUsername(dto.getUsername());
        merchant.setPassword(PasswordEncoderUtil.encode(dto.getPassword()));
        merchant.setMerchantName(dto.getMerchantName());
        merchant.setLegalPerson(dto.getLegalPerson());
        merchant.setPhone(dto.getPhone());
        merchant.setAddress(dto.getAddress());
        merchant.setBusinessLicense(dto.getBusinessLicense());
        merchant.setStatus("PENDING");   // 默认待审核
        merchant.setBusinessStatus("OPEN");
        merchantMapper.insert(merchant);
    }

    @Override
    public Merchant login(String username, String rawPassword) {
        Merchant merchant = merchantMapper.selectByUsername(username);
        if (merchant == null) {
            return null; // 调用方会跳转到注册页
        }
        if (!PasswordEncoderUtil.matches(rawPassword, merchant.getPassword())) {
            throw new BusinessException("密码错误");
        }
        if (!"APPROVED".equals(merchant.getStatus())) {
            throw new BusinessException("账号未审核通过或已被封禁");
        }
        return merchant;
    }

    @Override
    public Merchant getById(Long id) {
        return merchantMapper.selectById(id);
    }

    @Override
    public void updateInfo(Long id, MerchantUpdateDTO dto) {
        Merchant merchant = merchantMapper.selectById(id);
        if (merchant == null) {
            throw new BusinessException("商家不存在");
        }
        merchant.setMerchantName(dto.getMerchantName());
        merchant.setLegalPerson(dto.getLegalPerson());
        merchant.setPhone(dto.getPhone());
        merchant.setAddress(dto.getAddress());
        merchant.setBusinessLicense(dto.getBusinessLicense());
        merchant.setBusinessStatus(dto.getBusinessStatus());
        merchantMapper.updateById(merchant);
    }

    @Override
    public void updateStatus(Long id, String status) {
        Merchant merchant = merchantMapper.selectById(id);
        if (merchant != null) {
            merchant.setStatus(status);
            merchantMapper.updateById(merchant);
        }
    }

    // -------------------------------------------------
    // **新增实现**：按状态查询
    // -------------------------------------------------
    @Override
    public List<Merchant> listByStatus(String status) {
        // 直接交给 Mapper 完成查询
        return merchantMapper.selectByStatus(status);
    }
}
