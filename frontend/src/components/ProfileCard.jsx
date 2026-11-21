import { Link } from 'react-router-dom'
import { api } from '../api'
import { useAuth } from '../auth'
import { useLoad } from '../hooks'

const ROLE_LABEL = { CANDIDATE: 'Candidate', RECRUITER: 'HR / Recruiter', ADMIN: 'Administrator' }

/** The left-hand profile card shown to signed-in users. */
export default function ProfileCard() {
  const { user } = useAuth()
  const isCand = user.role === 'CANDIDATE'
  const isRec = user.role === 'RECRUITER'
  const { data } = useLoad(() => (isCand ? api.get('/api/candidate/profile') : isRec ? api.get('/api/jobs/mine') : Promise.resolve(null)), [user.role])
  const initials = user.name.split(' ').map((w) => w[0]).slice(0, 2).join('').toUpperCase()

  let strength = null
  if (isCand && data) {
    const parts = [data.headline, data.skills, data.hasResume, data.experienceYears != null, data.phone]
    strength = Math.round((parts.filter(Boolean).length / parts.length) * 100)
  }

  return (
    <div className="card pc">
      <div className="pc-cover" />
      <span className="avatar big">{initials}</span>
      <h3>{user.name}</h3>
      <p className="muted small">{isCand && data?.headline ? data.headline : ROLE_LABEL[user.role]}</p>
      <div className="pc-meta">
        {isCand && data && (<>
          <div className="meter"><i style={{ width: `${strength}%` }} /></div>
          <p className="small">Profile strength: <b>{strength}%</b></p>
          {!data.hasResume && <Link to="/resume" className="small">Upload your resume →</Link>}
        </>)}
        {isRec && data && <p className="small">Jobs posted: <b>{data.length}</b></p>}
        {user.role === 'ADMIN' && <p className="small">Platform administration</p>}
      </div>
    </div>
  )
}
