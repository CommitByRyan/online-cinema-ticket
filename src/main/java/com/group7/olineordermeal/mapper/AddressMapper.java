package com.group7.olineordermeal.mapper;

import com.group7.olineordermeal.entity.Address;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface AddressMapper {

    /** 查询用户所有地址 */
    List<Address> selectByUserId(@Param("userId") Long userId);

    /** 查询用户默认地址（若有） */
    Address selectDefaultByUserId(@Param("userId") Long userId);

    /** 根据ID查询单条地址（用于编辑） */
    Address selectById(@Param("id") Long id);

    /** 新增地址 */
    int insert(Address address);

    /** 更新地址 */
    int updateById(Address address);

    /** 删除地址 */
    int deleteById(@Param("id") Long id);

    /** 清除用户所有默认地址（在新增/编辑时使用） */
    int clearDefaultByUserId(@Param("userId") Long userId);
}
