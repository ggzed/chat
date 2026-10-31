package com.ggzed.chat.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 对话请求
 */
@Data
public class ChatRequest {

    @NotBlank(message = "消息内容不能为空")
    @Size(max = 4000, message = "消息内容长度不能超过 4000")
    private String message;

    /** 会话 ID，同一 ID 的多轮对话互不串扰，不同 ID 彼此隔离 */
    @NotBlank(message = "会话 ID 不能为空")
    @Size(max = 64, message = "会话 ID 长度不能超过 64")
    private String sessionId;
}
