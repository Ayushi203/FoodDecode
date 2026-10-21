import { useEffect, useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import api, { payload, errorMessage } from '../api'
import { ErrorBanner, PackArt, ScoreBadge } from '../components/Ui'

export default function Compare() {
  const [params, setParams] = useSearchParams()
  const idsParam = params.get('ids') || ''
  const [left, setLeft] = useState(idsParam.split(',')[0] || '')
  const [right, setRight] = useState(idsParam.split(',')[1] || '')
  const [options, setOptions] = useState([])
  const [rows, setRows] = useState([])
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    api
      .get('/api/products/search')
      .then((res) => setOptions(payload(res) || []))
      .catch((err) => setError(errorMessage(err)))
  }, [])

  useEffect(() => {
    const ids = [left, right].filter(Boolean).join(',')
    if (!left || !right) {
      setRows([])
      return
    }
    setLoading(true)
    api
      .get('/api/products/compare', { params: { ids } })
      .then((res) => setRows(payload(res) || []))
      .catch((err) => setError(errorMessage(err)))
      .finally(() => setLoading(false))
  }, [left, right])

  function pick(which, value) {
    const nextLeft = which === 'left' ? value : left
    const nextRight = which === 'right' ? value : right
    setLeft(nextLeft)
    setRight(nextRight)
    const ids = [nextLeft, nextRight].filter(Boolean).join(',')
    const next = new URLSearchParams(params)
    if (ids) next.set('ids', ids)
    else next.delete('ids')
    setParams(next)
  }

  return (
    <div>
      <h1>Compare packs</h1>
      <p className="lede">Pick two products. Scores, sugar, and sodium sit side by side.</p>
      <ErrorBanner error={error} />
      <div className="compare-picks">
        <label>
          Pack A
          <select value={left} onChange={(e) => pick('left', e.target.value)}>
            <option value="">Choose…</option>
            {options.map((p) => (
              <option key={p.id} value={p.id}>
                {p.name}
              </option>
            ))}
          </select>
        </label>
        <label>
          Pack B
          <select value={right} onChange={(e) => pick('right', e.target.value)}>
            <option value="">Choose…</option>
            {options.map((p) => (
              <option key={p.id} value={p.id}>
                {p.name}
              </option>
            ))}
          </select>
        </label>
      </div>
      {loading && <p className="status">Loading comparison…</p>}
      {rows.length === 2 && (
        <div className="compare-grid">
          {rows.map((p) => (
            <article key={p.id} className="panel">
              <PackArt src={p.imageUrl} name={p.name} alt={p.name} />
              <h2>{p.name}</h2>
              <p className="whisper">{p.brand}</p>
              <ScoreBadge score={p.healthScore} />
              <table className="facts">
                <tbody>
                  <tr>
                    <th>Sugar g</th>
                    <td>{p.nutrition?.sugarG ?? '—'}</td>
                  </tr>
                  <tr>
                    <th>Sodium mg</th>
                    <td>{p.nutrition?.sodiumMg ?? '—'}</td>
                  </tr>
                  <tr>
                    <th>Calories</th>
                    <td>{p.nutrition?.calories ?? '—'}</td>
                  </tr>
                  <tr>
                    <th>Harmful flags</th>
                    <td>{(p.ingredients || []).filter((i) => i.harmful).length}</td>
                  </tr>
                </tbody>
              </table>
            </article>
          ))}
        </div>
      )}
    </div>
  )
}
