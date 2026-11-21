import { render, screen, waitFor } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { vi } from 'vitest'

const auth = { user: null, logout: vi.fn() }
vi.mock('../auth', () => ({ useAuth: () => auth, RequireRole: ({ children }) => children }))
vi.mock('../api', () => ({ api: { get: vi.fn(() => Promise.resolve([{ id: 1, title: 'Java Dev', company: 'Acme', location: 'Pune', createdAt: new Date().toISOString() }])) } }))

import Layout from '../components/Layout'
import Landing from '../pages/Landing'
import Home from '../pages/Home'

const wrap = (el) => render(<MemoryRouter>{el}</MemoryRouter>)

describe('navigation per role', () => {
  it('guest sees Join now and Sign in', () => {
    auth.user = null
    wrap(<Layout />)
    expect(screen.getByText('Join now')).toBeInTheDocument()
    expect(screen.getByText('Sign in')).toBeInTheDocument()
  })
  it('recruiter sees recruiter links only', async () => {
    auth.user = { id: 1, name: 'Rita HR', role: 'RECRUITER' }
    wrap(<Layout />)
    expect(screen.getByText('Post a job')).toBeInTheDocument()
    expect(screen.queryByText('Users')).toBeNull()
    expect(screen.queryByText('Applications')).toBeNull()
  })
  it('admin sees admin links', () => {
    auth.user = { id: 9, name: 'Administrator', role: 'ADMIN' }
    wrap(<Layout />)
    expect(screen.getByText('Dashboard')).toBeInTheDocument()
    expect(screen.getByText('Users')).toBeInTheDocument()
  })
  it('candidate sees candidate links', () => {
    auth.user = { id: 2, name: 'Cara', role: 'CANDIDATE' }
    wrap(<Layout />)
    expect(screen.getByText('Resume & AI')).toBeInTheDocument()
    expect(screen.queryByText('Post a job')).toBeNull()
  })
})

describe('welcome page', () => {
  it('shows hero, the three roles and the latest jobs', async () => {
    auth.user = null
    wrap(<Landing />)
    expect(screen.getByText(/Hiring that understands/)).toBeInTheDocument()
    expect(screen.getByText('HR / Recruiters')).toBeInTheDocument()
    await waitFor(() => expect(screen.getByText('Java Dev')).toBeInTheDocument())
  })
  it('guests get the landing page at home', () => {
    auth.user = null
    wrap(<Home />)
    expect(screen.getByText('Create your free account')).toBeInTheDocument()
  })
})
