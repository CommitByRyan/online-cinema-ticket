package com.group7.olineordermeal.common.config;

import com.github.pagehelper.PageInterceptor;
import org.apache.ibatis.plugin.Interceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

/**
 * MyBatis 配置类，主要用于配置分页插件 PageHelper
 */
@Configuration
public class MyBatisConfig {

    @Bean
    public PageInterceptor pageInterceptor() {
        PageInterceptor interceptor = new PageInterceptor();
        Properties props = new Properties();
        props.setProperty("helperDialect", "mysql");  // 数据库方言
        props.setProperty("reasonable", "true");     // 页码合理化（如 pageNum=0 自动转为1）
        interceptor.setProperties(props);
        return interceptor;
    }
}