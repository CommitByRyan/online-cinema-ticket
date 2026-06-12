package com.group7.olineordermeal.service.impl;

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

    @Override
    public void updateStatus(Long id, String status) {
        Merchant merchant = merchantMapper.selectById(id);
        if (merchant != null) {
            merchant.setStatus(status);
            merchantMapper.updateById(merchant);
        }
    }

    @Override
    public List<Merchant> listByStatus(String status) {
        return merchantMapper.selectByStatus(status);
    }
}