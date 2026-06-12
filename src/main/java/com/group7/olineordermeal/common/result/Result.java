package com.group7.olineordermeal.common.result;
/**
 * 统一 API 返回结果
 * 所有 Controller 的方法都要返回这个对象，保证前端处理一致
 * @param <T> 返回的数据类型
 */
public class Result<T> {
    private int code;   // 状态码，200表示成功，500表示失败
    private String msg; // 提示信息
    private T data;     // 实际数据

    // 构造器私有，只能通过静态方法创建
    private Result() {}

    // 成功返回（不带数据）
    public static <T> Result<T> success() {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMsg("success");
        return result;
    }

    // 成功返回（带数据）
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMsg("success");
        result.setData(data);
        return result;
    }

    // 失败返回（自定义错误信息）
    public static <T> Result<T> error(String msg) {
        Result<T> result = new Result<>();
        result.setCode(500);
        result.setMsg(msg);
        return result;
    }

    // 失败返回（自定义错误码和信息）
    public static <T> Result<T> error(int code, String msg) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMsg(msg);
        return result;
    }

    // getter 和 setter（Lombok 可以简化，但这里手动写，保证小白能看懂）
    public int getCode() { return code; }
    public void setCode(int code) { this.code = code; }
    public String getMsg() { return msg; }
    public void setMsg(String msg) { this.msg = msg; }
    public T getData() { return data; }
    public void setData(T data) { this.data = data; }
}