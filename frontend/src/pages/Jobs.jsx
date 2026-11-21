import { useEffect, useState } from 'react'
import { Link, useNavigate, useSearchParams } from 'react-router-dom'
import { api } from '../api'
import { useLoad } from '../hooks'
import JobPanel from '../components/JobPanel'
import { Alert, Empty, Loading, when } from '../components/Ui'

/** Two-pane jobs board: the list on the left, the selected job on the right (single column on phones). */
export default function Jobs() {
  const [params] = useSearchParams()
  const q = params.get('q') || ''
  const navigate = useNavigate()
  const { data, error, loading } = useLoad(() => api.get(`/api/jobs?q=${encodeURIComponent(q)}`), [q])
  const [selected, setSelected] = useState(null)

  useEffect(() => { if (data?.length) setSelected((s) => (data.some((j) => j.id === s) ? s : data[0].id)) }, [data])

  const open = (e, id) => {
    if (window.matchMedia('(min-width: 900px)').matches) { e.preventDefault(); setSelected(id) } else { e.preventDefault(); navigate(`/jobs/${id}`) }
  }

  return (
    <div className="stack">
      <div className="card head">
        <h1>{q ? `Results for “${q}”` : 'Jobs for you'}</h1>
        <p className="muted small">{data ? `${data.length} open ${data.length === 1 ? 'position' : 'positions'}` : ''}</p>
      </div>
      <Alert>{error}</Alert>
      {loading && !data ? <Loading /> : !data?.length ? <Empty>No open jobs found.</Empty> : (
        <div className="twopane">
          <div className="card list">
            {data.map((j) => (
              <Link key={j.id} to={`/jobs/${j.id}`} onClick={(e) => open(e, j.id)} className={`jobrow ${selected === j.id ? 'on' : ''}`}>
                <span className="co-logo">{j.company[0]}</span>
                <div>
                  <h3>{j.title}</h3>
                  <p className="small">{j.company}</p>
                  <p className="muted small">{j.location || 'Location not set'}{j.experienceLevel ? ` · ${j.experienceLevel}` : ''}</p>
                  <p className="muted small">{when(j.createdAt)}</p>
                </div>
              </Link>
            ))}
          </div>
          <div className="card detail">{selected && <JobPanel id={selected} />}</div>
        </div>
      )}
    </div>
  )
}
