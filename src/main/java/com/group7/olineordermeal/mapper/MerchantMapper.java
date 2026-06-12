package com.group7.olineordermeal.mapper;

import com.group7.olineordermeal.entity.Merchant;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface MerchantMapper {
    int insert(Merchant merchant);
    int updateById(Merchant merchant);
    Merchant selectById(Long id);
    Merchant selectByUsername(String username);
    List<Merchant> selectByStatus(@Param("status") String status);
}