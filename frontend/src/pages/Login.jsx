import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../AuthContext'
import { ErrorBanner } from '../components/Ui'
import { errorMessage } from '../api'

export default function Login() {
  const { login } = useAuth()
  const navigate = useNavigate()
  const [email, setEmail] = useState('user@fooddecode.dev')
  const [password, setPassword] = useState('Password@123')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  async function onSubmit(e) {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      await login(email, password)
      navigate('/dashboard')
    } catch (err) {
      setError(errorMessage(err))
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="auth-shell">
      <form className="panel auth" onSubmit={onSubmit}>
        <h1>Log in</h1>
        <p className="lede">Use your FoodDecode account to save packs and history.</p>
        <ErrorBanner error={error} />
        <label>
          Email
          <input value={email} onChange={(e) => setEmail(e.target.value)} type="email" required />
        </label>
        <label>
          Password
          <input value={password} onChange={(e) => setPassword(e.target.value)} type="password" required />
        </label>
        <button type="submit" disabled={loading}>
          {loading ? 'Signing in…' : 'Log in'}
        </button>
        <p>
          New here? <Link to="/register">Create an account</Link>
        </p>
        <p>
          <Link to="/forgot-password">Forgot password</Link>
        </p>
      </form>
    </div>
  )
}
