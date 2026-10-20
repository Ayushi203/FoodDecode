import { useEffect, useRef, useState } from 'react'

export function scoreClass(score) {
  if (score >= 80) return 'good'
  if (score >= 50) return 'ok'
  return 'poor'
}

export function ScoreBadge({ score }) {
  return (
    <span className={`score ${scoreClass(score)}`} title="Health score out of 100">
      <strong>{score ?? '—'}</strong>
      <span>/100</span>
    </span>
  )
}

export function PackArt({ src, name, alt }) {
  const [failed, setFailed] = useState(false)
  const loadedRef = useRef(false)
  useEffect(() => {
    loadedRef.current = false
    setFailed(false)
    if (!src) return undefined
    const t = setTimeout(() => {
      if (!loadedRef.current) setFailed(true)
    }, 3500)
    return () => clearTimeout(t)
  }, [src])
  const showImage = src && !failed
  return (
    <div className="pack-art">
      {showImage ? (
        <img
          src={src}
          alt={alt || ''}
          referrerPolicy="no-referrer"
          onLoad={() => {
            loadedRef.current = true
          }}
          onError={() => setFailed(true)}
        />
      ) : (
        <div className="pack-fallback">
          <svg viewBox="0 0 48 48" width="36" height="36" aria-hidden="true">
            <rect x="10" y="8" width="28" height="34" rx="3" fill="none" stroke="currentColor" strokeWidth="2" />
            <path d="M16 18h16M16 24h12M16 30h8" stroke="currentColor" strokeWidth="2" fill="none" />
          </svg>
          <span>{name ? name.slice(0, 18) : 'Pack'}</span>
        </div>
      )}
    </div>
  )
}

export function ProductCard({ product, onClick }) {
  return (
    <div className="shelf-item">
      <button type="button" className="shelf-hit" onClick={onClick}>
        <PackArt src={product.imageUrl} name={product.name} />
        <div className="shelf-meta">
          <span className="shelf-brand">{product.brand || 'Unknown brand'}</span>
          <strong className="shelf-name">{product.name}</strong>
          <ScoreBadge score={product.healthScore} />
          {product.recalled && <span className="recall-chip">Recall</span>}
        </div>
      </button>
      <BuyNowButton url={product.purchaseUrl} className="cart-mini" />
    </div>
  )
}

export function BuyNowButton({ url, className }) {
  if (url) {
    return (
      <a
        className={className}
        href={url}
        target="_blank"
        rel="noopener noreferrer"
        onClick={(e) => e.stopPropagation()}
      >
        Buy now
      </a>
    )
  }
  return (
    <button type="button" className={`${className} is-muted`} disabled title="Purchase link not available yet">
      Buy now
    </button>
  )
}

export function ErrorBanner({ error }) {
  if (!error) return null
  return <p className="error" role="alert">{error}</p>
}

export function StatusLine({ loading, empty, emptyText }) {
  if (loading) return <p className="status">Loading…</p>
  if (empty) return <p className="empty">{emptyText}</p>
  return null
}

export function PageTitle({ kicker, title, children }) {
  return (
    <header className="page-title">
      {kicker && <p className="kicker">{kicker}</p>}
      <h1>{title}</h1>
      {children}
    </header>
  )
}
