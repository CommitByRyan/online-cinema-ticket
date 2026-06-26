package com.group7.olineordermeal.entity;

import lombok.Data;
import java.time.LocalDateTime;

    /**
     * 收货地址，对应表 tb_address
     */
    @Data
    public class Address {
        private Long id;
        private Long userId;            // 所属用户ID
        private String receiverName;    // 收货人姓名
        private String phone;           // 收货人电话
        private String province;        // 省
        private String city;            // 市
        private String district;        // 区/县
        private String detailAddress;   // 详细地址
        private Integer isDefault;      // 1=默认地址，0=非默认
        private LocalDateTime createTime;
    }

