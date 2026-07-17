package com.example.webflux.config;

import com.example.webflux.dto.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

/**
 * 全局异常处理器（@RestControllerAdvice）
 *
 * <p>统一处理所有 Controller 抛出的异常，返回标准 Result 格式。
 * 替代原来在各个 Controller 内部定义的 @ExceptionHandler。
 *
 * <p>注意 WebFlux 中的返回类型是 {@code Mono<Result<T>>}
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 请求参数校验失败 — 400
     * WebExchangeBindException 是 WebFlux 的校验异常（等价于 MVC 的 MethodArgumentNotValidException）
     */
    @ExceptionHandler(WebExchangeBindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Mono<Result<Map<String, String>>> handleValidation(WebExchangeBindException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );
        log.warn("⚠️  参数校验失败: {}", errors);
        return Mono.just(Result.<Map<String, String>>builder()
                .code(400)
                .message("参数校验失败")
                .data(errors)
                .build());
    }

    /**
     * 业务参数错误 — 400
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Mono<Result<Void>> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("⚠️  参数错误: {}", ex.getMessage());
        return Mono.just(Result.badRequest(ex.getMessage()));
    }

    /**
     * ResponseStatusException — 各种 HTTP 错误状态
     */
    @ExceptionHandler(ResponseStatusException.class)
    public Mono<Result<Void>> handleResponseStatus(ResponseStatusException ex) {
        log.warn("⚠️  HTTP {} : {}", ex.getStatusCode(), ex.getReason());
        return Mono.just(Result.error(ex.getStatusCode().value(), ex.getReason()));
    }

    /**
     * 兜底异常 — 500
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Mono<Result<Void>> handleGeneral(Exception ex) {
        log.error("❌ 服务器内部错误: {}", ex.getMessage(), ex);
        return Mono.just(Result.serverError("服务器内部错误: " + ex.getMessage()));
    }
}
