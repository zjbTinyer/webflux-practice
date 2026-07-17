import { useState } from 'react'
import { toast } from 'sonner'
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '../components/ui/card'
import { Button } from '../components/ui/button'
import { Badge } from '../components/ui/badge'
import { useSSE } from '../hooks/useSSE'
import { Search } from 'lucide-react'

export default function DemoPage() {
  const [demoResult, setDemoResult] = useState<string | null>(null)
  const [loading, setLoading] = useState('')
  const userSSE = useSSE<string>('/api/users/stream')
  const heartbeatSSE = useSSE<string>('/api/users/heartbeat')

  const runDemo = async (endpoint: string) => {
    setLoading(endpoint)
    try {
      const res = await fetch(endpoint)
      if (!res.ok) {
        const err = await res.json()
        toast.error(err.message || '请求失败')
      } else {
        const data = await res.json()
        setDemoResult(JSON.stringify(data.data ?? data, null, 2))
        toast.success('演示完成')
      }
    } catch (e: unknown) {
      toast.error((e as Error).message)
    } finally {
      setLoading('')
    }
  }

  return (
    <div className="max-w-5xl mx-auto space-y-6">
      <h2 className="text-2xl font-bold">Demo 演示</h2>

      {/* SSE 区域 */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        <Card>
          <CardHeader>
            <CardTitle>SSE 用户流</CardTitle>
            <CardDescription>每1秒推送一个用户（delayElements + limitRate）</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="flex gap-2 mb-3">
              <Button size="sm" disabled={userSSE.status === 'connected'} onClick={userSSE.start}>开始</Button>
              <Button size="sm" variant="outline" onClick={userSSE.abort}>停止</Button>
              <Badge variant={userSSE.status === 'connected' ? 'success' : 'secondary'}>{userSSE.status}</Badge>
            </div>
            <div className="bg-slate-900 text-green-400 rounded-md p-3 max-h-48 overflow-y-auto text-xs font-mono">
              {userSSE.data.length === 0 ? <span className="text-slate-500">等待数据...</span> :
                userSSE.data.map((d, i) => <div key={i} className="py-0.5"><span className="text-yellow-400">#{i + 1}</span> {d}</div>)}
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>SSE 心跳</CardTitle>
            <CardDescription>每2秒推送服务器状态（Flux.interval）</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="flex gap-2 mb-3">
              <Button size="sm" disabled={heartbeatSSE.status === 'connected'} onClick={heartbeatSSE.start}>开始</Button>
              <Button size="sm" variant="outline" onClick={heartbeatSSE.abort}>停止</Button>
              <Badge variant={heartbeatSSE.status === 'connected' ? 'success' : 'secondary'}>{heartbeatSSE.status}</Badge>
            </div>
            <div className="bg-slate-900 text-green-400 rounded-md p-3 max-h-48 overflow-y-auto text-xs font-mono">
              {heartbeatSSE.data.length === 0 ? <span className="text-slate-500">等待数据...</span> :
                heartbeatSSE.data.map((d, i) => <div key={i} className="py-0.5">{d}</div>)}
            </div>
          </CardContent>
        </Card>
      </div>

      {/* 操作符演示 */}
      <Card>
        <CardHeader><CardTitle><Search className="inline w-4 h-4 mr-1" />操作符演示</CardTitle></CardHeader>
        <CardContent>
          <div className="grid grid-cols-2 md:grid-cols-4 gap-3 mb-4">
            <Button variant="outline" disabled={loading === '/api/demo/zip'} onClick={() => runDemo('/api/demo/zip')}>Mono.zip</Button>
            <Button variant="outline" disabled={loading === '/api/demo/collect'} onClick={() => runDemo('/api/demo/collect')}>collectList</Button>
            <Button variant="outline" disabled={loading === '/api/demo/context'} onClick={() => runDemo('/api/demo/context')}>Context</Button>
            <Button variant="destructive" disabled={loading === '/api/demo/error/999'} onClick={() => runDemo('/api/demo/error/999')}>错误处理</Button>
          </div>
          {demoResult && (
            <pre className="bg-slate-900 text-green-400 rounded-md p-3 text-xs font-mono overflow-x-auto">{demoResult}</pre>
          )}
        </CardContent>
      </Card>
    </div>
  )
}
