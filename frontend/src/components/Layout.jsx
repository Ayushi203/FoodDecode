import { useEffect, useState } from 'react'
import { Link, NavLink, Outlet, useLocation, useNavigate, useSearchParams } from 'react-router-dom'
import { useAuth } from '../AuthContext'
import api, { payload } from '../api'

function Icon({ d, label }) {
  return (
    <svg viewBox="0 0 24 24" width="22" height="22" aria-hidden="true" fill="none" stroke="currentColor" strokeWidth="1.8">
      <title>{label}</title>
      <path d={d} strokeLinecap="round" strokeLinejoin="round" />
    </svg>
  )
}

const icons = {
  home: 'M4 10.5 12 4l8 6.5V20a1 1 0 0 1-1 1h-5v-6H10v6H5a1 1 0 0 1-1-1z',
  search: 'M10.5 18a7.5 7.5 0 1 1 0-15 7.5 7.5 0 0 1 0 15zm6-1.5 4 4',
  scan: 'M5 7V5h2M17 5h2v2M19 17v2h-2M7 19H5v-2M8 12h8',
  saved: 'M6 4h12v16l-6-3.5L6 20V4z',
  history: 'M4 12a8 8 0 1 0 2.3-5.6M4 4v4h4M12 8v5l3 2',
  profile: 'M12 12a4 4 0 1 0-4-4 4 4 0 0 0 4 4zm-7 9a7 7 0 0 1 14 0',
  admin: 'M4 18V8l8-4 8 4v10l-8 4zM12 12v6',
  cart: 'M6 6h15l-1.5 9h-12zM6 6 5 3H2M9 20a1 1 0 1 0 0-2 1 1 0 0 0 0 2zm9 0a1 1 0 1 0 0-2 1 1 0 0 0 0 2z',
  browse: 'M4 4h7v7H4zM13 4h7v7h-7zM4 13h7v7H4zM13 13h7v7h-7z',
  bell: 'M12 4a5 5 0 0 1 5 5c0 5 2 6 2 6H5s2-1 2-6a5 5 0 0 1 5-5zm0 16a2.2 2.2 0 0 0 2-1.4H10A2.2 2.2 0 0 0 12 20z',
  bag: 'M6 8h12l-1 12H7L6 8zm3 0V6a3 3 0 0 1 6 0v2',
  mic: 'M12 3a3 3 0 0 1 3 3v6a3 3 0 0 1-6 0V6a3 3 0 0 1 3-3zm-5 8a5 5 0 0 0 10 0M12 16v4M8 20h8',
}

