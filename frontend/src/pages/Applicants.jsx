import { useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { api } from '../api'
import { useAction, useLoad } from '../hooks'
import AiResult, { Score } from '../components/AiResult'
import { Alert, Empty, Loading, Skills, when } from '../components/Ui'

const STATUSES = ['APPLIED', 'SHORTLISTED', 'REJECTED', 'HIRED']

export default function Applicants() {
  const { id } = useParams()
  const job = useLoad(() => api.get(`/api/jobs/${id}`), [id])
  const { data, error, loading, setData } = useLoad(() => api.get(`/api/jobs/${id}/applications`), [id])
  const [results, setResults] = useState({})
  const [working, setWorking] = useState('')
  const act = useAction()

  const replace = (updated) => setData((list) => list.map((a) => (a.id === updated.id ? { ...a, ...updated, candidate: updated.candidate || a.candidate } : a)))

  const setStatus = (a, status) => act.run(async () => replace(await api.patch(`/api/applications/${a.id}/status`, { status })))
  const screen = (a) => act.run(async () => {
    setWorking(`s${a.id}`)
    try {
      const r = await api.post(`/api/ai/applications/${a.id}/screen`)
      setResults((m) => ({ ...m, [a.id]: r })); setData((list) => list.map((x) => (x.id === a.id ? { ...x, matchScore: r.score } : x)))
    } finally { setWorking('') }
  })
  const questions = (a) => act.run(async () => {
    setWorking(`q${a.id}`)
    try { const r = await api.post(`/api/ai/applications/${a.id}/interview-questions?count=8`); setResults((m) => ({ ...m, [`q${a.id}`]: r })) } finally { setWorking('') }
  })

  if (loading && !data) return <Loading />
  const sorted = [...(data || [])].sort((a, b) => (b.matchScore ?? -1) - (a.matchScore ?? -1))

  return (
    <div className="stack">
      <p><Link to="/recruiter/jobs">← My jobs</Link></p>
      <h1>Applicants{job.data ? `: ${job.data.title}` : ''}</h1>
      <Alert>{error || act.error}</Alert>
      {!sorted.length ? <Empty>No applications yet.</Empty> : sorted.map((a) => (
        <div key={a.id} className="card">
          <div className="row between">
            <div>
              <h3>{a.candidate.name}</h3>
              <p className="muted">{a.candidate.headline || 'No headline'} · {a.candidate.experienceYears ?? '?'} yrs · {a.candidate.email} · applied {when(a.appliedAt)}</p>
            </div>
            <div className="row"><Score value={a.matchScore} label="AI match" />
              <select aria-label="Application status" value={a.status} disabled={act.busy} onChange={(e) => setStatus(a, e.target.value)}>{STATUSES.map((s) => <option key={s}>{s}</option>)}</select></div>
          </div>
          <Skills text={a.candidate.skills} />
          {a.coverNote && <p className="note"><b>Cover note:</b> {a.coverNote}</p>}
          <div className="row">
            <button className="btn" disabled={!!working} onClick={() => screen(a)}>{working === `s${a.id}` ? 'Screening…' : '✨ AI screen'}</button>
            <button className="btn alt" disabled={!!working} onClick={() => questions(a)}>{working === `q${a.id}` ? 'Writing…' : '❓ Interview questions'}</button>
          </div>
          {results[a.id] && <AiResult analysis={results[a.id]} />}
          {results[`q${a.id}`] && <AiResult analysis={results[`q${a.id}`]} />}
        </div>
      ))}
    </div>
  )
}
