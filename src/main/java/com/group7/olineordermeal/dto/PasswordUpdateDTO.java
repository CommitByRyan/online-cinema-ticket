package com.group7.olineordermeal.dto;

import lombok.Data;

    @Data
    public class PasswordUpdateDTO {
        private String oldPassword;
        private String newPassword;
    }

