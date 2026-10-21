import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import api, { payload, errorMessage } from '../api'
import { ErrorBanner, StatusLine } from '../components/Ui'

export default function History() {
  const [pageData, setPageData] = useState(null)
  const [page, setPage] = useState(0)
  const [method, setMethod] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)
  const navigate = useNavigate()

  useEffect(() => {
    setLoading(true)
    setError('')
    api
      .get('/api/users/me/history', { params: { page, size: 8, method: method || undefined } })
      .then((res) => setPageData(payload(res)))
      .catch((err) => setError(errorMessage(err)))
      .finally(() => setLoading(false))
  }, [method, page])

  const rows = pageData?.content || []
  const totalPages = pageData?.totalPages || 0

  return (
    <div>
      <h1>Scan history</h1>
      <p className="lede">What you already looked up — barcode, search, or photo.</p>
      <ErrorBanner error={error} />
      <label>
        Method
        <select
          className="toolbar"
          value={method}
          onChange={(e) => {
            setPage(0)
            setMethod(e.target.value)
          }}
        >
          <option value="">All methods</option>
          <option value="BARCODE">Barcode</option>
          <option value="SEARCH">Search</option>
          <option value="IMAGE">Image</option>
        </select>
      </label>
      <StatusLine loading={loading} empty={!loading && rows.length === 0} emptyText="No history for this filter." />
      <ul className="history">
        {rows.map((row) => (
          <li key={row.id}>
            <button type="button" className="linkish" onClick={() => navigate(`/products/${row.product.id}`)}>
              {row.product.name}
            </button>
            <span className="whisper">
              {row.scanMethod} · {row.scannedAt ? new Date(row.scannedAt).toLocaleString() : ''}
            </span>
          </li>
        ))}
      </ul>
      {totalPages > 1 && (
        <p className="pager">
          <button type="button" disabled={page <= 0} onClick={() => setPage((p) => p - 1)}>
            Previous
          </button>
          <span>
            Page {page + 1} / {totalPages}
          </span>
          <button type="button" disabled={page + 1 >= totalPages} onClick={() => setPage((p) => p + 1)}>
            Next
          </button>
        </p>
      )}
    </div>
  )
}
