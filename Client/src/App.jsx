// Root component — router wraps AuthProvider so useNavigate works inside context
import { BrowserRouter, Routes, Route } from 'react-router-dom'
import { AuthProvider } from './context/AuthContext'
import Navbar from './components/Navbar'
import ProtectedRoute from './components/ProtectedRoute'
import PublicOnlyRoute from './components/PublicOnlyRoute'
import Home from './pages/Home'
import Login from './pages/Login'
import Register from './pages/Register'
import BookTable from './pages/BookTable'
import MyBookings from './pages/MyBookings'
import CustomerDashboard from './pages/CustomerDashboard'
import OwnerDashboard from './pages/OwnerDashboard'
import AdminDashboard from './pages/AdminDashboard'
// Static informational pages — no auth required
import AboutUs from './pages/AboutUs'
import ContactUs from './pages/ContactUs'
import HelpCenter from './pages/HelpCenter'
// Floating customer support chatbot — renders only for ROLE_CUSTOMER
import ChatBot from './components/ChatBot'

export default function App() {
  return (
    /*
     * WHY BrowserRouter wraps AuthProvider (not the other way around):
     * AuthContext now calls useNavigate() for the logout redirect and the
     * auth:unauthorized event handler.  useNavigate() requires a Router
     * ancestor in the tree — so BrowserRouter must be the outermost wrapper.
     */
    <BrowserRouter>
      <AuthProvider>
        <Routes>
          {/*
            Public auth pages — wrapped in PublicOnlyRoute.
            If the user is already logged in, PublicOnlyRoute redirects them
            to their role dashboard instead of showing the form.
          */}
          <Route path="/login"    element={<PublicOnlyRoute><Login /></PublicOnlyRoute>}    />
          <Route path="/register" element={<PublicOnlyRoute><Register /></PublicOnlyRoute>} />

          {/* App pages — all have the Navbar */}
          <Route path="/*" element={
            <>
              <Navbar />
              <Routes>
                <Route path="/"    element={<Home />} />

                {/* Booking — restaurantId from URL keeps FK chain intact */}
                <Route path="/restaurant/:restaurantId/book" element={<BookTable />} />

                {/* Customer-only */}
                <Route path="/my-bookings"       element={<ProtectedRoute><MyBookings /></ProtectedRoute>} />
                <Route path="/dashboard/customer" element={<ProtectedRoute role="customer"><CustomerDashboard /></ProtectedRoute>} />

                {/* Owner-only */}
                <Route path="/dashboard/owner"   element={<ProtectedRoute role="owner"><OwnerDashboard /></ProtectedRoute>} />

                {/* Admin-only */}
                <Route path="/dashboard/admin"   element={<ProtectedRoute role="admin"><AdminDashboard /></ProtectedRoute>} />

                {/* Static informational pages — public, no auth required */}
                <Route path="/about"   element={<AboutUs />}    />
                <Route path="/contact" element={<ContactUs />}  />
                <Route path="/help"    element={<HelpCenter />} />
              </Routes>
              {/*
                ChatBot renders here — outside <Routes> so it persists
                across all pages without remounting on route change.
                ChatBot.jsx internally checks isCustomer and returns null
                for non-customers — no route guard needed here.
              */}
              <ChatBot />
            </>
          } />
        </Routes>
      </AuthProvider>
    </BrowserRouter>
  )
}
