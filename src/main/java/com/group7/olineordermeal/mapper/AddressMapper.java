package com.group7.olineordermeal.mapper;

import com.group7.olineordermeal.entity.Address;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

    @Mapper
    public interface AddressMapper {

        /**
         * 查询某个用户的所有地址
         */
        List<Address> selectByUserId(@Param("userId") Long userId);

        /**
         * 查询某个用户的默认地址
         */
        Address selectDefaultByUserId(@Param("userId") Long userId);

        /**
         * 根据ID查询地址
         */
        Address selectById(@Param("id") Long id);

        /**
         * 插入新地址
         */
        int insert(Address address);

        /**
         * 更新地址
         */
        int updateById(Address address);

        /**
         * 删除地址
         */
        int deleteById(@Param("id") Long id);

        /**
         * 将该用户所有地址的 is_default 设为 0
         */
        int clearDefaultByUserId(@Param("userId") Long userId);
    }
