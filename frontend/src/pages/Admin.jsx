import { useEffect, useState } from 'react'
import { Bar, BarChart, CartesianGrid, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts'
import api, { payload, errorMessage } from '../api'
import { ErrorBanner, PackArt, StatusLine } from '../components/Ui'

const emptyProduct = { name: '', brand: '', category: '', barcode: '', imageUrl: '', purchaseUrl: '' }
const emptyIngredient = { name: '', harmful: false, harmCategory: '' }
const olive = '#3f4a2a'
const paprika = '#c45c26'
const rule = '#d9d0c3'

export default function Admin() {
  const [products, setProducts] = useState([])
  const [ingredients, setIngredients] = useState([])
  const [analytics, setAnalytics] = useState(null)
  const [form, setForm] = useState(emptyProduct)
  const [editingId, setEditingId] = useState(null)
  const [ingForm, setIngForm] = useState(emptyIngredient)
  const [editingIngId, setEditingIngId] = useState(null)
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)
  const [recallReason, setRecallReason] = useState('')

  function load() {
    setLoading(true)
    setError('')
    Promise.all([
      api.get('/api/admin/products'),
      api.get('/api/admin/ingredients'),
      api.get('/api/admin/analytics'),
    ])
      .then(([p, i, a]) => {
        setProducts(payload(p) || [])
        setIngredients(payload(i) || [])
        setAnalytics(payload(a))
      })
      .catch((err) => setError(errorMessage(err)))
      .finally(() => setLoading(false))
  }

  useEffect(load, [])

  async function saveProduct(e) {
    e.preventDefault()
    setError('')
    try {
      if (editingId) {
        await api.put(`/api/admin/products/${editingId}`, form)
      } else {
        await api.post('/api/admin/products', form)
      }
      setForm(emptyProduct)
      setEditingId(null)
      load()
    } catch (err) {
      setError(errorMessage(err))
    }
  }

  async function removeProduct(id) {
    setError('')
    try {
      await api.delete(`/api/admin/products/${id}`)
      if (editingId === id) {
        setEditingId(null)
        setForm(emptyProduct)
      }
      load()
    } catch (err) {
      setError(errorMessage(err))
    }
  }

  async function removeIngredient(id) {
    setError('')
    try {
      await api.delete(`/api/admin/ingredients/${id}`)
      if (editingIngId === id) {
        setEditingIngId(null)
        setIngForm(emptyIngredient)
      }
      load()
    } catch (err) {
      setError(errorMessage(err))
    }
  }

  async function saveIngredient(e) {
    e.preventDefault()
    setError('')
    try {
      const body = {
        name: ingForm.name,
        harmful: !!ingForm.harmful,
        harmCategory: ingForm.harmCategory || null,
      }
      if (editingIngId) {
        await api.put(`/api/admin/ingredients/${editingIngId}`, body)
      } else {
        await api.post('/api/admin/ingredients', body)
      }
      setIngForm(emptyIngredient)
      setEditingIngId(null)
      load()
    } catch (err) {
      setError(errorMessage(err))
    }
  }

  return (
    <div>
      <h1>Admin</h1>
      <p className="lede">Catalogue, flags, and how often packs are scanned.</p>
      <ErrorBanner error={error} />
      <StatusLine loading={loading} />
      {analytics && (
        <section className="charts">
          <div className="metric">
            <span className="whisper">Accounts</span>
            <strong>{analytics.totalUsers}</strong>
            <span className="whisper">{analytics.totalScans} scans</span>
          </div>
          <div className="panel chart">
            <h2>Most scanned</h2>
            {(analytics.mostScannedProducts || []).length === 0 ? (
              <p className="empty">No scans yet.</p>
            ) : (
              <ResponsiveContainer width="100%" height={220}>
                <BarChart data={analytics.mostScannedProducts}>
                  <CartesianGrid stroke={rule} vertical={false} />
                  <XAxis dataKey="name" hide />
                  <YAxis stroke={olive} allowDecimals={false} />
                  <Tooltip />
                  <Bar dataKey="count" fill={olive} />
                </BarChart>
              </ResponsiveContainer>
            )}
          </div>
          <div className="panel chart">
            <h2>Flagged ingredients</h2>
            {(analytics.mostFlaggedHarmfulIngredients || []).length === 0 ? (
              <p className="empty">No flagged ingredients in products.</p>
            ) : (
              <ResponsiveContainer width="100%" height={220}>
                <BarChart data={analytics.mostFlaggedHarmfulIngredients}>
                  <CartesianGrid stroke={rule} vertical={false} />
                  <XAxis dataKey="name" hide />
                  <YAxis stroke={olive} allowDecimals={false} />
                  <Tooltip />
                  <Bar dataKey="count" fill={paprika} />
                </BarChart>
              </ResponsiveContainer>
            )}
          </div>
        </section>
      )}
      <form className="panel" onSubmit={saveProduct}>
        <h2>{editingId ? `Edit product #${editingId}` : 'Add product'}</h2>
        <input placeholder="Name" value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} required />
        <input placeholder="Brand" value={form.brand} onChange={(e) => setForm({ ...form, brand: e.target.value })} />
        <input placeholder="Category" value={form.category} onChange={(e) => setForm({ ...form, category: e.target.value })} />
        <input placeholder="Barcode" value={form.barcode} onChange={(e) => setForm({ ...form, barcode: e.target.value })} />
        <input placeholder="Image URL" value={form.imageUrl} onChange={(e) => setForm({ ...form, imageUrl: e.target.value })} />
        <input
          placeholder="Purchase URL (Amazon, Blinkit, BigBasket…)"
          value={form.purchaseUrl}
          onChange={(e) => setForm({ ...form, purchaseUrl: e.target.value })}
        />
        <div className="row-actions">
          <button type="submit">{editingId ? 'Update product' : 'Create'}</button>
          {editingId && (
            <button
              type="button"
              className="secondary"
              onClick={() => {
                setEditingId(null)
                setForm(emptyProduct)
                setRecallReason('')
              }}
            >
              Cancel
            </button>
          )}
        </div>
      </form>
      {editingId && (
        <section className="panel">
          <h2>Food recall</h2>
          <p className="whisper">Flags this pack for shoppers who saved, scanned, or carted it.</p>
          <label>
            Recall reason
            <textarea
              value={recallReason}
              onChange={(e) => setRecallReason(e.target.value)}
              rows={3}
              placeholder="Why this pack is recalled"
            />
          </label>
          <div className="row-actions">
            <button
              type="button"
              className="danger"
              onClick={async () => {
                setError('')
                try {
                  await api.post(`/api/admin/products/${editingId}/recall`, { reason: recallReason })
                  load()
                } catch (err) {
                  setError(errorMessage(err))
                }
              }}
            >
              Mark as recalled
            </button>
            <button
              type="button"
              className="secondary"
              onClick={async () => {
                setError('')
                try {
                  await api.post(`/api/admin/products/${editingId}/recall/clear`)
                  setRecallReason('')
                  load()
                } catch (err) {
                  setError(errorMessage(err))
                }
              }}
            >
              Clear recall
            </button>
          </div>
        </section>
      )}
      <h2>Products</h2>
      <div className="admin-table">
        <table className="facts">
          <thead>
            <tr>
              <th>Pack</th>
              <th>Name</th>
              <th>Brand</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            {products.map((p) => (
              <tr key={p.id}>
                <td>
                  <PackArt src={p.imageUrl} name={p.name} />
                </td>
                <td>
                  {p.name}
                  {p.recalled && <span className="recall-chip"> Recall</span>}
                </td>
                <td>{p.brand}</td>
                <td>
                  <div className="row-actions">
                    <button
                      type="button"
                      className="secondary"
                      onClick={() => {
                        setEditingId(p.id)
                        setForm({
                          name: p.name || '',
                          brand: p.brand || '',
                          category: p.category || '',
                          barcode: p.barcode || '',
                          imageUrl: p.imageUrl || '',
                          purchaseUrl: p.purchaseUrl || '',
                        })
                        setRecallReason(p.recallReason || '')
                      }}
                    >
                      Edit
                    </button>
                    <button type="button" className="danger" onClick={() => removeProduct(p.id)}>
                      Delete
                    </button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      <form className="panel" onSubmit={saveIngredient}>
        <h2>{editingIngId ? `Edit ingredient #${editingIngId}` : 'Add ingredient'}</h2>
        <input placeholder="Name" value={ingForm.name} onChange={(e) => setIngForm({ ...ingForm, name: e.target.value })} required />
        <label className="inline">
          <input
            type="checkbox"
            checked={ingForm.harmful}
            onChange={(e) => setIngForm({ ...ingForm, harmful: e.target.checked })}
          />
          Flagged as harmful
        </label>
        <input
          placeholder="Harm category"
          value={ingForm.harmCategory}
          onChange={(e) => setIngForm({ ...ingForm, harmCategory: e.target.value })}
        />
        <div className="row-actions">
          <button type="submit">{editingIngId ? 'Update ingredient' : 'Create ingredient'}</button>
          {editingIngId && (
            <button
              type="button"
              className="secondary"
              onClick={() => {
                setEditingIngId(null)
                setIngForm(emptyIngredient)
              }}
            >
              Cancel
            </button>
          )}
        </div>
      </form>
      <h2>Ingredients</h2>
      <ul className="ing-admin">
        {ingredients.map((i) => (
          <li key={i.id} className={i.harmful ? 'harm' : ''}>
            <span>
              {i.name}
              {i.harmful ? ` · ${i.harmCategory}` : ''}
            </span>
            <div className="row-actions">
            <button
              type="button"
              className="secondary"
              onClick={() => {
                setEditingIngId(i.id)
                setIngForm({
                  name: i.name || '',
                  harmful: !!i.harmful,
                  harmCategory: i.harmCategory || '',
                })
              }}
            >
              Edit
            </button>
            <button type="button" className="danger" onClick={() => removeIngredient(i.id)}>
              Delete
            </button>
            </div>
          </li>
        ))}
      </ul>
    </div>
  )
}
