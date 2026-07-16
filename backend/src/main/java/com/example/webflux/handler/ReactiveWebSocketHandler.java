package com.example.webflux.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket 响应式处理器
 *
 * <h2>WebFlux WebSocket 核心概念</h2>
 *
 * <pre>
 * WebSocketSession
 * ├── receive() → Flux&lt;WebSocketMessage&gt;  // 接收客户端消息（入站流）
 * └── send(Flux&lt;WebSocketMessage&gt;)         // 向客户端发送消息（出站流）
 * </pre>
 *
 * <h2>Sinks 是什么？</h2>
 * Sinks 是 Reactor 提供的一个"程序化发射器"，允许我们手动向 Flux/Mono 中发送数据。
 * 它类似于传统的 EventBus/发布订阅模式，但完全是响应式的。
 *
 * <p>Sinks 的种类：
 * <ul>
 *   <li>{@code Sinks.many().multicast()} — 多播：多个订阅者，但不缓存历史数据</li>
 *   <li>{@code Sinks.many().replay()} — 重放：多个订阅者，新订阅者也能收到历史数据</li>
 *   <li>{@code Sinks.many().unicast()} — 单播：仅允许一个订阅者</li>
 *   <li>{@code Sinks.one()} — 单值 Sink，类似 Mono</li>
 * </ul>
 *
 * <h2>本 Handler 的功能</h2>
 * <ol>
 *   <li>接收客户端消息 → 广播给所有连接的客户端（群聊）</li>
 *   <li>定时推送服务器时间（演示 Sinks 的用法）</li>
 *   <li>用户上线/下线通知</li>
 * </ol>
 */
@Slf4j
@Component
public class ReactiveWebSocketHandler implements WebSocketHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Sinks.Many 用作事件总线
     * - multicast(): 多播模式，多个订阅者都能收到
     * - onBackpressureBuffer(): 背压策略 — 当下游消费不过来时缓冲在队列中
     * - directBestEffort(): 尽力投递模式
     */
    private final Sinks.Many<String> chatSink = Sinks.many()
            .multicast()
            .onBackpressureBuffer(100, false);  // 缓冲区最多100条，满了则丢弃

    /** 记录当前在线的 session ID */
    private final Map<String, WebSocketSession> onlineSessions = new ConcurrentHashMap<>();

    /**
     * handle 方法 — WebSocket 连接建立后调用
     *
     * <p>返回值 {@code Mono<Void>} 表示这个 WebSocket 会话的生命周期。
     * 当 Mono 完成时，WebSocket 连接关闭。
     *
     * <p>核心模式：
     * <pre>
     * session.receive()  ← 入站 Flux（读客户端消息）
     * session.send(flux)  ← 出站 Flux（向客户端发消息）
     * Mono.zip(receive处理, send处理)  ← 两者并行，任一完成就关闭连接
     * </pre>
     */
    @Override
    public Mono<Void> handle(WebSocketSession session) {
        // 1. 入站处理：接收客户端消息
        var inbound = session.receive()
                // 【map】将 WebSocketMessage 转为文本
                .map(WebSocketMessage::getPayloadAsText)
                .doOnNext(msg -> log.info("📨 [WS] 收到消息: session={}, msg={}", session.getId(), msg))
                // 将消息广播到 chatSink
                .doOnNext(chatSink::tryEmitNext)
                .doOnError(e -> log.error("📨 [WS] 入站错误: {}", e.getMessage()))
                .then();  // 忽略每条消息，只等完成信号

        // 2. 出站处理：向客户端发送消息
        var outbound = session.send(
                // 【Flux.merge】合并两个源：
                //   源1: 聊天广播（chatSink.asFlux()）
                //   源2: 定时心跳（Flux.interval）
                Flux.merge(
                        // 源1：聊天消息广播
                        chatSink.asFlux()
                                .map(msg -> toMessage(session, msg)),
                        // 源2：每10秒发送一次服务器时间
                        Flux.interval(Duration.ofSeconds(10))
                                .map(tick -> toMessage(session,
                                        "🕐 服务器时间: " + LocalDateTime.now() + " | 在线: "
                                                + onlineSessions.size() + " 人"))
                )
        );

        // 3. 用户上线
        onlineSessions.put(session.getId(), session);
        chatSink.tryEmitNext("👋 用户 " + session.getId() + " 加入了聊天室");
        log.info("🟢 [WS] 新连接: {}, 当前在线: {}", session.getId(), onlineSessions.size());

        // 4. 入站和出站并行处理，任一完成或出错就关闭连接
        return Mono.zip(inbound, outbound)
                .doFinally(signalType -> {
                    // 用户下线清理
                    onlineSessions.remove(session.getId());
                    chatSink.tryEmitNext("🚪 用户 " + session.getId() + " 离开了聊天室");
                    log.info("🔴 [WS] 连接关闭: {}, 当前在线: {}", session.getId(), onlineSessions.size());
                })
                .then();
    }

    /** 将字符串转为 WebSocketMessage */
    private WebSocketMessage toMessage(WebSocketSession session, String text) {
        return session.textMessage(text);
    }
}
