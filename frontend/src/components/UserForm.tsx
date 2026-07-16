import React, { useState, useEffect } from 'react';
import { UserDTO } from '../api/userApi';

interface Props {
  editingUser: { id?: number; name: string; email: string; age?: number } | null;
  onSubmit: (dto: UserDTO) => void;
  onCancel: () => void;
}

/**
 * 用户表单组件
 *
 * 对应后端 POST /api/users → Mono<Result<User>>
 * 提交时把表单数据组装为 UserDTO 发送
 */
const UserForm: React.FC<Props> = ({ editingUser, onSubmit, onCancel }) => {
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [age, setAge] = useState<number | ''>('');

  useEffect(() => {
    if (editingUser) {
      setName(editingUser.name);
      setEmail(editingUser.email);
      setAge(editingUser.age ?? '');
    } else {
      setName('');
      setEmail('');
      setAge('');
    }
  }, [editingUser]);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!name.trim() || !email.trim()) {
      alert('姓名和邮箱不能为空');
      return;
    }
    onSubmit({
      id: editingUser?.id,
      name: name.trim(),
      email: email.trim(),
      age: age === '' ? undefined : Number(age),
    });
  };

  return (
    <div className="user-form">
      <h3>{editingUser ? '✏️ 编辑用户（PUT → Mono<User>）' : '➕ 创建用户（POST → Mono<User>）'}</h3>
      <form onSubmit={handleSubmit}>
        <div className="form-group">
          <label>姓名：</label>
          <input
            type="text"
            value={name}
            onChange={(e) => setName(e.target.value)}
            placeholder="请输入姓名"
            required
          />
        </div>
        <div className="form-group">
          <label>邮箱：</label>
          <input
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            placeholder="请输入邮箱"
            required
          />
        </div>
        <div className="form-group">
          <label>年龄：</label>
          <input
            type="number"
            value={age}
            onChange={(e) => setAge(e.target.value === '' ? '' : Number(e.target.value))}
            placeholder="请输入年龄"
            min="1"
          />
        </div>
        <div className="form-actions">
          <button type="submit" className="btn btn-primary">
            {editingUser ? '更新' : '创建'}
          </button>
          {editingUser && (
            <button type="button" className="btn btn-secondary" onClick={onCancel}>
              取消
            </button>
          )}
        </div>
      </form>
    </div>
  );
};

export default UserForm;
