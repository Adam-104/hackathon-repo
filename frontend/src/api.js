const BASE = (import.meta.env.VITE_API_BASE_URL || '').replace(/\/$/, '')

export const tokenStore = {
  get: () => localStorage.getItem('sr_token'),
  set: (t) => localStorage.setItem('sr_token', t),
  clear: () => { localStorage.removeItem('sr_token'); localStorage.removeItem('sr_user') },
}

async function request(method, path, { body, form } = {}) {
  const headers = {}
  const token = tokenStore.get()
  if (token) headers.Authorization = `Bearer ${token}`
  let payload
  if (form) payload = form
  else if (body !== undefined) { headers['Content-Type'] = 'application/json'; payload = JSON.stringify(body) }

  let res
  try {
    res = await fetch(`${BASE}${path}`, { method, headers, body: payload })
  } catch {
    throw new Error('Cannot reach the server. Check your connection and try again.')
  }
  const text = await res.text()
  let data = null
  try { data = text ? JSON.parse(text) : null } catch { /* not JSON */ }
  if (!res.ok) {
    if (res.status === 401 && token) { tokenStore.clear(); window.location.assign('/login') }
    throw new Error(data?.message || `Request failed (${res.status}).`)
  }
  return data
}

export const api = {
  get: (p) => request('GET', p),
  post: (p, body) => request('POST', p, { body }),
  put: (p, body) => request('PUT', p, { body }),
  patch: (p, body) => request('PATCH', p, { body }),
  upload: (p, file) => { const f = new FormData(); f.append('file', file); return request('POST', p, { form: f }) },
}
