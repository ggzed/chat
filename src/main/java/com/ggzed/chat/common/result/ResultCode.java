package com.ggzed.chat.common.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 返回状态码
 */
@Getter
@AllArgsConstructor
public enum ResultCode {

    SUCCESS(200, "操作成功"),
    FAIL(500, "操作失败"),
    PARAM_ERROR(400, "参数校验失败"),
    FORBIDDEN(403, "禁止访问"),
    AI_TIMEOUT(504, "AI 服务调用超时"),
    AI_TOKEN_EXCEEDED(429, "Token 超限");

    private final Integer code;
    private final String message;
}
