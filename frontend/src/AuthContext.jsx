import { createContext, useContext, useMemo, useState } from 'react'
import api, { payload } from './api'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    const raw = localStorage.getItem('fd_user')
    return raw ? JSON.parse(raw) : null
  })

  function storeSession(data) {
    localStorage.setItem('fd_access_token', data.accessToken)
    localStorage.setItem('fd_refresh_token', data.refreshToken)
    const next = {
      email: data.email,
      fullName: data.fullName,
      role: data.role,
    }
    localStorage.setItem('fd_user', JSON.stringify(next))
    setUser(next)
  }

  const value = useMemo(
    () => ({
      user,
      isAdmin: user?.role === 'ADMIN',
      async login(email, password) {
        const data = payload(await api.post('/api/auth/login', { email, password }))
        storeSession(data)
        return data
      },
      async register(fullName, email, password) {
        const data = payload(await api.post('/api/auth/register', { fullName, email, password }))
        storeSession(data)
        return data
      },
      updateUser(partial) {
        setUser((prev) => {
          if (!prev) return prev
          const next = { ...prev, ...partial }
          localStorage.setItem('fd_user', JSON.stringify(next))
          return next
        })
      },
      logout() {
        const refreshToken = localStorage.getItem('fd_refresh_token')
        if (refreshToken) {
          api.post('/api/auth/logout', { refreshToken }).catch(() => {})
        }
        localStorage.removeItem('fd_access_token')
        localStorage.removeItem('fd_refresh_token')
        localStorage.removeItem('fd_user')
        setUser(null)
        if (!window.location.pathname.startsWith('/login')) {
          window.location.href = '/'
        }
      },
    }),
    [user],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  return useContext(AuthContext)
}
