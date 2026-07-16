import React from 'react';
import { User } from '../api/userApi';

interface Props {
  users: User[];
  loading: boolean;
  onDelete: (id: number) => void;
  onEdit: (user: User) => void;
}

/**
 * 用户列表组件
 *
 * 展示从 WebFlux Flux<User> 反序列化后的用户列表。
 * 虽然后端是逐个发射的 Flux 流，但作为普通 JSON 请求时，
 * 框架会等所有元素收集完再一次性序列化为 JSON 数组返回。
 */
const UserList: React.FC<Props> = ({ users, loading, onDelete, onEdit }) => {
  if (loading) {
    return <div className="loading">⏳ 加载中...（等待 Mono/Flux 完成）</div>;
  }

  if (users.length === 0) {
    return <div className="empty">📭 暂无用户数据，请先创建或刷新</div>;
  }

  return (
    <div className="user-list">
      <h3>📋 用户列表（Flux&lt;User&gt; → JSON Array）</h3>
      <table>
        <thead>
          <tr>
            <th>ID</th>
            <th>姓名</th>
            <th>邮箱</th>
            <th>年龄</th>
            <th>创建时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          {users.map((user) => (
            <tr key={user.id}>
              <td>{user.id}</td>
              <td>{user.name}</td>
              <td>{user.email}</td>
              <td>{user.age}</td>
              <td>{user.createdAt ? new Date(user.createdAt).toLocaleString() : '-'}</td>
              <td className="actions">
                <button className="btn-sm btn-edit" onClick={() => onEdit(user)}>
                  ✏️ 编辑
                </button>
                <button
                  className="btn-sm btn-danger"
                  onClick={() => user.id && onDelete(user.id)}
                >
                  🗑️ 删除
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};

export default UserList;
