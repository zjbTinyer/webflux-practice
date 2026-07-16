package com.example.webflux;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;

/**
 * WebFlux 响应式编程练习项目 —— 启动类
 *
 * <h2>什么是 WebFlux？</h2>
 * Spring WebFlux 是 Spring 5 引入的响应式 Web 框架，基于 Reactor 库实现。
 * 与传统的 Spring MVC (Servlet 阻塞模型) 不同，WebFlux 运行在非阻塞的 Netty 服务器上。
 *
 * <h2>核心概念</h2>
 * <ul>
 *   <li><b>Mono&lt;T&gt;</b> — 发布 0~1 个元素的响应式流（类似 Java Optional）</li>
 *   <li><b>Flux&lt;T&gt;</b> — 发布 0~N 个元素的响应式流（类似 Java Stream）</li>
 *   <li><b>背压(Backpressure)</b> — 下游控制上游生产速度的机制</li>
 *   <li><b>R2DBC</b> — Reactive Relational Database Connectivity，响应式数据库驱动</li>
 * </ul>
 *
 * <h2>本项目的练习目标</h2>
 * 通过 CRUD + SSE + WebSocket + 各种操作符，掌握 WebFlux 核心用法。
 *
 * @author webflux-practice
 */
@SpringBootApplication
@EnableR2dbcRepositories  // 启用 R2DBC 响应式仓库扫描
public class WebfluxPracticeApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebfluxPracticeApplication.class, args);
    }
}
