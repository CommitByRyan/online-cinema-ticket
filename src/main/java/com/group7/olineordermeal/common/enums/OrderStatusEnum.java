package com.group7.olineordermeal.common.enums;
/**
 * 订单状态枚举
 * 定义所有可能的订单状态，避免字符串硬编码
 */
public enum OrderStatusEnum {
    PENDING("待支付", 0),
    PAID("已支付", 1),
    CONFIRMED("商家已接单", 2),
    PREPARING("制作中", 3),
    DELIVERED("配送中", 4),
    COMPLETED("已完成", 5),
    CANCELLED("已取消", -1);

    private String desc;  // 中文描述
    private int code;     // 数字代码

    // 构造器
    OrderStatusEnum(String desc, int code) {
        this.desc = desc;
        this.code = code;
    }

    // getter
    public String getDesc() { return desc; }
    public int getCode() { return code; }

    // 根据数字代码获取枚举（供数据库转换用）
    public static OrderStatusEnum fromCode(int code) {
        for (OrderStatusEnum s : values()) {
            if (s.code == code) return s;
        }
        return null;
    }

    // 判断该状态是否允许用户取消（用于业务逻辑）
    public boolean canCancel() {
        return this == PENDING || this == PAID;  // 待支付和已支付（商家未接单前）可取消
    }
}