package com.example.webflux.util;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SignalType;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.List;
import java.util.function.Function;

/**
 * 响应式工具类 —— 独立演示各种操作符的使用
 *
 * <p>这个类不依赖 Spring Bean，可以直接在 main 方法或测试中使用。
 * 涵盖了最常用的 Reactor 核心操作符的独立示例。
 */
@Slf4j
public class ReactiveUtils {

    // ======================================================================
    // 1. Mono 基础操作
    // ======================================================================

    /**
     * Mono 的创建方式
     *
     * <pre>
     * Mono.just(T)         — 立即求值（已知值）
     * Mono.justOrEmpty(T)  — 值可能为 null，null → Mono.empty()
     * Mono.defer(() → X)  — 惰性求值（每次订阅时重新计算）
     * Mono.fromCallable()  — 包装可能抛异常的同步调用
     * Mono.fromRunnable()  — 包装 Runnable
     * Mono.empty()         — 空 Mono
     * Mono.error(Throwable)— 错误 Mono
     * Mono.never()         — 永远不会完成的 Mono
     * </pre>
     */
    public static void monoCreationExamples() {
        // just: 已知值直接包装
        Mono<String> mono1 = Mono.just("Hello WebFlux");

        // justOrEmpty: 自动处理 null
        Mono<String> mono2 = Mono.justOrEmpty(null);  // 结果是 Mono.empty()

        // defer: 惰性求值 — 每次订阅时才执行
        Mono<String> mono3 = Mono.defer(() -> Mono.just("当前时间: " + System.currentTimeMillis()));

        // fromCallable: 包装可能抛异常的阻塞代码
        Mono<String> mono4 = Mono.fromCallable(() -> {
            // 模拟耗时操作
            Thread.sleep(100);
            return "result";
        });

        // fromSupplier: 与 fromCallable 类似，但不抛受检异常
        Mono<Long> mono5 = Mono.fromSupplier(System::currentTimeMillis);

        log.info("Mono 创建示例已就绪");
    }

    // ======================================================================
    // 2. Flux 基础操作
    // ======================================================================

    /**
     * Flux 的创建方式
     *
     * <pre>
     * Flux.just(T...)         — 已知元素
     * Flux.fromArray(T[])     — 从数组
     * Flux.fromIterable(List) — 从集合
     * Flux.fromStream(Stream) — 从 Java Stream
     * Flux.range(start, n)    — 整数序列
     * Flux.interval(period)   — 定时发射递增 Long
     * Flux.generate(sink →)   — 同步生成（背压安全）
     * Flux.create(sink →)     — 异步生成（手动控制背压）
     * </pre>
     */
    public static void fluxCreationExamples() {
        // just: 多个已知元素
        Flux<String> flux1 = Flux.just("A", "B", "C");

        // range: 整数范围
        Flux<Integer> flux2 = Flux.range(1, 5);  // 1, 2, 3, 4, 5

        // fromIterable: 从集合创建
        Flux<String> flux3 = Flux.fromIterable(List.of("X", "Y", "Z"));

        // interval: 定时发射（需配合 Scheduler）
        Flux<Long> flux4 = Flux.interval(Duration.ofSeconds(1))
                .take(5);  // 只取前 5 个

        // generate: 同步生成（背压安全）
        Flux<Integer> flux5 = Flux.generate(
                () -> 0,              // 初始状态
                (state, sink) -> {    // 生成逻辑
                    if (state >= 3) {
                        sink.complete();  // 完成信号
                    } else {
                        sink.next(state * 10);  // 发射元素
                    }
                    return state + 1;  // 返回新状态
                }
        );

        // create: 异步生成（可以手动控制背压）
        Flux<String> flux6 = Flux.create(sink -> {
            sink.next("元素1");
            sink.next("元素2");
            sink.complete();
        });

        log.info("Flux 创建示例已就绪");
    }

    // ======================================================================
    // 3. 错误处理操作符
    // ======================================================================

