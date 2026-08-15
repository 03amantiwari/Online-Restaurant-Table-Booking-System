// Registration form — collects user info and chosen role
import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { authService } from '../services/authService'

export default function Register() {
  const navigate = useNavigate()
  // Form field values including roleName
  const [form, setForm] = useState({
    fullName: '', email: '', phoneNumber: '',
    password: '', confirm: '', dateOfBirth: '',
    roleName: 'ROLE_CUSTOMER',   // default role selection
  })
  const [errors,   setErrors]   = useState({})
  const [apiError, setApiError] = useState('')
  const [loading,  setLoading]  = useState(false)

  // Client-side validation before hitting the API
  const validate = () => {
    const e = {}
    if (!form.fullName.trim())                             e.fullName     = 'Name is required'
    if (!form.email.match(/^[^\s@]+@[^\s@]+\.[^\s@]+$/))  e.email        = 'Valid email required'
    if (!form.phoneNumber.match(/^[0-9]{10}$/))            e.phoneNumber  = '10-digit phone required'
    if (!form.dateOfBirth)                                 e.dateOfBirth  = 'Date of birth required'
    if (form.password.length < 8)                          e.password     = 'Min 8 characters'
    if (form.password !== form.confirm)                    e.confirm      = 'Passwords do not match'
    return e
  }

  const handleChange = e => {
    setForm({ ...form, [e.target.name]: e.target.value })
    if (errors[e.target.name]) setErrors({ ...errors, [e.target.name]: '' })
    setApiError('')
  }

  // Sends registration payload to backend, redirects to login on success
  const handleSubmit = async (e) => {
    e.preventDefault()
    const errs = validate()
    if (Object.keys(errs).length) { setErrors(errs); return }
    setLoading(true)
    try {
      // POST /api/v1/auth/signup — roleName accepted: ROLE_CUSTOMER | ROLE_OWNER
      await authService.register({
        fullName:    form.fullName,
        dateOfBirth: form.dateOfBirth,
        email:       form.email,
        password:    form.password,
        phoneNumber: form.phoneNumber,
        roleName:    form.roleName,
      })
      navigate('/login', { state: { registered: true } })
    } catch (err) {
      const msg = err.response?.data?.message || err.response?.data || 'Registration failed.'
      setApiError(typeof msg === 'string' ? msg : 'An error occurred.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-screen bg-gray-50 flex items-center justify-center px-4 py-10">
      <div className="w-full max-w-md">
        <div className="text-center mb-8">
          <Link to="/" className="inline-flex items-center gap-2 mb-6">
            <div className="w-9 h-9 bg-brand-500 rounded-xl flex items-center justify-center">
              <span className="text-white font-bold">ES</span>
            </div>
            <span className="font-display font-bold text-2xl text-gray-900">EasySeat</span>
          </Link>
          <h1 className="text-2xl font-display font-bold text-gray-900">Create an account</h1>
        </div>

        <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-8">
          {apiError && (
            <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-xl text-sm mb-5">
              {apiError}
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-4">
            {/* Role selector — Admin cannot be chosen from UI */}
            <div>
              <label className="block text-xs font-medium text-gray-500 mb-2 uppercase tracking-wide">I am registering as</label>
              <div className="grid grid-cols-2 gap-3">
                {[
                  { value: 'ROLE_CUSTOMER', label: '🧑 Customer',        desc: 'Book tables at restaurants' },
                  { value: 'ROLE_OWNER',    label: '🏪 Restaurant Owner', desc: 'Manage my restaurants'      },
                ].map(opt => (
                  <button
                    key={opt.value}
                    type="button"
                    onClick={() => setForm({ ...form, roleName: opt.value })}
                    className={`p-3 rounded-xl border-2 text-left transition-all
                      ${form.roleName === opt.value
                        ? 'border-brand-500 bg-red-50'
                        : 'border-gray-200 bg-white hover:border-gray-300'}`}
                  >
                    <p className="text-sm font-medium text-gray-900">{opt.label}</p>
                    <p className="text-xs text-gray-400 mt-0.5">{opt.desc}</p>
                  </button>
                ))}
              </div>
            </div>

            <Field name="fullName"    label="Full Name"         placeholder="Arjun Sharma"        form={form} errors={errors} handleChange={handleChange} />
            <Field name="email"       label="Email"             type="email" placeholder="you@example.com" form={form} errors={errors} handleChange={handleChange} />
            <Field name="phoneNumber" label="Phone"             placeholder="10-digit mobile"     form={form} errors={errors} handleChange={handleChange} />
            <Field name="dateOfBirth" label="Date of Birth"     type="date"                       form={form} errors={errors} handleChange={handleChange} />
            <Field name="password"    label="Password"          type="password" placeholder="Min 8 chars, 1 uppercase, 1 number, 1 special (#@$*)" form={form} errors={errors} handleChange={handleChange} />
            <Field name="confirm"     label="Confirm Password"  type="password" placeholder="Re-enter password" form={form} errors={errors} handleChange={handleChange} />

            <button type="submit" disabled={loading}
              className="w-full bg-brand-500 text-white font-medium py-3 rounded-xl hover:bg-brand-600 transition-colors disabled:opacity-60 mt-2">
              {loading ? 'Creating account...' : 'Create Account'}
            </button>
          </form>
        </div>

        <p className="text-center text-sm text-gray-500 mt-6">
          Already have an account?{' '}
          <Link to="/login" className="text-brand-500 font-medium hover:underline">Sign in</Link>
        </p>
      </div>
    </div>
  )
}

// Reusable input field with inline error — defined outside to avoid remount on render
const Field = ({ name, label, type = 'text', placeholder, form, errors, handleChange }) => (
  <div>
    <label className="block text-xs font-medium text-gray-500 mb-1.5 uppercase tracking-wide">{label}</label>
    <input
      type={type} name={name} value={form[name] || ''} onChange={handleChange}
      placeholder={placeholder}
      className={`w-full border rounded-xl px-4 py-3 text-sm focus:outline-none transition-colors
        ${errors[name] ? 'border-red-300 focus:border-red-500 focus:ring-1 focus:ring-red-500'
                       : 'border-gray-200 focus:border-brand-500 focus:ring-1 focus:ring-brand-500'}`}
    />
    {errors[name] && <p className="text-xs text-red-500 mt-1">{errors[name]}</p>}
  </div>
)
