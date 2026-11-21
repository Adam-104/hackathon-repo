import { Link } from 'react-router-dom'
import { api } from '../api'
import { useLoad } from '../hooks'
import { when } from '../components/Ui'

const FEATURES = [
  ['Resume analysis', 'Get a score, strengths and gaps from your real resume in seconds.'],
  ['Job match score', 'See how well you fit a role before you apply, with the reasons.'],
  ['Interview practice', 'Role-specific questions generated from the job and your profile.'],
  ['Resume improvement', 'Concrete rewrites tailored to the job you want.'],
]

export default function Landing() {
  const { data } = useLoad(() => api.get('/api/jobs'))
  const latest = (data || []).slice(0, 4)
  return (
    <div className="landing">
      <section className="lhero">
        <div>
          <h1>Hiring that understands <em>people</em>, not just keywords.</h1>
          <p>SmartRecruit connects candidates and HR teams. AI reads real resumes, scores the match and prepares the interview.</p>
          <div className="row">
            <Link className="btn big" to="/register">Join now</Link>
            <Link className="btn ghost big" to="/jobs">Browse jobs</Link>
          </div>
        </div>
        <div className="lcard card">
          <span className="co-logo">S</span>
          <h3>Java Backend Developer</h3>
          <p className="muted small">Hyderabad · Junior</p>
          <div className="mini-score"><b>86</b><span>AI match</span></div>
          <p className="small">Strong Spring Boot and MySQL experience. Add Docker to close the gap.</p>
        </div>
      </section>

      <section className="lsec">
        <h2>Built for three kinds of users</h2>
        <div className="cols">
          <div className="card pad"><h3>Candidates</h3><p className="muted">Upload a resume, search jobs, apply in one click and track every application.</p></div>
          <div className="card pad"><h3>HR / Recruiters</h3><p className="muted">Post jobs, see applicants ranked by AI match and move them through shortlist and hire.</p></div>
          <div className="card pad"><h3>Administrators</h3><p className="muted">Oversee users and jobs, disable accounts and keep the platform clean.</p></div>
        </div>
      </section>

      <section className="lsec">
        <h2>Four AI features, one workflow</h2>
        <div className="cols">{FEATURES.map(([t, d]) => <div key={t} className="card pad"><h3>{t}</h3><p className="muted">{d}</p></div>)}</div>
      </section>

      {latest.length > 0 && (
        <section className="lsec">
          <h2>Latest jobs</h2>
          <div className="cols">{latest.map((j) => (
            <Link key={j.id} to={`/jobs/${j.id}`} className="card pad joblink">
              <span className="co-logo">{j.company[0]}</span>
              <h3>{j.title}</h3><p className="small">{j.company}</p>
              <p className="muted small">{j.location} · {when(j.createdAt)}</p>
            </Link>))}</div>
        </section>
      )}
      <section className="lcta card"><h2>Ready to get hired, or to hire?</h2><Link className="btn big" to="/register">Create your free account</Link></section>
    </div>
  )
}
