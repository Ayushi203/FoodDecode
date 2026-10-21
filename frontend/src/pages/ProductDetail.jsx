import { useEffect, useState } from 'react'
import { Link, useLocation, useParams } from 'react-router-dom'
import api, { payload, errorMessage, friendlyError, sanitizeAiText } from '../api'
import { useAuth } from '../AuthContext'
import { ErrorBanner, PackArt, ScoreBadge, BuyNowButton } from '../components/Ui'

const posted = new Set()

export default function ProductDetail() {
  const { id } = useParams()
  const location = useLocation()
  const { user } = useAuth()
  const [product, setProduct] = useState(null)
  const [alts, setAlts] = useState([])
  const [altNote, setAltNote] = useState('')
  const [altsLoading, setAltsLoading] = useState(true)
  const [explain, setExplain] = useState({})
  const [busy, setBusy] = useState({})
  const [summary, setSummary] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)
  const [tab, setTab] = useState('overview')
  const [reviews, setReviews] = useState([])
  const [rating, setRating] = useState(5)
  const [comment, setComment] = useState('')
  const [meId, setMeId] = useState(null)
  const [cartMsg, setCartMsg] = useState('')
  const [favMsg, setFavMsg] = useState('')

  useEffect(() => {
    setError('')
    setLoading(true)
    setAltsLoading(true)
    setTab('overview')
    setSummary('')
    setExplain({})
    setFavMsg('')
    setCartMsg('')
    api
      .get(`/api/products/${id}`)
      .then((res) => setProduct(payload(res)))
      .catch((err) => setError(errorMessage(err)))
      .finally(() => setLoading(false))
    api
      .get(`/api/products/${id}/reviews`)
      .then((res) => setReviews(payload(res) || []))
      .catch(() => setReviews([]))
    api.get('/api/users/me').then((res) => setMeId(payload(res)?.id)).catch(() => {})
    api
      .get(`/api/products/${id}/alternatives`)
      .then((res) => {
        setAlts(payload(res) || [])
        setAltNote(res.data?.message || '')
      })
      .catch(() => {
        setAlts([])
        setAltNote('')
      })
      .finally(() => setAltsLoading(false))
  }, [id])

  useEffect(() => {
    if (!user || !id) return
    const method = location.state?.scanMethod || 'SEARCH'
    const key = `${user.email}-${id}-${method}`
    if (posted.has(key)) return
    posted.add(key)
    api.post('/api/scan-history', { productId: Number(id), scanMethod: method }).catch(() => {
      posted.delete(key)
    })
  }, [id, user, location.state])

  async function simplify(ingredientId) {
    setBusy((b) => ({ ...b, [ingredientId]: true }))
    try {
      const data = payload(await api.get(`/api/ingredients/${ingredientId}/explanation`, { params: { lang: 'en' } }))
      setExplain((prev) => ({ ...prev, [ingredientId]: sanitizeAiText(data.simpleExplanation) }))
    } catch (err) {
      setExplain((prev) => ({ ...prev, [ingredientId]: friendlyError(err, 'ai') }))
    } finally {
      setBusy((b) => ({ ...b, [ingredientId]: false }))
    }
  }

  async function loadSummary() {
    setBusy((b) => ({ ...b, summary: true }))
    try {
      const data = payload(await api.post('/api/ai/summarize', { productId: Number(id) }))
      setSummary(sanitizeAiText(data.text))
    } catch (err) {
      setSummary(friendlyError(err, 'ai'))
    } finally {
      setBusy((b) => ({ ...b, summary: false }))
    }
  }

  async function addCart() {
    setCartMsg('')
    try {
      await api.post('/api/cart/items', { productId: Number(id), quantity: 1 })
      setCartMsg('Added to cart')
    } catch (err) {
      setError(errorMessage(err))
    }
  }

  async function saveReview(e) {
    e.preventDefault()
    setError('')
    try {
      payload(await api.post(`/api/products/${id}/reviews`, { rating, comment }))
      const list = payload(await api.get(`/api/products/${id}/reviews`))
      setReviews(list || [])
      const fresh = payload(await api.get(`/api/products/${id}`))
      setProduct(fresh)
      setComment('')
    } catch (err) {
      setError(errorMessage(err))
    }
  }

  async function deleteReview(reviewId) {
    try {
      await api.delete(`/api/reviews/${reviewId}`)
      setReviews((prev) => prev.filter((r) => r.id !== reviewId))
    } catch (err) {
      setError(errorMessage(err))
    }
  }
  async function favorite() {
    setFavMsg('')
    try {
      await api.post('/api/favorites', { productId: Number(id) })
      setFavMsg('Saved to your shelf')
    } catch (err) {
      setError(errorMessage(err))
    }
  }

  if (loading) return <p className="status">Loading pack…</p>
  if (!product) return <ErrorBanner error={error} />

  const n = product.nutrition
  const tabs = [
    ['overview', 'Pack'],
    ['ingredients', 'Ingredients'],
    ['nutrition', 'Nutrition'],
    ['alternatives', 'Swap'],
  ]

  return (
    <article className="detail">
      <ErrorBanner error={error} />
      {favMsg && <p className="ok">{favMsg}</p>}
      {cartMsg && <p className="ok">{cartMsg}</p>}
      {product.recalled && (
        <div className="alert-banner recall" role="alert">
          <strong>Food recall</strong>
          {product.recallReason || 'This pack has been flagged by an administrator. Do not use it until the recall is cleared.'}
        </div>
      )}
      {(product.allergyWarning || []).length > 0 && (
        <div className="alert-banner" role="alert">
          <strong>Allergy warning</strong>
          This pack contains allergens you marked: {(product.allergyWarning || []).join(', ')}.
        </div>
      )}
      <header className="detail-head">
        <PackArt src={product.imageUrl} name={product.name} alt={product.name} />
        <div>
          <p className="whisper">{product.brand} · {product.category}</p>
          <h1>{product.name}</h1>
          <ScoreBadge score={product.healthScore} />
          {product.averageRating != null && (
            <span className="whisper"> · Community {Number(product.averageRating).toFixed(1)}/5 ({product.reviewCount || 0})</span>
          )}
          <div className="score-meter" aria-hidden="true">
            <span style={{ width: `${Math.max(0, Math.min(100, product.healthScore || 0))}%` }} />
          </div>
          <p>
            <button type="button" onClick={favorite}>
              Save pack
            </button>{' '}
            <button type="button" onClick={addCart}>
              Add to cart
            </button>{' '}
            <BuyNowButton url={product.purchaseUrl} className="btn-primary" />{' '}
            {!product.purchaseUrl && (
              <span className="whisper">Purchase link not available yet</span>
            )}{' '}
            <Link className="linkish" to={`/compare?ids=${product.id}`}>
              Compare
            </Link>
          </p>
        </div>
      </header>
      <div className="tabs" role="tablist">
        {tabs.map(([key, label]) => (
          <button
            key={key}
            type="button"
            role="tab"
            aria-selected={tab === key}
            className={tab === key ? 'active' : ''}
            onClick={() => setTab(key)}
          >
            {label}
          </button>
        ))}
      </div>
      {tab === 'overview' && (
        <section className="panel">
          <p>Barcode {product.barcode || 'not on file'}</p>
          <button type="button" className="secondary" disabled={busy.summary} onClick={loadSummary}>
            {busy.summary ? 'Writing a short read…' : 'Plain-language summary'}
          </button>
          {summary && (
            <p>
              {summary}{' '}
              {/try again/i.test(summary) && (
                <button type="button" className="linkish" onClick={loadSummary}>
                  Retry
                </button>
              )}
            </p>
          )}
        </section>
      )}
      {tab === 'ingredients' && (
        <ul className="ing-list">
          {(product.ingredients || []).map((ing) => (
            <li key={ing.id} className={ing.harmful ? 'harm' : ''}>
              <div>
                <strong>{ing.name}</strong>
                {ing.harmful && <em> {ing.harmCategory}</em>}
              </div>
              <button type="button" className="linkish" disabled={busy[ing.id]} onClick={() => simplify(ing.id)}>
                {busy[ing.id] ? 'Simplifying…' : 'Say this simply'}
              </button>
              {explain[ing.id] && (
                <p>
                  {explain[ing.id]}{' '}
                  {/try again/i.test(explain[ing.id]) && (
                    <button type="button" className="linkish" onClick={() => simplify(ing.id)}>
                      Retry
                    </button>
                  )}
                </p>
              )}
            </li>
          ))}
          {(product.ingredients || []).length === 0 && <p className="empty">No ingredients listed on this pack.</p>}
        </ul>
      )}
      {tab === 'nutrition' &&
        (n ? (
          <table className="facts">
            <tbody>
              <tr><th>Calories</th><td>{n.calories}</td></tr>
              <tr><th>Protein g</th><td>{n.proteinG}</td></tr>
              <tr><th>Carbs g</th><td>{n.carbsG}</td></tr>
              <tr><th>Fat g</th><td>{n.fatsG}</td></tr>
              <tr><th>Sugar g</th><td>{n.sugarG}</td></tr>
              <tr><th>Sodium mg</th><td>{n.sodiumMg}</td></tr>
              <tr><th>Fibre g</th><td>{n.fibreG}</td></tr>
              <tr><th>Serving</th><td>{n.servingSize}</td></tr>
            </tbody>
          </table>
        ) : (
          <p className="empty">No nutrition panel for this pack.</p>
        ))}
      {tab === 'alternatives' && (
        <div>
          {altsLoading && <p className="status">Looking for other packs…</p>}
          {!altsLoading && alts.length > 0 && altNote && !altNote.startsWith('No healthier') && (
            <p className="lede">{altNote}</p>
          )}
          <ul className="alt-list">
            {alts.map((a) => (
              <li key={a.id}>
                <Link className="alt-link" to={`/products/${a.id}`}>
                  <PackArt src={a.imageUrl} name={a.name} />
                  <span>
                    <strong>{a.name}</strong>
                    <span className="whisper">
                      {a.brand} · {a.healthScore}/100
                    </span>
                  </span>
                </Link>
              </li>
            ))}
          </ul>
          {!altsLoading && alts.length === 0 && (
            <p className="empty">
              No comparable alternatives found yet. We only suggest a healthier pack of the same kind — not unrelated
              foods.
            </p>
          )}
        </div>
      )}
      <section className="panel" style={{ marginTop: '1.2rem' }}>
        <h2>Reviews</h2>
        <form onSubmit={saveReview}>
          <div className="stars">
            {[1, 2, 3, 4, 5].map((n) => (
              <button key={n} type="button" className={n <= rating ? 'on' : ''} onClick={() => setRating(n)}>
                ★
              </button>
            ))}
          </div>
          <label>
            Comment
            <textarea value={comment} onChange={(e) => setComment(e.target.value)} rows={3} />
          </label>
          <button type="submit">Save review</button>
        </form>
        <ul className="review-list">
          {reviews.map((r) => (
            <li key={r.id}>
              <strong>{r.userName}</strong>{' '}
              <span className="whisper">
                {r.rating}/5 · {r.createdAt ? new Date(r.createdAt).toLocaleDateString() : ''}
              </span>
              {r.comment && <p>{r.comment}</p>}
              {meId === r.userId && (
                <button type="button" className="linkish" onClick={() => deleteReview(r.id)}>
                  Delete
                </button>
              )}
            </li>
          ))}
        </ul>
        {reviews.length === 0 && <p className="empty">No reviews yet. Be the first.</p>}
      </section>
    </article>
  )
}
