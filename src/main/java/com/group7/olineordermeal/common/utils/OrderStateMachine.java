package com.group7.olineordermeal.common.utils;

import com.group7.olineordermeal.common.enums.OrderStatusEnum;
import com.group7.olineordermeal.common.exception.BusinessException;

/**
 * 订单状态机：定义哪些状态转换是合法的，防止非法操作
 */
public class OrderStateMachine {

    /**
     * 检查从 from 状态是否可以转换为 to 状态
     * @param from 当前状态
     * @param to   目标状态
     * @throws BusinessException 如果不允许转换则抛出异常
     */
    public static void checkTransition(OrderStatusEnum from, OrderStatusEnum to) {
        if (from == null || to == null) {
            throw new BusinessException("订单状态无效");
        }
        boolean allowed = false;
        switch (from) {
            case PENDING:
                allowed = (to == OrderStatusEnum.PAID || to == OrderStatusEnum.CANCELLED);
                break;
            case PAID:
                allowed = (to == OrderStatusEnum.CONFIRMED || to == OrderStatusEnum.CANCELLED);
                break;
            case CONFIRMED:
                allowed = (to == OrderStatusEnum.PREPARING);
                break;
            case PREPARING:
                allowed = (to == OrderStatusEnum.DELIVERED);
                break;
            case DELIVERED:
                allowed = (to == OrderStatusEnum.COMPLETED);
                break;
            default:
                allowed = false;
        }
        if (!allowed) {
            throw new BusinessException(String.format("订单状态不允许从 %s 修改为 %s", from.getDesc(), to.getDesc()));
        }
    }
}