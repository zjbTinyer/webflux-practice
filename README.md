# ⚡ WebFlux 响应式编程项目

Spring Boot 3.2 + WebFlux + R2DBC + PostgreSQL | React 19 + TypeScript + Tailwind CSS

## 📖 项目简介

全栈响应式编程项目，涵盖 WebFlux/Reactor 核心操作符实战。支持 **H2 快速开发** 和 **PostgreSQL 生产部署** 两种模式。

- 🔙 **后端**：Spring Boot 3.2 + WebFlux + R2DBC + Flyway 迁移 + SpringDoc + Actuator
- 🔜 **前端**：React 19 + TypeScript + Tailwind CSS + shadcn/ui + React Router + TanStack Query
- 🌐 **通信**：RESTful API + SSE + WebSocket
- 🐳 **部署**：Docker Compose 一键启动（PostgreSQL + Backend + Nginx Frontend）

---

## 🚀 快速启动

### 方式一：Docker Compose（推荐，一键启动）

```bash
cp .env.example .env
docker compose up -d
```

访问 `http://localhost`，所有服务自动启动。

### 方式二：本地开发（H2 内存数据库）

**环境要求**：JDK 17+ / Maven 3.8+ / Node.js 24+ / npm 11+

**启动后端（端口 8080）**：
```bash
cd backend && mvn spring-boot:run
```
启动后 Flyway 自动建表，DataInitializer 插入 8 条测试数据。

**启动前端（端口 3000）**：
```bash
cd frontend && npm install && npm run dev
```
访问 `http://localhost:3000`。

### 运行测试

```bash
# 后端测试
cd backend && mvn test

# 前端测试
cd frontend && npm test
```

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
| 数据库迁移 | Flyway | 自动建表 |
| 数据库(dev) | H2 | 内存模式 |
| 数据库(prod) | PostgreSQL | 16 (Docker) |
| API 文档 | SpringDoc OpenAPI | 2.5.0 |
| 监控 | Actuator | Health/Metrics |
| HTTP 客户端 | Reactor Netty | - |
| 构建工具 | Maven | 3.x |
| Java | JDK | 17+ |
| 前端框架 | React | 19.2 |
| UI 组件 | shadcn/ui + Tailwind | 3.x |
| 状态管理 | TanStack Query | 5.x |
| 路由 | React Router | 7.x |
| 语言 | TypeScript | 5.5 |
| 构建工具 | Vite | 5.4 |
| Node.js | 运行时 | 24+（系统安装） |
| 部署 | Docker Compose | 3.8 |

---

## 📡 生产级端点

| 端点 | 说明 |
|------|------|
| Swagger UI | `http://localhost:8080/swagger-ui.html` |
| API Docs | `http://localhost:8080/api-docs` |
| Health | `http://localhost:8080/actuator/health` |
| Metrics | `http://localhost:8080/actuator/metrics` |

## 📄 License

MIT
