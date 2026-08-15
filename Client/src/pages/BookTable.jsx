import { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { restaurantService } from '../services/restaurantService'
import { bookingService } from '../services/bookingService'

// Step labels for the progress bar at the top
const STEPS = ['Select Slot & Date', 'Pick a Table', 'Confirm']

export default function BookTable() {
  const { restaurantId } = useParams()   // comes from /restaurant/:restaurantId/book
  // isCustomer flag — only ROLE_CUSTOMER can complete a booking
  const { user, isCustomer } = useAuth()
  const navigate = useNavigate()

  // ── Data from the backend ──────────────────────────────────────────────────
  const [restaurant,  setRestaurant]  = useState(null)
  const [timeSlots,   setTimeSlots]   = useState([])   // all slots for this restaurant
  const [tables,      setTables]      = useState([])   // all tables for this restaurant
  const [pageLoading, setPageLoading] = useState(true)
  const [pageError,   setPageError]   = useState(null)

  // ── User selections ────────────────────────────────────────────────────────
  const [step,           setStep]          = useState(0)
  const [bookingDate,    setBookingDate]   = useState(new Date().toISOString().split('T')[0])
  const [selectedSlot,   setSelectedSlot]  = useState(null)   // full slot object from backend
  const [guestCount,     setGuestCount]    = useState(2)
  const [selectedTable,  setSelectedTable] = useState(null)   // full table object from backend
  const [specialRequest, setSpecialRequest]= useState('')

  // ── Submit state ───────────────────────────────────────────────────────────
  const [submitting, setSubmitting] = useState(false)
  const [booking,    setBooking]    = useState(null)   // the confirmed BookingResponseDto

  // ── 1. Fetch restaurant info + tables + time slots on mount ────────────────
  useEffect(() => {
    Promise.all([
      restaurantService.getById(restaurantId),
      restaurantService.getTables(restaurantId),
      restaurantService.getTimeSlots(restaurantId),
    ])
      .then(([restResp, tablesResp, slotsResp]) => {
        setRestaurant(restResp.data)
        setTables(tablesResp.data)
        // Show only active time slots
        setTimeSlots(slotsResp.data.filter(s => s.active))
      })
      .catch(() => setPageError('Could not load restaurant data. Is the backend running?'))
      .finally(() => setPageLoading(false))
  }, [restaurantId])

  // ── 2. Filter tables based on guest count ─────────────────────────────────
  // A table is usable if its seating capacity >= number of guests AND its status is AVAILABLE
  const availableTables = tables.filter(
    t => t.seatingCapacity >= guestCount && t.tableStatus === 'AVAILABLE'
  )

  // ── 3. Handle "Confirm Booking" button ────────────────────────────────────
  // Submits booking to backend — restricted to ROLE_CUSTOMER only
  const handleConfirm = async () => {
    if (!user) {
      navigate('/login')   // Not logged in — redirect to login
      return
    }
    if (!isCustomer) {
      // Owners and admins can browse but cannot make reservations
      alert('Table reservations are available to Customer accounts only.')
      return
    }

    setSubmitting(true)
    try {
      /*
       * POST /api/v1/api/bookings
       * Payload matches BookingCreateRequestDto exactly:
       *   userId, restaurantId, tableId, timeSlotId, bookingDate, partySize, specialRequest
       */
      const payload = {
        userId:         user.id,
        restaurantId:   Number(restaurantId),
        tableId:        selectedTable.tableId,
        timeSlotId:     selectedSlot.id,
        bookingDate:    bookingDate,
        partySize:      guestCount,
        specialRequest: specialRequest || null,
      }
      const { data } = await bookingService.createBooking(payload)
      setBooking(data)   // store the confirmed booking to show success screen
    } catch (err) {
      const msg = err.response?.data?.message || err.response?.data || 'Booking failed. Please try again.'
      alert(typeof msg === 'string' ? msg : 'Booking failed.')
    } finally {
      setSubmitting(false)
    }
  }

  // ── Loading / Error states ─────────────────────────────────────────────────
  if (pageLoading) return (
    <div className="min-h-screen bg-gray-50 flex items-center justify-center">
      <p className="text-gray-400">Loading restaurant details...</p>
    </div>
  )

  if (pageError) return (
    <div className="min-h-screen bg-gray-50 flex items-center justify-center px-4">
      <div className="text-center">
        <p className="text-red-500 mb-4">{pageError}</p>
        <button onClick={() => navigate('/')} className="text-brand-500 underline text-sm">← Back to Home</button>
      </div>
    </div>
  )

  // ── Success screen shown after booking is created ──────────────────────────
  if (booking) return (
    <div className="min-h-screen bg-gray-50 flex items-center justify-center px-4">
      <div className="text-center max-w-sm">
        <div className="w-20 h-20 bg-green-100 rounded-full flex items-center justify-center mx-auto mb-6">
          <span className="text-4xl">✅</span>
        </div>
        <h2 className="font-display text-2xl font-bold text-gray-900 mb-2">Booking Confirmed!</h2>
        <p className="text-gray-500 mb-1">
          <strong>{booking.restaurantName}</strong>
        </p>
        <p className="text-gray-500 mb-1">
          Table #{booking.tableNumber} · {booking.timeSlotLabel}
        </p>
        <p className="text-gray-500 mb-1">
          {booking.bookingDate} · {booking.partySize} guests
        </p>
        <p className="text-xs text-gray-400 mb-8">
          Ref: {booking.bookingReference}
        </p>
        <div className="flex gap-3 justify-center">
          <button
            onClick={() => navigate('/my-bookings')}
            className="bg-brand-500 text-white px-6 py-2.5 rounded-xl hover:bg-brand-600 transition-colors text-sm font-medium"
          >
            View My Bookings
          </button>
          <button
            onClick={() => navigate('/')}
            className="border border-gray-200 text-gray-700 px-6 py-2.5 rounded-xl hover:bg-gray-50 transition-colors text-sm font-medium"
          >
            Back to Home
          </button>
        </div>
      </div>
    </div>
  )

  // ── Main booking UI ────────────────────────────────────────────────────────
  return (
    <div className="min-h-screen bg-gray-50">
      <div className="max-w-5xl mx-auto px-4 py-8">

        {/* Header */}
        <div className="mb-6">
          <button onClick={() => navigate('/')} className="text-sm text-gray-400 hover:text-brand-500 mb-2 block">
            ← Back to restaurants
          </button>
          <h1 className="font-display text-3xl font-bold text-gray-900">
            Reserve at {restaurant?.name}
          </h1>
          <p className="text-gray-500 text-sm mt-1">{restaurant?.address} · {restaurant?.city}</p>
        </div>

        {/* Step progress bar */}
        <div className="flex items-center gap-0 mb-8">
          {STEPS.map((s, i) => (
            <div key={s} className="flex items-center">
              <div className={`flex items-center gap-2 px-4 py-2 rounded-full text-sm font-medium transition-all
                ${i === step ? 'bg-brand-500 text-white' : i < step ? 'bg-green-100 text-green-700' : 'bg-gray-100 text-gray-400'}`}>
                <span>{i < step ? '✓' : i + 1}</span>
                <span className="hidden sm:block">{s}</span>
              </div>
              {i < STEPS.length - 1 && (
                <div className={`h-0.5 w-8 mx-1 ${i < step ? 'bg-green-300' : 'bg-gray-200'}`} />
              )}
            </div>
          ))}
        </div>

        {/* ── STEP 0: Date, Guests, Time Slot selection ── */}
        {step === 0 && (
          <div className="bg-white rounded-2xl border border-gray-100 shadow-sm p-6">
            <h2 className="font-display text-lg font-bold text-gray-900 mb-5">When are you coming?</h2>

            <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 mb-6">
              {/* Date picker */}
              <div>
                <label className="block text-xs font-medium text-gray-500 mb-1.5 uppercase tracking-wide">Date</label>
                <input
                  type="date"
                  min={new Date().toISOString().split('T')[0]}
                  value={bookingDate}
                  onChange={e => setBookingDate(e.target.value)}
                  className="w-full border border-gray-200 rounded-xl px-3 py-2.5 text-sm focus:outline-none focus:border-brand-500 focus:ring-1 focus:ring-brand-500"
                />
              </div>

              {/* Guest count */}
              <div>
                <label className="block text-xs font-medium text-gray-500 mb-1.5 uppercase tracking-wide">Guests</label>
                <input
                  type="number"
                  min={1} max={20}
                  value={guestCount}
                  onChange={e => setGuestCount(Number(e.target.value))}
                  className="w-full border border-gray-200 rounded-xl px-3 py-2.5 text-sm focus:outline-none focus:border-brand-500 focus:ring-1 focus:ring-brand-500"
                />
              </div>

              {/* Special request */}
              <div>
                <label className="block text-xs font-medium text-gray-500 mb-1.5 uppercase tracking-wide">Special Request (optional)</label>
                <input
                  type="text"
                  value={specialRequest}
                  onChange={e => setSpecialRequest(e.target.value)}
                  placeholder="e.g. window seat, anniversary"
                  className="w-full border border-gray-200 rounded-xl px-3 py-2.5 text-sm focus:outline-none focus:border-brand-500 focus:ring-1 focus:ring-brand-500"
                />
              </div>
            </div>

            {/* Time slot grid — fetched from the backend */}
            <div>
              <label className="block text-xs font-medium text-gray-500 mb-3 uppercase tracking-wide">
                Time Slot — {timeSlots.length} available
              </label>

              {timeSlots.length === 0 ? (
                <p className="text-sm text-gray-400">No time slots configured for this restaurant yet.</p>
              ) : (
                <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 gap-3">
                  {timeSlots.map(slot => (
                    <button
                      key={slot.id}
                      onClick={() => setSelectedSlot(slot)}
                      className={`p-3 rounded-xl border-2 text-left transition-all
                        ${selectedSlot?.id === slot.id
                          ? 'border-brand-500 bg-red-50'
                          : 'border-gray-200 bg-white hover:border-brand-300'}`}
                    >
                      <p className="text-sm font-medium text-gray-900">{slot.label}</p>
                      <p className="text-xs text-gray-400 mt-0.5">
                        {slot.startTime} – {slot.endTime}
                      </p>
                      {slot.maxCovers && (
                        <p className="text-xs text-gray-400">Max {slot.maxCovers} covers</p>
                      )}
                    </button>
                  ))}
                </div>
              )}
            </div>

            <button
              onClick={() => setStep(1)}
              disabled={!selectedSlot || !bookingDate}
              className="mt-6 w-full bg-brand-500 text-white font-medium py-3 rounded-xl hover:bg-brand-600 transition-colors text-sm disabled:opacity-40"
            >
              See Available Tables →
            </button>
          </div>
        )}

        {/* ── STEP 1: Table grid ── */}
        {step === 1 && (
          <div>
            <div className="flex items-center justify-between mb-4">
              <div>
                <h2 className="font-display text-xl font-bold text-gray-900">Choose a Table</h2>
                <p className="text-gray-500 text-sm">
                  {bookingDate} · {selectedSlot?.label} · {guestCount} guests
                </p>
              </div>
              <button
                onClick={() => { setStep(0); setSelectedTable(null) }}
                className="text-sm text-gray-500 hover:text-brand-500 border border-gray-200 px-4 py-2 rounded-xl"
              >
                ← Change Slot
              </button>
            </div>

            {/* Status legend */}
            <div className="flex gap-4 mb-4 text-xs text-gray-500">
              {[['bg-green-200', 'Available'], ['bg-red-200', 'Unavailable']].map(([color, label]) => (
                <span key={label} className="flex items-center gap-1.5">
                  <span className={`w-3 h-3 rounded-full ${color}`} />{label}
                </span>
              ))}
            </div>

            {availableTables.length === 0 ? (
              <div className="text-center py-16 text-gray-400">
                <p className="text-4xl mb-3">🪑</p>
                <p className="font-medium">No tables can seat {guestCount} guests</p>
                <p className="text-sm">Try reducing your guest count or pick a different slot</p>
              </div>
            ) : (
              <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 gap-4 mb-6">
                {availableTables.map(table => (
                  <TableCard
                    key={table.tableId}
                    table={table}
                    selected={selectedTable?.tableId === table.tableId}
                    onSelect={setSelectedTable}
                  />
                ))}
              </div>
            )}

            {/* Sticky bottom bar appears once a table is selected */}
            {selectedTable && (
              <div className="sticky bottom-4 bg-white border border-gray-200 rounded-2xl p-4 shadow-lg flex items-center justify-between">
                <div>
                  <p className="font-medium text-gray-900">Table #{selectedTable.tableNumber} selected</p>
                  <p className="text-sm text-gray-500 capitalize">
                    {selectedTable.locationType?.toLowerCase()} · {selectedTable.seatingCapacity} seats
                  </p>
                </div>
                <button
                  onClick={() => setStep(2)}
                  className="bg-brand-500 text-white px-6 py-2.5 rounded-xl hover:bg-brand-600 transition-colors text-sm font-medium"
                >
                  Continue →
                </button>
              </div>
            )}
          </div>
        )}

        {/* ── STEP 2: Confirm ── */}
        {step === 2 && selectedTable && selectedSlot && (
          <div className="max-w-md mx-auto">
            <div className="bg-white rounded-2xl border border-gray-100 shadow-sm p-6">
              <h2 className="font-display text-xl font-bold text-gray-900 mb-5">Confirm Your Reservation</h2>

              <div className="space-y-3 mb-6">
                {[
                  ['🍽️ Restaurant', restaurant?.name],
                  ['📅 Date',        bookingDate],
                  ['🕐 Time Slot',   selectedSlot.label],
                  ['👥 Guests',      `${guestCount} people`],
                  ['🪑 Table',       `#${selectedTable.tableNumber}`],
                  ['📍 Seating',     selectedTable.locationType?.toLowerCase()],
                  ['💺 Capacity',    `${selectedTable.seatingCapacity} seats`],
                  ...(specialRequest ? [['📝 Request', specialRequest]] : []),
                ].map(([label, value]) => (
                  <div key={label} className="flex items-center justify-between py-2 border-b border-gray-50">
                    <span className="text-sm text-gray-500">{label}</span>
                    <span className="text-sm font-medium text-gray-900 capitalize">{value}</span>
                  </div>
                ))}
              </div>

              {!user && (
                <div className="bg-amber-50 border border-amber-200 rounded-xl p-3 mb-4 text-sm text-amber-700">
                  You'll be redirected to login to complete your booking.
                </div>
              )}

              {/* Billing summary — ratePerSeat × guestCount */}
              {selectedTable.ratePerSeat > 0 && (
                <div className="bg-gray-50 rounded-xl p-4 mb-4 border border-gray-100">
                  <p className="text-xs font-medium text-gray-500 uppercase tracking-wide mb-3">Billing Estimate</p>
                  <div className="space-y-2 text-sm">
                    <div className="flex justify-between text-gray-600">
                      <span>Rate per seat</span>
                      <span>₹{selectedTable.ratePerSeat}</span>
                    </div>
                    <div className="flex justify-between text-gray-600">
                      <span>Guests</span>
                      <span>× {guestCount}</span>
                    </div>
                    <div className="flex justify-between font-bold text-gray-900 pt-2 border-t border-gray-200">
                      <span>Total Estimate</span>
                      <span className="text-brand-500">₹{(selectedTable.ratePerSeat * guestCount).toFixed(2)}</span>
                    </div>
                  </div>
                  <p className="text-xs text-gray-400 mt-2">* Final amount confirmed at restaurant</p>
                </div>
              )}

              {/* Banner shown to owners/admins — they can view but not book */}
              {user && !isCustomer && (
                <div className="bg-amber-50 border border-amber-200 rounded-xl p-3 mb-4 text-sm text-amber-700">
                  🔒 Table reservations are restricted to Customer accounts.
                  Owners and Admins can browse but cannot make bookings.
                </div>
              )}

              <div className="flex gap-3">
                <button
                  onClick={() => setStep(1)}
                  className="flex-1 border border-gray-200 text-gray-700 py-3 rounded-xl hover:bg-gray-50 transition-colors text-sm font-medium"
                >
                  ← Back
                </button>
                {/* Disabled for non-customers — title explains why on hover */}
                <button
                  onClick={handleConfirm}
                  disabled={submitting || (user && !isCustomer)}
                  title={user && !isCustomer ? 'Only Customer accounts can book tables' : ''}
                  className="flex-1 bg-brand-500 text-white py-3 rounded-xl hover:bg-brand-600 transition-colors text-sm font-medium disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  {submitting ? 'Confirming...' : 'Confirm Booking'}
                </button>
              </div>
            </div>
          </div>
        )}

      </div>
    </div>
  )
}

// ── TableCard sub-component ────────────────────────────────────────────────────
// Adapts to the backend's RestaurantTableResponseDto shape:
//   tableId, tableNumber, seatingCapacity, locationType, tableStatus
function TableCard({ table, selected, onSelect }) {
  const isSelectable = table.tableStatus === 'AVAILABLE'
  const locationIcon = table.locationType === 'OUTDOOR' ? '🌿'
                     : table.locationType === 'ROOFTOP' ? '🏙️'
                     : table.locationType === 'WINDOW'  ? '🪟'
                     : '🏠'

  return (
    <button
      onClick={() => isSelectable && onSelect(table)}
      disabled={!isSelectable}
      className={`
        w-full p-4 rounded-xl border-2 text-left transition-all duration-200
        ${isSelectable ? 'cursor-pointer hover:border-brand-500 hover:shadow-md' : 'cursor-not-allowed opacity-50'}
        ${selected ? 'border-brand-500 bg-red-50 shadow-md' : 'border-gray-200 bg-white'}
      `}
    >
      <div className="flex items-start justify-between mb-2">
        <span className="font-display font-bold text-gray-900 text-lg">#{table.tableNumber}</span>
        {selected && <span className="text-brand-500 text-lg">✓</span>}
      </div>
      <div className="space-y-1">
        <div className="flex items-center gap-1 text-sm text-gray-600">
          <span>👥</span>
          <span>{table.seatingCapacity} seats</span>
        </div>
        <div className="flex items-center gap-1 text-sm text-gray-600">
          <span>{locationIcon}</span>
          <span className="capitalize">{table.locationType?.toLowerCase()}</span>
        </div>
      </div>
      <span className={`mt-2 inline-block text-xs font-medium px-2 py-0.5 rounded-full border capitalize
        ${isSelectable ? 'text-green-700 bg-green-50 border-green-200' : 'text-red-700 bg-red-50 border-red-200'}`}>
        {table.tableStatus?.toLowerCase()}
      </span>
    </button>
  )
}
