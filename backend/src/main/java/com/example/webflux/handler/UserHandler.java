package com.example.webflux.handler;

import com.example.webflux.dto.Result;
import com.example.webflux.dto.UserDTO;
import com.example.webflux.entity.User;
import com.example.webflux.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.net.URI;

/**
 * 用户 —— 函数式端点 Handler
 *
 * <h2>函数式端点 vs 注解方式</h2>
 *
 * <table>
 *   <tr><th></th><th>注解方式</th><th>函数式端点</th></tr>
 *   <tr>
 *     <td>定义方式</td>
 *     <td>@RestController + @GetMapping</td>
 *     <td>RouterFunction + HandlerFunction</td>
 *   </tr>
 *   <tr>
 *     <td>请求路由</td>
 *     <td>框架自动匹配</td>
 *     <td>手动在 RouterConfig 中定义</td>
 *   </tr>
 *   <tr>
 *     <td>方法签名</td>
 *     <td>任意参数 + 返回 Mono/Flux</td>
 *     <td>ServerRequest → Mono&lt;ServerResponse&gt;</td>
 *   </tr>
 *   <tr>
 *     <td>适用场景</td>
 *     <td>快速开发，迁移旧项目</td>
 *     <td>需要完全控制路由、组合中间件的复杂场景</td>
 *   </tr>
 * </table>
 *
 * <h2>HandlerFunction 本质</h2>
 * 就是一个函数: {@code ServerRequest → Mono<ServerResponse>}
 * <ul>
 *   <li>ServerRequest: 封装了请求的所有信息（路径、参数、Body）</li>
 *   <li>ServerResponse: 构建响应的建造器（状态码、Header、Body）</li>
 * </ul>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserHandler {

    private final UserService userService;

    /**
     * 处理"查询所有用户"请求
     *
     * <p>Handler 方法的标准写法：
     * <pre>
     * public Mono&lt;ServerResponse&gt; xxx(ServerRequest request) {
     *     return ServerResponse
     *         .status(HTTP状态)
     *         .body(响应数据, 数据类型.class);
     * }
     * </pre>
     */
    public Mono<ServerResponse> findAll(ServerRequest request) {
        return ServerResponse
                .ok()  // 200 OK
                .body(userService.findAll(), User.class);  // Flux<User> → JSON 数组
    }

    /**
     * 处理"按 ID 查询"请求
     *
     * <p>路径变量通过 {@code request.pathVariable("id")} 获取
     */
    public Mono<ServerResponse> findById(ServerRequest request) {
        // 从路径中提取 id 变量
        Long id = Long.valueOf(request.pathVariable("id"));

        return userService.findById(id)
                .flatMap(user -> ServerResponse.ok().bodyValue(Result.ok(user)))
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    /**
     * 处理"创建用户"请求
     *
     * <p>请求体通过 {@code request.bodyToMono(UserDTO.class)} 获取
     * 注意：bodyToMono 返回的是 Mono<UserDTO>，完全响应式
     */
    public Mono<ServerResponse> create(ServerRequest request) {
        return request
                // bodyToMono: 将请求体反序列化为 Mono<UserDTO>
                // 这是一个非阻塞操作（不像传统 Servlet 需要先读完整个请求体）
                .bodyToMono(UserDTO.class)
                .flatMap(userService::create)
                .flatMap(user ->
                        ServerResponse
                                .created(URI.create("/api/func/users/" + user.getId()))  // 201 + Location header
                                .bodyValue(Result.ok(user))
                )
                // 【onErrorResume】在 Handler 层统一处理校验错误
                .onErrorResume(IllegalArgumentException.class, e ->
                        ServerResponse.badRequest().bodyValue(Result.badRequest(e.getMessage()))
                );
    }

    /**
     * 处理"更新用户"请求
     */
    public Mono<ServerResponse> update(ServerRequest request) {
        Long id = Long.valueOf(request.pathVariable("id"));

        return request.bodyToMono(UserDTO.class)
                .flatMap(dto -> userService.update(id, dto))
                .flatMap(user -> ServerResponse.ok().bodyValue(Result.ok(user)))
                .onErrorResume(e -> ServerResponse.badRequest()
                        .bodyValue(Result.error(400, e.getMessage())));
    }

    /**
     * 处理"删除用户"请求
     */
    public Mono<ServerResponse> delete(ServerRequest request) {
        Long id = Long.valueOf(request.pathVariable("id"));

        return userService.delete(id)
                // then + ServerResponse.noContent(): 删除成功返回 204 No Content
                .then(ServerResponse.noContent().build());
    }
}
