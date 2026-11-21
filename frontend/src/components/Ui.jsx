export const Alert = ({ children, kind = 'error' }) => (children ? <div className={`alert ${kind}`} role="alert">{children}</div> : null)
export const Loading = ({ text = 'Loading…' }) => <p className="muted center pad">{text}</p>
export const Empty = ({ children }) => <p className="muted center pad">{children}</p>
export const Skills = ({ text }) => (text ? <div className="chips">{text.split(',').map((s) => s.trim()).filter(Boolean).map((s, i) => <span key={i} className="chip">{s}</span>)}</div> : null)
export const StatusBadge = ({ status }) => <span className={`badge ${status}`}>{status}</span>
export const when = (iso) => (iso ? new Date(iso).toLocaleDateString(undefined, { day: 'numeric', month: 'short', year: 'numeric' }) : '')
