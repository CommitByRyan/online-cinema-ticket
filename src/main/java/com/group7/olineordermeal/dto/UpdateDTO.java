package com.group7.olineordermeal.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import lombok.Data;

    @Data
    public class UpdateDTO {
        @NotNull(message = "商家ID不能为空")
        private Long id;

        /** 店铺名称 */
        private String merchantName;

        /** 联系电话 */
        @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
        private String phone;

        /** 店铺地址 */
        private String address;

        /** 营业执照号 */
        private String businessLicense;

        /** 营业状态：0停业 / 1营业 */
        private String status;
    }

