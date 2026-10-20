import { Navigate } from 'react-router-dom'
import { useAuth } from '../AuthContext'

export default function Protected({ children, admin }) {
  const { user, isAdmin } = useAuth()
  if (!user) {
    return <Navigate to="/" replace />
  }
  if (admin && !isAdmin) {
    return <Navigate to="/dashboard" replace />
  }
  return children
}
