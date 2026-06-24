package com.group7.olineordermeal.dto;

import javax.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginDTO {
    /** 商家登录用户名 */
    @NotBlank(message = "用户名不能为空")
    private String username;

    /** 登录密码 */
    @NotBlank(message = "密码不能为空")
    private String password;
}

