import { Link, Navigate } from 'react-router-dom'
import { useAuth } from '../AuthContext'

function BarcodeMark() {
  return (
    <svg viewBox="0 0 24 24" width="22" height="22" aria-hidden="true">
      <rect x="3" y="5" width="2" height="14" fill="currentColor" />
      <rect x="7" y="5" width="1.4" height="14" fill="currentColor" />
      <rect x="10" y="5" width="2.4" height="14" fill="currentColor" />
      <rect x="14" y="5" width="1.2" height="14" fill="currentColor" />
      <rect x="17" y="5" width="2" height="14" fill="currentColor" />
      <rect x="20.2" y="5" width="1" height="14" fill="currentColor" />
    </svg>
  )
}

function FoodMark() {
  return (
    <svg viewBox="0 0 48 48" width="36" height="36" aria-hidden="true">
      <rect x="10" y="8" width="28" height="34" rx="3" fill="none" stroke="currentColor" strokeWidth="2" />
      <path d="M16 18h16M16 24h12M16 30h8" stroke="currentColor" strokeWidth="2" fill="none" />
    </svg>
  )
}

function CheckMark() {
  return (
    <svg viewBox="0 0 16 16" width="16" height="16" aria-hidden="true">
      <circle cx="8" cy="8" r="8" fill="var(--olive)" />
      <path d="M4.5 8.2 7 10.6 11.5 5.6" fill="none" stroke="#fbf8f2" strokeWidth="1.6" strokeLinecap="round" strokeLinejoin="round" />
    </svg>
  )
}

export default function Landing() {
  const { user } = useAuth()
  const token = typeof window !== 'undefined' ? localStorage.getItem('fd_access_token') : null
  if (user && token) {
    return <Navigate to="/dashboard" replace />
  }

  return (
    <div className="landing">
      <div className="landing-split">
        <div className="landing-copy">
          <div className="landing-lockup">
            <span className="landing-mark" aria-hidden="true">
              <BarcodeMark />
            </span>
            <span className="landing-wordmark">FoodDecode</span>
          </div>
          <h1>Read the label before it reads you</h1>
          <p className="landing-lede">
            Every packaged food has a story its front label won&apos;t tell you. FoodDecode scans the fine print
            and turns it into something you can actually act on.
          </p>
          <div className="landing-actions">
            <Link className="btn-primary" to="/register">
              Create account
            </Link>
            <Link className="btn-secondary" to="/login">
              Log in
            </Link>
          </div>
        </div>
        <aside className="landing-card" aria-hidden="true">
          <div className="landing-pack">
            <div className="landing-thumb">
              <FoodMark />
            </div>
            <div>
              <strong>Maggi masala magic</strong>
              <p>Maggi · instant noodles</p>
              <span className="score ok">
                <strong>62</strong>
                <span>/100</span>
              </span>
            </div>
          </div>
          <ul className="landing-checks">
            <li>
              <CheckMark />
              Real barcode lookup
            </li>
            <li>
              <CheckMark />
              Plain-language ingredients
            </li>
            <li>
              <CheckMark />
              Healthier swap suggestions
            </li>
          </ul>
        </aside>
      </div>
    </div>
  )
}
