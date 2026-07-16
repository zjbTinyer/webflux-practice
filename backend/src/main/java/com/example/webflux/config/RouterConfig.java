package com.example.webflux.config;

import com.example.webflux.handler.UserHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;

/**
 * 函数式端点路由配置
 *
 * <h2>什么是 RouterFunction？</h2>
 * 函数式编程风格的"路由表"，替代 @RequestMapping 注解：
 * <pre>
 *   请求方法 + 路径模式 → Handler 方法
 * </pre>
 *
 * <h2>RouterFunctions.route() 语法</h2>
 * <pre>{@code
 * return RouterFunctions
 *     .route(GET("/path"), handler::method)            // 单个路由
 *     .andRoute(POST("/path"), handler::method)         // 追加路由
 *     .andRoute(GET("/path/{id}"), handler::method);    // 路径变量 {id}
 * }</pre>
 *
 * <h2>嵌套路由</h2>
 * 使用 {@code RouterFunctions.nest()} 可以为一组路由添加公共前缀和过滤器：
 * <pre>{@code
 * return RouterFunctions.nest(
 *     path("/api/func/users"),  // 公共路径前缀
 *     RouterFunctions
 *         .route(GET(""), handler::findAll)            // 实际路径: GET /api/func/users
 *         .andRoute(GET("/{id}"), handler::findById)   // 实际路径: GET /api/func/users/{id}
 * );
 * }</pre>
 *
 * <h2>注解方式 vs 函数式端点 对比</h2>
 * <pre>
 * | 访问路径              | 注解方式                        | 函数式端点             |
 * |----------------------|--------------------------------|-----------------------|
 * | GET /api/users       | UserController#findAll()      | (本类未配置)           |
 * | GET /api/func/users  | (无)                           | UserHandler#findAll()  |
 * | POST /api/func/users | (无)                           | UserHandler#create()   |
 * </pre>
 */
@Configuration
public class RouterConfig {

    /**
     * 定义用户相关的函数式路由
     *
     * <p>访问路径以 /api/func/users 开头，区别于注解方式的 /api/users
     *
     * <p>参数说明：
     * <ul>
     *   <li>{@code UserHandler} 由 Spring 自动注入（@Component）</li>
     *   <li>返回 {@code RouterFunction<ServerResponse>}，Spring 会自动注册为 Bean</li>
     * </ul>
     */
    @Bean
    public RouterFunction<ServerResponse> userRoutes(UserHandler handler) {
        return RouterFunctions.nest(
                // 公共路径前缀
                path("/api/func/users"),
                RouterFunctions
                        // GET /api/func/users → 查询全部
                        .route(GET(""), handler::findAll)
                        // GET /api/func/users/{id} → 按 ID 查询
                        .andRoute(GET("/{id}"), handler::findById)
                        // POST /api/func/users → 创建
                        .andRoute(POST(""), handler::create)
                        // PUT /api/func/users/{id} → 更新
                        .andRoute(PUT("/{id}"), handler::update)
                        // DELETE /api/func/users/{id} → 删除
                        .andRoute(DELETE("/{id}"), handler::delete)
        );
    }
}
