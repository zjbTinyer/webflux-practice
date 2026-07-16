import React, { useState, useCallback, useEffect } from 'react';
import UserList from './components/UserList';
import UserForm from './components/UserForm';
import SSEDisplay from './components/SSEDisplay';
import DemoPanel from './components/DemoPanel';
import { useSSE } from './hooks/useSSE';
import {
  User,
  UserDTO,
  Result,
  fetchAllUsers,
  createUser,
  updateUser,
  deleteUser,
  fetchZipDemo,
  fetchCollectDemo,
  fetchErrorDemo,
} from './api/userApi';

/**
 * WebFlux 响应式编程练习 —— 主应用组件
 *
 * <h3>前后端联动演示</h3>
 * <ul>
 *   <li>左侧: 用户 CRUD（调用后端 Mono/Flux API）</li>
 *   <li>右侧: SSE 流式数据（消费后端 text/event-stream）</li>
 *   <li>底部: 操作符演示面板（zip, collectList, 错误处理）</li>
 * </ul>
 */
const App: React.FC = () => {
  // ========== 用户 CRUD 状态 ==========
  const [users, setUsers] = useState<User[]>([]);
  const [loading, setLoading] = useState(false);
  const [editingUser, setEditingUser] = useState<User | null>(null);

  // ========== SSE 状态 ==========
  const userSSE = useSSE<User>('/api/users/stream');
  const heartbeatSSE = useSSE<string>('/api/users/heartbeat');

  // ========== 演示面板状态 ==========
  const [zipResult, setZipResult] = useState<Result<string> | null>(null);
  const [collectResult, setCollectResult] = useState<Result<string> | null>(null);
  const [loadingDemo, setLoadingDemo] = useState('');

  // ========== 初始加载 ==========
  const loadUsers = useCallback(async () => {
    setLoading(true);
    try {
      const data = await fetchAllUsers();
      setUsers(data);
    } catch (err: unknown) {
      alert('加载用户失败: ' + (err as Error).message);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadUsers();
  }, [loadUsers]);

  // ========== CRUD 操作 ==========
  const handleCreate = async (dto: UserDTO) => {
    try {
      await createUser(dto);
      alert('✅ 创建成功（Mono<Result<User>> 完成）');
      setEditingUser(null);
      loadUsers();
    } catch (err: unknown) {
      alert('❌ 创建失败: ' + (err as Error).message);
    }
  };

  const handleUpdate = async (dto: UserDTO) => {
    if (!dto.id) return;
    try {
      await updateUser(dto.id, dto);
      alert('✅ 更新成功（Mono.zip 合并后 flatMap）');
      setEditingUser(null);
      loadUsers();
    } catch (err: unknown) {
      alert('❌ 更新失败: ' + (err as Error).message);
    }
  };

  const handleDelete = async (id: number) => {
    if (!confirm('确认删除？（调用 then() 忽略上游返回 Void）')) return;
    try {
      await deleteUser(id);
      alert('✅ 删除成功（Mono<Void> → thenReturn）');
      loadUsers();
    } catch (err: unknown) {
      alert('❌ 删除失败: ' + (err as Error).message);
    }
  };

  const handleEdit = (user: User) => {
    setEditingUser(user);
  };

  // ========== 演示操作 ==========
  const handleZipDemo = async () => {
    setLoadingDemo('zip');
    try {
      const result = await fetchZipDemo();
      setZipResult(result);
    } catch (err: unknown) {
      alert('zip 演示失败: ' + (err as Error).message);
    } finally {
      setLoadingDemo('');
    }
  };

  const handleCollectDemo = async () => {
    setLoadingDemo('collect');
    try {
      const result = await fetchCollectDemo();
      setCollectResult(result);
    } catch (err: unknown) {
      alert('collectList 演示失败: ' + (err as Error).message);
    } finally {
      setLoadingDemo('');
    }
  };

  const handleErrorDemo = async () => {
    setLoadingDemo('error');
    try {
      const result = await fetchErrorDemo(999); // 不存在的 ID
      alert(JSON.stringify(result));
    } catch (err: unknown) {
      alert('🎯 捕获到预期错误（timeout + retry + onErrorMap）: ' + (err as Error).message);
    } finally {
      setLoadingDemo('');
    }
  };

  return (
    <div className="app">
      <header className="app-header">
        <h1>⚡ WebFlux 响应式编程练习</h1>
        <p>
          Spring Boot 3.x + WebFlux + R2DBC + H2 | React 18 + TypeScript
          &nbsp;|&nbsp;
          <span className="tag">Mono</span>
          <span className="tag">Flux</span>
          <span className="tag">SSE</span>
          <span className="tag">WebSocket</span>
        </p>
      </header>

      <main className="app-main">
        {/* ====== 左侧：CRUD 区域 ====== */}
        <section className="section-crud">
          <UserForm
            editingUser={editingUser}
            onSubmit={editingUser ? handleUpdate : handleCreate}
            onCancel={() => setEditingUser(null)}
          />

          <UserList
            users={users}
            loading={loading}
            onDelete={handleDelete}
            onEdit={handleEdit}
          />

          <button className="btn btn-secondary" onClick={loadUsers}>
            {'🔄 刷新列表（再次调用 Flux<User>）'}
          </button>
        </section>

        {/* ====== 右侧：SSE 区域 ====== */}
        <section className="section-sse">
          <SSEDisplay
            title="SSE 用户流（delayElements + limitRate）"
            description="每 1 秒推送一个用户，演示背压控制"
            items={userSSE.data.map((u) => `${u.name} (${u.email})`)}
            status={userSSE.status}
            onStart={userSSE.start}
            onStop={userSSE.abort}
          />

          <SSEDisplay
            title="SSE 心跳流（Flux.interval）"
            description="每 2 秒推送一次系统状态"
            items={heartbeatSSE.data}
            status={heartbeatSSE.status}
            onStart={heartbeatSSE.start}
            onStop={heartbeatSSE.abort}
          />
        </section>
      </main>

      {/* ====== 底部：演示面板 ====== */}
      <section className="section-demo">
        <DemoPanel
          onZipDemo={handleZipDemo}
          onCollectDemo={handleCollectDemo}
          onErrorDemo={handleErrorDemo}
          zipResult={zipResult}
          collectResult={collectResult}
          loadingDemo={loadingDemo}
        />
      </section>
    </div>
  );
};

export default App;
