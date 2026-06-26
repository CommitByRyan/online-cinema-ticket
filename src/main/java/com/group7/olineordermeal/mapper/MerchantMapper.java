package com.group7.olineordermeal.mapper;

import com.group7.olineordermeal.entity.Merchant;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface MerchantMapper {

    int insert(Merchant merchant);
    int updateById(Merchant merchant);
    Merchant selectById(@Param("id") Long id);
    Merchant selectByUsername(@Param("username") String username);

    /**
     * 新增：根据状态查询商家列表
     *
     * @param status PENDING / APPROVED / BANNED
     * @return 匹配的商家集合
     */
    List<Merchant> selectByStatus(@Param("status") String status);
}
