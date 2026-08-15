// Login page — authenticates user and redirects by role
import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { authService } from '../services/authService'

export default function Login() {
  const { login } = useAuth()
  const navigate  = useNavigate()
  // Stores email and password input values
  const [form,    setForm]    = useState({ email: '', password: '' })
  const [error,   setError]   = useState('')
  const [loading, setLoading] = useState(false)

  const handleChange = e => setForm({ ...form, [e.target.name]: e.target.value })

  // Submits credentials to backend and redirects based on role
  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    if (!form.email || !form.password) { setError('All fields are required.'); return }
    setLoading(true)
    try {
      // POST /api/v1/auth/signin → { id, fullName, roles, jwt }
      const { data } = await authService.login(form)
      login(data)

      // Smart redirect: different dashboard per role
      if (data.roles?.includes('ROLE_ADMIN'))  navigate('/dashboard/admin')
      else if (data.roles?.includes('ROLE_OWNER')) navigate('/dashboard/owner')
      else navigate('/')
    } catch (err) {
      const msg = err.response?.data?.message || err.response?.data || 'Invalid email or password.'
      setError(typeof msg === 'string' ? msg : 'Login failed. Please try again.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-screen bg-gray-50 flex items-center justify-center px-4">
      <div className="w-full max-w-md">
        <div className="text-center mb-8">
          <Link to="/" className="inline-flex items-center gap-2 mb-6">
            <div className="w-9 h-9 bg-brand-500 rounded-xl flex items-center justify-center">
              <span className="text-white font-bold">ES</span>
            </div>
            <span className="font-display font-bold text-2xl text-gray-900">EasySeat</span>
          </Link>
          <h1 className="text-2xl font-display font-bold text-gray-900">Welcome back</h1>
        </div>

        <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-8">
          {error && (
            <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-xl text-sm mb-5">
              {error}
            </div>
          )}
          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="block text-xs font-medium text-gray-500 mb-1.5 uppercase tracking-wide">Email</label>
              <input type="email" name="email" value={form.email} onChange={handleChange}
                placeholder="you@example.com"
                className="w-full border border-gray-200 rounded-xl px-4 py-3 text-sm focus:outline-none focus:border-brand-500 focus:ring-1 focus:ring-brand-500"
              />
            </div>
            <div>
              <label className="block text-xs font-medium text-gray-500 mb-1.5 uppercase tracking-wide">Password</label>
              <input type="password" name="password" value={form.password} onChange={handleChange}
                placeholder="••••••••"
                className="w-full border border-gray-200 rounded-xl px-4 py-3 text-sm focus:outline-none focus:border-brand-500 focus:ring-1 focus:ring-brand-500"
              />
            </div>
            <button type="submit" disabled={loading}
              className="w-full bg-brand-500 text-white font-medium py-3 rounded-xl hover:bg-brand-600 transition-colors disabled:opacity-60 mt-2">
              {loading ? 'Signing in...' : 'Sign In'}
            </button>
          </form>
        </div>

        <p className="text-center text-sm text-gray-500 mt-6">
          Don't have an account?{' '}
          <Link to="/register" className="text-brand-500 font-medium hover:underline">Create one</Link>
        </p>
      </div>
    </div>
  )
}
