import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../auth'
import { useAction } from '../hooks'
import { Alert } from '../components/Ui'

export default function Login() {
  const { login } = useAuth()
  const navigate = useNavigate()
  const [form, setForm] = useState({ email: '', password: '' })
  const { busy, error, run } = useAction()
  const set = (k) => (e) => setForm({ ...form, [k]: e.target.value })

  const submit = (e) => {
    e.preventDefault()
    run(async () => {
      const user = await login(form.email, form.password)
      navigate(user.role === 'RECRUITER' ? '/recruiter/jobs' : user.role === 'ADMIN' ? '/admin' : '/')
    })
  }

  return (
    <form className="card narrow" onSubmit={submit}>
      <h1>Sign in</h1>
      <p className="muted">Stay on top of your career: jobs, applications and AI tools.</p>
      <Alert>{error}</Alert>
      <label>Email<input type="email" value={form.email} onChange={set('email')} autoComplete="email" required /></label>
      <label>Password<input type="password" value={form.password} onChange={set('password')} autoComplete="current-password" required /></label>
      <button className="btn block-btn" disabled={busy}>{busy ? 'Signing in…' : 'Sign in'}</button>
      <p className="muted small center">New to SmartRecruit? <Link to="/register">Join now</Link></p>
    </form>
  )
}
