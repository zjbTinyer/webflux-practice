import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import { Card, CardContent, CardHeader, CardTitle } from '../components/ui/card'
import { Button } from '../components/ui/button'
import { Input } from '../components/ui/input'
import { Label } from '../components/ui/label'
import { Badge } from '../components/ui/badge'
import { Skeleton } from '../components/ui/skeleton'
import { Separator } from '../components/ui/separator'
import type { User, UserDTO, PageResponse, Result } from '../api/userApi'

const BASE = '/api/users'

async function fetchUsers(page: number, size: number): Promise<Result<PageResponse<User>>> {
  const res = await fetch(`${BASE}?page=${page}&size=${size}`)
  if (!res.ok) throw new Error('获取用户失败')
  return res.json()
}

async function createUser(dto: UserDTO): Promise<Result<User>> {
  const res = await fetch(BASE, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(dto) })
  if (!res.ok) { const e = await res.json(); throw new Error(e.message || '创建失败') }
  return res.json()
}

async function updateUser(id: number, dto: UserDTO): Promise<Result<User>> {
  const res = await fetch(`${BASE}/${id}`, { method: 'PUT', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(dto) })
  if (!res.ok) throw new Error('更新失败')
  return res.json()
}

async function deleteUser(id: number): Promise<void> {
  const res = await fetch(`${BASE}/${id}`, { method: 'DELETE' })
  if (!res.ok) throw new Error('删除失败')
}

export default function UsersPage() {
  const [page, setPage] = useState(0)
  const [size, setSize] = useState(10)
  const [editingId, setEditingId] = useState<number | null>(null)
  const [formName, setFormName] = useState('')
  const [formEmail, setFormEmail] = useState('')
  const [formAge, setFormAge] = useState('')
  const queryClient = useQueryClient()

  const { data, isLoading, isError, error } = useQuery({
    queryKey: ['users', page, size],
    queryFn: () => fetchUsers(page, size),
  })

  const createMut = useMutation({ mutationFn: createUser, onSuccess: () => { queryClient.invalidateQueries({ queryKey: ['users'] }); toast.success('创建成功'); resetForm() } })
  const updateMut = useMutation({ mutationFn: ({ id, dto }: { id: number; dto: UserDTO }) => updateUser(id, dto), onSuccess: () => { queryClient.invalidateQueries({ queryKey: ['users'] }); toast.success('更新成功'); resetForm() } })
  const deleteMut = useMutation({ mutationFn: deleteUser, onSuccess: () => { queryClient.invalidateQueries({ queryKey: ['users'] }); toast.success('删除成功') } })

  const pageData = data?.data

  const startEdit = (u: User) => {
    setEditingId(u.id!)
    setFormName(u.name)
    setFormEmail(u.email)
    setFormAge(u.age?.toString() || '')
  }

  const resetForm = () => {
    setEditingId(null)
    setFormName('')
    setFormEmail('')
    setFormAge('')
  }

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    const dto: UserDTO = { name: formName, email: formEmail, age: formAge ? Number(formAge) : undefined }
    if (editingId) {
      updateMut.mutate({ id: editingId, dto })
    } else {
      createMut.mutate(dto)
    }
  }

  if (isError) return <div className="text-red-500 p-6">加载失败: {(error as Error).message}</div>

  return (
    <div className="max-w-5xl mx-auto space-y-6">
      <h2 className="text-2xl font-bold">用户管理</h2>

      {/* 表单 */}
      <Card>
        <CardHeader>
          <CardTitle>{editingId ? '编辑用户' : '创建用户'}</CardTitle>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit} className="space-y-3">
            <div className="grid grid-cols-1 md:grid-cols-4 gap-3">
              <div><Label>姓名</Label><Input required value={formName} onChange={e => setFormName(e.target.value)} placeholder="姓名" /></div>
              <div><Label>邮箱</Label><Input required type="email" value={formEmail} onChange={e => setFormEmail(e.target.value)} placeholder="email" /></div>
              <div><Label>年龄</Label><Input type="number" value={formAge} onChange={e => setFormAge(e.target.value)} placeholder="年龄" /></div>
              <div className="flex items-end gap-2">
                <Button type="submit" disabled={createMut.isPending || updateMut.isPending}>
                  {editingId ? '更新' : '创建'}
                </Button>
                {editingId && <Button type="button" variant="outline" onClick={resetForm}>取消</Button>}
              </div>
            </div>
          </form>
        </CardContent>
      </Card>

      <Separator />

      {/* 表格 */}
      <Card>
        <CardHeader className="flex flex-row items-center justify-between">
          <CardTitle>用户列表</CardTitle>
          {pageData && <Badge variant="secondary">共 {pageData.totalElements} 条</Badge>}
        </CardHeader>
        <CardContent>
          {isLoading ? (
            <div className="space-y-2">{Array.from({ length: 3 }).map((_, i) => <Skeleton key={i} className="h-10 w-full" />)}</div>
          ) : pageData?.content.length === 0 ? (
            <p className="text-slate-400 text-center py-8">暂无数据</p>
          ) : (
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b text-left text-slate-500">
                  <th className="py-2 pr-4">ID</th><th className="py-2 pr-4">姓名</th><th className="py-2 pr-4">邮箱</th><th className="py-2 pr-4">年龄</th><th className="py-2">操作</th>
                </tr>
              </thead>
              <tbody>
                {pageData?.content.map(u => (
                  <tr key={u.id} className="border-b last:border-0 hover:bg-slate-50">
                    <td className="py-2 pr-4">{u.id}</td>
                    <td className="py-2 pr-4">{u.name}</td>
                    <td className="py-2 pr-4">{u.email}</td>
                    <td className="py-2 pr-4">{u.age ?? '-'}</td>
                    <td className="py-2 flex gap-1">
                      <Button size="sm" variant="outline" onClick={() => startEdit(u)}>编辑</Button>
                      <Button size="sm" variant="destructive" onClick={() => { if (confirm('确认删除？')) deleteMut.mutate(u.id!) }}>删除</Button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}

          {/* 分页控件 */}
          {pageData && pageData.totalPages > 0 && (
            <div className="flex items-center justify-between mt-4 pt-4 border-t">
              <div className="flex items-center gap-2 text-sm text-slate-500">
                <span>每页</span>
                <select value={size} onChange={e => { setSize(Number(e.target.value)); setPage(0) }} className="border rounded px-2 py-0.5 text-sm">
                  {[5, 10, 20].map(n => <option key={n} value={n}>{n}</option>)}
                </select>
                <span>条</span>
              </div>
              <div className="flex items-center gap-2">
                <Button size="sm" variant="outline" disabled={pageData.first} onClick={() => setPage(p => Math.max(0, p - 1))}>上一页</Button>
                <span className="text-sm text-slate-500">第 {page + 1} / {pageData.totalPages} 页</span>
                <Button size="sm" variant="outline" disabled={pageData.last} onClick={() => setPage(p => p + 1)}>下一页</Button>
              </div>
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  )
}
