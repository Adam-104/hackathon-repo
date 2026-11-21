import { Link } from 'react-router-dom'
import { api } from '../api'
import { useAction, useLoad } from '../hooks'
import { Alert, Empty, Loading, Skills, when } from '../components/Ui'

export default function RecruiterJobs() {
  const { data, error, loading, reload } = useLoad(() => api.get('/api/jobs/mine'))
  const act = useAction()
  const toggle = (j) => act.run(async () => { await api.patch(`/api/jobs/${j.id}/status`, { status: j.status === 'OPEN' ? 'CLOSED' : 'OPEN' }); reload() })

  if (loading && !data) return <Loading />
  return (
    <div className="stack">
      <div className="row between"><h1>My jobs</h1><Link className="btn" to="/recruiter/jobs/new">+ Post a job</Link></div>
      <Alert>{error || act.error}</Alert>
      {!data?.length ? <Empty>No jobs yet. Post your first job to start receiving applications.</Empty> : data.map((j) => (
        <div key={j.id} className="card">
          <div className="row between">
            <div><h3><Link to={`/jobs/${j.id}`}>{j.title}</Link></h3><p className="muted">{j.company}{j.location ? ` · ${j.location}` : ''} · posted {when(j.createdAt)}</p></div>
            <span className={`badge ${j.status}`}>{j.status}</span>
          </div>
          <Skills text={j.requiredSkills} />
          <div className="row">
            <Link className="btn" to={`/recruiter/jobs/${j.id}/applicants`}>Applicants</Link>
            <Link className="btn ghost" to={`/recruiter/jobs/${j.id}/edit`}>Edit</Link>
            <button className="btn ghost" disabled={act.busy} onClick={() => toggle(j)}>{j.status === 'OPEN' ? 'Close job' : 'Reopen job'}</button>
          </div>
        </div>
      ))}
    </div>
  )
}
