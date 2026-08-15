import { useState, useEffect } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { bookingService } from '../services/bookingService'

// Map backend BookingStatus enum values → display label + colour
const statusConfig = {
  PENDING:   { label: 'Pending',   color: 'text-amber-700 bg-amber-50 border-amber-200'  },
  CONFIRMED: { label: 'Confirmed', color: 'text-green-700 bg-green-50 border-green-200'  },
  CANCELLED: { label: 'Cancelled', color: 'text-red-700 bg-red-50 border-red-200'        },
  COMPLETED: { label: 'Completed', color: 'text-blue-700 bg-blue-50 border-blue-200'     },
  REJECTED:  { label: 'Rejected',  color: 'text-gray-700 bg-gray-50 border-gray-200'     },
  NO_SHOW:   { label: 'No Show',   color: 'text-purple-700 bg-purple-50 border-purple-200'},
}

export default function MyBookings() {
  const { user } = useAuth()
  const navigate = useNavigate()

  const [bookings, setBookings] = useState([])
  const [loading,  setLoading]  = useState(true)
  const [error,    setError]    = useState(null)
  const [filter,   setFilter]   = useState('all')

  // ── Fetch this user's bookings on mount ──────────────────────────────────
  useEffect(() => {
    if (!user?.id) return

    /*
     * GET /api/v1/api/bookings/my?userId={id}
     * Returns List<BookingResponseDto> for the given user.
     * Backend field: BookingResponseDto → { id, bookingReference, restaurantName,
     *   tableNumber, timeSlotLabel, bookingDate, partySize, status, ... }
     */
    bookingService.getMyBookings(user.id)
      .then(({ data }) => setBookings(data))
      .catch(() => setError('Could not load bookings. Please try again.'))
      .finally(() => setLoading(false))
  }, [user])

  // ── Cancel a booking ─────────────────────────────────────────────────────
  const handleCancel = async (bookingId) => {
    if (!window.confirm('Cancel this booking?')) return
    try {
      const { data } = await bookingService.cancelBooking(bookingId, user.id)
      // Replace the booking in local state with the updated one from the backend
      setBookings(prev => prev.map(b => b.id === bookingId ? data : b))
    } catch (err) {
      const msg = err.response?.data?.message || 'Could not cancel booking.'
      alert(typeof msg === 'string' ? msg : 'Could not cancel booking.')
    }
  }

  // ── Filter bookings by status tab ────────────────────────────────────────
  const filtered = filter === 'all'
    ? bookings
    : bookings.filter(b => b.status === filter.toUpperCase())

  const isUpcoming = (dateStr) => new Date(dateStr) >= new Date()

  // ── Render ────────────────────────────────────────────────────────────────
  if (loading) return (
    <div className="min-h-screen bg-gray-50 flex items-center justify-center">
      <p className="text-gray-400">Loading your bookings...</p>
    </div>
  )

  if (error) return (
    <div className="min-h-screen bg-gray-50 flex items-center justify-center">
      <p className="text-red-500">{error}</p>
    </div>
  )

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="max-w-4xl mx-auto px-4 py-8">

        {/* Header */}
        <div className="mb-8">
          <h1 className="font-display text-3xl font-bold text-gray-900 mb-1">My Bookings</h1>
          <p className="text-gray-500">Hello, {user?.name || 'Guest'} · {bookings.length} total reservations</p>
        </div>

        {/* Filter tabs */}
        <div className="flex gap-2 mb-6 flex-wrap">
          {['all', 'PENDING', 'CONFIRMED', 'CANCELLED', 'COMPLETED'].map(f => (
            <button
              key={f}
              onClick={() => setFilter(f)}
              className={`px-4 py-2 rounded-full text-sm font-medium capitalize transition-all
                ${filter === f ? 'bg-brand-500 text-white' : 'bg-white border border-gray-200 text-gray-600 hover:border-brand-300'}`}
            >
              {f.toLowerCase()}
            </button>
          ))}
        </div>

        {/* Booking cards */}
        {filtered.length === 0 ? (
          <div className="text-center py-20">
            <p className="text-5xl mb-4">🍽️</p>
            <p className="font-display text-xl font-bold text-gray-900 mb-2">No bookings found</p>
            <p className="text-gray-500 text-sm mb-6">
              {filter === 'all' ? "You haven't made any bookings yet." : `No ${filter.toLowerCase()} bookings.`}
            </p>
            <Link
              to="/"
              className="bg-brand-500 text-white px-6 py-3 rounded-xl hover:bg-brand-600 transition-colors text-sm font-medium inline-block"
            >
              Browse Restaurants →
            </Link>
          </div>
        ) : (
          <div className="space-y-4">
            {filtered.map(booking => {
              const upcoming = isUpcoming(booking.bookingDate)
              const cfg = statusConfig[booking.status] ?? { label: booking.status, color: 'text-gray-600 bg-gray-50 border-gray-200' }
              const canCancel = upcoming && !['CANCELLED','COMPLETED','REJECTED','NO_SHOW'].includes(booking.status)

              return (
                <div key={booking.id} className="bg-white rounded-2xl border border-gray-100 shadow-sm p-5">
                  <div className="flex flex-col sm:flex-row sm:items-start justify-between gap-4">
                    <div className="flex-1">

                      {/* Title row */}
                      <div className="flex items-center gap-3 mb-3 flex-wrap">
                        <h3 className="font-display font-bold text-gray-900 text-lg">
                          {booking.restaurantName}
                        </h3>
                        <span className={`text-xs font-medium px-2.5 py-0.5 rounded-full border ${cfg.color}`}>
                          {cfg.label}
                        </span>
                        {upcoming && canCancel && (
                          <span className="text-xs text-blue-600 bg-blue-50 px-2 py-0.5 rounded-full border border-blue-200">
                            Upcoming
                          </span>
                        )}
                      </div>

                      {/* Details grid */}
                      <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 text-sm">
                        <div>
                          <p className="text-gray-400 text-xs uppercase tracking-wide">Date</p>
                          <p className="font-medium text-gray-800 mt-0.5">{booking.bookingDate}</p>
                        </div>
                        <div>
                          <p className="text-gray-400 text-xs uppercase tracking-wide">Time</p>
                          <p className="font-medium text-gray-800 mt-0.5">{booking.timeSlotLabel}</p>
                        </div>
                        <div>
                          <p className="text-gray-400 text-xs uppercase tracking-wide">Guests</p>
                          <p className="font-medium text-gray-800 mt-0.5">{booking.partySize} people</p>
                        </div>
                        <div>
                          <p className="text-gray-400 text-xs uppercase tracking-wide">Table</p>
                          <p className="font-medium text-gray-800 mt-0.5">#{booking.tableNumber}</p>
                        </div>
                      </div>
                    </div>

                    {/* Cancel button — only for cancellable upcoming bookings */}
                    {canCancel && (
                      <button
                        onClick={() => handleCancel(booking.id)}
                        className="text-sm border border-red-200 text-red-500 px-4 py-2 rounded-xl hover:bg-red-50 transition-colors"
                      >
                        Cancel
                      </button>
                    )}
                  </div>

                  {/* Booking reference footer */}
                  <div className="mt-3 pt-3 border-t border-gray-50 flex items-center justify-between text-xs text-gray-400">
                    <span>Ref: {booking.bookingReference}</span>
                    {booking.specialRequest && (
                      <span>📝 {booking.specialRequest}</span>
                    )}
                  </div>
                </div>
              )
            })}
          </div>
        )}

        {/* Bottom CTA */}
        <div className="mt-8 text-center">
          <Link
            to="/"
            className="inline-block bg-brand-500 text-white px-8 py-3 rounded-xl hover:bg-brand-600 transition-colors text-sm font-medium"
          >
            + New Booking
          </Link>
        </div>

      </div>
    </div>
  )
}
