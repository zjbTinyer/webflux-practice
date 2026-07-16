package com.example.webflux.config;

import com.example.webflux.handler.ReactiveWebSocketHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;

import java.util.Map;

/**
 * WebSocket 配置
 *
 * <h2>WebFlux 中配置 WebSocket 的步骤</h2>
 * <ol>
 *   <li>创建 Handler 实现 {@link WebSocketHandler} 接口</li>
 *   <li>通过 {@link SimpleUrlHandlerMapping} 将 URL 路径映射到 Handler</li>
 *   <li>注册 {@link WebSocketHandlerAdapter} 来激活 WebSocket 支持</li>
 * </ol>
 *
 * <h2>与 Spring MVC WebSocket 的区别</h2>
 * <ul>
 *   <li>MVC: 需要 @EnableWebSocket + 实现 WebSocketConfigurer 接口</li>
 *   <li>WebFlux: 直接注册 HandlerMapping bean，没有 Servlet 容器依赖</li>
 * </ul>
 */
@Configuration
public class WebSocketConfig {

    /**
     * 注册 WebSocket 处理器适配器
     *
     * <p>WebSocketHandlerAdapter 负责将底层的 WebSocket 握手请求
     * 委托给对应的 WebSocketHandler 处理。
     */
    @Bean
    public WebSocketHandlerAdapter handlerAdapter() {
        return new WebSocketHandlerAdapter();
    }

    /**
     * URL 路径 → Handler 映射
     *
     * <p>将 /ws/chat 路径映射到 ReactiveWebSocketHandler：
     * 前端通过 ws://localhost:8080/ws/chat 连接
     *
     * <p>setOrder(-1): 确保 WebSocket 映射优先级高于普通 HTTP 路由
     */
    @Bean
    public HandlerMapping webSocketMapping(ReactiveWebSocketHandler handler) {
        return new SimpleUrlHandlerMapping(
                Map.of("/ws/chat", (WebSocketHandler) handler),
                -1  // 优先级高于普通路由
        );
    }
}
