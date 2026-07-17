package com.example.webflux.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.CorsRegistry;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.config.WebFluxConfigurer;

/**
 * 全局 CORS 配置
 *
 * <p>替代各个 Controller 上的 {@code @CrossOrigin} 注解，
 * 集中管理跨域策略，覆盖注解方式和函数式端点。
 */
@Configuration
@EnableWebFlux
public class CorsConfig implements WebFluxConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("http://localhost:*", "http://127.0.0.1:*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("Content-Type", "Authorization")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
