import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../auth'
import { useAction } from '../hooks'
import { Alert } from '../components/Ui'
import { api } from '../api'
import PasswordField from '../components/PasswordField'

export default function Login() {
  const { login } = useAuth()
  const navigate = useNavigate()
  const [form, setForm] = useState({ email: '', password: '' })
  const { busy, error, run } = useAction()
  const [resent, setResent] = useState(false)
  const set = (k) => (e) => setForm({ ...form, [k]: e.target.value })

  const submit = (e) => {
    e.preventDefault()
    run(async () => {
      const user = await login(form.email, form.password)
      navigate(user.role === 'RECRUITER' ? '/recruiter/jobs' : user.role === 'ADMIN' ? '/admin' : '/')
    })
  }

  const needsVerify = error.toLowerCase().includes('verify your email')
  const resend = () => run(async () => {
    await api.post('/api/auth/resend-verification', { email: form.email })
    setResent(true)
  })

  return (
    <form className="card narrow" onSubmit={submit}>
      <h1>Sign in</h1>
      <p className="muted">Stay on top of your career: jobs, applications and AI tools.</p>
      <Alert>{error}</Alert>
      {needsVerify && (
        <p className="small">
          {resent ? 'A new verification link is on its way.' : <button type="button" className="link-btn" onClick={resend}>Resend verification email</button>}
        </p>
      )}
      <label>Email<input type="email" value={form.email} onChange={set('email')} autoComplete="email" required /></label>
      <PasswordField label="Password" value={form.password} onChange={set('password')} autoComplete="current-password" required />
      <button className="btn block-btn" disabled={busy}>{busy ? 'Signing in…' : 'Sign in'}</button>
      <p className="muted small center">New to SmartRecruit? <Link to="/register">Join now</Link></p>
    </form>
  )
}
