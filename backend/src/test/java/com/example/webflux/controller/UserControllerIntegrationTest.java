package com.example.webflux.controller;

import com.example.webflux.dto.UserDTO;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * UserController 集成测试
 *
 * 使用 @SpringBootTest 启动完整应用上下文，WebTestClient 发送真实 HTTP 请求
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserControllerIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    @Order(1)
    void createUser_shouldReturn201() {
        UserDTO dto = UserDTO.builder()
                .name("集成测试用户")
                .email("integration@test.com")
                .age(28)
                .build();

        webTestClient.post()
                .uri("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(dto)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.code").isEqualTo(200)
                .jsonPath("$.data.name").isEqualTo("集成测试用户");
    }

    @Test
    @Order(2)
    void findAll_withPagination_shouldReturnPageResponse() {
        webTestClient.get()
                .uri("/api/users?page=0&size=5")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.code").isEqualTo(200)
                .jsonPath("$.data.content").isArray()
                .jsonPath("$.data.totalElements").isNumber()
                .jsonPath("$.data.totalPages").isNumber()
                .jsonPath("$.data.size").isEqualTo(5);
    }

    @Test
    @Order(3)
    void findAll_withoutPagination_shouldReturnFluxArray() {
        webTestClient.get()
                .uri("/api/users/all")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$").isArray();
    }

    @Test
    @Order(4)
    void findById_whenNotFound_shouldReturn400() {
        webTestClient.get()
                .uri("/api/users/99999")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.code").isEqualTo(400);
    }

    @Test
    @Order(5)
    void createUser_withInvalidData_shouldReturn400() {
        UserDTO dto = UserDTO.builder()
                .name("")    // 空名称
                .email("not-an-email")  // 无效邮箱
                .build();

        webTestClient.post()
                .uri("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(dto)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.code").isEqualTo(400);
    }

    @Test
    @Order(6)
    void funcEndpoint_findAll_shouldReturnArray() {
        webTestClient.get()
                .uri("/api/func/users")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$").isArray();
    }
}
