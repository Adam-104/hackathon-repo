import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../auth'
import { useAction } from '../hooks'
import { Alert } from '../components/Ui'
import { api } from '../api'
import PasswordField from '../components/PasswordField'

export default function Register() {
  const { register } = useAuth()
  const navigate = useNavigate()
  const [form, setForm] = useState({ name: '', email: '', password: '', confirm: '', role: 'CANDIDATE' })
  const { busy, error, run, setError } = useAction()
  const [sentTo, setSentTo] = useState('')
  const [resent, setResent] = useState(false)
  const set = (k) => (e) => setForm({ ...form, [k]: e.target.value })

  const submit = (e) => {
    e.preventDefault()
    if (form.password !== form.confirm) { setError('Passwords do not match.'); return }
    run(async () => {
      const { name, email, password, role } = form
      const result = await register({ name, email, password, role })
      if (result.pending) { setSentTo(result.user.email); return }
      navigate(result.role === 'RECRUITER' ? '/recruiter/jobs' : '/resume')
    })
  }

  const resend = () => run(async () => {
    await api.post('/api/auth/resend-verification', { email: sentTo })
    setResent(true)
  })

  if (sentTo) {
    return (
      <div className="card narrow">
        <h1>Check your email</h1>
        <p className="muted">We sent a verification link to <strong>{sentTo}</strong>. Open it to activate your account, then sign in.</p>
        <Alert>{error}</Alert>
        {resent && <p className="muted small">A new link is on its way. It can take a minute to arrive.</p>}
        <button type="button" className="btn block-btn" disabled={busy} onClick={resend}>{busy ? 'Sending…' : 'Resend email'}</button>
        <p className="muted small center">Wrong address? <Link to="/register" onClick={() => setSentTo('')}>Start again</Link> · <Link to="/login">Sign in</Link></p>
      </div>
    )
  }

  return (
    <form className="card narrow" onSubmit={submit}>
      <h1>Join SmartRecruit</h1>
      <div className="seg" role="radiogroup" aria-label="Account type">
        {[['CANDIDATE', 'Candidate (job seeker)'], ['RECRUITER', 'HR / Recruiter (post jobs)']].map(([value, label]) => (
          <button type="button" key={value} role="radio" aria-checked={form.role === value}
            className={form.role === value ? 'on' : ''} onClick={() => setForm({ ...form, role: value })}>{label}</button>
        ))}
      </div>
      <Alert>{error}</Alert>
      <label>Full name<input value={form.name} onChange={set('name')} autoComplete="name" required /></label>
      <label>Email<input type="email" value={form.email} onChange={set('email')} autoComplete="email" required /></label>
      <PasswordField label="Password" value={form.password} onChange={set('password')} minLength={8} maxLength={72}
        autoComplete="new-password" required hint={<span className="muted small">8 to 72 characters.</span>} />
      <PasswordField label="Confirm password" value={form.confirm} onChange={set('confirm')} minLength={8} maxLength={72}
        autoComplete="new-password" required
        hint={form.confirm && form.confirm !== form.password ? <span className="pw-bad small">Passwords do not match.</span> : null} />
      <button className="btn block-btn" disabled={busy}>{busy ? 'Creating…' : 'Agree & join'}</button>
      <p className="muted small center">Already on SmartRecruit? <Link to="/login">Sign in</Link></p>
    </form>
  )
}
