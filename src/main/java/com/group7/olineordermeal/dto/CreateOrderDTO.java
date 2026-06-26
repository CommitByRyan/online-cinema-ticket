package com.group7.olineordermeal.dto;

import lombok.Data;

@Data
public class CreateOrderDTO {
    private Long addressId;   // 选中的收货地址ID
    private String remark;    // 备注
}