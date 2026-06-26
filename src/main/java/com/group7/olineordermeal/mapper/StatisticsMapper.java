package com.group7.olineordermeal.mapper;

import org.apache.ibatis.annotations.Mapper;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 统计报表（XML 版），所有 SQL 在同名 XML 中定义
 */
@Mapper
public interface StatisticsMapper {

    long countCompletedOrders();

    BigDecimal sumCompletedOrderAmount();

    long countActiveUsersSince(LocalDateTime since);

    long countActiveMerchantsSince(LocalDateTime since);
}
