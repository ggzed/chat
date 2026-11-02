package com.ggzed.chat.ctl;

import com.ggzed.chat.common.result.Result;
import com.ggzed.chat.model.request.RagRequest;
import com.ggzed.chat.service.RagService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 风控验证 API
 */
@RestController
@RequestMapping("/api/security")
@RequiredArgsConstructor
public class SecurityController {

    private final RagService ragService;

    /**
     * 安全测试：绕过 RAG 直接发指令，验证高危工具调用的风控拦截
     */
    @PostMapping("/test")
    public Result<String> test(@RequestBody @Valid RagRequest request) {
        return Result.success(ragService.testSecurity(request.getQuery()));
    }
}
