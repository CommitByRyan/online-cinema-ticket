package com.group7.olineordermeal.common.exception;

/**
 * 业务异常类，用于抛出可预知的业务错误（如：余额不足、订单状态不对等）
 * 会被全局异常处理器捕获并返回友好提示
 */
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}
