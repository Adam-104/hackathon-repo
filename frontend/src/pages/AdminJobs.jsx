import { Link } from 'react-router-dom'
import { api } from '../api'
import { useAction, useLoad } from '../hooks'
import { Alert, Empty, Loading, when } from '../components/Ui'

export default function AdminJobs() {
  const { data, error, loading, setData } = useLoad(() => api.get('/api/admin/jobs'))
  const act = useAction()
  const toggle = (j) => act.run(async () => {
    const updated = await api.patch(`/api/admin/jobs/${j.id}/status`, { status: j.status === 'OPEN' ? 'CLOSED' : 'OPEN' })
    setData((list) => list.map((x) => (x.id === j.id ? updated : x)))
  })
  if (loading) return <Loading />
  return (
    <div className="stack">
      <div className="card head"><h1>All jobs</h1><p className="muted small">Close any job that breaks the platform rules.</p></div>
      <Alert>{error || act.error}</Alert>
      {!data?.length ? <Empty>No jobs have been posted yet.</Empty> : data.map((j) => (
        <div key={j.id} className="card row between">
          <div><h3><Link to={`/jobs/${j.id}`}>{j.title}</Link></h3><p className="muted small">{j.company} · posted by {j.recruiterName} · {when(j.createdAt)}</p></div>
          <div className="row"><span className={`badge ${j.status}`}>{j.status}</span>
            <button className="btn ghost small-btn" disabled={act.busy} onClick={() => toggle(j)}>{j.status === 'OPEN' ? 'Close' : 'Reopen'}</button></div>
        </div>
      ))}
    </div>
  )
}
