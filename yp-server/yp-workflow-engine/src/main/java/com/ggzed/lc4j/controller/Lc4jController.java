package com.ggzed.lc4j.controller;

import com.ggzed.lc4j.result.Result;
import com.ggzed.lc4j.agent.FinancialAgent;
import com.ggzed.lc4j.model.dto.ChatResponse;
import com.ggzed.lc4j.model.request.ChatRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * LangChain4j 对话 API，路径与入参对齐 yp-assistant-engine
 */
@RestController
@RequestMapping("/api/chat")
public class Lc4jController {

    private final FinancialAgent financialAgent;

    public Lc4jController(FinancialAgent financialAgent) {
        this.financialAgent = financialAgent;
    }

    @PostMapping("/simple")
    public Result<ChatResponse> simple(@RequestBody @Valid ChatRequest request) {
        String content = financialAgent.chat(request.sessionId(), request.message());
        return Result.success(new ChatResponse(request.sessionId(), content));
    }
}
