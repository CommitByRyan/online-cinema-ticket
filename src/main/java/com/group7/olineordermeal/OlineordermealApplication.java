package com.group7.olineordermeal;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
// 新增这一行，扫描mapper文件夹，解决启动报错
@MapperScan("com.group7.olineordermeal.mapper")
public class OlineordermealApplication {
    public static void main(String[] args) {
        SpringApplication.run(OlineordermealApplication.class, args);
    }
}
