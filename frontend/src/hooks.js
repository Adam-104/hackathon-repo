import { useCallback, useEffect, useState } from 'react'

/** Loads data once (and again on reload()). Returns { data, error, loading, reload, setData }. */
export function useLoad(fn, deps = []) {
  const [data, setData] = useState(null)
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)
  const run = useCallback(() => {
    setLoading(true); setError('')
    return fn().then(setData).catch((e) => setError(e.message)).finally(() => setLoading(false))
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, deps)
  useEffect(() => { run() }, [run])
  return { data, error, loading, reload: run, setData }
}

/** Wraps an async action with busy/error state. */
export function useAction() {
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')
  const run = async (fn) => {
    setBusy(true); setError('')
    try { return await fn() } catch (e) { setError(e.message) } finally { setBusy(false) }
  }
  return { busy, error, run, setError }
}
