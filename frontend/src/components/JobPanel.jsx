import { useState } from 'react'
import { Link } from 'react-router-dom'
import { api } from '../api'
import { useAuth } from '../auth'
import { useAction, useLoad } from '../hooks'
import AiResult from './AiResult'
import { Alert, Loading, Skills, when } from './Ui'

/** Full details of one job, with AI match and apply for candidates. Used on the jobs board and the job page. */
export default function JobPanel({ id }) {
  const { user } = useAuth()
  const { data: job, error, loading } = useLoad(() => api.get(`/api/jobs/${id}`), [id])
  const [match, setMatch] = useState(null)
  const [note, setNote] = useState('')
  const [applied, setApplied] = useState(false)
  const ai = useAction()
  const apply = useAction()

  if (loading) return <Loading />
  if (error) return <Alert>{error}</Alert>
  const isCandidate = user?.role === 'CANDIDATE'

  return (
    <div className="panel">
      <h1>{job.title}</h1>
      <p>{job.company}{job.location ? ` · ${job.location}` : ''}</p>
      <p className="muted small">{job.experienceLevel ? `${job.experienceLevel} · ` : ''}Posted {when(job.createdAt)} by {job.recruiterName}</p>
      {job.status === 'CLOSED' && <Alert kind="info">This job is no longer accepting applications.</Alert>}

      {!user && <p className="note"><Link to="/login">Sign in</Link> or <Link to="/register">join now</Link> to apply and see your AI match score.</p>}
      {isCandidate && (
        <div className="row">
          {job.status === 'OPEN' && !applied && <a className="btn" href="#apply">Apply</a>}
          <button className="btn ghost" disabled={ai.busy} onClick={() => ai.run(async () => setMatch(await api.post('/api/ai/match', { jobId: job.id })))}>
            {ai.busy ? 'Matching…' : '✨ AI match'}
          </button>
          <Link className="btn ghost" to={`/prep/${job.id}`}>Interview prep</Link>
        </div>
      )}
      {user?.role === 'RECRUITER' && user.id === job.recruiterId && (
        <div className="row"><Link className="btn" to={`/recruiter/jobs/${job.id}/applicants`}>View applicants</Link><Link className="btn ghost" to={`/recruiter/jobs/${job.id}/edit`}>Edit job</Link></div>
      )}
      <Alert>{ai.error}</Alert>
      {match && <AiResult analysis={match} />}

      <hr />
      <h3>About the job</h3>
      <Skills text={job.requiredSkills} />
      <p className="pre">{job.description}</p>

      {isCandidate && job.status === 'OPEN' && (
        <div id="apply" className="apply-box">
          <h3>Apply to {job.company}</h3>
          {applied ? <Alert kind="ok">Application sent. Track it under Applications.</Alert> : (
            <>
              <label>Cover note (optional)<textarea rows={3} maxLength={2000} value={note} onChange={(e) => setNote(e.target.value)} /></label>
              <Alert>{apply.error}</Alert>
              <button className="btn" disabled={apply.busy} onClick={() => apply.run(async () => { await api.post(`/api/jobs/${job.id}/apply`, { coverNote: note }); setApplied(true) })}>
                {apply.busy ? 'Sending…' : 'Submit application'}
              </button>
              {apply.error.includes('resume') && <p className="small"><Link to="/resume">Upload your resume</Link> first.</p>}
            </>
          )}
        </div>
      )}
    </div>
  )
}
