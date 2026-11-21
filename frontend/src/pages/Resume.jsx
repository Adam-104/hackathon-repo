import { useEffect, useState } from 'react'
import { api } from '../api'
import { useAction, useLoad } from '../hooks'
import AiResult from '../components/AiResult'
import { Alert, Loading, when } from '../components/Ui'

export default function Resume() {
  const profile = useLoad(() => api.get('/api/candidate/profile'))
  const jobs = useLoad(() => api.get('/api/jobs'))
  const history = useLoad(() => api.get('/api/ai/history'))
  const [form, setForm] = useState({ headline: '', skills: '', phone: '', experienceYears: '' })
  const [current, setCurrent] = useState(null)
  const [targetJob, setTargetJob] = useState('')
  const save = useAction()
  const upload = useAction()
  const analyze = useAction()
  const improve = useAction()
  const [saved, setSaved] = useState('')

  useEffect(() => {
    const p = profile.data
    if (p) setForm({ headline: p.headline || '', skills: p.skills || '', phone: p.phone || '', experienceYears: p.experienceYears ?? '' })
  }, [profile.data])

  if (profile.loading && !profile.data) return <Loading />
  const p = profile.data
  const set = (k) => (e) => setForm({ ...form, [k]: e.target.value })

  const show = (result) => { setCurrent(result); history.reload() }

  return (
    <div className="stack">
      <h1>My resume & AI tools</h1>
      <Alert>{profile.error}</Alert>

      <div className="card">
        <h3>1. Profile</h3>
        <form onSubmit={(e) => {
          e.preventDefault(); setSaved('')
          save.run(async () => {
            const body = { ...form, experienceYears: form.experienceYears === '' ? null : Number(form.experienceYears) }
            profile.setData(await api.put('/api/candidate/profile', body)); setSaved('Profile saved.')
          })
        }}>
          <div className="cols">
            <label>Headline<input value={form.headline} onChange={set('headline')} maxLength={200} placeholder="Java Full Stack Developer (fresher)" /></label>
            <label>Phone<input value={form.phone} onChange={set('phone')} maxLength={30} /></label>
          </div>
          <div className="cols">
            <label>Skills (comma separated)<input value={form.skills} onChange={set('skills')} maxLength={1000} placeholder="Java, Spring Boot, MySQL, React" /></label>
            <label>Years of experience<input type="number" min="0" max="60" value={form.experienceYears} onChange={set('experienceYears')} /></label>
          </div>
          <Alert>{save.error}</Alert><Alert kind="ok">{saved}</Alert>
          <button className="btn" disabled={save.busy}>{save.busy ? 'Saving…' : 'Save profile'}</button>
        </form>
      </div>

      <div className="card">
        <h3>2. Resume</h3>
        <p className="muted small">Upload a text-based PDF or TXT file (max 5 MB). Only the text is stored, and it is used by the AI features.</p>
        <p>{p?.hasResume ? <>✅ <b>{p.resumeFileName}</b> uploaded ({p.resumeChars.toLocaleString()} characters read)</> : 'No resume uploaded yet.'}</p>
        <input type="file" accept=".pdf,.txt,application/pdf,text/plain" disabled={upload.busy}
          onChange={(e) => { const f = e.target.files[0]; if (f) upload.run(async () => profile.setData(await api.upload('/api/candidate/resume', f))); e.target.value = '' }} />
        {upload.busy && <p className="muted">Reading your resume…</p>}
        <Alert>{upload.error}</Alert>
      </div>

      <div className="card">
        <h3>3. AI tools</h3>
        <div className="row">
          <button className="btn" disabled={!p?.hasResume || analyze.busy}
            onClick={() => analyze.run(async () => show(await api.post('/api/ai/analyze-resume')))}>
            {analyze.busy ? 'Analysing…' : '🔍 Analyse my resume'}
          </button>
        </div>
        <Alert>{analyze.error}</Alert>
        <div className="row top">
          <label className="grow">Improve my resume for a job (optional)
            <select value={targetJob} onChange={(e) => setTargetJob(e.target.value)}>
              <option value="">General improvements</option>
              {jobs.data?.map((j) => <option key={j.id} value={j.id}>{j.title} · {j.company}</option>)}
            </select>
          </label>
          <button className="btn alt" disabled={!p?.hasResume || improve.busy}
            onClick={() => improve.run(async () => show(await api.post('/api/ai/improve-resume', { jobId: targetJob ? Number(targetJob) : null })))}>
            {improve.busy ? 'Working…' : '✍️ Improve my resume'}
          </button>
        </div>
        <Alert>{improve.error}</Alert>
        {!p?.hasResume && <p className="muted small">Upload a resume to use the AI tools.</p>}
        {current && <AiResult analysis={current} />}
      </div>

      <div className="card">
        <h3>Past AI results</h3>
        {!history.data?.length ? <p className="muted">Nothing yet.</p> : history.data.map((h) => (
          <details key={h.id} className="hist">
            <summary><b>{h.type}</b>{h.jobTitle ? ` · ${h.jobTitle}` : ''}{h.score != null ? ` · score ${h.score}` : ''} <span className="muted small">{when(h.createdAt)}</span></summary>
            <AiResult analysis={h} />
          </details>
        ))}
      </div>
    </div>
  )
}
