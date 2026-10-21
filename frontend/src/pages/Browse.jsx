import { useEffect, useMemo, useState } from 'react'
import { Link, useNavigate, useSearchParams } from 'react-router-dom'
import api, { payload, errorMessage } from '../api'
import { ErrorBanner, ProductCard, StatusLine } from '../components/Ui'

function labelCat(value) {
  return (value || '').replaceAll('_', ' ')
}

function inScoreBand(score, band) {
  const n = score ?? 0
  if (band === 'high') return n >= 80
  if (band === 'mid') return n >= 50 && n < 80
  if (band === 'low') return n < 50
  return true
}

export default function Browse() {
  const navigate = useNavigate()
  const [params, setParams] = useSearchParams()
  const q = params.get('q') || ''
  const scoreBand = params.get('score') || 'all'
  const tab = params.get('tab') || 'all'
  const [products, setProducts] = useState([])
  const [categories, setCategories] = useState([])
  const [recent, setRecent] = useState([])
  const [favorites, setFavorites] = useState([])
  const [scanTotal, setScanTotal] = useState(0)
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)
  const [recommended, setRecommended] = useState([])
  const [diet, setDiet] = useState('NONE')
  const [recalls, setRecalls] = useState([])

  useEffect(() => {
    setLoading(true)
    setError('')
    Promise.all([
      api.get('/api/products/search', { params: q ? { query: q } : {} }),
      api.get('/api/products/categories'),
      api.get('/api/users/me/history', { params: { page: 0, size: 8 } }),
      api.get('/api/users/me/favorites'),
      api.get('/api/users/me'),
      api.get('/api/products/recommended'),
      api.get('/api/users/me/recall-alerts'),
    ])
      .then(([p, c, h, f, me, rec, alerts]) => {
        setProducts(payload(p) || [])
        setCategories((payload(c) || []).filter(Boolean))
        const page = payload(h)
        setScanTotal(page?.totalElements || 0)
        const seen = new Set()
        const recents = []
        for (const row of page?.content || []) {
          if (row.product && !seen.has(row.product.id)) {
            seen.add(row.product.id)
            recents.push(row.product)
          }
        }
        setRecent(recents)
        setFavorites(payload(f) || [])
        setDiet(payload(me)?.dietPreference || 'NONE')
        setRecommended(payload(rec) || [])
        setRecalls(payload(alerts) || [])
      })
      .catch((err) => setError(errorMessage(err)))
      .finally(() => setLoading(false))
  }, [q])

  const catalog = useMemo(() => {
    let list = products
    if (tab === 'recent') list = recent
    else if (tab === 'favorites') list = favorites
    else if (tab !== 'all') list = products.filter((p) => (p.category || '').toLowerCase() === tab)
    return list.filter((p) => inScoreBand(p.healthScore, scoreBand))
  }, [products, recent, favorites, tab, scoreBand])

  const avgScore = useMemo(() => {
    if (!recent.length) return null
    const sum = recent.reduce((acc, p) => acc + (p.healthScore || 0), 0)
    return Math.round(sum / recent.length)
  }, [recent])

  function setTab(next) {
    const copy = new URLSearchParams(params)
    if (next === 'all') copy.delete('tab')
    else copy.set('tab', next)
    setParams(copy)
  }

  return (
    <div className="dash-layout">
      <div>
        <section className="dash-banner">
          <div>
            <h1>Browse packs</h1>
            <p className="lede">Search a pack, scan a barcode, or open something you already checked.</p>
          </div>
          <button type="button" className="btn-scan" onClick={() => navigate('/scan')}>
            Quick scan
          </button>
        </section>
        <div className="cats" role="tablist">
          {[
            ['all', 'All'],
            ['recent', 'Recently scanned'],
            ['favorites', 'Favorites'],
            ...categories.map((c) => [c, labelCat(c)]),
          ].map(([value, label]) => (
            <button
              key={value}
              type="button"
              role="tab"
              className={tab === value ? 'on' : ''}
              onClick={() => setTab(value)}
            >
              {label}
            </button>
          ))}
        </div>
        <ErrorBanner error={error} />
        {recalls.length > 0 && (
          <div className="alert-banner recall" role="alert">
            <strong>Recall alerts</strong>
            {recalls.map((r) => r.name).join(', ')} — check favorites, cart, or recent scans.
          </div>
        )}
        {diet && diet !== 'NONE' ? (
          <section className="panel" style={{ marginBottom: '1rem' }}>
            <h2>Recommended for you</h2>
            <p className="whisper">Matched to {diet.replaceAll('_', ' ').toLowerCase()} and higher health scores.</p>
            {recommended.length === 0 && (
              <p className="empty">No packs match this diet yet. Try another preference in Profile.</p>
            )}
            <div className="catalog">
              {recommended.map((p) => (
                <ProductCard key={`rec-${p.id}`} product={p} onClick={() => navigate(`/products/${p.id}`)} />
              ))}
            </div>
          </section>
        ) : (
          <p className="lede">
            Set a diet preference in <Link to="/profile">Profile</Link> to see a “Recommended for you” shelf here.
          </p>
        )}
        <StatusLine
          loading={loading}
          empty={!loading && catalog.length === 0}
          emptyText={q ? 'No packs matched that search and filter.' : 'No packs in this view yet.'}
        />
        <div className="catalog">
          {catalog.map((p) => (
            <ProductCard key={p.id} product={p} onClick={() => navigate(`/products/${p.id}`)} />
          ))}
        </div>
      </div>
      <aside className="side-panel">
        <section className="metric">
          <h2>Quick stats</h2>
          <p>
            <strong>{scanTotal}</strong>
            <span className="whisper"> scans logged</span>
          </p>
          <p>
            <strong>{favorites.length}</strong>
            <span className="whisper"> favorites</span>
          </p>
          <p>
            <strong>{avgScore ?? '—'}</strong>
            <span className="whisper"> avg score of recent packs</span>
          </p>
        </section>
        <section>
          <h2>Recently scanned</h2>
          {recent.length === 0 && <p className="empty">Nothing scanned yet.</p>}
          <ul className="side-list">
            {recent.slice(0, 6).map((p) => (
              <li key={p.id}>
                <Link to={`/products/${p.id}`}>{p.name}</Link>
                <span className="whisper">{p.healthScore}/100</span>
              </li>
            ))}
          </ul>
        </section>
      </aside>
    </div>
  )
}
