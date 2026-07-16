/**
 * 用户 API 封装
 *
 * 本文件演示如何使用前端代码与 WebFlux 后端交互：
 * 1. 普通 JSON 请求（Mono 响应）
 * 2. SSE (Server-Sent Events) 流式消费
 * 3. 批量创建请求
 */

const BASE_URL = '/api/users';

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

export interface UserDTO {
  id?: number;
  name: string;
  email: string;
  age?: number;
}

// ========== 普通 CRUD 请求 ==========

/** 查询所有用户 — Flux<User> → JSON 数组 */
export async function fetchAllUsers(): Promise<User[]> {
  const res = await fetch(BASE_URL);
  if (!res.ok) throw new Error(`请求失败: ${res.status}`);
  return res.json();
}

/** 按 ID 查询 — Mono<Result<User>> */
export async function fetchUserById(id: number): Promise<Result<User>> {
  const res = await fetch(`${BASE_URL}/${id}`);
  if (!res.ok) throw new Error(`请求失败: ${res.status}`);
  return res.json();
}

/** 创建用户 — POST + Mono<Result<User>> */
export async function createUser(dto: UserDTO): Promise<Result<User>> {
  const res = await fetch(BASE_URL, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(dto),
  });
  if (!res.ok) {
    const err = await res.json();
    throw new Error(err.message || '创建失败');
  }
  return res.json();
}

/** 更新用户 — PUT + Mono<Result<User>> */
export async function updateUser(id: number, dto: UserDTO): Promise<Result<User>> {
  const res = await fetch(`${BASE_URL}/${id}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(dto),
  });
  if (!res.ok) throw new Error('更新失败');
  return res.json();
}

/** 删除用户 — DELETE + Mono<Result<void>> */
export async function deleteUser(id: number): Promise<Result<void>> {
  const res = await fetch(`${BASE_URL}/${id}`, { method: 'DELETE' });
  if (!res.ok) throw new Error('删除失败');
  return res.json();
}

// ========== 搜索 ==========

export async function searchUsersByName(name: string): Promise<User[]> {
  const res = await fetch(`${BASE_URL}/search?name=${encodeURIComponent(name)}`);
  return res.json();
}

export async function searchUsersByEmail(keyword: string): Promise<User[]> {
  const res = await fetch(`${BASE_URL}/email?keyword=${encodeURIComponent(keyword)}`);
  return res.json();
}

export async function fetchUsersByMinAge(age: number): Promise<User[]> {
  const res = await fetch(`${BASE_URL}/age/${age}`);
  return res.json();
}

// ========== 批量创建 ==========

export async function batchCreateUsers(dtos: UserDTO[]): Promise<User[]> {
  const res = await fetch(`${BASE_URL}/batch`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(dtos),
  });
  if (!res.ok) throw new Error('批量创建失败');
  return res.json();
}

// ========== 演示接口 ==========

export async function fetchZipDemo(): Promise<Result<string>> {
  const res = await fetch(`${BASE_URL}/demo/zip`);
  return res.json();
}

export async function fetchCollectDemo(): Promise<Result<string>> {
  const res = await fetch(`${BASE_URL}/demo/collect`);
  return res.json();
}

export async function fetchErrorDemo(id: number): Promise<Result<User>> {
  const res = await fetch(`${BASE_URL}/demo/error/${id}`);
  return res.json();
}
