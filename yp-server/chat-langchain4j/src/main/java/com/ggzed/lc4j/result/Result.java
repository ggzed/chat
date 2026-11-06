package com.ggzed.lc4j.result;

/**
 * 统一返回结构，字段与 chat-springai 的 Result 完全一致
 */
public record Result<T>(Integer code, String message, T data) {

    public static <T> Result<T> success(T data) {
        return new Result<>(200, "操作成功", data);
    }

    public static <T> Result<T> error(Integer code, String message) {
        return new Result<>(code, message, null);
    }
}
