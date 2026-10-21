import { useState } from 'react'
import { Link } from 'react-router-dom'
import api, { errorMessage, payload } from '../api'
import { ErrorBanner } from '../components/Ui'

export default function ForgotPassword() {
  const [step, setStep] = useState(1)
  const [email, setEmail] = useState('')
  const [otp, setOtp] = useState('')
  const [resetToken, setResetToken] = useState('')
  const [newPassword, setNewPassword] = useState('')
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  async function sendOtp(e) {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      const res = await api.post('/api/auth/forgot-password', { email })
      setMessage(res.data.message)
      setStep(2)
    } catch (err) {
      setError(errorMessage(err))
    } finally {
      setLoading(false)
    }
  }

  async function verify(e) {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      const data = payload(await api.post('/api/auth/verify-otp', { email, otp }))
      setResetToken(data.resetToken)
      setMessage('OTP verified. Choose a new password.')
      setStep(3)
    } catch (err) {
      setError(errorMessage(err))
    } finally {
      setLoading(false)
    }
  }

  async function reset(e) {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      const res = await api.post('/api/auth/reset-password', { resetToken, newPassword })
      setMessage(res.data.message)
      setStep(4)
    } catch (err) {
      setError(errorMessage(err))
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="auth-shell">
      <div className="panel auth">
        <h1>Reset password</h1>
        <p className="lede">We’ll email a 6-digit code. If mail isn’t set up, the code is also printed in the server console.</p>
        <ErrorBanner error={error} />
        {message && <p className="ok">{message}</p>}
        {step === 1 && (
          <form onSubmit={sendOtp}>
            <label>
              Email
              <input value={email} onChange={(e) => setEmail(e.target.value)} type="email" required />
            </label>
            <button type="submit" disabled={loading}>
              {loading ? 'Sending…' : 'Send code'}
            </button>
          </form>
        )}
        {step === 2 && (
          <form onSubmit={verify}>
            <label>
              6-digit code
              <input value={otp} onChange={(e) => setOtp(e.target.value)} required maxLength={6} />
            </label>
            <button type="submit" disabled={loading}>
              {loading ? 'Checking…' : 'Verify'}
            </button>
          </form>
        )}
        {step === 3 && (
          <form onSubmit={reset}>
            <label>
              New password
              <input value={newPassword} onChange={(e) => setNewPassword(e.target.value)} type="password" minLength={8} required />
            </label>
            <button type="submit" disabled={loading}>
              {loading ? 'Updating…' : 'Update password'}
            </button>
          </form>
        )}
        {step === 4 && <Link to="/login">Back to login</Link>}
        {step < 4 && (
          <p>
            <Link to="/login">Log in instead</Link>
          </p>
        )}
      </div>
    </div>
  )
}
