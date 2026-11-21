import { createContext, useContext, useState } from 'react'
import { Navigate } from 'react-router-dom'
import { api, tokenStore } from './api'

const AuthContext = createContext(null)
export const useAuth = () => useContext(AuthContext)

function loadUser() {
  try { return tokenStore.get() ? JSON.parse(localStorage.getItem('sr_user')) : null } catch { return null }
}

export function AuthProvider({ children }) {
  const [user, setUser] = useState(loadUser)

  const finish = (res) => {
    tokenStore.set(res.token)
    localStorage.setItem('sr_user', JSON.stringify(res.user))
    setUser(res.user)
    return res.user
  }
  const login = async (email, password) => finish(await api.post('/api/auth/login', { email, password }))
  const register = async (data) => finish(await api.post('/api/auth/register', data))
  const logout = () => { tokenStore.clear(); setUser(null) }

  return <AuthContext.Provider value={{ user, login, register, logout }}>{children}</AuthContext.Provider>
}

/** Sends visitors to the login page, and the wrong role back to the jobs board. */
export function RequireRole({ role, children }) {
  const { user } = useAuth()
  if (!user) return <Navigate to="/login" replace />
  if (role && user.role !== role) return <Navigate to="/" replace />
  return children
}