    /**
     * 错误处理策略对比
     *
     * <pre>
     * onErrorReturn(value)     — 发生错误时返回固定值
     * onErrorResume(fallback)  — 发生错误时切换到备选 Mono/Flux
     * onErrorMap(transform)    — 将一种异常转换为另一种异常
     * onErrorComplete()        — 发生错误时直接完成（吞掉错误）
     * onErrorStop()            — 发生错误时停止，不再传递
     * retry(n)                 — 重试 n 次
     * retryWhen(…​)            — 条件重试
     * </pre>
     */
    public static void errorHandlingExamples() {
        Flux<Integer> source = Flux.just(1, 2, 0, 4)
                .map(i -> 10 / i);  // 当 i=0 时会抛出 ArithmeticException

        // 方式1：onErrorReturn — 错误时返回默认值
        source.onErrorReturn(-1)
                .subscribe(v -> log.info("onErrorReturn: {}", v));
        // 输出: 10, 5, -1（遇到错误后流终止）

        // 方式2：onErrorResume — 错误时切换到备选流
        source.onErrorResume(e -> {
                    log.warn("发生错误: {}，切换到备选值", e.getMessage());
                    return Flux.just(100, 200);
                })
                .subscribe(v -> log.info("onErrorResume: {}", v));
        // 输出: 10, 5, 100, 200

        // 方式3：retry — 自动重试
        Flux.just("data")
                .flatMap(d -> {
                    if (Math.random() > 0.3) {
                        return Mono.<String>error(new RuntimeException("随机失败"));
                    }
                    return Mono.just(d);
                })
                .retry(3)  // 最多重试 3 次
                .doOnError(e -> log.error("重试耗尽: {}", e.getMessage()))
                .subscribe();

        log.info("错误处理示例完成");
    }

    // ======================================================================
    // 4. 转换操作符
    // ======================================================================

    /**
     * map vs flatMap vs concatMap 对比
     *
     * <pre>
     * map(Function<T, R>)        — 同步转换 T → R
     * flatMap(T → Mono<R>)       — 异步转换 + 展平 + 并行
     * flatMap(T → Flux<R>)       — 异步转换 + 展平 + 并行
     * flatMapMany(T → Flux<R>)   — Mono 转 Flux
     * concatMap(T → Mono<R>)     — 异步转换 + 展平 + 顺序（前一个完成才处理下一个）
     * flatMapSequential(T → M)   — 异步转换 + 展平 + 保持原始顺序
     * </pre>
     */
    public static void mapVsFlatMapExample() {
        // map: 同步转换 String → Integer
        Flux<String> names = Flux.just("Alice", "Bob", "Charlie");
        names.map(String::length)
                .subscribe(len -> log.info("map: 名字长度 = {}", len));

        // flatMap: 异步查询（比如查数据库）
        names.flatMap(name -> {
                    // 模拟异步操作：查询该名字的用户数量
                    return Mono.just(name.length())
                            .delayElement(Duration.ofMillis(100))  // 模拟延迟
                            .map(len -> name + " 长度=" + len);
                })
                .subscribe(result -> log.info("flatMap: {}", result));

        // concatMap: 严格按顺序（前一个完成才处理下一个）
        names.concatMap(name -> {
                    log.info("  concatMap 处理: {}", name);
                    return Mono.just(name.length())
                            .delayElement(Duration.ofMillis(100));
                })
                .subscribe(len -> log.info("concatMap: {}", len));

        log.info("map/flatMap 示例完成");
    }

    // ======================================================================
    // 5. 过滤操作符
    // ======================================================================

