import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import api, { payload, errorMessage } from '../api'
import { ErrorBanner, PackArt, ScoreBadge, StatusLine } from '../components/Ui'

export default function Cart() {
  const [cart, setCart] = useState({ items: [] })
  const [analysis, setAnalysis] = useState(null)
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)

  function load() {
    setLoading(true)
    Promise.all([api.get('/api/cart'), api.get('/api/cart/analysis')])
      .then(([c, a]) => {
        setCart(payload(c) || { items: [] })
        setAnalysis(payload(a))
      })
      .catch((err) => setError(errorMessage(err)))
      .finally(() => setLoading(false))
  }

  useEffect(load, [])

  async function setQty(id, quantity) {
    setError('')
    try {
      if (quantity < 1) {
        await api.delete(`/api/cart/items/${id}`)
      } else {
        await api.put(`/api/cart/items/${id}`, { quantity })
      }
      load()
    } catch (err) {
      setError(errorMessage(err))
    }
  }

  const items = cart.items || []

  return (
    <div className="cart-layout">
      <div>
        <h1>Cart</h1>
        <ErrorBanner error={error} />
        <StatusLine loading={loading} empty={!loading && items.length === 0} emptyText="Cart is empty. Add a pack from search or a product page." />
        <ul className="history">
          {items.map((row) => (
            <li key={row.id}>
              <Link to={`/products/${row.product.id}`} className="alt-link">
                <PackArt src={row.product.imageUrl} name={row.product.name} />
                <span>
                  <strong>{row.product.name}</strong>
                  <span className="whisper">{row.product.brand}</span>
                  <ScoreBadge score={row.product.healthScore} />
                </span>
              </Link>
              <div className="qty-row">
                <button type="button" className="secondary" onClick={() => setQty(row.id, row.quantity - 1)}>
                  −
                </button>
                <span>{row.quantity}</span>
                <button type="button" onClick={() => setQty(row.id, row.quantity + 1)}>
                  +
                </button>
                <button type="button" className="danger" onClick={() => setQty(row.id, 0)}>
                  Remove
                </button>
              </div>
            </li>
          ))}
        </ul>
      </div>
      {analysis && (
        <aside className="side-panel">
          <section className="metric">
            <h2>Cart health</h2>
            <p>
              <strong>{analysis.averageHealthScore ?? '—'}</strong>
              <span className="whisper"> average score</span>
            </p>
            <p>
              <strong>{analysis.harmfulFlagCount}</strong>
              <span className="whisper"> harmful flags</span>
            </p>
            <p className="lede">{analysis.verdict}</p>
          </section>
        </aside>
      )}
    </div>
  )
}
