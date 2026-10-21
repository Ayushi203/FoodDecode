import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import api, { payload, errorMessage } from '../api'
import { ErrorBanner, ProductCard, StatusLine } from '../components/Ui'

export default function Favorites() {
  const [items, setItems] = useState([])
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)
  const navigate = useNavigate()

  function load() {
    setLoading(true)
    api
      .get('/api/users/me/favorites')
      .then((res) => setItems(payload(res) || []))
      .catch((err) => setError(errorMessage(err)))
      .finally(() => setLoading(false))
  }

  useEffect(load, [])

  async function remove(id) {
    setError('')
    try {
      await api.delete(`/api/favorites/${id}`)
      load()
    } catch (err) {
      setError(errorMessage(err))
    }
  }

  return (
    <div>
      <h1>Saved packs</h1>
      <p className="lede">Packs you marked while shopping, ready to open again.</p>
      <ErrorBanner error={error} />
      <StatusLine loading={loading} empty={!loading && items.length === 0} emptyText="No saved packs yet. Open a product and tap Save pack." />
      <div className="grid">
        {items.map((p) => (
          <div key={p.id} className="fav">
            <ProductCard product={p} onClick={() => navigate(`/products/${p.id}`)} />
            <button type="button" className="danger" onClick={() => remove(p.id)}>
              Remove
            </button>
          </div>
        ))}
      </div>
    </div>
  )
}
