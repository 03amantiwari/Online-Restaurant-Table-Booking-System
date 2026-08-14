// Owner dashboard — profile, restaurants list, create restaurant, booking metrics
import { useState, useEffect } from 'react'
import { useAuth } from '../context/AuthContext'
import { userService } from '../services/userService'
import { restaurantService } from '../services/restaurantService'
import { bookingService } from '../services/bookingService'

// Location type options matching backend enum
const LOCATION_TYPES = ['INDOOR', 'OUTDOOR', 'WINDOW', 'ROOFTOP', 'PRIVATE']

// Empty row templates for dynamic table/slot arrays
const EMPTY_TABLE = { tableNumber: '', seatingCapacity: '', locationType: 'INDOOR', ratePerSeat: '' }
const EMPTY_SLOT  = { label: '', startTime: '', endTime: '', maxCovers: '' }

export default function OwnerDashboard() {
  const { user } = useAuth()

  // Owner profile data fetched from backend
  const [profile,     setProfile]     = useState(null)
  // Owned restaurants list
  const [restaurants, setRestaurants] = useState([])
  // Bookings for the currently selected restaurant
  const [bookings,    setBookings]    = useState([])
  const [loading,     setLoading]     = useState(true)
  const [error,       setError]       = useState(null)

  // Which restaurant card is selected for bookings view
  const [selectedRestId, setSelectedRestId] = useState(null)
  // Status filter on booking table
  const [statusFilter,   setStatusFilter]   = useState('all')
  // Tracks which booking's status dropdown is currently being updated
  const [updatingBookingId, setUpdatingBookingId] = useState(null)
  // Brief success message shown after status update
  const [statusToast, setStatusToast] = useState('')

  // Profile inline edit state
  const [editing,  setEditing]  = useState(false)
  const [editForm, setEditForm] = useState({ fullName: '', phoneNumber: '' })
  const [saving,   setSaving]   = useState(false)

  // Controls whether the "Create Restaurant" form is visible
  const [showCreateForm, setShowCreateForm] = useState(false)
  // Tracks which restaurant toggle is in progress (by id) to show loading state
  const [togglingId, setTogglingId] = useState(null)
  // Tracks which restaurant soft-delete/restore is in progress
  const [softActionId, setSoftActionId] = useState(null)

  /**
   * Restaurant creation form state.
   * tables and timeSlots are arrays — each entry maps to one nested DTO.
   * These are sent as-is in the POST /restaurants payload body.
   */
  const [restForm, setRestForm] = useState({
    name: '', city: '', address: '', contactNumber: '',
    contactEmail: '', priceBand: 2,
    tables:    [{ ...EMPTY_TABLE }],   // start with 1 empty table row
    timeSlots: [{ ...EMPTY_SLOT }],    // start with 1 empty slot row
  })
  const [creating, setCreating] = useState(false)
  const [createError, setCreateError] = useState('')

  // Fetch owner profile and their restaurants on mount
  useEffect(() => {
    if (!user?.id) return
    Promise.all([
      userService.getOwner(user.id),
      restaurantService.getAll(),
    ])
      .then(([profileResp, restResp]) => {
        setProfile(profileResp.data)
        setEditForm({ fullName: profileResp.data.fullName, phoneNumber: profileResp.data.phoneNumber })
        // Filter all restaurants to only those owned by this user
        const owned = restResp.data.filter(r => r.ownerUserId === user.id)
        setRestaurants(owned)
        if (owned.length > 0) setSelectedRestId(owned[0].id)
      })
      .catch(() => setError('Could not load dashboard.'))
      .finally(() => setLoading(false))
  }, [user])

  // Fetch bookings whenever the selected restaurant changes
  useEffect(() => {
    if (!selectedRestId) return
    bookingService.getRestaurantBookings(selectedRestId)
      .then(({ data }) => setBookings(data))
      .catch(() => setBookings([]))
  }, [selectedRestId])

  // Filter bookings by the selected status tab
  const filteredBookings = statusFilter === 'all'
    ? bookings
    : bookings.filter(b => b.status === statusFilter)

  // ── Profile edit handlers ─────────────────────────────────────────────────

  // Sends updated profile to backend and refreshes local state
  const handleSaveProfile = async () => {
    setSaving(true)
    try {
      const { data } = await userService.updateOwner(user.id, editForm)
      setProfile(data)
      setEditing(false)
    } catch { alert('Profile update failed.') }
    finally { setSaving(false) }
  }

  // ── Toggle restaurant open/closed status ─────────────────────────────────

  /**
   * Sends PATCH /restaurants/{id}/toggle-status to backend.
   * On success, replaces the matching restaurant in local state with the
   * updated object returned by the backend — no full refetch needed.
   *
   * WHY update state from the response (not from local toggle)?
   * The backend is the source of truth. If the save fails, our local state
   * stays accurate because we only update it on a 200 OK response.
   */
  const handleToggleStatus = async (e, restaurantId) => {
    e.stopPropagation()   // prevent restaurant card onClick from firing
    setTogglingId(restaurantId)
    try {
      const { data } = await restaurantService.toggleStatus(restaurantId)
      // Replace only the toggled restaurant in local array
      setRestaurants(prev => prev.map(r => r.id === restaurantId ? data : r))
    } catch {
      alert('Could not update restaurant status. Please try again.')
    } finally {
      setTogglingId(null)
    }
  }

  // ── Soft-delete & Restore ────────────────────────────────────────────────

  /**
   * Soft-delete — sets restaurant active=false on backend.
   * Restaurant disappears from customer Home page immediately.
   * We update local state from the backend response (source of truth).
   *
   * e.stopPropagation() prevents the card's onClick from also firing.
   */
  const handleSoftDelete = async (e, restaurantId) => {
    e.stopPropagation()
    setSoftActionId(restaurantId)
    try {
      const { data } = await restaurantService.softDelete(restaurantId)
      setRestaurants(prev => prev.map(r => r.id === restaurantId ? data : r))
    } catch {
      alert('Could not unlist restaurant. Please try again.')
    } finally {
      setSoftActionId(null)
    }
  }

  // Restore — sets active=true, restaurant reappears on Home page
  const handleRestore = async (e, restaurantId) => {
    e.stopPropagation()
    setSoftActionId(restaurantId)
    try {
      const { data } = await restaurantService.restore(restaurantId)
      setRestaurants(prev => prev.map(r => r.id === restaurantId ? data : r))
    } catch {
      alert('Could not restore restaurant. Please try again.')
    } finally {
      setSoftActionId(null)
    }
  }

  // ── Owner booking status change ───────────────────────────────────────────

  /**
   * Called when owner selects a new status from the dropdown on a booking row.
   *
   * FLOW:
   *   1. Owner picks status from <select> dropdown
   *   2. PUT /api/bookings/{id}/status  { status: "CONFIRMED" }
   *   3. Backend updates booking + resets table to AVAILABLE if terminal status
   *   4. Replace matching booking in local state with updated response
   *   5. Show a brief toast message for 2 seconds
   *
   * WHY update from response?
   * The backend is the source of truth. We only update local state on 200 OK,
   * so if the API fails, the dropdown reverts and nothing is corrupted.
   */
  const handleStatusChange = async (bookingId, newStatus) => {
    setUpdatingBookingId(bookingId)
    try {
      const { data } = await bookingService.updateStatusByOwner(bookingId, newStatus)
      // Replace only the updated booking in local array
      setBookings(prev => prev.map(b => b.id === bookingId ? data : b))
      // Show brief success toast
      setStatusToast(`Status updated to ${newStatus}`)
      setTimeout(() => setStatusToast(''), 2000)
    } catch {
      alert('Could not update booking status. Please try again.')
    } finally {
      setUpdatingBookingId(null)
    }
  }

  // ── Create Restaurant form handlers ───────────────────────────────────────

  // Updates top-level restaurant fields (name, city, etc.)
  const handleRestFormChange = (field, value) =>
    setRestForm(prev => ({ ...prev, [field]: value }))

  // Updates one field inside a specific table row
  const handleTableChange = (index, field, value) => {
    const updated = [...restForm.tables]
    updated[index] = { ...updated[index], [field]: value }
    setRestForm(prev => ({ ...prev, tables: updated }))
  }

  // Updates one field inside a specific time slot row
  const handleSlotChange = (index, field, value) => {
    const updated = [...restForm.timeSlots]
    updated[index] = { ...updated[index], [field]: value }
    setRestForm(prev => ({ ...prev, timeSlots: updated }))
  }

  // Appends a blank table row to the tables array
  const addTableRow = () =>
    setRestForm(prev => ({ ...prev, tables: [...prev.tables, { ...EMPTY_TABLE }] }))

  // Removes a table row by index
  const removeTableRow = (index) =>
    setRestForm(prev => ({ ...prev, tables: prev.tables.filter((_, i) => i !== index) }))

  // Appends a blank slot row to the timeSlots array
  const addSlotRow = () =>
    setRestForm(prev => ({ ...prev, timeSlots: [...prev.timeSlots, { ...EMPTY_SLOT }] }))

  // Removes a slot row by index
  const removeSlotRow = (index) =>
    setRestForm(prev => ({ ...prev, timeSlots: prev.timeSlots.filter((_, i) => i !== index) }))

  /**
   * Submits the single cascading POST /restaurants request.
   *
   * Payload shape matches RestaurantCreateRequestDto exactly:
   * {
   *   name, city, address, contactNumber, contactEmail, priceBand,
   *   tables: [{ tableNumber, seatingCapacity, locationType, ratePerSeat }],
   *   timeSlots: [{ label, startTime, endTime, maxCovers }]
   * }
   *
   * Backend saves Restaurant + RestaurantTable[] + TimeSlot[] in ONE @Transactional call.
   * If any part fails, the whole transaction rolls back — no partial data corruption.
   */
  const handleCreateRestaurant = async () => {
    setCreateError('')
    // Basic client-side guard before hitting the API
    if (!restForm.name.trim() || !restForm.city.trim() || !restForm.address.trim()) {
      setCreateError('Name, city, and address are required.')
      return
    }
    if (restForm.tables.length === 0) {
      setCreateError('At least one table is required.')
      return
    }
    if (restForm.timeSlots.length === 0) {
      setCreateError('At least one time slot is required.')
      return
    }

    setCreating(true)
    try {
      // Build the payload — convert numeric strings to numbers for the DTO
      const payload = {
        name:          restForm.name.trim(),
        city:          restForm.city.trim(),
        address:       restForm.address.trim(),
        contactNumber: restForm.contactNumber.trim(),
        contactEmail:  restForm.contactEmail.trim(),
        priceBand:     Number(restForm.priceBand),
        tables: restForm.tables.map(t => ({
          tableNumber:     Number(t.tableNumber),
          seatingCapacity: Number(t.seatingCapacity),
          locationType:    t.locationType,
          ratePerSeat:     Number(t.ratePerSeat),
        })),
        timeSlots: restForm.timeSlots.map(s => ({
          label:     s.label.trim(),
          startTime: s.startTime,   // "HH:mm:ss" format expected by backend
          endTime:   s.endTime,
          maxCovers: s.maxCovers ? Number(s.maxCovers) : null,
        })),
      }

      const { data } = await restaurantService.createRestaurant(payload)

      // Add the newly created restaurant to local list without refetching
      setRestaurants(prev => [...prev, data])
      setSelectedRestId(data.id)

      // Reset form and hide it
      setRestForm({
        name: '', city: '', address: '', contactNumber: '',
        contactEmail: '', priceBand: 2,
        tables: [{ ...EMPTY_TABLE }],
        timeSlots: [{ ...EMPTY_SLOT }],
      })
      setShowCreateForm(false)
    } catch (err) {
      const msg = err.response?.data?.message || err.response?.data || 'Failed to create restaurant.'
      setCreateError(typeof msg === 'string' ? msg : 'Failed to create restaurant.')
    } finally {
      setCreating(false)
    }
  }

  if (loading) return <div className="min-h-screen flex items-center justify-center text-gray-400">Loading dashboard...</div>
  if (error)   return <div className="min-h-screen flex items-center justify-center text-red-500">{error}</div>

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="max-w-5xl mx-auto px-4 py-8">

        <div className="mb-8">
          <h1 className="font-display text-3xl font-bold text-gray-900">Owner Dashboard</h1>
          <p className="text-gray-500 text-sm mt-1">Manage your restaurants and view reservations</p>
        </div>

        {/* ── Profile Card ── */}
        <div className="bg-white rounded-2xl border border-gray-100 shadow-sm p-6 mb-6">
          <div className="flex items-center justify-between mb-4">
            <h2 className="font-display text-lg font-bold text-gray-900">Profile</h2>
            {!editing && (
              <button onClick={() => setEditing(true)}
                className="text-sm text-brand-500 border border-brand-200 px-4 py-1.5 rounded-lg hover:bg-red-50">
                Edit Profile
              </button>
            )}
          </div>
          {editing ? (
            <div className="space-y-3">
              <div>
                <label className="text-xs text-gray-500 uppercase">Full Name</label>
                <input value={editForm.fullName}
                  onChange={e => setEditForm({ ...editForm, fullName: e.target.value })}
                  className="w-full mt-1 border border-gray-200 rounded-xl px-4 py-2.5 text-sm focus:outline-none focus:border-brand-500"
                />
              </div>
              <div>
                <label className="text-xs text-gray-500 uppercase">Phone Number</label>
                <input value={editForm.phoneNumber}
                  onChange={e => setEditForm({ ...editForm, phoneNumber: e.target.value })}
                  className="w-full mt-1 border border-gray-200 rounded-xl px-4 py-2.5 text-sm focus:outline-none focus:border-brand-500"
                />
              </div>
              <div className="flex gap-3">
                <button onClick={handleSaveProfile} disabled={saving}
                  className="bg-brand-500 text-white px-5 py-2 rounded-xl text-sm hover:bg-brand-600 disabled:opacity-60">
                  {saving ? 'Saving...' : 'Save'}
                </button>
                <button onClick={() => setEditing(false)}
                  className="border border-gray-200 text-gray-600 px-5 py-2 rounded-xl text-sm">
                  Cancel
                </button>
              </div>
            </div>
          ) : (
            <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
              {[['ID', profile?.id], ['Name', profile?.fullName], ['Email', profile?.email], ['Phone', profile?.phoneNumber]]
                .map(([label, value]) => (
                  <div key={label}>
                    <p className="text-xs text-gray-400 uppercase">{label}</p>
                    <p className="text-sm font-medium text-gray-800 mt-0.5">{value || '—'}</p>
                  </div>
                ))}
            </div>
          )}
        </div>

        {/* ── My Restaurants + Create Button ── */}
        <div className="bg-white rounded-2xl border border-gray-100 shadow-sm p-6 mb-6">
          <div className="flex items-center justify-between mb-4">
            <h2 className="font-display text-lg font-bold text-gray-900">
              My Restaurants ({restaurants.length})
            </h2>
            {/* Toggle the create form on/off */}
            <button
              onClick={() => { setShowCreateForm(!showCreateForm); setCreateError('') }}
              className="text-sm bg-brand-500 text-white px-4 py-1.5 rounded-lg hover:bg-brand-600 transition-colors">
              {showCreateForm ? '✕ Cancel' : '+ Add Restaurant'}
            </button>
          </div>

          {/* ── Create Restaurant Form (shown when showCreateForm is true) ── */}
          {showCreateForm && (
            <div className="border border-gray-200 rounded-xl p-5 mb-5 bg-gray-50">
              <h3 className="font-semibold text-gray-900 mb-4">New Restaurant Details</h3>

              {createError && (
                <div className="bg-red-50 border border-red-200 text-red-700 text-sm px-4 py-3 rounded-xl mb-4">
                  {createError}
                </div>
              )}

              {/* Basic restaurant info grid */}
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 mb-5">
                {[
                  ['name',          'Restaurant Name',  'text',   'e.g. Spice Garden'],
                  ['city',          'City',             'text',   'e.g. Pune'],
                  ['address',       'Address',          'text',   'Full address'],
                  ['contactNumber', 'Contact Number',   'text',   '10-digit number'],
                  ['contactEmail',  'Contact Email',    'email',  'optional'],
                ].map(([field, label, type, placeholder]) => (
                  <div key={field}>
                    <label className="text-xs text-gray-500 uppercase tracking-wide">{label}</label>
                    <input
                      type={type}
                      value={restForm[field]}
                      onChange={e => handleRestFormChange(field, e.target.value)}
                      placeholder={placeholder}
                      className="w-full mt-1 border border-gray-200 rounded-xl px-3 py-2 text-sm focus:outline-none focus:border-brand-500"
                    />
                  </div>
                ))}
                <div>
                  <label className="text-xs text-gray-500 uppercase tracking-wide">Price Band (1-5)</label>
                  <input
                    type="number" min="1" max="5"
                    value={restForm.priceBand}
                    onChange={e => handleRestFormChange('priceBand', e.target.value)}
                    className="w-full mt-1 border border-gray-200 rounded-xl px-3 py-2 text-sm focus:outline-none focus:border-brand-500"
                  />
                </div>
              </div>

              {/* ── Tables Section ── */}
              <div className="mb-5">
                <div className="flex items-center justify-between mb-2">
                  <p className="text-sm font-semibold text-gray-700">Tables</p>
                  {/* Add a new blank table row */}
                  <button onClick={addTableRow}
                    className="text-xs text-brand-500 border border-brand-200 px-3 py-1 rounded-lg hover:bg-red-50">
                    + Add Table
                  </button>
                </div>
                <div className="space-y-2">
                  {restForm.tables.map((table, idx) => (
                    <div key={idx} className="grid grid-cols-2 sm:grid-cols-5 gap-2 items-end p-3 bg-white rounded-xl border border-gray-200">
                      <div>
                        <label className="text-xs text-gray-400">Table #</label>
                        <input type="number" value={table.tableNumber}
                          onChange={e => handleTableChange(idx, 'tableNumber', e.target.value)}
                          className="w-full mt-0.5 border border-gray-200 rounded-lg px-2 py-1.5 text-xs focus:outline-none focus:border-brand-500"
                        />
                      </div>
                      <div>
                        <label className="text-xs text-gray-400">Seats</label>
                        <input type="number" value={table.seatingCapacity}
                          onChange={e => handleTableChange(idx, 'seatingCapacity', e.target.value)}
                          className="w-full mt-0.5 border border-gray-200 rounded-lg px-2 py-1.5 text-xs focus:outline-none focus:border-brand-500"
                        />
                      </div>
                      <div>
                        <label className="text-xs text-gray-400">Location</label>
                        <select value={table.locationType}
                          onChange={e => handleTableChange(idx, 'locationType', e.target.value)}
                          className="w-full mt-0.5 border border-gray-200 rounded-lg px-2 py-1.5 text-xs focus:outline-none focus:border-brand-500 bg-white">
                          {LOCATION_TYPES.map(t => <option key={t}>{t}</option>)}
                        </select>
                      </div>
                      <div>
                        <label className="text-xs text-gray-400">Rate/Seat (₹)</label>
                        <input type="number" value={table.ratePerSeat}
                          onChange={e => handleTableChange(idx, 'ratePerSeat', e.target.value)}
                          className="w-full mt-0.5 border border-gray-200 rounded-lg px-2 py-1.5 text-xs focus:outline-none focus:border-brand-500"
                        />
                      </div>
                      {/* Remove row — only show if more than 1 table */}
                      <button
                        onClick={() => removeTableRow(idx)}
                        disabled={restForm.tables.length === 1}
                        className="text-red-400 text-xs hover:text-red-600 disabled:opacity-30 pb-1 text-center">
                        ✕
                      </button>
                    </div>
                  ))}
                </div>
              </div>

              {/* ── Time Slots Section ── */}
              <div className="mb-5">
                <div className="flex items-center justify-between mb-2">
                  <p className="text-sm font-semibold text-gray-700">Time Slots</p>
                  {/* Add a new blank slot row */}
                  <button onClick={addSlotRow}
                    className="text-xs text-brand-500 border border-brand-200 px-3 py-1 rounded-lg hover:bg-red-50">
                    + Add Slot
                  </button>
                </div>
                <div className="space-y-2">
                  {restForm.timeSlots.map((slot, idx) => (
                    <div key={idx} className="grid grid-cols-2 sm:grid-cols-5 gap-2 items-end p-3 bg-white rounded-xl border border-gray-200">
                      <div>
                        <label className="text-xs text-gray-400">Label</label>
                        <input type="text" value={slot.label} placeholder="e.g. Dinner"
                          onChange={e => handleSlotChange(idx, 'label', e.target.value)}
                          className="w-full mt-0.5 border border-gray-200 rounded-lg px-2 py-1.5 text-xs focus:outline-none focus:border-brand-500"
                        />
                      </div>
                      <div>
                        <label className="text-xs text-gray-400">Start (HH:mm)</label>
                        <input type="time" value={slot.startTime}
                          onChange={e => handleSlotChange(idx, 'startTime', e.target.value + ':00')}
                          className="w-full mt-0.5 border border-gray-200 rounded-lg px-2 py-1.5 text-xs focus:outline-none focus:border-brand-500"
                        />
                      </div>
                      <div>
                        <label className="text-xs text-gray-400">End (HH:mm)</label>
                        <input type="time" value={slot.endTime}
                          onChange={e => handleSlotChange(idx, 'endTime', e.target.value + ':00')}
                          className="w-full mt-0.5 border border-gray-200 rounded-lg px-2 py-1.5 text-xs focus:outline-none focus:border-brand-500"
                        />
                      </div>
                      <div>
                        <label className="text-xs text-gray-400">Max Covers</label>
                        <input type="number" value={slot.maxCovers}
                          onChange={e => handleSlotChange(idx, 'maxCovers', e.target.value)}
                          className="w-full mt-0.5 border border-gray-200 rounded-lg px-2 py-1.5 text-xs focus:outline-none focus:border-brand-500"
                        />
                      </div>
                      {/* Remove slot row */}
                      <button
                        onClick={() => removeSlotRow(idx)}
                        disabled={restForm.timeSlots.length === 1}
                        className="text-red-400 text-xs hover:text-red-600 disabled:opacity-30 pb-1 text-center">
                        ✕
                      </button>
                    </div>
                  ))}
                </div>
              </div>

              {/* Submit single cascading request */}
              <button
                onClick={handleCreateRestaurant}
                disabled={creating}
                className="w-full bg-brand-500 text-white font-medium py-3 rounded-xl hover:bg-brand-600 transition-colors disabled:opacity-60 text-sm">
                {creating ? 'Creating...' : '🍽️ Create Restaurant (Single API Call)'}
              </button>
            </div>
          )}

          {/* Existing restaurants grid — split into ACTIVE and UNLISTED */}
          {restaurants.length === 0 && !showCreateForm ? (
            <div className="text-center py-8 text-gray-400">
              <p className="text-sm">No restaurants yet. Click "Add Restaurant" to create one.</p>
            </div>
          ) : (
            <>
              {/* ── Active restaurants ── */}
              {restaurants.filter(r => !r.deleted).length > 0 && (
                <>
                  <p className="text-xs text-gray-400 uppercase tracking-wide mb-3 font-medium">
                    Active ({restaurants.filter(r => !r.deleted).length})
                  </p>
                  <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 mb-6">
                    {restaurants.filter(r => !r.deleted).map(r => (
                      <div
                        key={r.id}
                        onClick={() => setSelectedRestId(r.id)}
                        className={`p-4 rounded-xl border-2 cursor-pointer transition-all
                          ${selectedRestId === r.id ? 'border-brand-500 bg-red-50' : 'border-gray-200 hover:border-gray-300'}`}
                      >
                        <div className="flex items-start justify-between gap-2">
                          <div className="flex-1 min-w-0">
                            <p className="font-semibold text-gray-900">{r.name}</p>
                            <p className="text-sm text-gray-500 mt-0.5">{r.address}, {r.city}</p>
                            <p className="text-xs text-gray-400 mt-1">📞 {r.contactNumber}</p>
                          </div>
                          <span className="shrink-0 text-xs font-bold px-2 py-0.5 rounded-full border text-green-700 bg-green-50 border-green-200">
                            OPEN
                          </span>
                        </div>

                        <div className="flex gap-2 mt-3">
                          {/* Toggle open/closed — does NOT affect visibility */}
                          <button
                            onClick={(e) => handleToggleStatus(e, r.id)}
                            disabled={togglingId === r.id}
                            className="flex-1 text-xs font-medium py-1.5 rounded-lg border border-red-200 text-red-600 hover:bg-red-50 transition-colors disabled:opacity-50">
                            {togglingId === r.id ? 'Updating...' : 'Mark as Closed'}
                          </button>
                          {/* Soft-delete — hides from customers */}
                          <button
                            onClick={(e) => handleSoftDelete(e, r.id)}
                            disabled={softActionId === r.id}
                            className="flex-1 text-xs font-medium py-1.5 rounded-lg border border-gray-300 text-gray-600 hover:bg-gray-50 transition-colors disabled:opacity-50">
                            {softActionId === r.id ? 'Unlisting...' : '🚫 Unlist'}
                          </button>
                        </div>
                      </div>
                    ))}
                  </div>
                </>
              )}

              {/* ── Unlisted (soft-deleted) restaurants ── */}
              {restaurants.filter(r => r.deleted).length > 0 && (
                <>
                  <p className="text-xs text-gray-400 uppercase tracking-wide mb-3 font-medium">
                    Unlisted / Hidden ({restaurants.filter(r => r.deleted).length})
                  </p>
                  <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                    {restaurants.filter(r => r.deleted).map(r => (
                      <div key={r.id}
                        className="p-4 rounded-xl border-2 border-dashed border-gray-200 opacity-70">
                        <div className="flex items-start justify-between gap-2">
                          <div className="flex-1 min-w-0">
                            <p className="font-semibold text-gray-600">{r.name}</p>
                            <p className="text-sm text-gray-400 mt-0.5">{r.address}, {r.city}</p>
                          </div>
                          <span className="shrink-0 text-xs font-bold px-2 py-0.5 rounded-full border text-gray-500 bg-gray-100 border-gray-300">
                            UNLISTED
                          </span>
                        </div>
                        <p className="text-xs text-gray-400 mt-2">
                          This restaurant is hidden from customers. Click Restore to relist it.
                        </p>
                        {/* Restore — brings restaurant back to Home page */}
                        <button
                          onClick={(e) => handleRestore(e, r.id)}
                          disabled={softActionId === r.id}
                          className="mt-3 w-full text-xs font-medium py-1.5 rounded-lg border border-green-200 text-green-700 hover:bg-green-50 transition-colors disabled:opacity-50">
                          {softActionId === r.id ? 'Restoring...' : '✅ Restore / Relist'}
                        </button>
                      </div>
                    ))}
                  </div>
                </>
              )}
            </>
          )}
        </div>

        {/* ── Reservation Metrics ── */}
        {selectedRestId && (
          <div className="bg-white rounded-2xl border border-gray-100 shadow-sm p-6">
            <h2 className="font-display text-lg font-bold text-gray-900 mb-4">
              Reservations — {restaurants.find(r => r.id === selectedRestId)?.name}
            </h2>

            {/* Status filter tabs */}
            <div className="flex gap-2 mb-5 flex-wrap">
              {['all', 'PENDING', 'CONFIRMED', 'COMPLETED', 'CANCELLED'].map(s => (
                <button key={s} onClick={() => setStatusFilter(s)}
                  className={`px-3 py-1.5 rounded-full text-xs font-medium capitalize transition-all
                    ${statusFilter === s ? 'bg-brand-500 text-white' : 'bg-gray-100 text-gray-600 hover:bg-gray-200'}`}>
                  {s.toLowerCase()}
                </button>
              ))}
            </div>

            {filteredBookings.length === 0 ? (
              <p className="text-center text-gray-400 text-sm py-8">No bookings match this filter.</p>
            ) : (
              <>
                {/* Toast shown briefly after a status change */}
                {statusToast && (
                  <div className="mb-3 bg-green-50 border border-green-200 text-green-700 text-xs px-4 py-2 rounded-xl">
                    ✓ {statusToast}
                  </div>
                )}
              <div className="overflow-x-auto">
                <table className="w-full text-sm">
                  <thead>
                    <tr className="text-xs text-gray-400 uppercase border-b border-gray-100">
                      <th className="py-2 text-left">Ref</th>
                      <th className="py-2 text-left">Customer</th>
                      <th className="py-2 text-left">Date</th>
                      <th className="py-2 text-left">Slot</th>
                      <th className="py-2 text-left">Guests</th>
                      <th className="py-2 text-left">Table</th>
                      <th className="py-2 text-left">Status</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-gray-50">
                    {filteredBookings.map(b => (
                      <tr key={b.id}>
                        <td className="py-2.5 text-gray-500 text-xs">{b.bookingReference}</td>
                        <td className="py-2.5 text-xs text-gray-700">{b.userFullName}</td>
                        <td className="py-2.5">{b.bookingDate}</td>
                        <td className="py-2.5 text-gray-600">{b.timeSlotLabel}</td>
                        <td className="py-2.5">{b.partySize}</td>
                        <td className="py-2.5">#{b.tableNumber}</td>
                        <td className="py-2.5">
                          {/*
                            Status dropdown — owner can change any non-terminal booking.
                            onChange fires handleStatusChange immediately (no confirm needed
                            since owners are professionals managing their restaurant).
                            The select is disabled while an update is in-flight.
                          */}
                          <select
                            value={b.status}
                            disabled={updatingBookingId === b.id}
                            onChange={e => handleStatusChange(b.id, e.target.value)}
                            className="text-xs border border-gray-200 rounded-lg px-2 py-1 bg-white focus:outline-none focus:border-brand-500 disabled:opacity-50 disabled:cursor-not-allowed"
                          >
                            {['PENDING','CONFIRMED','COMPLETED','CANCELLED','NO_SHOW','REJECTED'].map(s => (
                              <option key={s} value={s}>{s}</option>
                            ))}
                          </select>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
              </>
            )}
          </div>
        )}

      </div>
    </div>
  )
}
