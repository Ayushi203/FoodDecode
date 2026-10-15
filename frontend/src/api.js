import axios from 'axios'

const api = axios.create({
  baseURL: import.meta.env.DEV ? '' : import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
})

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('fd_access_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

api.interceptors.response.use(
  (res) => res,
  (err) => {
    const url = err.config?.url || ''
    const onAuthPage = ['/login', '/register', '/forgot-password'].some((p) =>
      window.location.pathname.startsWith(p),
    )
    if (err.response?.status === 401 && !url.includes('/api/auth/') && !onAuthPage) {
      localStorage.removeItem('fd_access_token')
      localStorage.removeItem('fd_refresh_token')
      localStorage.removeItem('fd_user')
      window.location.href = '/login'
    }
    return Promise.reject(err)
  },
)

export function payload(res) {
  return res.data?.data
}

const AI_HINT = /gemini|api key|secrets\.properties|application-secrets|AI_|generateContent|BAD_GATEWAY|stack trace|java\.|Exception/i

export function errorMessage(err) {
  const raw = err.response?.data?.message || err.message || 'Request failed'
  if (AI_HINT.test(raw)) {
    return 'Something went wrong. Please try again in a moment.'
  }
  return raw
}

export function sanitizeAiText(text) {
  if (!text) return text
  if (AI_HINT.test(text)) {
    return 'Something went wrong generating this explanation — please try again in a moment.'
  }
  return text
}

export function friendlyError(err, kind) {
  const raw = err.response?.data?.message || err.message || 'Request failed'
  if (kind === 'ai' || AI_HINT.test(raw)) {
    return 'Something went wrong generating this explanation — please try again in a moment.'
  }
  if (kind === 'lookup' && /404|not found|no product/i.test(raw)) {
    return 'No pack matched that barcode. Check the digits and try again.'
  }
  if (kind === 'lookup') {
    return 'Could not look that pack up right now. Try again in a moment.'
  }
  return errorMessage(err)
}

export default api
