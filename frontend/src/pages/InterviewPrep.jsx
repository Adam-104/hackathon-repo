import { useState } from 'react'
import { useParams } from 'react-router-dom'
import { api } from '../api'
import { useAction, useLoad } from '../hooks'
import AiResult from '../components/AiResult'
import { Alert } from '../components/Ui'

export default function InterviewPrep() {
  const { jobId: fromUrl } = useParams()
  const jobs = useLoad(() => api.get('/api/jobs'))
  const [jobId, setJobId] = useState(fromUrl || '')
  const [count, setCount] = useState(8)
  const [result, setResult] = useState(null)
  const gen = useAction()

  return (
    <div className="stack">
      <h1>Interview prep</h1>
      <p className="muted">The AI reads your resume and the job posting, then writes the questions you are most likely to be asked, with hints.</p>
      <div className="card">
        <div className="row top">
          <label className="grow">Job
            <select value={jobId} onChange={(e) => setJobId(e.target.value)}>
              <option value="">Choose a job…</option>
              {jobs.data?.map((j) => <option key={j.id} value={j.id}>{j.title} · {j.company}</option>)}
            </select>
          </label>
          <label>Questions
            <select value={count} onChange={(e) => setCount(Number(e.target.value))}>{[5, 8, 10, 12].map((n) => <option key={n}>{n}</option>)}</select>
          </label>
          <button className="btn" disabled={!jobId || gen.busy}
            onClick={() => gen.run(async () => setResult(await api.post('/api/ai/interview-questions', { jobId: Number(jobId), count })))}>
            {gen.busy ? 'Writing questions…' : 'Generate'}
          </button>
        </div>
        <Alert>{gen.error}</Alert>
      </div>
      {result && <div className="card"><AiResult analysis={result} /></div>}
    </div>
  )
}
