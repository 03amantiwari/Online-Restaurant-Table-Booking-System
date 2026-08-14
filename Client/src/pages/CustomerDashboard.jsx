// Customer dashboard — profile info + booking history with tabs
import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { userService } from '../services/userService'
import { bookingService } from '../services/bookingService'

// Status badge colour map matching backend BookingStatus enum
const statusConfig = {
  PENDING:   { label: 'Pending',   color: 'text-amber-700 bg-amber-50 border-amber-200'   },
  CONFIRMED: { label: 'Confirmed', color: 'text-green-700 bg-green-50 border-green-200'   },
  CANCELLED: { label: 'Cancelled', color: 'text-red-700 bg-red-50 border-red-200'         },
  COMPLETED: { label: 'Completed', color: 'text-blue-700 bg-blue-50 border-blue-200'      },
  REJECTED:  { label: 'Rejected',  color: 'text-gray-700 bg-gray-50 border-gray-200'      },
  NO_SHOW:   { label: 'No Show',   color: 'text-purple-700 bg-purple-50 border-purple-200'},
}

export default function CustomerDashboard() {
  const { user } = useAuth()

  // Profile data fetched from backend
  const [profile,  setProfile]  = useState(null)
  // All bookings for this customer
  const [bookings, setBookings] = useState([])
  const [loading,  setLoading]  = useState(true)
  const [error,    setError]    = useState(null)

  // Controls which tab is visible: 'upcoming' or 'history'
  const [activeTab, setActiveTab] = useState('upcoming')

  // Inline edit state for profile form
  const [editing,   setEditing]   = useState(false)
  const [editForm,  setEditForm]  = useState({ fullName: '', phoneNumber: '' })
  const [saving,    setSaving]    = useState(false)

  // Tracks which booking's cancel is in-flight
  const [cancellingId, setCancellingId] = useState(null)
  // Confirmation modal state — null = hidden, bookingId = showing for that booking
  const [confirmCancelId, setConfirmCancelId] = useState(null)

  // Fetch profile and bookings in parallel on mount
  useEffect(() => {
    if (!user?.id) return
    Promise.all([
      userService.getCustomer(user.id),
      bookingService.getMyBookings(user.id),
    ])
      .then(([profileResp, bookingsResp]) => {
        setProfile(profileResp.data)
        setBookings(bookingsResp.data)
        setEditForm({ fullName: profileResp.data.fullName, phoneNumber: profileResp.data.phoneNumber })
      })
      .catch(() => setError('Could not load dashboard. Is the backend running?'))
      .finally(() => setLoading(false))
  }, [user])

  // Split bookings into upcoming vs past/cancelled using booking date
  const upcoming = bookings.filter(b =>
    new Date(b.bookingDate) >= new Date() && !['CANCELLED','COMPLETED','REJECTED','NO_SHOW'].includes(b.status)
  )
  const history = bookings.filter(b =>
    new Date(b.bookingDate) < new Date() || ['CANCELLED','COMPLETED','REJECTED','NO_SHOW'].includes(b.status)
  )

  // Opens the confirmation modal for a specific booking id
  const handleCancelClick = (id) => setConfirmCancelId(id)

  // Sends cancel request after user confirms in the modal
  const handleConfirmCancel = async () => {
    const id = confirmCancelId
    setConfirmCancelId(null)
    setCancellingId(id)
    try {
      const { data } = await bookingService.cancelBooking(id, user.id)
      // Replace cancelled booking in local state with updated response
      setBookings(prev => prev.map(b => b.id === id ? data : b))
    } catch {
      alert('Could not cancel booking. Please try again.')
    } finally {
      setCancellingId(null)
    }
  }

  // Submits updated profile to backend
  const handleSaveProfile = async () => {
    setSaving(true)
    try {
      const { data } = await userService.updateCustomer(user.id, editForm)
      setProfile(data)
      setEditing(false)
    } catch {
      alert('Profile update failed.')
    } finally {
      setSaving(false)
    }
  }

  if (loading) return <div className="min-h-screen flex items-center justify-center text-gray-400">Loading dashboard...</div>
  if (error)   return <div className="min-h-screen flex items-center justify-center text-red-500">{error}</div>

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="max-w-5xl mx-auto px-4 py-8">

        {/*
          Confirmation Modal — shown when confirmCancelId is set.
          WHY a modal instead of window.confirm()?
          window.confirm() is a browser native dialog — it blocks the main thread
          and cannot be styled. A modal is a React component that we control
          completely: styling, copy, button labels, async behaviour.
        */}
        {confirmCancelId && (
          <div className="fixed inset-0 bg-black/40 z-50 flex items-center justify-center px-4">
            <div className="bg-white rounded-2xl shadow-xl p-6 max-w-sm w-full">
              <h3 className="font-display text-lg font-bold text-gray-900 mb-2">Cancel Booking?</h3>
              <p className="text-sm text-gray-500 mb-5">
                This action cannot be undone. The table will be released for other customers.
              </p>
              <div className="flex gap-3">
                <button
                  onClick={() => setConfirmCancelId(null)}
                  className="flex-1 border border-gray-200 text-gray-700 py-2.5 rounded-xl text-sm hover:bg-gray-50">
                  Keep Booking
                </button>
                <button
                  onClick={handleConfirmCancel}
                  className="flex-1 bg-brand-500 text-white py-2.5 rounded-xl text-sm hover:bg-brand-600">
                  Yes, Cancel
                </button>
              </div>
            </div>
          </div>
        )}

        {/* Page heading */}
        <div className="mb-8">
          <h1 className="font-display text-3xl font-bold text-gray-900">My Dashboard</h1>
          <p className="text-gray-500 text-sm mt-1">Manage your profile and reservations</p>
        </div>

        {/* ── Profile Card ── */}
        <div className="bg-white rounded-2xl border border-gray-100 shadow-sm p-6 mb-6">
          <div className="flex items-center justify-between mb-4">
            <h2 className="font-display text-lg font-bold text-gray-900">Profile</h2>
            {!editing && (
              <button onClick={() => setEditing(true)}
                className="text-sm text-brand-500 border border-brand-200 px-4 py-1.5 rounded-lg hover:bg-red-50 transition-colors">
                Edit Profile
              </button>
            )}
          </div>

          {editing ? (
            <div className="space-y-3">
              {/* Editable name field */}
              <div>
                <label className="text-xs text-gray-500 uppercase tracking-wide">Full Name</label>
                <input value={editForm.fullName}
                  onChange={e => setEditForm({ ...editForm, fullName: e.target.value })}
                  className="w-full mt-1 border border-gray-200 rounded-xl px-4 py-2.5 text-sm focus:outline-none focus:border-brand-500"
                />
              </div>
              {/* Editable phone field */}
              <div>
                <label className="text-xs text-gray-500 uppercase tracking-wide">Phone Number</label>
                <input value={editForm.phoneNumber}
                  onChange={e => setEditForm({ ...editForm, phoneNumber: e.target.value })}
                  className="w-full mt-1 border border-gray-200 rounded-xl px-4 py-2.5 text-sm focus:outline-none focus:border-brand-500"
                />
              </div>
              <div className="flex gap-3 pt-1">
                <button onClick={handleSaveProfile} disabled={saving}
                  className="bg-brand-500 text-white px-5 py-2 rounded-xl text-sm hover:bg-brand-600 disabled:opacity-60">
                  {saving ? 'Saving...' : 'Save Changes'}
                </button>
                <button onClick={() => setEditing(false)}
                  className="border border-gray-200 text-gray-600 px-5 py-2 rounded-xl text-sm hover:bg-gray-50">
                  Cancel
                </button>
              </div>
            </div>
          ) : (
            // Read-only profile display
            <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
              {[
                ['ID',    profile?.id],
                ['Name',  profile?.fullName],
                ['Email', profile?.email],
                ['Phone', profile?.phoneNumber],
              ].map(([label, value]) => (
                <div key={label}>
                  <p className="text-xs text-gray-400 uppercase tracking-wide">{label}</p>
                  <p className="text-sm font-medium text-gray-800 mt-0.5">{value || '—'}</p>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* ── Booking History Tabs ── */}
        <div className="bg-white rounded-2xl border border-gray-100 shadow-sm p-6">
          <h2 className="font-display text-lg font-bold text-gray-900 mb-4">My Reservations</h2>

          {/* Tab switcher */}
          <div className="flex gap-2 mb-5">
            {[
              { key: 'upcoming', label: `Upcoming (${upcoming.length})` },
              { key: 'history',  label: `History (${history.length})`   },
            ].map(tab => (
              <button key={tab.key} onClick={() => setActiveTab(tab.key)}
                className={`px-4 py-2 rounded-full text-sm font-medium transition-all
                  ${activeTab === tab.key ? 'bg-brand-500 text-white' : 'bg-gray-100 text-gray-600 hover:bg-gray-200'}`}>
                {tab.label}
              </button>
            ))}
          </div>

          {/* Booking list for selected tab */}
          {(activeTab === 'upcoming' ? upcoming : history).length === 0 ? (
            <div className="text-center py-12 text-gray-400">
              <p className="text-3xl mb-2">🍽️</p>
              <p className="text-sm">No {activeTab === 'upcoming' ? 'upcoming' : 'past'} bookings.</p>
              {activeTab === 'upcoming' && (
                <Link to="/" className="mt-3 inline-block text-brand-500 text-sm hover:underline">Browse Restaurants →</Link>
              )}
            </div>
          ) : (
            <div className="space-y-3">
              {(activeTab === 'upcoming' ? upcoming : history).map(b => {
                const cfg = statusConfig[b.status] ?? { label: b.status, color: 'text-gray-600 bg-gray-50 border-gray-200' }
                const canCancel = activeTab === 'upcoming'
                return (
                  <div key={b.id} className="border border-gray-100 rounded-xl p-4">
                    <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
                      <div>
                        {/* Restaurant name + status badge */}
                        <div className="flex items-center gap-2 mb-2">
                          <span className="font-semibold text-gray-900">{b.restaurantName}</span>
                          <span className={`text-xs px-2 py-0.5 rounded-full border ${cfg.color}`}>{cfg.label}</span>
                        </div>
                        {/* Enhanced booking detail row — all key info visible at a glance */}
                        <div className="grid grid-cols-2 sm:grid-cols-4 gap-2 text-xs text-gray-500 mb-1">
                          <span>📅 {b.bookingDate}</span>
                          <span>🕐 {b.timeSlotLabel}</span>
                          <span>👥 {b.partySize} guests</span>
                          <span>🪑 Table #{b.tableNumber}</span>
                        </div>
                        <p className="text-xs text-gray-400">Ref: {b.bookingReference}</p>
                      </div>
                      {canCancel && (
                        <button
                          onClick={() => handleCancelClick(b.id)}
                          disabled={cancellingId === b.id}
                          className="text-sm border border-red-200 text-red-500 px-4 py-1.5 rounded-lg hover:bg-red-50 transition-colors disabled:opacity-50">
                          {cancellingId === b.id ? 'Cancelling...' : 'Cancel'}
                        </button>
                      )}
                    </div>
                  </div>
                )
              })}
            </div>
          )}
        </div>

      </div>
    </div>
  )
}
