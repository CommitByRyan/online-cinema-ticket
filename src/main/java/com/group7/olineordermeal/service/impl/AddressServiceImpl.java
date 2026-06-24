package com.group7.olineordermeal.service.impl;

import com.group7.olineordermeal.common.exception.BusinessException;
import com.group7.olineordermeal.dto.AddressDTO;
import com.group7.olineordermeal.entity.Address;
import com.group7.olineordermeal.mapper.AddressMapper;
import com.group7.olineordermeal.service.AddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

    @Service
    public class AddressServiceImpl implements AddressService {

        @Autowired
        private AddressMapper addressMapper;

        @Override
        public List<Address> listByUser(Long userId) {
            return addressMapper.selectByUserId(userId);
        }

        @Override
        public Address getDefaultAddress(Long userId) {
            return addressMapper.selectDefaultByUserId(userId);
        }

        @Override
        @Transactional
        public void addAddress(Long userId, AddressDTO dto) {
            Address address = new Address();
            address.setUserId(userId);
            address.setReceiverName(dto.getReceiverName());
            address.setPhone(dto.getPhone());
            address.setProvince(dto.getProvince());
            address.setCity(dto.getCity());
            address.setDistrict(dto.getDistrict());
            address.setDetailAddress(dto.getDetailAddress());
            // 如果设置为默认地址，需要先清除该用户的其他默认地址
            if (dto.getIsDefault() == 1) {
                addressMapper.clearDefaultByUserId(userId);
                address.setIsDefault(1);
            } else {
                // 如果用户还没有任何地址，自动将第一个设为默认
                if (addressMapper.selectByUserId(userId).isEmpty()) {
                    address.setIsDefault(1);
                } else {
                    address.setIsDefault(0);
                }
            }
            addressMapper.insert(address);
        }

        @Override
        @Transactional
        public void updateAddress(Long addressId, AddressDTO dto) {
            Address address = addressMapper.selectById(addressId);
            if (address == null) {
                throw new BusinessException("地址不存在");
            }
            address.setReceiverName(dto.getReceiverName());
            address.setPhone(dto.getPhone());
            address.setProvince(dto.getProvince());
            address.setCity(dto.getCity());
            address.setDistrict(dto.getDistrict());
            address.setDetailAddress(dto.getDetailAddress());
            if (dto.getIsDefault() == 1) {
                addressMapper.clearDefaultByUserId(address.getUserId());
                address.setIsDefault(1);
            } else {
                address.setIsDefault(0);
            }
            addressMapper.updateById(address);
        }

        @Override
        public void deleteAddress(Long addressId, Long userId) {
            Address address = addressMapper.selectById(addressId);
            if (address == null || !address.getUserId().equals(userId)) {
                throw new BusinessException("地址不存在或无权限");
            }
            addressMapper.deleteById(addressId);
        }

        @Override
        @Transactional
        public void setDefault(Long addressId, Long userId) {
            // 先清除该用户所有默认地址
            addressMapper.clearDefaultByUserId(userId);
            // 再将指定地址设为默认
            Address address = addressMapper.selectById(addressId);
            if (address == null || !address.getUserId().equals(userId)) {
                throw new BusinessException("地址不存在");
            }
            address.setIsDefault(1);
            addressMapper.updateById(address);
        }
    }
