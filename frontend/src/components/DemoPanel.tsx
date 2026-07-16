import React from 'react';
import { Result } from '../api/userApi';

interface Props {
  onZipDemo: () => void;
  onCollectDemo: () => void;
  onErrorDemo: () => void;
  zipResult: Result<string> | null;
  collectResult: Result<string> | null;
  loadingDemo: string;
}

/**
 * 操作符演示面板
 *
 * 用于测试后端的各类 WebFlux 操作符：
 * - zip 合并（Mono.zip 等待两个异步查询）
 * - collectList 聚合（Flux → Mono<List>）
 * - 错误处理（timeout + retry + onErrorMap）
 */
const DemoPanel: React.FC<Props> = ({
  onZipDemo,
  onCollectDemo,
  onErrorDemo,
  zipResult,
  collectResult,
  loadingDemo,
}) => {
  return (
    <div className="demo-panel">
      <h3>🧪 WebFlux 操作符演示</h3>
      <p className="desc">点击按钮触发后端不同的操作符链路</p>

      <div className="demo-buttons">
        {/* zip 演示 */}
        <div className="demo-card">
          <h4>Mono.zip 合并演示</h4>
          <p>同时查询"用户总数"和"第一个用户"，用 zip 合并两个异步结果</p>
          <button
            className="btn btn-primary"
            onClick={onZipDemo}
            disabled={loadingDemo === 'zip'}
          >
            {loadingDemo === 'zip' ? '⏳...' : '▶️ 运行 zip'}
          </button>
          {zipResult && (
            <div className="demo-result success">
              ✅ {zipResult.data}
            </div>
          )}
        </div>

        {/* collectList 演示 */}
        <div className="demo-card">
          <h4>collectList 聚合演示</h4>
          <p>将所有用户名收集起来，用 collectList() 转为逗号分隔的字符串</p>
          <button
            className="btn btn-primary"
            onClick={onCollectDemo}
            disabled={loadingDemo === 'collect'}
          >
            {loadingDemo === 'collect' ? '⏳...' : '▶️ 运行 collectList'}
          </button>
          {collectResult && (
            <div className="demo-result success">
              ✅ {collectResult.data}
            </div>
          )}
        </div>

        {/* 错误处理演示 */}
        <div className="demo-card">
          <h4>错误处理演示</h4>
          <p>查询不存在的用户 ID=999，测试 timeout + retry + onErrorMap</p>
          <button
            className="btn btn-danger"
            onClick={onErrorDemo}
            disabled={loadingDemo === 'error'}
          >
            {loadingDemo === 'error' ? '⏳...' : '▶️ 触发错误'}
          </button>
        </div>
      </div>
    </div>
  );
};

export default DemoPanel;
