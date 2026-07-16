import React from 'react';

interface Props {
  items: string[];
  status: string;
  title: string;
  description: string;
  onStart: () => void;
  onStop: () => void;
}

/**
 * SSE 流式数据展示组件
 *
 * 演示消费 WebFlux SSE 端点的数据：
 * - GET /api/users/stream → Flux<User> (SSE)
 * - GET /api/users/heartbeat → Flux<String> (SSE)
 * - GET /api/users/demo/merge → Flux<String> (SSE)
 */
const SSEDisplay: React.FC<Props> = ({
  items,
  status,
  title,
  description,
  onStart,
  onStop,
}) => {
  return (
    <div className="sse-display">
      <h3>📡 {title}</h3>
      <p className="desc">{description}</p>
      <div className="sse-controls">
        <button
          className="btn btn-primary"
          onClick={onStart}
          disabled={status === 'connecting' || status === 'connected'}
        >
          {status === 'connecting' ? '⏳ 连接中...' : '▶️ 开始接收'}
        </button>
        <button
          className="btn btn-secondary"
          onClick={onStop}
          disabled={status !== 'connected'}
        >
          ⏹️ 停止
        </button>
        <span className={`status-badge status-${status}`}>
          {status === 'idle' && '⚪ 未连接'}
          {status === 'connecting' && '🟡 连接中...'}
          {status === 'connected' && '🟢 已连接（SSE 推送中）'}
          {status === 'error' && '🔴 连接错误'}
          {status === 'closed' && '⚫ 已关闭'}
        </span>
      </div>
      <div className="sse-output">
        {items.length === 0 ? (
          <p className="empty">等待 SSE 数据流推送...</p>
        ) : (
          <ul>
            {items.map((item, idx) => (
              <li key={idx} className="sse-item">
                <span className="sse-index">#{idx + 1}</span>
                <span>
                  {typeof item === 'string'
                    ? item
                    : JSON.stringify(item)}
                </span>
              </li>
            ))}
          </ul>
        )}
      </div>
    </div>
  );
};

export default SSEDisplay;
