# ⚡ WebFlux 响应式编程练习项目

Spring Boot 3.2 + WebFlux + R2DBC + H2 后端 | React 18 + TypeScript 前端

## 📖 项目简介

本项目是一个**全栈响应式编程练习项目**，旨在全面掌握 Spring WebFlux 和 Reactor 的响应式编程范式。项目包含：

- 🔙 **后端**：Spring Boot 3.2 + WebFlux + R2DBC + H2 内存数据库
- 🔜 **前端**：React 18 + TypeScript + Vite
- 🌐 **通信**：RESTful API + SSE（Server-Sent Events）+ WebSocket

后端所有接口返回类型都是 `Mono<T>` 或 `Flux<T>`，涵盖了 Reactive Streams 的绝大多数操作符，每个方法都有详细的中文注释解释操作符用途。

---

## 🚀 快速启动

### 环境要求

- JDK 17+
- Maven 3.8+
- Node.js 24+（使用机器安装的版本：`node --version`）
- npm 11+（随 Node.js 自带）

### 1. 启动后端（端口 8080）

```bash
cd backend
mvn spring-boot:run
```

启动后会自动：
- 执行 `schema.sql` 创建 `users` 表
- 通过 `DataInitializer` 插入 8 条示例数据

验证后端启动成功：

```bash
curl http://localhost:8080/api/users
```

### 2. 启动前端（端口 3000）

```bash
cd frontend
npm install
npm run dev
```

浏览器访问 `http://localhost:3000`。

前端通过 Vite 代理将 `/api` 和 `/ws` 请求转发到后端 8080 端口。

---

## 📁 项目结构

```
webflux-practice/
├── README.md
├── .gitignore
├── backend/                                  # Spring Boot 后端
│   ├── pom.xml                               # Maven 配置
│   └── src/main/java/com/example/webflux/
│       ├── WebfluxPracticeApplication.java   # 启动类
│       ├── config/
│       │   ├── DataInitializer.java          # 数据初始化（8条用户）
│       │   ├── RouterConfig.java             # 函数式端点路由配置
│       │   ├── WebClientConfig.java          # WebClient Bean 配置
│       │   └── WebSocketConfig.java          # WebSocket 路由映射
│       ├── controller/
│       │   └── UserController.java           # 注解方式 Controller
│       ├── dto/
│       │   ├── Result.java                   # 统一响应体
│       │   └── UserDTO.java                  # 用户 DTO
│       ├── entity/
│       │   └── User.java                     # 用户实体
│       ├── handler/
│       │   ├── UserHandler.java              # 函数式端点 Handler
│       │   └── ReactiveWebSocketHandler.java # WebSocket 处理器
│       ├── repository/
│       │   └── UserRepository.java           # R2DBC 响应式仓库
│       ├── service/
│       │   ├── UserService.java              # 服务接口
│       │   └── impl/
│       │       └── UserServiceImpl.java      # 核心实现（20+ 操作符）
│       └── util/
│           └── ReactiveUtils.java            # 独立操作符演示工具类
└── frontend/                                 # React 前端
    ├── package.json
    ├── vite.config.ts                        # Vite 配置（含代理）
    ├── tsconfig.json
    └── src/
        ├── main.tsx                          # 入口
        ├── App.tsx                           # 主应用组件
        ├── App.css                           # 全局样式
        ├── api/
        │   └── userApi.ts                    # API 请求封装
        ├── hooks/
        │   └── useSSE.ts                     # SSE 消费 Hook
        └── components/
            ├── UserList.tsx                  # 用户列表
            ├── UserForm.tsx                  # 创建/编辑表单
            ├── SSEDisplay.tsx                # SSE 数据展示
            └── DemoPanel.tsx                 # 操作符演示面板
```

---

## 🎯 涵盖的 WebFlux/Reactor 操作符

### 一、创建操作符

| 操作符 | 说明 | 示例 |
|--------|------|------|
| `Mono.just()` | 包装已知值（eager） | `Mono.just("hello")` |
| `Mono.justOrEmpty()` | 值可为 null，null → empty | `Mono.justOrEmpty(null)` |
| `Mono.defer()` | 惰性求值（每次订阅重新计算） | `Mono.defer(() -> Mono.just(System.currentTimeMillis()))` |
| `Mono.fromCallable()` | 包装可能抛异常的同步调用 | `Mono.fromCallable(() -> db.findById(1L))` |
| `Mono.error()` | 创建错误流 | `Mono.error(new RuntimeException())` |
| `Flux.just()` | 多个已知元素 | `Flux.just("A", "B", "C")` |
| `Flux.range()` | 整数范围 | `Flux.range(1, 10)` |
| `Flux.fromIterable()` | 从集合创建 | `Flux.fromIterable(list)` |
| `Flux.interval()` | 定时发射（适合心跳） | `Flux.interval(Duration.ofSeconds(2))` |

