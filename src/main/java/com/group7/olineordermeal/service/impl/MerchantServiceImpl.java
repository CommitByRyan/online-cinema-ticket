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

/**
 * 商家（Merchant）业务实现类
 *
 * ① 已有的注册、登录、查询、更新、状态修改已经实现
 * ② 下面额外实现了两项管理员需要的功能：
 *      • 按状态查询（listByStatus）   – 现有
 *      • 查询所有商家（listAll）      – 新增
 *
 * 请确保在 {@link MerchantMapper} 中已经声明了
 *   - List<Merchant> selectByStatus(String status);
 *   - List<Merchant> selectAll();
 *   并在对应的 XML 中实现 SQL（见步骤 2）。
 */
@Service
public class MerchantServiceImpl implements MerchantService {

    @Autowired
    private MerchantMapper merchantMapper;

    // -------------------------------------------------
    // 1️⃣ 注册
    // -------------------------------------------------
    @Override
    public void register(MerchantRegisterDTO dto) {
        // 1. 检查用户名是否已存在
        if (merchantMapper.selectByUsername(dto.getUsername()) != null) {
            throw new BusinessException("用户名已存在");
        }

        // 2. 组装实体并加密密码
        Merchant merchant = new Merchant();
        merchant.setUsername(dto.getUsername());
        merchant.setPassword(PasswordEncoderUtil.encode(dto.getPassword()));
        merchant.setMerchantName(dto.getMerchantName());
        merchant.setLegalPerson(dto.getLegalPerson());
        merchant.setPhone(dto.getPhone());
        merchant.setAddress(dto.getAddress());
        merchant.setBusinessLicense(dto.getBusinessLicense());

        // 3. 默认状态：待审核（PENDING），营业状态：营业中（OPEN）
        merchant.setStatus("PENDING");
        merchant.setBusinessStatus("OPEN");

        // 4. 写入 DB
        merchantMapper.insert(merchant);
    }

    // -------------------------------------------------
    // 2️⃣ 登录（含状态校验）
    // -------------------------------------------------
    @Override
    public Merchant login(String username, String rawPassword) {
        Merchant merchant = merchantMapper.selectByUsername(username);
        if (merchant == null) {
            // 账号不存在 → 调用方自行跳转到注册页
            return null;
        }

        // 密码不匹配
        if (!PasswordEncoderUtil.matches(rawPassword, merchant.getPassword())) {
            throw new BusinessException("密码错误");
        }

        // 只有通过审核且未被封禁的才能登录
        if (!"APPROVED".equals(merchant.getStatus())) {
            throw new BusinessException("账号未审核通过或已被封禁");
        }

        return merchant;
    }

    // -------------------------------------------------
    // 3️⃣ 根据 ID 查询单个商家
    // -------------------------------------------------
    @Override
    public Merchant getById(Long id) {
        return merchantMapper.selectById(id);
    }

    // -------------------------------------------------
    // 4️⃣ 更新商家信息（店铺信息、营业状态等）
    // -------------------------------------------------
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

    // -------------------------------------------------
    // 5️⃣ 更新商家状态（APPROVED / BANNED / PENDING）
    // -------------------------------------------------
    @Override
    public void updateStatus(Long id, String status) {
        Merchant merchant = merchantMapper.selectById(id);
        if (merchant != null) {
            merchant.setStatus(status);
            merchantMapper.updateById(merchant);
        }
    }

    // -------------------------------------------------
    // 6️⃣ 按状态查询商家（管理员审核列表等）   → 已有实现
    // -------------------------------------------------
    @Override
    public List<Merchant> listByStatus(String status) {
        return merchantMapper.selectByStatus(status);
    }

    // -------------------------------------------------
    // 7️⃣ **新增**：查询所有商家（管理员页面 /admin/merchant/list 使用）
    // -------------------------------------------------
    @Override
    public List<Merchant> listAll() {
        // 直接交给 Mapper 完成 SELECT * FROM tb_merchant
        return merchantMapper.selectAll();
    }
}
