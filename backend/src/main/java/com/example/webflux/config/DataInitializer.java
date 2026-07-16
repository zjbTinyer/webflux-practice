package com.example.webflux.config;

import com.example.webflux.entity.User;
import com.example.webflux.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;

/**
 * 数据初始化器
 *
 * <p>实现 CommandLineRunner 在应用启动后自动执行，
 * 向 H2 内存数据库中插入示例数据，方便练习。
 *
 * <h3>响应式批量插入</h3>
 * 用 {@code Flux.just(...).concatMap(repo::save)} 实现顺序批量插入。
 *
 * <p>concatMap 保证按顺序保存，如果用 flatMap 则会并行保存（顺序不确定）。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;

    @Override
    public void run(String... args) {
        log.info("🚀 开始初始化示例数据...");

        Flux.just(
                        User.builder().name("张三").email("zhangsan@example.com").age(25).createdAt(LocalDateTime.now()).build(),
                        User.builder().name("李四").email("lisi@example.com").age(30).createdAt(LocalDateTime.now()).build(),
                        User.builder().name("王五").email("wangwu@example.com").age(28).createdAt(LocalDateTime.now()).build(),
                        User.builder().name("赵六").email("zhaoliu@example.com").age(35).createdAt(LocalDateTime.now()).build(),
                        User.builder().name("孙七").email("sunqi@example.com").age(22).createdAt(LocalDateTime.now()).build(),
                        User.builder().name("周八").email("zhouba@example.com").age(40).createdAt(LocalDateTime.now()).build(),
                        User.builder().name("吴九").email("wujiu@example.com").age(27).createdAt(LocalDateTime.now()).build(),
                        User.builder().name("郑十").email("zhengshi@example.com").age(33).createdAt(LocalDateTime.now()).build()
                )
                // concatMap: 顺序保存（前一个完成才保存下一个）
                .concatMap(user -> userRepository.save(user)
                        .doOnNext(saved -> log.debug("   ✅ 初始化: {} (ID={})", saved.getName(), saved.getId()))
                )
                // count() 操作符：计算 Flux 中元素总数
                .count()
                .subscribe(
                        count -> log.info("🎉 数据初始化完成，共 {} 条用户记录", count),
                        error -> log.error("❌ 数据初始化失败: {}", error.getMessage())
                );
    }
}
