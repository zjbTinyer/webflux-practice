package com.example.webflux.repository;

import com.example.webflux.entity.User;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 用户响应式仓库
 *
 * <h3>R2dbcRepository 接口说明</h3>
 * R2dbcRepository 继承自 ReactiveCrudRepository，所有方法都返回 Mono/Flux：
 * <ul>
 *   <li>{@code Mono<T> save(T entity)} — 保存实体，返回 Mono</li>
 *   <li>{@code Mono<T> findById(ID id)} — 按主键查询，返回 Mono（可能为空）</li>
 *   <li>{@code Flux<T> findAll()} — 查询全部，返回 Flux</li>
 *   <li>{@code Mono<Void> deleteById(ID id)} — 按主键删除</li>
 *   <li>{@code Mono<Long> count()} — 计数</li>
 * </ul>
 *
 * <h3>方法命名规则（自动生成查询）</h3>
 * Spring Data 会根据方法名自动生成 SQL：
 * <ul>
 *   <li>{@code findByName(String name)} → {@code WHERE name = ?}</li>
 *   <li>{@code findByAgeGreaterThan(int age)} → {@code WHERE age > ?}</li>
 *   <li>{@code findByNameLike(String pattern)} → {@code WHERE name LIKE ?}</li>
 * </ul>
 */
@Repository
public interface UserRepository extends R2dbcRepository<User, Long> {

    /**
     * 🔍 按名称查询用户
     * 返回值用 {@code Flux<User>} 而非 {@code Mono<User>}，
     * 因为可能有多个用户同名
     */
    Flux<User> findByName(String name);

    /**
     * 🔍 按年龄大于指定值查询
     * 方法名中的 GreaterThan 会被 R2DBC 解析为 SQL 的 >
     */
    Flux<User> findByAgeGreaterThan(int age);

    /**
     * 🔍 自定义 SQL 查询 - 按邮箱模糊搜索
     * 使用 @Query 注解可以直接写原生 SQL
     *
     * 注意：返回值为 {@code Flux<User>}，即使结果可能是空集合也不会返回 null
     */
    @Query("SELECT * FROM users WHERE email LIKE CONCAT('%', :keyword, '%')")
    Flux<User> searchByEmail(String keyword);

    /**
     * 🔍 自定义 SQL + 参数绑定
     * :name 和 :age 会自动绑定方法参数
     */
    @Query("SELECT * FROM users WHERE name = :name AND age >= :age")
    Flux<User> findByNameAndMinAge(String name, int age);

    /**
     * 📄 分页查询 — 按 ID 降序排列
     * R2DBC 使用 OFFSET/LIMIT 实现分页（类似 SQL 标准语法）
     */
    @Query("SELECT * FROM users ORDER BY id DESC LIMIT :size OFFSET :offset")
    Flux<User> findAllPaged(int size, long offset);
}
