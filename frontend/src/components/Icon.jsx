const PATHS = {
  home: 'M3 11l9-8 9 8M5 10v10h5v-6h4v6h5V10',
  briefcase: 'M3 8h18v12H3zM9 8V5h6v3M3 13h18',
  doc: 'M6 3h9l4 4v14H6zM14 3v5h5M9 13h7M9 17h7',
  chat: 'M4 5h16v11H9l-5 4zM8 9h8M8 12h5',
  chart: 'M4 20V10M10 20V4M16 20v-8M22 20H2',
  plus: 'M12 5v14M5 12h14',
  users: 'M9 11a3.5 3.5 0 100-7 3.5 3.5 0 000 7zM2 20c0-3.5 3-6 7-6s7 2.5 7 6M17 5a3 3 0 010 6M19 14c2 .7 3 2.4 3 4.5',
  shield: 'M12 3l8 3v6c0 5-3.5 8-8 9-4.5-1-8-4-8-9V6z',
  search: 'M11 4a7 7 0 105 12l4 4M11 4',
}

export default function Icon({ name }) {
  return (
    <svg viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
      <path d={PATHS[name]} />
    </svg>
  )
}
