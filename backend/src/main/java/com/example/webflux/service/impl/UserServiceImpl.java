package com.example.webflux.service.impl;

import com.example.webflux.dto.UserDTO;
import com.example.webflux.entity.User;
import com.example.webflux.repository.UserRepository;
import com.example.webflux.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuple2;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * 用户服务实现 —— WebFlux 操作符综合演示
 *
 * <h2>响应式编程核心思想</h2>
 * <pre>
 * 传统命令式:
 *   User user = userRepository.findById(1L);  // 阻塞等待结果
 *   return user;
 *
 * 响应式:
 *   Mono&lt;User&gt; userMono = userRepository.findById(1L);  // 不阻塞，只是声明了一个"未来操作"
 *   return userMono.map(user -> { ... });  // 链式声明数据转换规则
 * </pre>
 *
 * <h2>本类涵盖的 WebFlux/Reactor 操作符</h2>
 * <ol>
 *   <li>map —— 同步转换</li>
 *   <li>flatMap —— 异步转换（展平 Mono&lt;Mono&lt;T&gt;&gt;）</li>
 *   <li>flatMapMany —— Mono 转 Flux</li>
 *   <li>filter —— 条件过滤</li>
 *   <li>defaultIfEmpty —— 空值默认值</li>
 *   <li>switchIfEmpty —— 空值时切换到备选流</li>
 *   <li>doOnNext —— 副作用：每个元素通过时触发</li>
 *   <li>doOnSuccess / doOnError / doFinally —— 生命周期钩子</li>
 *   <li>onErrorReturn —— 出错时返回默认值</li>
 *   <li>onErrorResume —— 出错时切换到备选流</li>
 *   <li>onErrorMap —— 异常类型转换</li>
 *   <li>transform / transformDeferred —— 操作符组合复用</li>
 *   <li>zip / zipWith —— 合并多个流</li>
 *   <li>concatMap —— 顺序执行</li>
 *   <li>delayElements —— 延迟发射</li>
 *   <li>timeout —— 超时控制</li>
 *   <li>retry —— 重试</li>
 *   <li>publishOn / subscribeOn —— 线程调度</li>
 *   <li>collectList —— Flux 转 Mono&lt;List&gt;</li>
 *   <li>then / thenReturn / thenMany —— 顺序执行</li>
 *   <li>merge —— 合并流（并行）</li>
 *   <li>limitRate —— 背压控制</li>
 * </ol>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    // ======================================================================
    // 一、基础 CRUD —— 演示 map、flatMap、filter、defaultIfEmpty 等基础操作符
    // ======================================================================

    /**
     * 🟢 Mono + map + switchIfEmpty —— 创建用户
     *
     * <pre>
     * 操作符链路:
     *   DTO → 转 Entity → 保存到数据库 → 包装结果
     * </pre>
     */
    @Override
    public Mono<User> create(UserDTO dto) {
        log.info("🔵 [create] 开始创建用户: {}", dto.getName());

        // ⚠️ 关键：使用 Mono.just() 或 Mono.fromCallable() 包装非响应式代码
        // Mono.just() 是立即求值的（eager），Mono.fromCallable() 是惰性求值的（lazy）
        return Mono.just(dto)
                // 【map 操作符】同步转换：DTO → Entity
                .map(d -> User.builder()
                        .name(d.getName())
                        .email(d.getEmail())
                        .age(d.getAge())
                        .createdAt(LocalDateTime.now())
                        .build())
                // 【doOnNext 操作符】副作用：打印日志，不影响流中的数据
                .doOnNext(user -> log.debug("✅ [create] DTO 转换完成: {}", user))
                // 【flatMap 操作符】异步转换：保存到数据库
                // flatMap 将 Mono<User> 展平为 Mono<User>（而不是 Mono<Mono<User>>）
                .flatMap(user -> userRepository.save(user))
                // 【doOnSuccess 操作符】不管成功还是失败都会触发
                .doOnSuccess(saved -> log.info("✅ [create] 用户创建成功: ID={}", saved != null ? saved.getId() : "null"));
    }

    /**
     * 🟢 Mono + switchIfEmpty —— 按 ID 查询
     *
     * <p>{@code switchIfEmpty} 是 key operator：当上游 Mono 为空时，切换到备选 Mono
     */
    @Override
    public Mono<User> findById(Long id) {
        log.info("🔍 [findById] 查询用户: ID={}", id);

        return userRepository.findById(id)
                // 【switchIfEmpty 操作符】如果数据库中没有该 ID 的记录，Mono 会是空的
                // 此时切换到 error Mono，统一处理"未找到"的情况
                .switchIfEmpty(Mono.error(
                        new IllegalArgumentException("用户不存在: ID=" + id)))
                .doOnNext(user -> log.debug("✅ [findById] 查询成功: {}", user.getName()));
    }

    /**
     * 🟢 Flux + collectList —— 查询所有用户
     *
     * <p>Flux 是 0~N 元素的响应式流，collectList() 把所有元素收集到一个 List 里
     */
    @Override
    public Flux<User> findAll() {
        log.info("📋 [findAll] 查询所有用户");

        return userRepository.findAll()
                .doOnNext(user -> log.debug("   📄 {}", user.getName()));
    }

    /**
     * 🟢 Mono + zip + flatMap —— 更新用户
     *
     * <p>{@code zip} 用于合并两个独立的结果：
     * 一个来自查旧数据，一个来自保存新数据
     */
    @Override
    public Mono<User> update(Long id, UserDTO dto) {
        log.info("✏️  [update] 更新用户: ID={}", id);

        // 【zip 操作符】同时获取"旧用户"和"ID确保存在"
        // zip 会等待两个 Mono 都完成，然后组合它们的结果
        return Mono.zip(
                        // 检查用户是否存在
                        findById(id),  // ← 这里复用了上面的 findById，已经带了错误处理
                        // 新数据
                        Mono.just(dto)
                )
                // zip 返回 Tuple2<User, UserDTO>
                .flatMap(tuple -> {
                    User existing = tuple.getT1();
                    UserDTO newData = tuple.getT2();
                    // 更新字段
                    existing.setName(newData.getName());
                    existing.setEmail(newData.getEmail());
                    existing.setAge(newData.getAge());
                    return userRepository.save(existing);
                })
                .doOnSuccess(updated -> log.info("✅ [update] 更新成功: ID={}", updated.getId()))
                // 【onErrorResume 操作符】出错时返回备选流
                .onErrorResume(e -> {
                    log.error("❌ [update] 更新失败: {}", e.getMessage());
                    return Mono.error(e);  // 这里也可以返回兜底值
                });
    }

    /**
     * 🟢 Mono + then + thenReturn —— 删除用户
     *
     * <p>重点理解：
     * <ul>
     *   <li>{@code then()} — 忽略上游结果，返回 Mono&lt;Void&gt;</li>
     *   <li>{@code thenReturn(value)} — 忽略上游结果，返回固定的值</li>
     *   <li>{@code thenMany(flux)} — 忽略上游结果，切换到另一个 Flux</li>
     * </ul>
     */
    @Override
    public Mono<Void> delete(Long id) {
        log.info("🗑️  [delete] 删除用户: ID={}", id);

        return findById(id)                          // Mono<User>
                .flatMap(user -> userRepository.deleteById(id))  // Mono<Void>
                // 【then 操作符】删除成功后，什么也不返回
                .then();
    }

    // ======================================================================
    // 二、高级查询 —— 演示 flatMapMany、filter、sort、默认值处理
    // ======================================================================

    /**
     * 🟠 flatMapMany —— 按名称搜索
     *
     * <p>{@code flatMapMany} 将 Mono 转换为 Flux（1个 → N个）
     */
    @Override
    public Flux<User> searchByName(String name) {
        log.info("🔍 [searchByName] 按名称搜索: {}", name);

        // flatMapMany：输入 Mono<String> → 输出 Flux<User>
        return Mono.just(name)
                // 【filter 操作符】过滤空输入
                .filter(n -> n != null && !n.isBlank())
                // 【switchIfEmpty】空输入时的处理
                .switchIfEmpty(Mono.error(new IllegalArgumentException("搜索关键字不能为空")))
                // 【flatMapMany 操作符】Mono → Flux 转换
                .flatMapMany(n -> userRepository.findByName(n)
                        // 【defaultIfEmpty】如果 Flux 没有元素，发出一个默认值
                        .defaultIfEmpty(
                                User.builder().name("未找到匹配用户").email("-").build())
                );
    }

    /**
     * 🟠 filter + sort —— 按年龄过滤
     *
     * <p>Flux 的 filter 类似 Java Stream 的 filter
     */
    @Override
    public Flux<User> findByAgeGreaterThan(int age) {
        log.info("🔍 [findByAgeGreaterThan] 年龄 > {}", age);

        return userRepository.findByAgeGreaterThan(age)
                // 【sort 操作符】自然排序（按 ID）
                .sort((a, b) -> Long.compare(a.getId(), b.getId()))
                // 【switchIfEmpty】没有符合条件的用户
                .switchIfEmpty(Flux.just(
                        User.builder().name("没有年龄 > " + age + " 的用户").email("-").age(0).build()
                ));
    }

    /**
     * 🟠 flatMapMany + collectList + defaultIfEmpty —— 邮箱模糊搜索
     */
    @Override
    public Flux<User> searchByEmail(String keyword) {
        log.info("🔍 [searchByEmail] 邮箱搜索: {}", keyword);

        return Mono.justOrEmpty(keyword)
                // 【flatMapMany 操作符】Mono → Flux
                .flatMapMany(k -> userRepository.searchByEmail(k))
                .switchIfEmpty(Flux.just(
                        User.builder().name("未找到匹配邮箱").email(keyword).build()
                ));
    }

    // ======================================================================
    // 三、批量操作 —— 演示 concatMap vs flatMap、collectList
    // ======================================================================

    /**
     * 🔴 concatMap vs flatMap 对比
     *
     * <p>这是 WebFlux 中最重要的两个操作符的区别：
     *
     * <pre>
     * flatMap:
     *   输入: A → B → C
     *   处理: 内部异步操作可能交错执行
     *   输出: 不保证顺序 (A→上→B→上→C→上 → 结果可能是 B,A,C)
     *
     * concatMap:
     *   输入: A → B → C
     *   处理: 必须前一个完成才处理下一个
     *   输出: 严格保持输入顺序 (A→B→C)
     * </pre>
     *
     * 保存用户时我们用 concatMap 保证顺序，
     * 但如果用 flatMap 则可以并行保存（更快但无序）。
     */
    @Override
    public Flux<User> batchCreate(Flux<UserDTO> dtoStream) {
        log.info("📦 [batchCreate] 批量创建用户");

        return dtoStream
                // 【index 操作符】给每个元素添加索引：(index, value)
                .index()
                // 【concatMap 操作符】顺序保存（可以用 flatMap 替代来并行保存）
                .concatMap(tuple -> {
                    long index = tuple.getT1();
                    UserDTO dto = tuple.getT2();
                    log.debug("   📝 [batchCreate] 处理第 {} 个: {}", index + 1, dto.getName());
                    return create(dto);
                })
                .doOnComplete(() -> log.info("✅ [batchCreate] 批量创建完成"));
    }

    /**
     * 🔴 transform —— 使用 transform 复用操作符组合
     *
     * <p>{@code transform} 允许你将一系列操作符封装成一个 Function，
     * 在多个地方复用。它本质上是一个 compose function。
     */
    @Override
    public Mono<User> findByIdWithFallback(Long id) {
        return userRepository.findById(id)
                // 【transform 操作符】应用一个"操作符组合函数"
                // applyFallbackLogic 是一个 Function<Mono<T>, Mono<T>>
                .transform(this::applyFallbackLogic);
    }

    /**
     * 可复用的兜底逻辑
     *
     * <p>这就是 "函数式组合" 的精髓：
     * 将常用的响应式链路封装成一个方法，通过 transform 在多处复用。
     *
     * <p>对比 {@code transformDeferred}：transform 是 eager 的（在装配时计算），
     * transformDeferred 是 lazy 的（每次订阅时重新计算）。
     */
    private Mono<User> applyFallbackLogic(Mono<User> mono) {
        return mono
                // 【delayElement 操作符】模拟延迟，测试超时场景
                // .delayElement(Duration.ofSeconds(5))  // 取消注释可测试 timeout 效果
                // 【timeout 操作符】设置超时时间
                .timeout(Duration.ofSeconds(3))
                // 超时时触发
                .onErrorResume(java.util.concurrent.TimeoutException.class, e -> {
                    log.warn("⏰ 查询超时，返回兜底用户");
                    Mono<User> fallback = Mono.just(
                            User.builder()
                                    .id(-1L)
                                    .name("兜底用户（查询超时）")
                                    .email("fallback@timeout.com")
                                    .age(0)
                                    .build()
                    );
                    // 延迟一点再返回兜底值，模拟 fallback 系统的响应时间
                    return fallback.delayElement(Duration.ofMillis(500));
                });
    }

    /**
     * 🟢 获取用户总数
     */
    @Override
    public Mono<Long> count() {
        return userRepository.count()
                .doOnNext(c -> log.debug("📊 [count] 用户总数: {}", c));
    }

    // ======================================================================
    // 四、SSE 演示方法（由 Controller 调用）
    // ======================================================================

    /**
     * 🔴 SSE 流式推送 —— 延迟发射 + 背压控制
     *
     * <p>SSE (Server-Sent Events) 是 HTTP 上从服务端向客户端推送事件的协议。
     * 在 WebFlux 中，Controller 返回 {@code Flux<ServerSentEvent<T>>} 或直接返回
     * {@code Flux<T>} + 设置 Content-Type 为 text/event-stream。
     *
     * <p>本方法演示的操作符：
     * <ul>
     *   <li>{@code delayElements} — 按间隔逐个发射元素</li>
     *   <li>{@code limitRate} — 背压控制，限制每次请求的元素数</li>
     *   <li>{@code doOnRequest} — 当下游请求数据时触发</li>
     * </ul>
     */
    public Flux<User> streamAllUsers() {
        log.info("📡 [SSE] 开始流式推送所有用户");

        return userRepository.findAll()
                // 【delayElements 操作符】每秒发射一个用户，模拟流式推送
                .delayElements(Duration.ofSeconds(1))
                // 【limitRate 操作符】背压控制：预取 5 个元素
                .limitRate(5)
                // 【doOnRequest 操作符】当下游请求 n 个元素时触发（用于监控背压）
                .doOnRequest(n -> log.debug("📡 [SSE] 下游请求 {} 个元素", n))
                .doOnNext(user -> log.debug("📡 [SSE] 推送: {}", user.getName()))
                .doOnCancel(() -> log.info("📡 [SSE] 客户端断开连接"));
    }

    /**
     * 🔴 interval 演示 —— 使用 Flux.interval 周期发送心跳
     *
     * <p>适合做在线人数统计、系统状态监控等场景
     */
    public Flux<String> heartbeatStream() {
        // 【Flux.interval 操作符】每 2 秒发射一个递增数字
        return Flux.interval(Duration.ofSeconds(2))
                // 【map 操作符】将数字转换为消息
                .map(tick -> "❤️ 心跳 #" + (tick + 1)
                        + " - 当前时间: " + LocalDateTime.now()
                        + " - 在线用户数: " + (int)(Math.random() * 100 + 1))
                // 【doOnSubscribe 操作符】订阅时触发
                .doOnSubscribe(s -> log.info("📡 [Heartbeat] 心跳流开始"));
    }

    // ======================================================================
    // 五、综合演示方法 —— zip/merge/concat/collectList 等高级操作符
    // ======================================================================

    /**
     * 🟣 zip —— 合并两个异步结果
     *
     * <p>zip 等待所有源完成，然后组合它们的结果。
     * 类似于"我等你，你等我，我们都好了再一起处理"。
     */
    public Mono<String> zipDemo() {
        // 两个独立的异步查询
        Mono<Long> countMono = userRepository.count();
        Mono<User> firstUserMono = userRepository.findAll()
                .next()  // 取第一个元素，返回 Mono<User>
                .defaultIfEmpty(User.builder().name("无用户").email("-").build());

        // zip 合并
        return Mono.zip(countMono, firstUserMono)
                .map(tuple -> String.format(
                        "用户总数: %d, 第一个用户: %s",
                        tuple.getT1(),
                        tuple.getT2().getName()
                ));
    }

    /**
     * 🟣 merge —— 合并多个 Flux（并行）
     *
     * <p>merge 与 concat 的区别：
     * <ul>
     *   <li>merge — 并行订阅所有源，元素按"谁先完成就先用谁"的顺序输出</li>
     *   <li>concat — 顺序订阅，必须等前一个源完成才开始下一个</li>
     * </ul>
     */
    public Flux<String> mergeDemo() {
        Flux<String> source1 = Flux.just("🍎", "🍊", "🍋")
                .delayElements(Duration.ofMillis(300));  // 每300ms发一个

        Flux<String> source2 = Flux.just("🍇", "🍓", "🍒")
                .delayElements(Duration.ofMillis(200));  // 每200ms发一个

        // 用 merge 合并，source2 的元素会先到（因为它间隔更短）
        // 如果换成 concat，则 source1 全部完成才轮到 source2
        return Flux.merge(source1, source2);
    }

    /**
     * 🟣 collectList —— 将 Flux 转换为 Mono<List>
     *
     * <p>适用于需要一次性获取所有数据的场景
     */
    public Mono<String> collectListDemo() {
        return userRepository.findAll()
                .map(User::getName)
                // 【collectList】将 Flux<String> 收集为 Mono<List<String>>
                .collectList()
                .map(names -> String.join(", ", names));
    }

    /**
     * 🟣 retry + onErrorMap —— 错误处理演示
     *
     * <p>常见错误处理策略链:
     * <pre>
     *   .timeout(…)           ← 超时了怎么办
     *   .retry(3)             ← 失败了重试几次
     *   .onErrorResume(…)     ← 还不行就降级
     *   .onErrorMap(…)        ← 或者转换为业务异常
     * </pre>
     */
    public Mono<User> errorHandlingDemo(Long id) {
        return userRepository.findById(id)
                // 【timeout】1 秒超时
                .timeout(Duration.ofSeconds(1))
                // 【retry 操作符】失败后最多重试 2 次
                .retry(2)
                // 【onErrorMap】将底层异常转换为更友好的业务异常
                .onErrorMap(e -> {
                    if (e instanceof java.util.concurrent.TimeoutException) {
                        return new RuntimeException("查询用户超时，已重试2次", e);
                    }
                    return new RuntimeException("查询用户失败: " + e.getMessage(), e);
                })
                .switchIfEmpty(Mono.error(new RuntimeException("用户不存在: ID=" + id)));
    }

    /**
     * 🟣 响应式上下文 —— Context 的写入与读取
     *
     * <p>在 WebFlux 中，传统的 ThreadLocal 不再有效（因为线程会切换），
     * 需要改用 Reactor 的 Context 来传递请求级上下文信息（如 traceId、租户ID等）
     */
    public Mono<String> contextDemo() {
        return Mono.just("hello")
                // 【flatMap + deferContextual】读取 Context 中的值
                .flatMap(s -> Mono.deferContextual(ctx -> {
                    // 从 Context 中读取值（如果有的话）
                    String traceId = ctx.getOrDefault("traceId", "unknown");
                    return Mono.just(s + " [traceId=" + traceId + "]");
                }));
        // 注意：Context 的写入通常在 Controller 或 Filter 层通过 .contextWrite() 完成
    }

    /**
     * 🟣 subscribeOn / publishOn —— 线程调度演示
     *
     * <pre>
     * subscribeOn: 影响"整个链"从源头开始运行在哪个调度器上
     * publishOn:  影响"之后的"操作符运行在哪个调度器上（可以多次调用切换）
     * </pre>
     */
    public Mono<String> schedulerDemo() {
        return Mono.fromCallable(() -> {
                    // 这段代码受 subscribeOn 影响
                    log.info("🟡 [Scheduler] fromCallable 线程: {}", Thread.currentThread().getName());
                    return "data";
                })
                // subscribeOn: 指定源在哪个调度器上运行
                .subscribeOn(Schedulers.boundedElastic())
                // publishOn: 之后的操作切换到 parallel 调度器
                .publishOn(Schedulers.parallel())
                .map(s -> {
                    log.info("🟢 [Scheduler] map 线程: {}", Thread.currentThread().getName());
                    return s.toUpperCase();
                })
                .doOnSuccess(s -> log.info("✅ [Scheduler] 完成, 线程: {}", Thread.currentThread().getName()));
    }
}
