package com.ggzed.chat.service;

import reactor.core.publisher.Flux;

public interface ChatService {
    String chat(String message);
    Flux<String> streamChat(String message);
}
