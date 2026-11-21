import { useEffect, useRef, useState } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import { api } from '../api'

export default function VerifyEmail() {
  const [params] = useSearchParams()
  const token = params.get('token')
  const [state, setState] = useState({ status: token ? 'loading' : 'error', message: token ? '' : 'This verification link is missing its token.' })
  const started = useRef(false)

  useEffect(() => {
    if (!token || started.current) return
    started.current = true // the token works once, so never send it twice
    api.post('/api/auth/verify-email', { token })
      .then((res) => setState({ status: 'ok', message: res?.message || 'Email verified.' }))
      .catch((e) => setState({ status: 'error', message: e.message }))
  }, [token])

  return (
    <div className="card narrow center">
      {state.status === 'loading' && <><h1>Verifying…</h1><p className="muted">One moment while we confirm your email.</p></>}
      {state.status === 'ok' && <>
        <h1>Email verified</h1>
        <p className="muted">{state.message}</p>
        <Link className="btn block-btn" to="/login">Sign in</Link>
      </>}
      {state.status === 'error' && <>
        <h1>Link problem</h1>
        <p className="muted">{state.message}</p>
        <p className="muted small">Sign in with your email and password to request a new link.</p>
        <Link className="btn block-btn" to="/login">Go to sign in</Link>
      </>}
    </div>
  )
}
