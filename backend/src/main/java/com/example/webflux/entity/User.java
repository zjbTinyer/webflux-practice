package com.example.webflux.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

/**
 * 用户实体类
 *
 * <h3>注意</h3>
 * R2DBC (Spring Data R2DBC) 与 JPA 不同：
 * <ul>
 *   <li>不支持关系映射 (@OneToMany, @ManyToMany 等)</li>
 *   <li>实体类就是纯粹的 POJO，没有懒加载、缓存等概念</li>
 *   <li>使用 @Id 标注主键字段</li>
 * </ul>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("users")  // 映射到数据库 users 表
public class User {

    /** 主键 ID，由数据库自动生成 */
    @Id
    private Long id;

    /** 用户名，不可为空 */
    @NotBlank(message = "用户名不能为空")
    private String name;

    /** 邮箱，需要符合邮箱格式 */
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    /** 年龄，必须 ≥ 1 */
    @Min(value = 1, message = "年龄必须大于0")
    private Integer age;

    /** 创建时间 */
    private LocalDateTime createdAt;
}
