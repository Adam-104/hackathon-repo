import { api } from '../api'
import { useAction, useLoad } from '../hooks'
import { Alert, Loading, when } from '../components/Ui'

const LABEL = { CANDIDATE: 'Candidate', RECRUITER: 'HR / Recruiter', ADMIN: 'Administrator' }

export default function AdminUsers() {
  const { data, error, loading, setData } = useLoad(() => api.get('/api/admin/users'))
  const act = useAction()
  const toggle = (u) => act.run(async () => {
    const updated = await api.patch(`/api/admin/users/${u.id}/enabled`, { enabled: !u.enabled })
    setData((list) => list.map((x) => (x.id === u.id ? updated : x)))
  })
  if (loading) return <Loading />
  return (
    <div className="stack">
      <div className="card head"><h1>Users</h1><p className="muted small">Disabled users can no longer sign in.</p></div>
      <Alert>{error || act.error}</Alert>
      <div className="card tablewrap">
        <table>
          <thead><tr><th>Name</th><th>Email</th><th>Role</th><th>Joined</th><th>Status</th><th /></tr></thead>
          <tbody>
            {data?.map((u) => (
              <tr key={u.id}>
                <td>{u.name}</td><td>{u.email}</td><td>{LABEL[u.role]}</td><td>{when(u.createdAt)}</td>
                <td><span className={`badge ${u.enabled ? 'OPEN' : 'CLOSED'}`}>{u.enabled ? 'ACTIVE' : 'DISABLED'}</span></td>
                <td>{u.role !== 'ADMIN' && <button className="btn ghost small-btn" disabled={act.busy} onClick={() => toggle(u)}>{u.enabled ? 'Disable' : 'Enable'}</button>}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  )
}
