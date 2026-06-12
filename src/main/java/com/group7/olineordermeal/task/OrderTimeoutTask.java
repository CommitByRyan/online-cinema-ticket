package com.group7.olineordermeal.task;

import com.group7.olineordermeal.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling  // 开启定时任务
public class OrderTimeoutTask {

    private static final Logger log = LoggerFactory.getLogger(OrderTimeoutTask.class);

    @Autowired
    private OrderService orderService;

    // 每 30 秒执行一次，fixedDelay 表示上一次执行完毕后等待 30 秒再执行
    @Scheduled(fixedDelay = 30000)
    public void cancelTimeoutOrders() {
        log.info("开始扫描过期订单...");
        int count = orderService.cancelExpiredPendingOrders();
        if (count > 0) {
            log.info("成功取消 {} 笔超时未支付订单", count);
        }
    }
}