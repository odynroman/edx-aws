package com.edxproject.app.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import reactor.core.publisher.Mono;

@Controller
public class AppController {

    @GetMapping("/")
    private Mono<String> getIndexPage() {
        return Mono.fromCallable(() -> "index.html");
    }
}
