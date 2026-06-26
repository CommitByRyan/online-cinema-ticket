package com.group7.olineordermeal.dto;

import lombok.Data;

    @Data
    public class AddressDTO {
        private String receiverName;
        private String phone;
        private String province;
        private String city;
        private String district;
        private String detailAddress;
        private Integer isDefault;  // 0或1
    }

