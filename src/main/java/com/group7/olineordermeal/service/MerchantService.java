package com.group7.olineordermeal.service;

import com.group7.olineordermeal.entity.Merchant;
import java.util.List;

public interface MerchantService {
    void updateStatus(Long id, String status);
    List<Merchant> listByStatus(String status);
}