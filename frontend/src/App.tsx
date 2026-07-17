import { Routes, Route } from 'react-router-dom'
import { ErrorBoundary } from 'react-error-boundary'
import Layout from './components/layout/Layout'
import HomePage from './pages/HomePage'
import UsersPage from './pages/UsersPage'
import DemoPage from './pages/DemoPage'

function ErrorFallback({ error, resetErrorBoundary }: { error: Error; resetErrorBoundary: () => void }) {
  return (
    <div className="p-6 text-center">
      <h3 className="text-lg font-bold text-red-600 mb-2">出错了</h3>
      <p className="text-slate-500 mb-3">{error.message}</p>
      <button className="px-4 py-2 bg-primary-600 text-white rounded-md" onClick={resetErrorBoundary}>重试</button>
    </div>
  )
}

export default function App() {
  return (
    <Routes>
      <Route element={<Layout />}>
        <Route path="/" element={<HomePage />} />
        <Route path="/users" element={
          <ErrorBoundary FallbackComponent={ErrorFallback}><UsersPage /></ErrorBoundary>
        } />
        <Route path="/demo" element={
          <ErrorBoundary FallbackComponent={ErrorFallback}><DemoPage /></ErrorBoundary>
        } />
      </Route>
    </Routes>
  )
}
