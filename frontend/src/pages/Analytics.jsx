import { api } from '../api'
import { useLoad } from '../hooks'
import { Alert, Loading } from '../components/Ui'

const Tile = ({ label, value }) => <div className="tile"><b>{value ?? '–'}</b><span>{label}</span></div>

function Bars({ rows, max }) {
  return rows.map(([label, value, extra]) => (
    <div className="bar-row" key={label}>
      <span className="bar-label" title={label}>{label}</span>
      <div className="bar"><i style={{ width: `${max ? (value / max) * 100 : 0}%` }} /></div>
      <span className="bar-val">{value}{extra}</span>
    </div>
  ))
}

export default function Analytics() {
  const { data: d, error, loading } = useLoad(() => api.get('/api/analytics/recruiter'))
  if (loading) return <Loading />
  if (error) return <Alert>{error}</Alert>
  const statusRows = Object.entries(d.byStatus)
  return (
    <div className="stack">
      <h1>Recruitment analytics</h1>
      <div className="tiles">
        <Tile label="Jobs posted" value={d.totalJobs} />
        <Tile label="Open jobs" value={d.openJobs} />
        <Tile label="Applications" value={d.totalApplications} />
        <Tile label="Avg AI match" value={d.averageMatchScore} />
      </div>
      <div className="cols">
        <div className="card"><h3>Hiring funnel</h3>
          <Bars rows={statusRows.map(([s, n]) => [s, n, ''])} max={Math.max(1, ...statusRows.map(([, n]) => n))} /></div>
        <div className="card"><h3>Skills you ask for most</h3>
          {d.topSkills.length ? <Bars rows={d.topSkills.map((s) => [s.skill, s.jobs, ' jobs'])} max={Math.max(...d.topSkills.map((s) => s.jobs))} /> : <p className="muted">Add skills to your jobs to see this.</p>}</div>
      </div>
      <div className="card"><h3>Applications per job</h3>
        {d.perJob.length ? <Bars rows={d.perJob.map((j) => [j.title, j.applications, j.averageScore != null ? ` · avg ${j.averageScore}` : ''])} max={Math.max(1, ...d.perJob.map((j) => j.applications))} /> : <p className="muted">No jobs yet.</p>}</div>
    </div>
  )
}
