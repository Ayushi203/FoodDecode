import { Navigate } from 'react-router-dom'
import { useAuth } from '../AuthContext'

export default function GuestOnly({ children }) {
  const { user } = useAuth()
  if (user) {
    return <Navigate to="/dashboard" replace />
  }
  return children
}
