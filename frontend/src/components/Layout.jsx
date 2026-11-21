import { useState } from 'react'
import { NavLink, Outlet, useNavigate } from 'react-router-dom'
import { useAuth } from '../auth'
import Icon from './Icon'
import ProfileCard from './ProfileCard'

const NAV = {
  CANDIDATE: [['/', 'Jobs', 'briefcase'], ['/applications', 'Applications', 'doc'], ['/resume', 'Resume & AI', 'users'], ['/prep', 'Interview prep', 'chat']],
  RECRUITER: [['/recruiter/jobs', 'My jobs', 'briefcase'], ['/recruiter/jobs/new', 'Post a job', 'plus'], ['/recruiter/analytics', 'Analytics', 'chart'], ['/jobs', 'All jobs', 'home']],
  ADMIN: [['/admin', 'Dashboard', 'chart'], ['/admin/users', 'Users', 'users'], ['/admin/jobs', 'Jobs', 'briefcase']],
  GUEST: [['/', 'Home', 'home'], ['/jobs', 'Jobs', 'briefcase']],
}
const ROLE_LABEL = { CANDIDATE: 'Candidate', RECRUITER: 'HR / Recruiter', ADMIN: 'Administrator' }

export default function Layout() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()
  const [q, setQ] = useState('')
  const links = NAV[user?.role || 'GUEST']
  const initials = (user?.name || '?').split(' ').map((w) => w[0]).slice(0, 2).join('').toUpperCase()

  return (
    <div className="shell">
      <header className="topbar">
        <div className="topbar-in">
          <NavLink to="/" className="brand" aria-label="SmartRecruit home"><span className="logo">SR</span></NavLink>
          <form className="topsearch" onSubmit={(e) => { e.preventDefault(); navigate(`/jobs?q=${encodeURIComponent(q)}`) }}>
            <Icon name="search" />
            <input value={q} onChange={(e) => setQ(e.target.value)} placeholder="Search jobs, companies, skills" aria-label="Search jobs" />
          </form>
          <nav className="nav" aria-label="Main">
            {links.map(([to, label, icon]) => (
              <NavLink key={to} to={to} end className={({ isActive }) => (isActive ? 'active' : '')}><Icon name={icon} /><span>{label}</span></NavLink>
            ))}
          </nav>
          <div className="who">
            {user ? (
              <>
                <span className="avatar" title={`${user.name} · ${ROLE_LABEL[user.role]}`}>{initials}</span>
                <button className="btn ghost small-btn" onClick={() => { logout(); navigate('/') }}>Log out</button>
              </>
            ) : (
              <>
                <NavLink to="/register" className="btn ghost small-btn">Join now</NavLink>
                <NavLink to="/login" className="btn small-btn">Sign in</NavLink>
              </>
            )}
          </div>
        </div>
      </header>
      <div className={`frame ${user ? 'with-side' : ''}`}>
        {user && <aside className="side"><ProfileCard /></aside>}
        <main className="page"><Outlet /></main>
      </div>
      <footer className="footer">SmartRecruit AI · VCube Hack Quest 1.0</footer>
    </div>
  )
}
