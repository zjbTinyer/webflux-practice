/**
 * 用户 API 类型定义
 *
 * 与后端 WebFlux DTO 结构一一对应。
 * API 调用在各页面中通过 TanStack Query + fetch 直接实现。
 */

// ========== 类型定义 ==========

export interface User {
  id?: number;
  name: string;
  email: string;
  age?: number;
  createdAt?: string;
}

export interface Result<T> {
  code: number;
  message: string;
  data: T;
}

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}

export interface UserDTO {
  id?: number;
  name: string;
  email: string;
  age?: number;
}
