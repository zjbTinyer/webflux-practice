import { Link } from 'react-router-dom'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '../components/ui/card'
import { Badge } from '../components/ui/badge'
import { Button } from '../components/ui/button'
import { Users, FlaskConical } from 'lucide-react'

const techTags = ['Mono', 'Flux', 'SSE', 'WebSocket', 'R2DBC', 'Flyway', 'SpringDoc']

export default function HomePage() {
  return (
    <div className="max-w-4xl mx-auto space-y-6">
      <div>
        <h1 className="text-3xl font-bold text-slate-900">WebFlux 响应式编程项目</h1>
        <p className="text-slate-500 mt-2">
          Spring Boot 3.2 + WebFlux + R2DBC + PostgreSQL | React 19 + TypeScript + Tailwind CSS
        </p>
        <div className="flex flex-wrap gap-1.5 mt-3">
          {techTags.map((t) => (
            <Badge key={t} variant="secondary">{t}</Badge>
          ))}
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2"><Users className="w-5 h-5" />用户管理</CardTitle>
            <CardDescription>CRUD + 分页查询，体验响应式 API</CardDescription>
          </CardHeader>
          <CardContent>
            <Link to="/users">
              <Button className="w-full">进入用户管理</Button>
            </Link>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2"><FlaskConical className="w-5 h-5" />Demo 演示</CardTitle>
            <CardDescription>SSE 流式推送 + 操作符实战</CardDescription>
          </CardHeader>
          <CardContent>
            <Link to="/demo">
              <Button variant="outline" className="w-full">查看演示</Button>
            </Link>
          </CardContent>
        </Card>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>快速入口</CardTitle>
        </CardHeader>
        <CardContent>
          <ul className="space-y-1 text-sm text-slate-600">
            <li>📡 Swagger UI: <a href="http://localhost:8080/swagger-ui.html" className="text-primary-600 hover:underline" target="_blank">localhost:8080/swagger-ui.html</a></li>
            <li>💚 Actuator: <a href="http://localhost:8080/actuator/health" className="text-primary-600 hover:underline" target="_blank">localhost:8080/actuator/health</a></li>
            <li>💬 WebSocket: <code className="bg-slate-100 px-1 rounded">ws://localhost:8080/ws/chat</code></li>
          </ul>
        </CardContent>
      </Card>
    </div>
  )
}
