package com.example.webflux.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/**
 * 用户 DTO（数据传输对象）
 *
 * <p>与 Entity 分离的原因：
 * <ul>
 *   <li>Entity 直接映射数据库表，DTO 面向 API 输入输出</li>
 *   <li>避免 API 暴露数据库内部字段（如某些敏感字段）</li>
 *   <li>DTO 可以组合多个 Entity 的数据</li>
 * </ul>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {

    /** ID（创建时不需要传，更新时需要） */
    private Long id;

    @NotBlank(message = "用户名不能为空")
    private String name;

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    @Min(value = 1, message = "年龄必须大于0")
    private Integer age;
}
