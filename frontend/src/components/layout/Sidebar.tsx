import { NavLink } from 'react-router-dom'
import { Home, Users, FlaskConical } from 'lucide-react'
import { cn } from '../../lib/utils'

const links = [
  { to: '/', label: '首页', icon: Home },
  { to: '/users', label: '用户管理', icon: Users },
  { to: '/demo', label: 'Demo 演示', icon: FlaskConical },
]

export default function Sidebar() {
  return (
    <aside className="w-56 bg-white border-r border-slate-200 min-h-screen p-4 flex flex-col">
      <div className="mb-6 px-2">
        <h1 className="text-lg font-bold text-primary-700">⚡ WebFlux</h1>
        <p className="text-xs text-slate-400 mt-0.5">响应式编程项目</p>
      </div>
      <nav className="flex flex-col gap-1">
        {links.map(({ to, label, icon: Icon }) => (
          <NavLink
            key={to}
            to={to}
            end={to === '/'}
            className={({ isActive }) =>
              cn(
                'flex items-center gap-3 px-3 py-2 rounded-md text-sm font-medium transition-colors',
                isActive
                  ? 'bg-primary-50 text-primary-700'
                  : 'text-slate-600 hover:bg-slate-100'
              )
            }
          >
            <Icon className="w-4 h-4" />
            {label}
          </NavLink>
        ))}
      </nav>
    </aside>
  )
}
