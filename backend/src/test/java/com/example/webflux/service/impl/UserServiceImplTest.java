package com.example.webflux.service.impl;

import com.example.webflux.dto.UserDTO;
import com.example.webflux.entity.User;
import com.example.webflux.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * UserServiceImpl 单元测试
 * 使用 Mockito 模拟 Repository，StepVerifier 验证响应式流
 */
@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .name("测试用户")
                .email("test@example.com")
                .age(25)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void findById_shouldReturnUser_whenExists() {
        when(userRepository.findById(1L)).thenReturn(Mono.just(testUser));

        Mono<User> result = userService.findById(1L);

        StepVerifier.create(result)
                .expectNextMatches(user -> user.getName().equals("测试用户")
                        && user.getEmail().equals("test@example.com"))
                .verifyComplete();
    }

    @Test
    void findById_shouldThrow_whenNotFound() {
        when(userRepository.findById(99L)).thenReturn(Mono.empty());

        Mono<User> result = userService.findById(99L);

        StepVerifier.create(result)
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    void create_shouldSaveAndReturnUser() {
        UserDTO dto = UserDTO.builder().name("新用户").email("new@example.com").age(30).build();
        when(userRepository.save(any(User.class))).thenReturn(Mono.just(testUser));

        Mono<User> result = userService.create(dto);

        StepVerifier.create(result)
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void findAll_shouldReturnAllUsers() {
        when(userRepository.findAll()).thenReturn(Flux.just(testUser,
                User.builder().id(2L).name("用户2").email("u2@example.com").build()));

        Flux<User> result = userService.findAll();

        StepVerifier.create(result)
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    void delete_shouldComplete_whenExists() {
        when(userRepository.findById(1L)).thenReturn(Mono.just(testUser));
        when(userRepository.deleteById(1L)).thenReturn(Mono.empty());

        Mono<Void> result = userService.delete(1L);

        StepVerifier.create(result)
                .verifyComplete();
    }

    @Test
    void findAllPaged_shouldReturnPageResponse() {
        when(userRepository.findAllPaged(10, 0)).thenReturn(Flux.just(testUser));
        when(userRepository.count()).thenReturn(Mono.just(15L));

        Mono<?> result = userService.findAllPaged(0, 10);

        StepVerifier.create(result)
                .expectNextMatches(page -> {
                    // 用反射验证 page 对象
                    String str = page.toString();
                    return str.contains("totalElements=15") && str.contains("totalPages=2");
                })
                .verifyComplete();
    }
}
