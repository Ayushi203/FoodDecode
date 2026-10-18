import { Link, Outlet } from 'react-router-dom'

export default function AuthLayout() {
  return (
    <div className="auth-app">
      <header className="auth-brandbar">
        <Link to="/" className="brand">
          FoodDecode
        </Link>
      </header>
      <Outlet />
    </div>
  )
}
