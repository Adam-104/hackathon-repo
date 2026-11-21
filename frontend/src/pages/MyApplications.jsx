import { Link } from 'react-router-dom'
import { api } from '../api'
import { useLoad } from '../hooks'
import { Score } from '../components/AiResult'
import { Alert, Empty, Loading, StatusBadge, when } from '../components/Ui'

export default function MyApplications() {
  const { data, error, loading } = useLoad(() => api.get('/api/applications/mine'))
  if (loading) return <Loading />
  return (
    <div className="stack">
      <h1>My applications</h1>
      <Alert>{error}</Alert>
      {!data?.length ? <Empty>You have not applied yet. <Link to="/">Browse jobs</Link>.</Empty> : data.map((a) => (
        <div key={a.id} className="card row between">
          <div>
            <h3><Link to={`/jobs/${a.jobId}`}>{a.jobTitle}</Link></h3>
            <p className="muted">{a.company} · applied {when(a.appliedAt)}</p>
          </div>
          <div className="row"><Score value={a.matchScore} label="AI match" /><StatusBadge status={a.status} /></div>
        </div>
      ))}
    </div>
  )
}
