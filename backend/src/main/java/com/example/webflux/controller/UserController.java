package com.example.webflux.controller;

import com.example.webflux.dto.Result;
import com.example.webflux.dto.UserDTO;
import com.example.webflux.entity.User;
import com.example.webflux.service.impl.UserServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 用户 Controller（注解方式）
 *
 * <h2>WebFlux 中两种定义 Controller 的方式</h2>
 * <ol>
 *   <li><b>注解方式</b> (@RestController, @GetMapping…) — 与 Spring MVC 写法相同，但返回值是 Mono/Flux</li>
 *   <li><b>函数式端点</b> (RouterFunction + HandlerFunction) — 纯函数式风格，见 {@code RouterConfig}</li>
 * </ol>
 *
 * <h2>关键区别</h2>
 * <ul>
 *   <li>Spring MVC: {@code @RestController} + 返回 {@code User} / {@code List<User>}</li>
 *   <li>WebFlux: {@code @RestController} + 返回 {@code Mono<User>} / {@code Flux<User>}</li>
 *   <li>WebFlux 中所有操作都是非阻塞的，框架会自动订阅并处理响应</li>
 * </ul>
 *
 * <h2>本 Controller 涵盖的特性</h2>
 * <ul>
 *   <li>基础 CRUD（Mono/Flux 返回值）</li>
 *   <li>SSE（Server-Sent Events）流式推送</li>
 *   <li>全局异常处理（@ExceptionHandler）</li>
 *   <li>请求参数校验（@Valid）</li>
 *   <li>批量操作</li>
 * </ul>
 */
@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserServiceImpl userService;

    // ======================================================================
    // 一、基础 CRUD —— 所有方法返回 Mono/Flux
    // ======================================================================

    /**
     * 📌 创建用户 —— 返回 Mono<Result<User>>
     *
     * <p>POST /api/users
     * <p>{@code @Valid} 会触发 Bean Validation，如果失败则抛出 WebExchangeBindException
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Result<User>> create(@Valid @RequestBody UserDTO dto) {
        return userService.create(dto)
                // map 将 User 包装为 Result<User>
                .map(Result::ok)
                // 如果校验/创建过程出错，这里不 catch，由全局异常处理器统一处理
                .doOnSuccess(result -> log.info("📤 [API] 创建用户响应: {}", result.getData().getId()));
    }

    /**
     * 📌 按 ID 查询 —— 返回 Mono<Result<User>>
     *
     * <p>GET /api/users/{id}
     * <p>注意路径变量用 {@code @PathVariable}
     */
    @GetMapping("/{id}")
    public Mono<Result<User>> findById(@PathVariable Long id) {
        return userService.findById(id)
                .map(Result::ok);
    }

    /**
     * 📌 查询全部用户（分页）
     *
     * <p>GET /api/users?page=0&size=10
     * <p>不传参数时默认返回第 0 页、每页 10 条
     */
    @GetMapping
    public Mono<Result<com.example.webflux.dto.PageResponse<User>>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return userService.findAllPaged(page, size).map(Result::ok);
    }

    /**
     * 📌 查询全部用户（不分页，Flux 模式）
     *
     * <p>GET /api/users/all
     */
    @GetMapping("/all")
    public Flux<User> findAll() {
        return userService.findAll();
    }

    /**
     * 📌 更新用户 —— 返回 Mono<Result<User>>
     *
     * <p>PUT /api/users/{id}
     */
    @PutMapping("/{id}")
    public Mono<Result<User>> update(@PathVariable Long id, @Valid @RequestBody UserDTO dto) {
        return userService.update(id, dto)
                .map(Result::ok);
    }

    /**
     * 📌 删除用户 —— 返回 Mono<Result<Void>>
     *
     * <p>DELETE /api/users/{id}
     * <p>使用 {@code thenReturn} 在删除成功后返回成功结果
     */
    @DeleteMapping("/{id}")
    public Mono<Result<Void>> delete(@PathVariable Long id) {
        return userService.delete(id)
                // thenReturn: 忽略上游 Void，返回固定的 Result
                .thenReturn(Result.ok());
    }

    // ======================================================================
    // 二、高级查询
    // ======================================================================

    /**
     * 📌 按名称搜索 —— GET /api/users/search?name=xxx
     */
    @GetMapping("/search")
    public Flux<User> searchByName(@RequestParam String name) {
        return userService.searchByName(name);
    }

    /**
     * 📌 按年龄过滤 —— GET /api/users/age/{age}
     */
    @GetMapping("/age/{age}")
    public Flux<User> findByAgeGreaterThan(@PathVariable int age) {
        return userService.findByAgeGreaterThan(age);
    }

    /**
     * 📌 邮箱搜索 —— GET /api/users/email?keyword=xxx
     */
    @GetMapping("/email")
    public Flux<User> searchByEmail(@RequestParam String keyword) {
        return userService.searchByEmail(keyword);
    }

    /**
     * 📌 条件查询 + 兜底逻辑 —— GET /api/users/{id}/fallback
     */
    @GetMapping("/{id}/fallback")
    public Mono<Result<User>> findByIdWithFallback(@PathVariable Long id) {
        return userService.findByIdWithFallback(id)
                .map(Result::ok);
    }

    // ======================================================================
    // 三、SSE (Server-Sent Events) 流式推送
    // ======================================================================

    /**
     * 📡 SSE 流式推送所有用户
     *
     * <p>GET /api/users/stream
     *
     * <p><b>SSE 是什么？</b>
     * 服务端向客户端推送实时数据的技术。客户端通过 EventSource API 接收。
     * 与 WebSocket 的区别：
     * <ul>
     *   <li>SSE: 单向（服务端→客户端），基于 HTTP，自动重连</li>
     *   <li>WebSocket: 双向，独立协议，需要额外处理重连</li>
     * </ul>
     *
     * <p>produces = MediaType.TEXT_EVENT_STREAM_VALUE 是关键配置：
     * Content-Type 会被设为 text/event-stream，浏览器识别后
     * 不会一次性等待所有数据，而是逐个接收事件。
     */
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<User> streamUsers() {
        return userService.streamAllUsers();
    }

    /**
     * 📡 SSE 心跳 —— GET /api/users/heartbeat
     *
     * <p>无限流，演示 Flux.interval 的用法。
     * 前端可以用此演示 背压、取消订阅 等概念。
     */
    @GetMapping(value = "/heartbeat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> heartbeat() {
        return userService.heartbeatStream();
    }

    // ======================================================================
    // 四、批量操作
    // ======================================================================

    /**
     * 📦 批量创建用户 —— POST /api/users/batch
     *
     * <p>接收 Flux<UserDTO>，返回 Flux<User>
     * <p>请求 Content-Type 保持 application/json，Flux 会被框架反序列化为 JSON 数组
     */
    @PostMapping("/batch")
    public Flux<User> batchCreate(@Valid @RequestBody Flux<UserDTO> dtoStream) {
        return userService.batchCreate(dtoStream);
    }
}
