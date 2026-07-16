import { useState, useEffect, useRef, useCallback } from 'react';

/**
 * SSE (Server-Sent Events) Hook
 *
 * 用于消费 WebFlux 后端通过 SSE 推送的流式数据。
 *
 * SSE 的核心：使用浏览器原生 EventSource API 或 fetch + ReadableStream
 * 来接收服务端 text/event-stream 格式的数据。
 *
 * @param url — SSE 端点的 URL
 * @returns { data, status, error, abort }
 *
 * 使用示例：
 * ```tsx
 * const { data, status } = useSSE<User[]>('/api/users/stream');
 * ```
 */
export function useSSE<T>(url: string) {
  const [data, setData] = useState<T[]>([]);
  const [status, setStatus] = useState<'idle' | 'connecting' | 'connected' | 'error'>('idle');
  const [error, setError] = useState<string | null>(null);
  const abortRef = useRef<AbortController | null>(null);

  const abort = useCallback(() => {
    abortRef.current?.abort();
    setStatus('idle');
    setData([]);
  }, []);

  const start = useCallback(() => {
    // 先取消之前的连接
    abortRef.current?.abort();

    const controller = new AbortController();
    abortRef.current = controller;

    setStatus('connecting');
    setData([]);
    setError(null);

    // 使用 fetch + ReadableStream 消费 SSE（比 EventSource 更灵活）
    fetch(url, {
      signal: controller.signal,
      headers: { Accept: 'text/event-stream' },
    })
      .then(async (response) => {
        if (!response.ok) {
          throw new Error(`HTTP ${response.status}`);
        }
        setStatus('connected');

        const reader = response.body?.getReader();
        if (!reader) throw new Error('无法获取 ReadableStream');

        const decoder = new TextDecoder();
        let buffer = '';

        // 持续读取流数据
        while (true) {
          const { done, value } = await reader.read();
          if (done) break;

          buffer += decoder.decode(value, { stream: true });

          // SSE 数据以 \n\n 分隔每条消息
          const lines = buffer.split('\n\n');
          // 保留最后一个不完整的分片
          buffer = lines.pop() || '';

          for (const line of lines) {
            // 提取 data: 前缀的内容
            const dataLine = line
              .split('\n')
              .filter((l) => l.startsWith('data:'))
              .map((l) => l.slice(5).trim())
              .join('');

            if (dataLine) {
              try {
                const parsed = JSON.parse(dataLine) as T;
                setData((prev) => [...prev, parsed]);  // 追加新数据
              } catch {
                // 非 JSON 数据（如心跳文本），直接追加
                setData((prev) => [...prev, dataLine as unknown as T]);
              }
            }
          }
        }
      })
      .catch((err) => {
        if (err.name === 'AbortError') return;  // 主动取消不算错误
        setStatus('error');
        setError(err.message);
      });
  }, [url]);

  // 组件卸载时取消 SSE 连接
  useEffect(() => {
    return () => {
      abortRef.current?.abort();
    };
  }, []);

  return { data, status, error, start, abort };
}

/**
 * 使用 EventSource 的简化版 SSE Hook
 * 适用于标准 SSE 格式（服务端返回 Content-Type: text/event-stream）
 */
export function useEventSource(url: string) {
  const [data, setData] = useState<string[]>([]);
  const [status, setStatus] = useState<'idle' | 'connecting' | 'connected' | 'closed'>('idle');
  const eventSourceRef = useRef<EventSource | null>(null);

  const close = useCallback(() => {
    eventSourceRef.current?.close();
    setStatus('closed');
  }, []);

  const start = useCallback(() => {
    eventSourceRef.current?.close();

    setStatus('connecting');
    setData([]);

    const es = new EventSource(url);
    eventSourceRef.current = es;

    es.onopen = () => setStatus('connected');

    es.onmessage = (event) => {
      setData((prev) => [...prev, event.data]);
    };

    es.onerror = () => {
      setStatus('closed');
      es.close();
    };
  }, [url]);

  useEffect(() => {
    return () => {
      eventSourceRef.current?.close();
    };
  }, []);

  return { data, status, start, close };
}