### 二、转换操作符

| 操作符 | 说明 | 关键区别 |
|--------|------|----------|
| `map` | 同步转换 T → R | 纯计算，无异步 |
| `flatMap` | 异步转换 + 展平 | **并行执行**，顺序不确定 |
| `flatMapMany` | Mono → Flux 转换 | 1个元素变为多个 |
| `concatMap` | 异步转换 + 顺序 | **严格顺序**，前一个完成才处理下一个 |
| `flatMapSequential` | 异步转换 + 保持原顺序 | 并行执行，但输出排回原序 |

### 三、过滤操作符

| 操作符 | 说明 |
|--------|------|
| `filter` | 条件过滤 |
| `take(n)` | 只取前 n 个 |
| `skip(n)` | 跳过前 n 个 |
| `distinct` | 去重 |
| `sort` | 排序 |

### 四、合并与组合

| 操作符 | 说明 |
|--------|------|
| `zip` | 等待所有源完成，对齐合并（类似"我等你，你等我"） |
| `merge` | 并行合并，元素交错（不保证顺序） |
| `concat` | 顺序合并，等前一个源完成才开始下一个 |
| `collectList` | Flux → Mono\<List\> |
| `collectMap` | Flux → Mono\<Map\> |
| `reduce` | 归约（类似 Stream.reduce） |
| `scan` | 累计（输出每步中间值） |

### 五、错误处理

| 操作符 | 说明 |
|--------|------|
| `onErrorReturn(value)` | 错误时返回固定默认值 |
| `onErrorResume(fn)` | 错误时切换到备选流 |
| `onErrorMap(fn)` | 异常类型转换 |
| `retry(n)` | 失败后重试 n 次 |
| `timeout(duration)` | 超时控制 |

### 六、副作用（Peeking）

| 操作符 | 触发时机 |
|--------|----------|
| `doOnSubscribe` | 被订阅时 |
| `doOnNext` | 每个元素通过时 |
| `doOnError` | 出错时 |
| `doOnComplete` | 正常完成时（Flux） |
| `doOnSuccess` | 正常完成时（Mono） |
| `doOnCancel` | 取消订阅时 |
| `doFinally` | 无论如何终止时 |
| `doOnRequest` | 下游请求数据时（监测背压） |

### 七、条件与逻辑

| 操作符 | 说明 |
|--------|------|
| `defaultIfEmpty(value)` | 为空时发默认值 |
| `switchIfEmpty(alt)` | 为空时切换到备选流 |
| `then()` | 忽略结果，返回 Mono\<Void\> |
| `thenReturn(value)` | 忽略结果，返回固定值 |
| `thenMany(flux)` | 忽略结果，切换到 Flux |

### 八、调度器与背压

| 操作符 / 概念 | 说明 |
|---------------|------|
| `publishOn(scheduler)` | 切换**下游**操作符的执行线程 |
| `subscribeOn(scheduler)` | 切换**上游**订阅和源的执行线程 |
| `Schedulers.parallel()` | CPU 密集任务 |
| `Schedulers.boundedElastic()` | I/O 密集任务 |
| `limitRate(n)` | 限制每次请求的元素数（背压） |

### 九、响应式上下文

| 操作符 | 说明 |
|--------|------|
| `contextWrite` | 向 Context 写入键值对 |
| `deferContextual` | 读取 Context 中的值 |

> 💡 Context 是 Reactor 中替代 ThreadLocal 的机制，用于在响应式链中传递请求级信息。

---

## 🔌 API 接口列表

### 注解方式（`/api/users`）

| 方法 | 路径 | 返回类型 | 说明 |
|------|------|----------|------|
| GET | `/api/users` | `Flux<User>` | 查询全部用户 |
| GET | `/api/users/{id}` | `Mono<Result<User>>` | 按 ID 查询 |
| POST | `/api/users` | `Mono<Result<User>>` | 创建用户 |
| PUT | `/api/users/{id}` | `Mono<Result<User>>` | 更新用户 |
| DELETE | `/api/users/{id}` | `Mono<Result<Void>>` | 删除用户 |
| GET | `/api/users/search?name=xxx` | `Flux<User>` | 按名称搜索 |
| GET | `/api/users/age/{age}` | `Flux<User>` | 按年龄过滤 |
| GET | `/api/users/email?keyword=xxx` | `Flux<User>` | 邮箱模糊搜索 |
| POST | `/api/users/batch` | `Flux<User>` | 批量创建 |
| GET | `/api/users/{id}/fallback` | `Mono<Result<User>>` | 带兜底逻辑的查询 |

