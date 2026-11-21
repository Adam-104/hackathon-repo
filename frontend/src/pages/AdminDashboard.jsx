import { api } from '../api'
import { useLoad } from '../hooks'
import { Alert, Loading } from '../components/Ui'

const Tile = ({ label, value }) => <div className="tile"><b>{value}</b><span>{label}</span></div>

export default function AdminDashboard() {
  const { data: s, error, loading } = useLoad(() => api.get('/api/admin/stats'))
  if (loading) return <Loading />
  if (error) return <Alert>{error}</Alert>
  return (
    <div className="stack">
      <div className="card head"><h1>Administration</h1><p className="muted small">Platform overview</p></div>
      <div className="tiles">
        <Tile label="Total users" value={s.users} /><Tile label="Candidates" value={s.candidates} />
        <Tile label="HR / Recruiters" value={s.recruiters} /><Tile label="Disabled accounts" value={s.disabledUsers} />
        <Tile label="Jobs" value={s.jobs} /><Tile label="Open jobs" value={s.openJobs} />
        <Tile label="Applications" value={s.applications} /><Tile label="AI results" value={s.aiResults} />
      </div>
    </div>
  )
}