    /**
     * 过滤操作符
     *
     * <pre>
     * filter(Predicate)     — 条件过滤
     * take(n)               — 只取前 n 个元素
     * takeLast(n)           — 只取最后 n 个元素
     * takeUntil(Predicate)  — 一直取，直到条件满足
     * takeWhile(Predicate)  — 一直取，直到条件不满足
     * skip(n)               — 跳过前 n 个元素
     * skipLast(n)           — 跳过最后 n 个元素
     * distinct()            — 去重
     * distinctUntilChanged()— 连续去重
     * elementAt(n)          — 取第 n 个元素
     * last()                — 取最后一个元素
     * </pre>
     */
    public static void filterExamples() {
        Flux<Integer> numbers = Flux.range(1, 20);

        // filter: 只保留偶数
        numbers.filter(n -> n % 2 == 0)
                .subscribe(n -> log.info("filter 偶数: {}", n));

        // take: 只取前 3 个
        Flux.range(1, 10).take(3)
                .subscribe(n -> log.info("take: {}", n));

        // skip + take: 跳过前 5 个，取 3 个（分页逻辑）
        Flux.range(1, 20)
                .skip(5)   // 跳过 1-5
                .take(3)   // 取 6-8
                .subscribe(n -> log.info("skip+take: {}", n));

        log.info("过滤示例完成");
    }

    // ======================================================================
    // 6. 组合操作符
    // ======================================================================

    /**
     * 组合操作符
     *
     * <pre>
     * zip(f1, f2, ...)     — 等待所有源完成，组合结果（对齐合并）
     * merge(f1, f2, ...)   — 并行合并（不保证顺序）
     * concat(f1, f2, ...)  — 顺序合并（等前一个完成才处理下一个）
     * combineLatest(f1, f2)— 任一源有新值时组合最新值
     * </pre>
     */
    public static void combiningExamples() {
        Flux<String> source1 = Flux.just("A1", "B1", "C1")
                .delayElements(Duration.ofMillis(300));
        Flux<String> source2 = Flux.just("A2", "B2", "C2")
                .delayElements(Duration.ofMillis(400));

        // zip: 对齐合并 — 1对1匹配
        Flux.zip(source1, source2, (s1, s2) -> s1 + "+" + s2)
                .subscribe(result -> log.info("zip: {}", result));
        // 输出: A1+A2, B1+B2, C1+C2

        // merge: 并行合并 — 元素交错（因为延迟不同）
        Flux.merge(source1, source2)
                .subscribe(result -> log.info("merge: {}", result));
        // 输出: A1, A2, B1, B2, C1, C2（交错）

        log.info("组合示例完成");
    }

    // ======================================================================
    // 7. 聚合操作符
    // ======================================================================

    /**
     * 聚合操作符
     *
     * <pre>
     * collectList()   — Flux → Mono&lt;List&gt;
     * collectMap()    — Flux → Mono&lt;Map&gt;
     * collectSortedList() — Flux → Mono&lt;List&gt;（排序）
     * reduce(seed, fn)— 归约（类似 Stream.reduce）
     * scan(seed, fn)  — 累计（每步都输出中间值）
     * count()         — 计数
     * all(predicate)  — 是否全部满足
     * any(predicate)  — 是否有任意满足
     * hasElement(v)   — 是否包含某值
     * </pre>
     */
    public static void aggregationExamples() {
        Flux<Integer> numbers = Flux.range(1, 5);

        // collectList: Flux → Mono<List>
        numbers.collectList()
                .subscribe(list -> log.info("collectList: {}", list));

        // reduce: 累加
        Flux.range(1, 5)
                .reduce(0, Integer::sum)
                .subscribe(sum -> log.info("reduce 累加: {}", sum));  // 15

        // scan: 逐步累加（输出中间值）
        Flux.range(1, 5)
                .scan(0, Integer::sum)
                .subscribe(step -> log.info("scan 步骤: {}", step));
        // 输出: 0, 1, 3, 6, 10, 15

        // count: 计数
        Flux.range(1, 100).count()
                .subscribe(c -> log.info("count: {}", c));

        // any: 是否存在满足条件的元素
        Flux.just("apple", "banana", "cherry")
                .any(fruit -> fruit.startsWith("b"))
                .subscribe(result -> log.info("any: {}", result));  // true

        log.info("聚合示例完成");
    }

    // ======================================================================
    // 8. 副作用操作符（Peeking / 生命周期钩子）
    // ======================================================================