### SSE 端点

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/users/stream` | 每秒推送1个用户（演示 delayElements + limitRate） |
| GET | `/api/users/heartbeat` | 每2秒推送心跳（演示 Flux.interval） |

### 操作符演示端点

| 方法 | 路径 | 演示内容 |
|------|------|----------|
| GET | `/api/users/demo/zip` | Mono.zip 合并两个异步查询 |
| GET | `/api/users/demo/merge` | Flux.merge 并行合并（SSE 展示交错效果） |
| GET | `/api/users/demo/collect` | collectList 聚合 |
| GET | `/api/users/demo/error/{id}` | timeout + retry + onErrorMap |
| GET | `/api/users/demo/scheduler` | publishOn + subscribeOn 线程切换 |
| GET | `/api/users/demo/context` | Context 写入与读取 |

### 函数式端点（`/api/func/users`）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/func/users` | 查询全部（函数式 Router） |
| GET | `/api/func/users/{id}` | 按 ID 查询 |
| POST | `/api/func/users` | 创建用户 |
| PUT | `/api/func/users/{id}` | 更新用户 |
| DELETE | `/api/func/users/{id}` | 删除用户 |

### WebSocket

| 协议 | 路径 | 说明 |
|------|------|------|
| WS | `ws://localhost:8080/ws/chat` | 群聊 + 定时心跳 + 上下线通知 |

---

## 🧪 关键概念速查

### Mono vs Flux

```
Mono<T>  →  0..1 个元素（类似 Optional）
Flux<T>  →  0..N 个元素（类似 Stream）
```

### flatMap vs concatMap

```
flatMap:   [A B C] → ┐→ A→  ┐ → 可能输出 B,A,C（并行）
                      → B→
                      └→ C→  ┘

concatMap: [A B C] → A→ B→ C→ → 输出 A,B,C（顺序）
```

### map vs flatMap

```
map:      String → Integer        (同步转换，不改变容器)
flatMap:  String → Mono<Integer>  (异步转换，展平 Mono<Mono<T>>)
```

### zip vs merge

```
zip:   等所有人都好了 → 一起组合
merge: 谁先好谁先走 → 交替输出
```

---

## 🛠️ 技术栈

| 层级 | 技术 | 版本 |
|------|------|------|
| 后端框架 | Spring Boot | 3.2.5 |
| 响应式 Web | Spring WebFlux | 6.1.6 |
| 响应式 ORM | Spring Data R2DBC | 3.2.5 |
| 数据库驱动 | r2dbc-h2 | 1.0.0 |
| 数据库 | H2（内存模式） | 2.2.224 |
| HTTP 客户端 | Reactor Netty | - |
| 构建工具 | Maven | 3.x |
| Java | JDK | 17+ |
| 前端框架 | React | 18.3 |
| 语言 | TypeScript | 5.5 |
| 构建工具 | Vite | 5.4 |
| Node.js | 运行时 | 24+（系统安装） |
| npm | 包管理器 | 11+（系统安装） |
| 代理 | Vite Proxy | - |

---

## 📝 学习路径建议

1. **入门**：先读 `UserServiceImpl` 的前几个方法，理解 `map`、`flatMap`、`Mono`、`Flux` 的基本用法
2. **进阶**：研究 `batchCreate` 理解 `concatMap` vs `flatMap` 的区别
3. **错误处理**：看 `findByIdWithFallback` 和 `errorHandlingDemo` 理解错误处理链路
4. **组合**：看 `zipDemo`、`mergeDemo` 理解多流合并
5. **SSE**：启动后端，用前端 SSE 组件观察流式数据推送
6. **WebSocket**：用多浏览器窗口连接 `/ws/chat` 体验响应式群聊
7. **函数式端点**：对比 `UserController`（注解）和 `UserHandler + RouterConfig`（函数式）两种写法
8. **工具类**：运行 `ReactiveUtils` 中的独立示例

---

## 📄 License

MIT — 仅供学习练习使用
