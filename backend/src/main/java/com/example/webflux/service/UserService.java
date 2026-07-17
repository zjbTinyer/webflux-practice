package com.example.webflux.service;

import com.example.webflux.dto.PageResponse;
import com.example.webflux.dto.UserDTO;
import com.example.webflux.entity.User;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 用户服务接口
 *
 * <p>所有方法返回类型都是 Mono 或 Flux —— 这就是响应式的核心：
 * 方法不直接返回结果，而是返回一个"未来会产生结果"的发布者。
 */
public interface UserService {

    // ========== 基础 CRUD (响应式) ==========

    /** 创建用户 - 返回 Mono，因为是单个结果 */
    Mono<User> create(UserDTO dto);

    /** 根据 ID 查询 - Mono 可能为空 (Mono.empty()) */
    Mono<User> findById(Long id);

    /** 查询所有用户 - Flux 表示 0~N 个结果 */
    Flux<User> findAll();

    /** 更新用户 */
    Mono<User> update(Long id, UserDTO dto);

    /** 删除用户 - Mono<Void> 表示没有返回值，只关心操作是否完成 */
    Mono<Void> delete(Long id);

    // ========== 高级查询 (展示操作符组合) ==========

    /** 按名称搜索（演示 flatMap/flatMapMany 操作符） */
    Flux<User> searchByName(String name);

    /** 按年龄过滤并排序（演示 filter + sort 操作符） */
    Flux<User> findByAgeGreaterThan(int age);

    /** 按邮箱模糊搜索（演示默认值处理） */
    Flux<User> searchByEmail(String keyword);

    // ========== 综合操作 (演示复杂响应式链) ==========

    /** 批量创建用户（演示 concatMap vs flatMap 的区别） */
    Flux<User> batchCreate(Flux<UserDTO> dtoStream);

    /** 条件查询用户并转换（演示 transform 操作符） */
    Mono<User> findByIdWithFallback(Long id);

    /** 获取用户总数（演示多个 Mono 组合） */
    Mono<Long> count();

    // ========== 分页查询 ==========

    /** 分页查询所有用户（生产级 API，Mono.zip 合并内容与总数） */
    Mono<PageResponse<User>> findAllPaged(int page, int size);
}
