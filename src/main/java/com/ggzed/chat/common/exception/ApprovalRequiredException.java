package com.ggzed.chat.common.exception;

/**
 * 需要人工审批的业务异常。
 * <p>
 * 当工具调用命中高危操作（如转账、扣款）时抛出，
 * 由全局异常处理器返回 403 状态码及提示信息。
 */
public class ApprovalRequiredException extends RuntimeException {

    public ApprovalRequiredException(String message) {
        super(message);
    }
}
