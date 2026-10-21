import { useEffect, useState } from 'react'
import api, { payload, errorMessage } from '../api'
import { useAuth } from '../AuthContext'
import { ErrorBanner } from '../components/Ui'

export default function Profile() {
  const { logout, updateUser } = useAuth()
  const [profile, setProfile] = useState(null)
  const [fullName, setFullName] = useState('')
  const [preferredLanguage, setPreferredLanguage] = useState('en')
  const [currentPassword, setCurrentPassword] = useState('')
  const [newPassword, setNewPassword] = useState('')
  const [allergens, setAllergens] = useState([])
  const [selected, setSelected] = useState([])
  const [diet, setDiet] = useState('NONE')
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)

  useEffect(() => {
    Promise.all([api.get('/api/users/me'), api.get('/api/allergens')])
      .then(([me, list]) => {
        const p = payload(me)
        setProfile(p)
        setFullName(p.fullName)
        setPreferredLanguage(p.preferredLanguage || 'en')
        setSelected(p.allergenIds || [])
        setDiet(p.dietPreference || 'NONE')
        setAllergens(payload(list) || [])
      })
      .catch((err) => setError(errorMessage(err)))
      .finally(() => setLoading(false))
  }, [])

  async function save(e) {
    e.preventDefault()
    setError('')
    setSaving(true)
    try {
      const p = payload(await api.put('/api/users/me', { fullName, preferredLanguage }))
      setProfile(p)
      updateUser({ fullName: p.fullName })
      setMessage('Profile saved')
    } catch (err) {
      setError(errorMessage(err))
    } finally {
      setSaving(false)
    }
  }

  async function saveAllergens(e) {
    e.preventDefault()
    setError('')
    setSaving(true)
    try {
      const rows = payload(await api.put('/api/users/me/allergens', { allergenIds: selected }))
      setSelected((rows || []).map((a) => a.id))
      setMessage('Allergens saved')
    } catch (err) {
      setError(errorMessage(err))
    } finally {
      setSaving(false)
    }
  }

  async function saveDiet(e) {
    e.preventDefault()
    setError('')
    setSaving(true)
    try {
      const p = payload(await api.put('/api/users/me/diet-preference', { dietPreference: diet }))
      setDiet(p.dietPreference)
      setMessage('Diet preference saved')
    } catch (err) {
      setError(errorMessage(err))
    } finally {
      setSaving(false)
    }
  }

  async function changePassword(e) {
    e.preventDefault()
    setError('')
    setSaving(true)
    try {
      const res = await api.put('/api/users/me/password', { currentPassword, newPassword })
      setMessage(res.data.message)
      setCurrentPassword('')
      setNewPassword('')
    } catch (err) {
      setError(errorMessage(err))
    } finally {
      setSaving(false)
    }
  }

  function toggleAllergen(id) {
    setSelected((prev) => (prev.includes(id) ? prev.filter((x) => x !== id) : [...prev, id]))
  }

  if (loading) return <p className="status">Loading profile…</p>
  if (!profile) return <ErrorBanner error={error} />

  return (
    <div>
      <h1>Profile</h1>
      <p className="whisper">{profile.email}</p>
      <ErrorBanner error={error} />
      {message && <p className="ok">{message}</p>}
      <div className="profile-grid">
        <form className="panel" onSubmit={save}>
          <h2>Account</h2>
          <label>
            Full name
            <input value={fullName} onChange={(e) => setFullName(e.target.value)} required />
          </label>
          <label>
            Language
            <input value={preferredLanguage} onChange={(e) => setPreferredLanguage(e.target.value)} />
          </label>
          <button type="submit" disabled={saving}>
            Save
          </button>
        </form>
        <form className="panel" onSubmit={changePassword}>
          <h2>Password</h2>
          <label>
            Current
            <input type="password" value={currentPassword} onChange={(e) => setCurrentPassword(e.target.value)} required />
          </label>
          <label>
            New
            <input type="password" value={newPassword} onChange={(e) => setNewPassword(e.target.value)} minLength={8} required />
          </label>
          <button type="submit" disabled={saving}>
            Change password
          </button>
        </form>
        <form className="panel" onSubmit={saveAllergens}>
          <h2>Allergy alerts</h2>
          <p className="whisper">We’ll warn you on a pack if these show up in the ingredient list.</p>
          <div className="chip-grid">
            {allergens.map((a) => (
              <label key={a.id} className="inline">
                <input
                  type="checkbox"
                  checked={selected.includes(a.id)}
                  onChange={() => toggleAllergen(a.id)}
                />
                {a.name}
              </label>
            ))}
          </div>
          <button type="submit" disabled={saving}>
            Save allergens
          </button>
        </form>
        <form className="panel" onSubmit={saveDiet}>
          <h2>Diet preference</h2>
          <label>
            Recommendation style
            <select value={diet} onChange={(e) => setDiet(e.target.value)}>
              <option value="NONE">None</option>
              <option value="VEGETARIAN">Vegetarian</option>
              <option value="VEGAN">Vegan</option>
              <option value="LOW_SUGAR">Low sugar</option>
              <option value="HIGH_PROTEIN">High protein</option>
            </select>
          </label>
          <button type="submit" disabled={saving}>
            Save diet
          </button>
        </form>
      </div>
      <p>
        <button type="button" className="danger" onClick={logout}>
          Log out
        </button>
      </p>
    </div>
  )
}
