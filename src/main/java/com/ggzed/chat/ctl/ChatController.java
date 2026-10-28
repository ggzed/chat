package com.ggzed.chat.ctl;

import com.ggzed.chat.common.result.Result;
import com.ggzed.chat.model.request.ChatRequest;
import com.ggzed.chat.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * 基础对话 API
 */
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    /**
     * 非流式对话
     */
    @PostMapping("/simple")
    public Result<String> simple(@RequestBody @Valid ChatRequest request) {
        return Result.success(chatService.chat(request.getMessage()));
    }

    /**
     * 流式对话（SSE 打字机效果）
     */
    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> stream(@RequestBody @Valid ChatRequest request) {
        return chatService.streamChat(request.getMessage());
    }
}
