export function Icon({ d, label, size = 22 }) {
  return (
    <svg viewBox="0 0 24 24" width={size} height={size} aria-hidden="true" fill="none" stroke="currentColor" strokeWidth="1.8">
      {label ? <title>{label}</title> : null}
      <path d={d} strokeLinecap="round" strokeLinejoin="round" />
    </svg>
  )
}

export const iconPaths = {
  search: 'M10.5 18a7.5 7.5 0 1 1 0-15 7.5 7.5 0 0 1 0 15zm6-1.5 4 4',
  mic: 'M12 3a3 3 0 0 1 3 3v6a3 3 0 0 1-6 0V6a3 3 0 0 1 3-3zm-5 8a5 5 0 0 0 10 0M12 16v4M8 20h8',
}
