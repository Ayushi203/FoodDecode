import { useEffect, useState } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import api, { payload, errorMessage } from '../api'
import { Icon, iconPaths } from '../components/Icons'
import { ErrorBanner, ProductCard } from '../components/Ui'

function inScoreBand(score, band) {
  const n = score ?? 0
  if (band === 'high') return n >= 80
  if (band === 'mid') return n >= 50 && n < 80
  if (band === 'low') return n < 50
  return true
}

export default function Search() {
  const [params, setParams] = useSearchParams()
  const q = params.get('q') || ''
  const scoreBand = params.get('score') || 'all'
  const [query, setQuery] = useState(q)
  const [items, setItems] = useState([])
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)
  const navigate = useNavigate()
  const Speech = typeof window !== 'undefined' && (window.SpeechRecognition || window.webkitSpeechRecognition)
  const [listening, setListening] = useState(false)

  useEffect(() => {
    setQuery(q)
    if (!q) {
      setItems([])
      return
    }
    setLoading(true)
    setError('')
    api
      .get('/api/products/search', { params: { query: q } })
      .then((res) => setItems(payload(res) || []))
      .catch((err) => setError(errorMessage(err)))
      .finally(() => setLoading(false))
  }, [q])

  function listen() {
    if (!Speech) return
    const rec = new Speech()
    rec.lang = 'en-IN'
    rec.onresult = (ev) => {
      const text = ev.results[0][0].transcript
      setQuery(text)
      const next = new URLSearchParams(params)
      next.set('q', text)
      setParams(next)
    }
    rec.onend = () => setListening(false)
    rec.onerror = () => setListening(false)
    setListening(true)
    rec.start()
  }

  function onSubmit(e) {
    e.preventDefault()
    const next = new URLSearchParams(params)
    next.set('q', query)
    setParams(next)
  }

  const shown = items.filter((p) => inScoreBand(p.healthScore, scoreBand))

  return (
    <div>
      <h1>Search results</h1>
      <form className="search-row" onSubmit={onSubmit}>
        <label className="sr-only" htmlFor="search-q">
          Product or brand
        </label>
        <div className="search-field">
          <span className="search-field-icon" aria-hidden="true">
            <Icon d={iconPaths.search} label="" />
          </span>
          <input id="search-q" value={query} onChange={(e) => setQuery(e.target.value)} placeholder="Product or brand" />
        </div>
        {Speech && (
          <button type="button" className={`icon-btn${listening ? ' live' : ''}`} onClick={listen} title="Voice search">
            <Icon d={iconPaths.mic} label="Voice search" />
          </button>
        )}
        <button type="submit">Search</button>
      </form>
      <ErrorBanner error={error} />
      {loading && <p className="status">Checking our shelf, then Open Food Facts…</p>}
      <div className="catalog">
        {shown.map((p) => (
          <ProductCard
            key={p.id}
            product={p}
            onClick={() => navigate(`/products/${p.id}`, { state: { scanMethod: 'SEARCH' } })}
          />
        ))}
      </div>
      {!loading && q && shown.length === 0 && <p className="empty">No packs matched that search and filter.</p>}
      {!q && !loading && <p className="empty">Type a name or barcode in the bar above to start.</p>}
    </div>
  )
}
