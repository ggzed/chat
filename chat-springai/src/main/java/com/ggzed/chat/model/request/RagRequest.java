package com.ggzed.chat.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * RAG 问答请求
 */
@Data
public class RagRequest {

    @NotBlank(message = "查询内容不能为空")
    @Size(max = 4000, message = "查询内容长度不能超过 4000")
    private String query;
}
