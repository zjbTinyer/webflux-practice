package com.example.webflux.controller;

import com.example.webflux.dto.Result;
import com.example.webflux.entity.User;
import com.example.webflux.service.impl.UserServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * WebFlux 操作符演示 Controller
 *
 * <p>所有演示端点以 /api/demo 为前缀，与业务 API 分离。
 * 每个端点展示一种或多种 Reactor 操作符的使用。
 */
@Slf4j
@RestController
@RequestMapping("/api/demo")
@RequiredArgsConstructor
@Tag(name = "Demo", description = "WebFlux 操作符演示")
public class DemoController {

    private final UserServiceImpl userService;

    /**
     * 🧪 Mono.zip 合并演示
     */
    @Operation(summary = "Mono.zip 演示", description = "同时查询用户总数和第一个用户，用 zip 合并结果")
    @GetMapping("/zip")
    public Mono<Result<String>> zipDemo() {
        return userService.zipDemo().map(Result::ok);
    }

    /**
     * 🧪 Flux.merge 并行合并演示（SSE）
     */
    @Operation(summary = "merge 演示", description = "并行合并两个 Flux 源，SSE 展示交错效果")
    @GetMapping(value = "/merge", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> mergeDemo() {
        return userService.mergeDemo();
    }

    /**
     * 🧪 collectList 聚合演示
     */
    @Operation(summary = "collectList 演示", description = "将 Flux 所有元素收集为 List")
    @GetMapping("/collect")
    public Mono<Result<String>> collectDemo() {
        return userService.collectListDemo().map(Result::ok);
    }

    /**
     * 🧪 错误处理链路演示
     */
    @Operation(summary = "错误处理演示", description = "测试 timeout + retry + onErrorMap")
    @GetMapping("/error/{id}")
    public Mono<Result<User>> errorHandlingDemo(@PathVariable Long id) {
        return userService.errorHandlingDemo(id).map(Result::ok);
    }

    /**
     * 🧪 调度器演示
     */
    @Operation(summary = "调度器演示", description = "演示 publishOn/subscribeOn 线程切换")
    @GetMapping("/scheduler")
    public Mono<Result<String>> schedulerDemo() {
        return userService.schedulerDemo().map(Result::ok);
    }

    /**
     * 🧪 Reactor Context 演示
     */
    @Operation(summary = "Context 演示", description = "演示 Reactor Context 写入与读取")
    @GetMapping("/context")
    public Mono<Result<String>> contextDemo() {
        return userService.contextDemo()
                .contextWrite(ctx -> ctx.put("traceId", "trace-demo-001"))
                .map(Result::ok);
    }
}
