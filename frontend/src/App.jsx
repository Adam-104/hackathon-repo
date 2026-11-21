import { Route, Routes } from 'react-router-dom'
import Layout from './components/Layout'
import { RequireRole } from './auth'
import Jobs from './pages/Jobs'
import Home from './pages/Home'
import JobDetail from './pages/JobDetail'
import Login from './pages/Login'
import Register from './pages/Register'
import Resume from './pages/Resume'
import MyApplications from './pages/MyApplications'
import InterviewPrep from './pages/InterviewPrep'
import RecruiterJobs from './pages/RecruiterJobs'
import JobForm from './pages/JobForm'
import Applicants from './pages/Applicants'
import Analytics from './pages/Analytics'
import AdminDashboard from './pages/AdminDashboard'
import AdminUsers from './pages/AdminUsers'
import AdminJobs from './pages/AdminJobs'
import NotFound from './pages/NotFound'

export default function App() {
  const cand = (el) => <RequireRole role="CANDIDATE">{el}</RequireRole>
  const rec = (el) => <RequireRole role="RECRUITER">{el}</RequireRole>
  const adm = (el) => <RequireRole role="ADMIN">{el}</RequireRole>
  return (
    <Routes>
      <Route element={<Layout />}>
        <Route path="/" element={<Home />} />
        <Route path="/jobs" element={<Jobs />} />
        <Route path="/jobs/:id" element={<JobDetail />} />
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
        <Route path="/resume" element={cand(<Resume />)} />
        <Route path="/applications" element={cand(<MyApplications />)} />
        <Route path="/prep" element={cand(<InterviewPrep />)} />
        <Route path="/prep/:jobId" element={cand(<InterviewPrep />)} />
        <Route path="/recruiter/jobs" element={rec(<RecruiterJobs />)} />
        <Route path="/recruiter/jobs/new" element={rec(<JobForm />)} />
        <Route path="/recruiter/jobs/:id/edit" element={rec(<JobForm />)} />
        <Route path="/recruiter/jobs/:id/applicants" element={rec(<Applicants />)} />
        <Route path="/recruiter/analytics" element={rec(<Analytics />)} />
        <Route path="/admin" element={adm(<AdminDashboard />)} />
        <Route path="/admin/users" element={adm(<AdminUsers />)} />
        <Route path="/admin/jobs" element={adm(<AdminJobs />)} />
        <Route path="*" element={<NotFound />} />
      </Route>
    </Routes>
  )
}