    /**
     * 副作用操作符（不改变流，仅观察）
     *
     * <pre>
     * doOnSubscribe(Consumer)    — 订阅时
     * doOnNext(Consumer)         — 每个元素通过时
     * doOnError(Consumer)        — 出错时
     * doOnComplete(Runnable)     — 正常完成时（仅 Flux）
     * doOnSuccess(Consumer)      — 正常完成时（仅 Mono）
     * doOnCancel(Runnable)       — 取消订阅时
     * doOnTerminate(Runnable)    — 终止时（成功或失败）
     * doFinally(Consumer)        — 无论如何终止（含 cancel）
     * doOnRequest(LongConsumer)  — 下游请求数据时
     * doOnDiscard(Consumer)      — 元素被丢弃时（如 take 后剩余的元素）
     * </pre>
     */
    public static void sideEffectExample() {
        Flux.range(1, 3)
                .doOnSubscribe(s -> log.info("🔄 已订阅"))
                .doOnNext(n -> log.info("📤 发射: {}", n))
                .doOnComplete(() -> log.info("✅ 完成"))
                .doFinally(signal -> log.info("🏁 最终信号: {}", signal))
                .subscribe();
    }

    // ======================================================================
    // 9. 调度器
    // ======================================================================

    /**
     * 调度器（Schedulers）— 控制代码运行在哪个线程
     *
     * <pre>
     * Schedulers.immediate()      — 调用者线程（默认行为）
     * Schedulers.single()         — 单一后台线程
     * Schedulers.parallel()       — CPU 核数个线程池（适合计算密集）
     * Schedulers.boundedElastic() — 弹性线程池（适合 I/O 密集，有上限）
     * Schedulers.fromExecutor()   — 从自定义 Executor 创建
     *
     * publishOn(Scheduler)  — 切换下游操作符的执行线程
     * subscribeOn(Scheduler)— 切换上游订阅和源的执行线程
     * </pre>
     */
    public static void schedulerExample() {
        Flux.range(1, 3)
                .map(i -> {
                    log.info("🔵 map1 在: {}", Thread.currentThread().getName());
                    return i * 10;
                })
                .publishOn(Schedulers.boundedElastic())
                .map(i -> {
                    log.info("🟢 map2 在: {}", Thread.currentThread().getName());
                    return i + 5;
                })
                .subscribeOn(Schedulers.parallel())  // 不生效（第一个操作符忽略 subscribeOn）
                .subscribe(
                        v -> log.info("📥 订阅者在: {}", Thread.currentThread().getName())
                );

        try { Thread.sleep(500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        log.info("调度器示例完成");
    }

    // ======================================================================
    // 10. 实用模式
    // ======================================================================

    /**
     * 条件操作符
     *
     * <pre>
     * defaultIfEmpty(value)  — 如果为空，发一个默认值
     * switchIfEmpty(alt)     — 如果为空，切换到备选流
     * repeat(n)              — 完成后重复 n 次
     * repeatWhen(factory)    — 条件重复
     * then()                 — 忽略结果，等完成后返回 Mono&lt;Void&gt;
     * thenReturn(v)          — 忽略结果，等完成后返回固定值
     * thenMany(Flux)         — 忽略结果，等完成后切换到 Flux
     * </pre>
     */
    public static void utilityPatterns() {
        // defaultIfEmpty
        Flux.empty().defaultIfEmpty("默认值")
                .subscribe(v -> log.info("defaultIfEmpty: {}", v));

        // switchIfEmpty
        Flux.empty().switchIfEmpty(Flux.just("备选1", "备选2"))
                .subscribe(v -> log.info("switchIfEmpty: {}", v));

        // then + thenReturn
        Mono.just("data")
                .flatMap(d -> {
                    log.info("处理: {}", d);
                    return Mono.empty();
                })
                .thenReturn("操作完成！")  // 忽略上游结果
                .subscribe(v -> log.info("thenReturn: {}", v));

        // switchIfEmpty vs defaultIfEmpty 区别
        log.info("switchIfEmpty（切换到另一个流）vs defaultIfEmpty（单个默认值）区别已演示");
    }
}