export default function Layout() {
  const { user, isAdmin, logout } = useAuth()
  const [open, setOpen] = useState(false)
  const [params, setParams] = useSearchParams()
  const [q, setQ] = useState(params.get('q') || '')
  const navigate = useNavigate()
  const location = useLocation()
  const score = params.get('score') || 'all'
  const initial = user?.fullName?.trim()?.charAt(0)?.toUpperCase() || 'U'

  const [listening, setListening] = useState(false)
  const [notes, setNotes] = useState([])
  const [cartCount, setCartCount] = useState(0)
  const [notesOpen, setNotesOpen] = useState(false)
  const Speech = typeof window !== 'undefined' && (window.SpeechRecognition || window.webkitSpeechRecognition)
  const onDashboard = location.pathname === '/dashboard'
  const unread = notes.filter((n) => n.unread).length

  useEffect(() => {
    setQ(params.get('q') || '')
  }, [params])

  useEffect(() => {
    api
      .get('/api/users/me/notifications')
      .then((res) => setNotes(payload(res) || []))
      .catch(() =>
        api
          .get('/api/users/me/recall-alerts')
          .then((res) =>
            setNotes(
              (payload(res) || []).map((r) => ({
                kind: 'RECALL',
                refId: r.productId,
                title: r.name,
                body: r.recallReason || 'This pack is currently recalled.',
                unread: true,
                href: `/products/${r.productId}`,
              })),
            ),
          )
          .catch(() => setNotes([])),
      )
    api
      .get('/api/cart')
      .then((res) => setCartCount((payload(res)?.items || []).length))
      .catch(() => setCartCount(0))
  }, [location.pathname])

  function onSearch(e) {
    e.preventDefault()
    const next = new URLSearchParams(params)
    if (q) next.set('q', q)
    else next.delete('q')
    const target = location.pathname.startsWith('/search') ? '/search' : '/browse'
    navigate(`${target}?${next.toString()}`)
  }

  function listen() {
    if (!Speech) return
    const rec = new Speech()
    rec.lang = 'en-IN'
    rec.onresult = (ev) => {
      const text = ev.results[0][0].transcript
      setQ(text)
      const next = new URLSearchParams(params)
      next.set('q', text)
      navigate(`/browse?${next.toString()}`)
    }
    rec.onend = () => setListening(false)
    rec.onerror = () => setListening(false)
    setListening(true)
    rec.start()
  }

  function onScore(value) {
    const next = new URLSearchParams(params)
    if (value === 'all') next.delete('score')
    else next.set('score', value)
    setParams(next)
  }

  return (
    <div className="app-shell">
      <aside className="rail" aria-label="Main">
        <Link to="/dashboard" className="rail-brand" title="FoodDecode">
          Fd
        </Link>
        <NavLink to="/dashboard" title="Dashboard">
          <Icon d={icons.home} label="Dashboard" />
          <span className="rail-label">Dashboard</span>
        </NavLink>
        <NavLink to="/browse" title="Browse">
          <Icon d={icons.browse} label="Browse" />
          <span className="rail-label">Browse</span>
        </NavLink>
        <NavLink to="/search" title="Search">
          <Icon d={icons.search} label="Search" />
          <span className="rail-label">Search</span>
        </NavLink>
        <NavLink to="/scan" title="Scanner" className="rail-scan">
          <Icon d={icons.scan} label="Scanner" />
          <span className="rail-label">Scan</span>
        </NavLink>
        <NavLink to="/cart" title="Cart">
          <Icon d={icons.cart} label="Cart" />
          <span className="rail-label">Cart</span>
        </NavLink>
        <NavLink to="/favorites" title="Favorites">
          <Icon d={icons.saved} label="Favorites" />
          <span className="rail-label">Favorites</span>
        </NavLink>
        <NavLink to="/history" title="History">
          <Icon d={icons.history} label="History" />
          <span className="rail-label">History</span>
        </NavLink>
        <NavLink to="/profile" title="Profile">
          <Icon d={icons.profile} label="Profile" />
          <span className="rail-label">Profile</span>
        </NavLink>
        {isAdmin && (
          <NavLink to="/admin" title="Admin">
            <Icon d={icons.admin} label="Admin" />
            <span className="rail-label">Admin</span>
          </NavLink>
        )}
      </aside>
      <div className="shell-main">
        <header className="chrome">
          {onDashboard ? (
            <h1 className="chrome-title">Dashboard</h1>
          ) : (
            <>
              <form className="chrome-search" onSubmit={onSearch}>
                <label className="sr-only" htmlFor="chrome-q">
                  Search products
                </label>
                <div className="search-field">
                  <span className="search-field-icon" aria-hidden="true">
                    <Icon d={icons.search} label="" />
                  </span>
                  <input
                    id="chrome-q"
                    value={q}
                    onChange={(e) => setQ(e.target.value)}
                    placeholder="Search a pack or brand"
                  />
                </div>
                {Speech && (
                  <button
                    type="button"
                    className={`icon-btn${listening ? ' live' : ''}`}
                    onClick={listen}
                    title="Voice search"
                  >
                    <Icon d={icons.mic} label="Voice search" />
                  </button>
                )}
                <button type="submit" className="icon-btn" title="Search">
                  <Icon d={icons.search} label="Search" />
                </button>
              </form>
              <label className="chrome-filter">
                Score
                <select value={score} onChange={(e) => onScore(e.target.value)}>
                  <option value="all">All scores</option>
                  <option value="high">80 and above</option>
                  <option value="mid">50–79</option>
                  <option value="low">Under 50</option>
                </select>
              </label>
            </>
          )}
          <div className="chrome-icons">
            <div className="menu">
              <button
                type="button"
                className="chrome-icon"
                title="Notifications"
                onClick={() => {
                  setNotesOpen((v) => !v)
                  setOpen(false)
                }}
              >
                <Icon d={icons.bell} label="Notifications" />
                {unread > 0 && <span className="chrome-badge">{unread}</span>}
              </button>
              {notesOpen && (
                <div className="menu-panel notes-panel">
                  {notes.filter((n) => n.unread).length === 0 && (
                    <p className="whisper">No new notifications</p>
                  )}
                  {notes
                    .filter((n) => n.unread)
                    .map((n) => (
                    <Link
                      key={`${n.kind}-${n.refId}`}
                      to={n.href}
                      className={n.unread ? 'note-unread' : ''}
                      onClick={() => {
                        setNotesOpen(false)
                        if (n.unread) {
                          api
                            .post('/api/users/me/notifications/read', { kind: n.kind, refId: n.refId })
                            .catch(() => {})
                          setNotes((prev) =>
                            prev.map((row) =>
                              row.kind === n.kind && row.refId === n.refId ? { ...row, unread: false } : row,
                            ),
                          )
                        }
                      }}
                    >
                      <strong>{n.title}</strong>
                      <span className="whisper">{n.body}</span>
                    </Link>
                  ))}
                </div>
              )}
            </div>
            <Link to="/cart" className="chrome-icon" title="Cart">
              <Icon d={icons.bag} label="Cart" />
              {cartCount > 0 && <span className="chrome-badge">{cartCount}</span>}
            </Link>
          </div>
          <div className="menu chrome-user">
            <button
              type="button"
              className={`avatar${unread ? ' recall-dot' : ''}`}
              onClick={() => setOpen((v) => !v)}
              aria-expanded={open}
            >
              <span>{initial}</span>
              <em>{user?.fullName?.split(' ')[0]}</em>
            </button>
            {open && (
              <div className="menu-panel">
                {unread > 0 && (
                  <Link to="/dashboard" onClick={() => setOpen(false)}>
                    {unread} recall alert{unread === 1 ? '' : 's'}
                  </Link>
                )}
                <Link to="/profile" onClick={() => setOpen(false)}>
                  Profile
                </Link>
                <Link to="/cart" onClick={() => setOpen(false)}>
                  Cart
                </Link>
                <Link to="/favorites" onClick={() => setOpen(false)}>
                  Favorites
                </Link>
                <Link to="/history" onClick={() => setOpen(false)}>
                  History
                </Link>
                <button type="button" onClick={logout}>
                  Log out
                </button>
              </div>
            )}
          </div>
        </header>
        <div className="workspace">
          <Outlet />
        </div>
      </div>
      <nav className="dock" aria-label="Phone">
        <NavLink to="/dashboard">Home</NavLink>
        <NavLink to="/browse">Browse</NavLink>
        <NavLink to="/scan" className="dock-scan">
          Scan
        </NavLink>
        <NavLink to="/cart">Cart</NavLink>
        <NavLink to="/favorites">Saved</NavLink>
      </nav>
    </div>
  )
}
