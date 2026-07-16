package com.example.webflux.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

/**
 * WebClient 配置
 *
 * <h2>什么是 WebClient？</h2>
 * WebClient 是 Spring 5 引入的<b>响应式 HTTP 客户端</b>，替代传统的 RestTemplate。
 *
 * <p>对比：
 * <ul>
 *   <li>{@code RestTemplate} — 同步、阻塞（已标记废弃）</li>
 *   <li>{@code WebClient} — 异步、非阻塞、响应式（推荐）</li>
 * </ul>
 *
 * <h2>WebClient 核心方法</h2>
 * <pre>{@code
 * WebClient.create()
 *     .get()              // HTTP 方法
 *     .uri("/path")       // 请求路径
 *     .retrieve()         // 发送请求并获取响应
 *     .bodyToMono(T.class) // 将响应体转为 Mono<T>
 *
 * // 或者
 *     .exchangeToMono(response -> ...) // 完全手动处理响应
 * }</pre>
 *
 * <h2>本配置</h2>
 * 创建一个配置了超时、连接池的 WebClient Bean，供项目中需要调用外部 API 的场景使用。
 */
@Configuration
public class WebClientConfig {

    /**
     * 创建 WebClient Bean
     *
     * <p>底层使用 Reactor Netty 作为 HTTP 客户端。
     * 通过 HttpClient 可以精细控制连接超时、读写超时等参数。
     */
    @Bean
    public WebClient webClient() {
        // 配置底层 Netty HTTP 客户端
        HttpClient httpClient = HttpClient.create()
                // 连接超时
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                // 响应超时
                .responseTimeout(Duration.ofSeconds(10))
                // 添加读写超时处理器
                .doOnConnected(conn ->
                        conn.addHandlerLast(new ReadTimeoutHandler(10))
                                .addHandlerLast(new WriteTimeoutHandler(10))
                );

        return WebClient.builder()
                // 服务自身的 baseUrl（用于演示 WebClient 调用自己的接口）
                .baseUrl("http://localhost:8080")
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                // 全局默认 header
                .defaultHeader("X-App", "webflux-practice")
                // 过滤器：记录请求日志
                .filter((request, next) -> {
                    System.out.println("🌐 [WebClient] 请求: " + request.method() + " " + request.url());
                    return next.exchange(request);
                })
                .build();
    }
}
