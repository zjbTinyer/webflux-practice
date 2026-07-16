package com.example.webflux.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一响应体
 *
 * <pre>
 * 响应格式:
 * {
 *     "code": 200,      // 业务状态码
 *     "message": "操作成功",  // 提示信息
 *     "data": { ... }    // 响应数据
 * }
 * </pre>
 *
 * 在 WebFlux 中，Controller 方法的返回值通常是 {@code Mono<Result<T>>}，
 * 表示"未来某个时刻会产生一个 Result"。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {

    /** 状态码 - 200 成功，4xx 客户端错误，5xx 服务端错误 */
    private Integer code;

    /** 提示信息 */
    private String message;

    /** 响应数据 */
    private T data;

    // ========== 便捷工厂方法 ==========

    /**
     * 成功响应（带数据）
     * 使用泛型方法，自动推断返回类型
     */
    public static <T> Result<T> ok(T data) {
        return Result.<T>builder()
                .code(200)
                .message("操作成功")
                .data(data)
                .build();
    }

    /** 成功响应（无数据） */
    public static <T> Result<T> ok() {
        return ok(null);
    }

    /** 失败响应 */
    public static <T> Result<T> error(Integer code, String message) {
        return Result.<T>builder()
                .code(code)
                .message(message)
                .data(null)
                .build();
    }

    /** 400 错误 */
    public static <T> Result<T> badRequest(String message) {
        return error(400, message);
    }

    /** 500 错误 */
    public static <T> Result<T> serverError(String message) {
        return error(500, message);
    }
}
