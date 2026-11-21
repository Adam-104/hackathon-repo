import { useState } from 'react'

/** Password input with a LinkedIn-style Show / Hide toggle. Extra props go to the input. */
export default function PasswordField({ label, hint, ...inputProps }) {
  const [visible, setVisible] = useState(false)
  return (
    <label>
      {label}
      <span className="pw-wrap">
        <input {...inputProps} type={visible ? 'text' : 'password'} />
        <button type="button" className="pw-toggle" onClick={() => setVisible((v) => !v)}
          aria-label={visible ? 'Hide password' : 'Show password'} aria-pressed={visible}>
          {visible ? 'Hide' : 'Show'}
        </button>
      </span>
      {hint}
    </label>
  )
}
