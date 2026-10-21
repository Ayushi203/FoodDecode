import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import {
  Area,
  AreaChart,
  Bar,
  BarChart,
  CartesianGrid,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from 'recharts'
import api, { payload, errorMessage } from '../api'
import { ErrorBanner, StatusLine } from '../components/Ui'

const olive = '#3f4a2a'
const paprika = '#c45c26'
const rule = '#d9d0c3'

function Sparkline({ values, stroke }) {
  const nums = (values || []).filter((n) => n != null).map((n) => Number(n))
  if (nums.length < 2) {
    return <p className="whisper">No weekly history yet</p>
  }
  const w = 140
  const h = 36
  const min = Math.min(...nums)
  const max = Math.max(...nums)
  const span = max - min || 1
  const pts = nums
    .map((n, i) => {
      const x = (i / (nums.length - 1)) * w
      const y = h - ((n - min) / span) * (h - 4) - 2
      return `${x},${y}`
    })
    .join(' ')
  return (
    <svg className="spark" viewBox={`0 0 ${w} ${h}`} width="100%" height="36" aria-hidden="true">
      <polyline fill="none" stroke={stroke} strokeWidth="2" points={pts} />
    </svg>
  )
}

function MetricCard({ label, metric, format, invert, stroke }) {
  const value = metric?.value
  const pct = metric?.changePercent
  const up = pct != null && pct >= 0
  const good = invert ? !up : up
  return (
    <section className="stat-card">
      <p className="whisper">{label}</p>
      <strong>{value == null ? '—' : format(value)}</strong>
      {pct == null ? (
        <p className="whisper">{metric?.trendNote || 'Not enough last-month data to trend'}</p>
      ) : (
        <p className={`stat-trend ${good ? 'up' : 'down'}`}>
          {up ? '▲' : '▼'} {Math.abs(pct)}% {metric.trendNote}
        </p>
      )}
      <Sparkline values={metric?.sparkline} stroke={stroke} />
    </section>
  )
}

function fmtInt(n) {
  return String(Math.round(n))
}

function fmtScore(n) {
  return Number(n).toFixed(1)
}

export default function Dashboard() {
  const [data, setData] = useState(null)
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    setLoading(true)
    api
      .get('/api/users/me/dashboard')
      .then((res) => setData(payload(res)))
      .catch((err) => setError(errorMessage(err)))
      .finally(() => setLoading(false))
  }, [])

  const trend = (data?.healthScoreTrend || []).filter((row) => row.averageScore != null)
  const cats = data?.scansByCategory || []

  return (
    <div className="analytics">
      <ErrorBanner error={error} />
      <StatusLine loading={loading} />
      {data && (
        <>
          <div className="stat-grid">
            <MetricCard
              label="Scans this month"
              metric={data.scansThisMonth}
              format={fmtInt}
              stroke={olive}
            />
            <MetricCard
              label="Avg. health score"
              metric={data.avgHealthScore}
              format={fmtScore}
              stroke={olive}
            />
            <MetricCard
              label="Items flagged"
              metric={data.itemsFlagged}
              format={fmtInt}
              invert
              stroke={paprika}
            />
            <MetricCard
              label="Favorites saved"
              metric={data.favoritesSaved}
              format={fmtInt}
              stroke={olive}
            />
          </div>
          <div className="dash-charts">
            <section className="panel chart">
              <h2>Health score trend</h2>
              {trend.length === 0 ? (
                <p className="empty">Scan a few packs to plot your weekly average score.</p>
              ) : (
                <ResponsiveContainer width="100%" height={260}>
                  <AreaChart data={trend}>
                    <CartesianGrid stroke={rule} vertical={false} />
                    <XAxis dataKey="label" stroke={olive} />
                    <YAxis stroke={olive} domain={[0, 100]} />
                    <Tooltip />
                    <Area
                      type="monotone"
                      dataKey="averageScore"
                      stroke={olive}
                      fill="#e4ead4"
                      name="Avg score"
                    />
                  </AreaChart>
                </ResponsiveContainer>
              )}
            </section>
            <section className="panel chart">
              <h2>Scans by category</h2>
              {cats.length === 0 ? (
                <p className="empty">No scan history to group by category yet.</p>
              ) : (
                <ResponsiveContainer width="100%" height={260}>
                  <BarChart data={cats} layout="vertical" margin={{ left: 8 }}>
                    <CartesianGrid stroke={rule} horizontal={false} />
                    <XAxis type="number" stroke={olive} allowDecimals={false} />
                    <YAxis type="category" dataKey="category" stroke={olive} width={110} />
                    <Tooltip />
                    <Bar dataKey="count" fill={olive} name="Scans" />
                  </BarChart>
                </ResponsiveContainer>
              )}
            </section>
          </div>
          <section className="panel dash-cta">
            <div>
              <h2>Quick scan</h2>
              <p className="whisper">Point the camera at a barcode to log another pack and refresh these stats.</p>
            </div>
            <Link className="btn-primary" to="/scan">
              Open scanner
            </Link>
          </section>
        </>
      )}
    </div>
  )
}
