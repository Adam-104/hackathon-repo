import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../auth'
import { useAction } from '../hooks'
import { Alert } from '../components/Ui'

export default function Register() {
  const { register } = useAuth()
  const navigate = useNavigate()
  const [form, setForm] = useState({ name: '', email: '', password: '', role: 'CANDIDATE' })
  const { busy, error, run } = useAction()
  const set = (k) => (e) => setForm({ ...form, [k]: e.target.value })

  const submit = (e) => {
    e.preventDefault()
    run(async () => {
      const user = await register(form)
      navigate(user.role === 'RECRUITER' ? '/recruiter/jobs' : '/resume')
    })
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
      <label>Password<input type="password" value={form.password} onChange={set('password')} minLength={8} maxLength={72}
        autoComplete="new-password" required /><span className="muted small">At least 8 characters.</span></label>
      <button className="btn block-btn" disabled={busy}>{busy ? 'Creating…' : 'Agree & join'}</button>
      <p className="muted small center">Already on SmartRecruit? <Link to="/login">Sign in</Link></p>
    </form>
  )
}
