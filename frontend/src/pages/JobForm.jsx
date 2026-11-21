import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { api } from '../api'
import { useAction } from '../hooks'
import { Alert } from '../components/Ui'

const EMPTY = { title: '', company: '', location: '', description: '', requiredSkills: '', experienceLevel: '' }

export default function JobForm() {
  const { id } = useParams()
  const navigate = useNavigate()
  const [form, setForm] = useState(EMPTY)
  const { busy, error, run, setError } = useAction()
  const set = (k) => (e) => setForm({ ...form, [k]: e.target.value })

  useEffect(() => {
    if (id) api.get(`/api/jobs/${id}`).then((j) => setForm({ ...EMPTY, ...Object.fromEntries(Object.keys(EMPTY).map((k) => [k, j[k] || ''])) })).catch((e) => setError(e.message))
  }, [id, setError])

  const submit = (e) => {
    e.preventDefault()
    run(async () => {
      if (id) await api.put(`/api/jobs/${id}`, form); else await api.post('/api/jobs', form)
      navigate('/recruiter/jobs')
    })
  }

  return (
    <form className="card" onSubmit={submit}>
      <h1>{id ? 'Edit job' : 'Post a job'}</h1>
      <Alert>{error}</Alert>
      <div className="cols">
        <label>Job title<input value={form.title} onChange={set('title')} maxLength={160} required /></label>
        <label>Company<input value={form.company} onChange={set('company')} maxLength={160} required /></label>
      </div>
      <div className="cols">
        <label>Location<input value={form.location} onChange={set('location')} maxLength={160} placeholder="Hyderabad / Remote" /></label>
        <label>Experience level<select value={form.experienceLevel} onChange={set('experienceLevel')}>
          <option value="">Any</option>{['Fresher', 'Junior', 'Mid', 'Senior'].map((l) => <option key={l}>{l}</option>)}</select></label>
      </div>
      <label>Required skills (comma separated)<input value={form.requiredSkills} onChange={set('requiredSkills')} maxLength={1000} placeholder="Java, Spring Boot, MySQL, REST" /></label>
      <label>Description<textarea rows={8} value={form.description} onChange={set('description')} maxLength={8000} required /></label>
      <button className="btn" disabled={busy}>{busy ? 'Saving…' : id ? 'Save changes' : 'Publish job'}</button>
    </form>
  )
}
