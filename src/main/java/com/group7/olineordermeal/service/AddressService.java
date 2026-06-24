package com.group7.olineordermeal.service;

import com.group7.olineordermeal.entity.Address;
import com.group7.olineordermeal.dto.AddressDTO;
import java.util.List;

    public interface AddressService {
        List<Address> listByUser(Long userId);
        Address getDefaultAddress(Long userId);
        void addAddress(Long userId, AddressDTO dto);
        void updateAddress(Long addressId, AddressDTO dto);
        void deleteAddress(Long addressId, Long userId);
        void setDefault(Long addressId, Long userId);
    }
