import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom'
import { AuthProvider } from './AuthContext'
import AuthLayout from './components/AuthLayout'
import GuestOnly from './components/GuestOnly'
import Layout from './components/Layout'
import Protected from './components/Protected'
import Admin from './pages/Admin'
import Compare from './pages/Compare'
import Dashboard from './pages/Dashboard'
import Favorites from './pages/Favorites'
import ForgotPassword from './pages/ForgotPassword'
import History from './pages/History'
import Login from './pages/Login'
import ProductDetail from './pages/ProductDetail'
import Profile from './pages/Profile'
import Register from './pages/Register'
import Scanner from './pages/Scanner'
import Search from './pages/Search'
import Cart from './pages/Cart'
import Browse from './pages/Browse'
import Landing from './pages/Landing'

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<Landing />} />
          <Route element={<AuthLayout />}>
            <Route
              path="/login"
              element={
                <GuestOnly>
                  <Login />
                </GuestOnly>
              }
            />
            <Route
              path="/register"
              element={
                <GuestOnly>
                  <Register />
                </GuestOnly>
              }
            />
            <Route
              path="/forgot-password"
              element={
                <GuestOnly>
                  <ForgotPassword />
                </GuestOnly>
              }
            />
          </Route>
          <Route
            element={
              <Protected>
                <Layout />
              </Protected>
            }
          >
            <Route path="/dashboard" element={<Dashboard />} />
            <Route path="/browse" element={<Browse />} />
            <Route path="/search" element={<Search />} />
            <Route path="/scan" element={<Scanner />} />
            <Route path="/products/:id" element={<ProductDetail />} />
            <Route path="/compare" element={<Compare />} />
            <Route path="/favorites" element={<Favorites />} />
            <Route path="/cart" element={<Cart />} />
            <Route path="/history" element={<History />} />
            <Route path="/profile" element={<Profile />} />
            <Route
              path="/admin"
              element={
                <Protected admin>
                  <Admin />
                </Protected>
              }
            />
            <Route path="*" element={<Navigate to="/dashboard" replace />} />
          </Route>
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  )
}
