package com.example.webflux.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 分页响应 DTO
 *
 * <p>WebFlux 中的分页不同于 Spring MVC 的 Page 对象。
 * R2DBC 不支持 Pageable，需要手动管理 offset/limit。
 *
 * @param <T> 数据项类型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {

    /** 当前页数据 */
    private List<T> content;

    /** 当前页码（从 0 开始） */
    private int page;

    /** 每页大小 */
    private int size;

    /** 总记录数 */
    private long totalElements;

    /** 总页数 */
    private int totalPages;

    /** 是否为第一页 */
    private boolean first;

    /** 是否为最后一页 */
    private boolean last;
}
