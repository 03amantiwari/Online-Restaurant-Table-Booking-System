// Admin dashboard — nested owner→restaurant hierarchy + customer directory
import { useState, useEffect } from 'react'
import { useAuth } from '../context/AuthContext'
import { userService } from '../services/userService'
import { restaurantService } from '../services/restaurantService'

export default function AdminDashboard() {
  const { user } = useAuth()

  // All registered owners fetched from backend
  const [owners,      setOwners]      = useState([])
  // All restaurants — used to show nested under each owner
  const [restaurants, setRestaurants] = useState([])
  // All registered customers
  const [customers,   setCustomers]   = useState([])
  const [loading,     setLoading]     = useState(true)
  const [error,       setError]       = useState(null)

  // Active tab: 'owners' or 'customers'
  const [activeTab, setActiveTab] = useState('owners')

  // Which owner row is expanded to show their restaurants
  const [expandedOwnerId, setExpandedOwnerId] = useState(null)
  // Which restaurant inside an owner is expanded to show tables
  const [expandedRestId,  setExpandedRestId]  = useState(null)
  // Lazy-loaded tables — { [restaurantId]: [table, ...] }
  const [tablesMap, setTablesMap] = useState({})

  // Fetch all owners, restaurants, and customers on mount
  useEffect(() => {
    Promise.all([
      userService.getAllOwners(),
      restaurantService.getAll(),
      userService.getAllCustomers(),
    ])
      .then(([ownersResp, restResp, custResp]) => {
        setOwners(ownersResp.data)
        setRestaurants(restResp.data)
        setCustomers(custResp.data)
      })
      .catch(() => setError('Could not load admin data.'))
      .finally(() => setLoading(false))
  }, [])

  // Toggle owner row accordion open/closed
  const toggleOwner = (ownerId) => {
    setExpandedOwnerId(prev => prev === ownerId ? null : ownerId)
    setExpandedRestId(null)  // collapse any open restaurant when switching owner
  }

  /**
   * Toggle restaurant accordion under an owner.
   * Lazy-loads tables from GET /restaurants/{id}/tables on first expand.
   */
  const toggleRestaurant = async (restId) => {
    if (expandedRestId === restId) { setExpandedRestId(null); return }
    setExpandedRestId(restId)
    if (!tablesMap[restId]) {
      try {
        const { data } = await restaurantService.getTables(restId)
        // Cache in tablesMap so we don't re-fetch on collapse/expand
        setTablesMap(prev => ({ ...prev, [restId]: data }))
      } catch {
        setTablesMap(prev => ({ ...prev, [restId]: [] }))
      }
    }
  }

  if (loading) return <div className="min-h-screen flex items-center justify-center text-gray-400">Loading admin data...</div>
  if (error)   return <div className="min-h-screen flex items-center justify-center text-red-500">{error}</div>

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="max-w-6xl mx-auto px-4 py-8">

        {/* Page heading */}
        <div className="mb-8">
          <h1 className="font-display text-3xl font-bold text-gray-900">Admin Dashboard</h1>
          <p className="text-gray-500 text-sm mt-1">Platform overview — {user?.name}</p>
        </div>

        {/* Summary stat cards */}
        <div className="grid grid-cols-2 sm:grid-cols-4 gap-4 mb-8">
          {[
            { label: 'Owners',      value: owners.length,                                icon: '🏪' },
            { label: 'Restaurants', value: restaurants.length,                           icon: '🍽️' },
            { label: 'Customers',   value: customers.length,                             icon: '🧑' },
            { label: 'Active Rest.',value: restaurants.filter(r => r.active).length,     icon: '✅' },
          ].map(s => (
            <div key={s.label} className="bg-white rounded-2xl border border-gray-100 shadow-sm p-5">
              <p className="text-2xl mb-1">{s.icon}</p>
              <p className="text-2xl font-bold text-gray-900">{s.value}</p>
              <p className="text-sm text-gray-500">{s.label}</p>
            </div>
          ))}
        </div>

        {/* Tab navigation */}
        <div className="flex gap-2 mb-6">
          {[
            { key: 'owners',    label: `Owner Directory (${owners.length})`    },
            { key: 'customers', label: `Customer Directory (${customers.length})` },
          ].map(tab => (
            <button key={tab.key} onClick={() => setActiveTab(tab.key)}
              className={`px-5 py-2.5 rounded-full text-sm font-medium transition-all
                ${activeTab === tab.key
                  ? 'bg-brand-500 text-white'
                  : 'bg-white border border-gray-200 text-gray-600 hover:border-brand-300'}`}>
              {tab.label}
            </button>
          ))}
        </div>

        {/* ── OWNER DIRECTORY TAB ────────────────────────────────────────── */}
        {activeTab === 'owners' && (
          <div className="bg-white rounded-2xl border border-gray-100 shadow-sm p-6">
            <h2 className="font-display text-lg font-bold text-gray-900 mb-4">
              Registered Owners
            </h2>

            {owners.length === 0 ? (
              <p className="text-center text-gray-400 text-sm py-8">No owners registered yet.</p>
            ) : (
              <div className="space-y-3">
                {owners.map(owner => {
                  // Restaurants belonging to this owner
                  const ownerRests = restaurants.filter(r => r.ownerUserId === owner.id)
                  const isOwnerOpen = expandedOwnerId === owner.id

                  return (
                    <div key={owner.id} className="border border-gray-100 rounded-xl overflow-hidden">

                      {/* Owner row — click to expand restaurant list */}
                      <div
                        onClick={() => toggleOwner(owner.id)}
                        className="p-4 cursor-pointer hover:bg-gray-50 flex items-center justify-between"
                      >
                        <div>
                          <p className="font-semibold text-gray-900">{owner.fullName}</p>
                          <p className="text-sm text-gray-500">{owner.email} · {owner.phoneNumber}</p>
                          <p className="text-xs text-gray-400 mt-0.5">
                            ID: {owner.id} · {ownerRests.length} restaurant{ownerRests.length !== 1 ? 's' : ''}
                          </p>
                        </div>
                        <span className="text-gray-400 text-sm ml-4">{isOwnerOpen ? '▲' : '▼'}</span>
                      </div>

                      {/* Expanded: list of this owner's restaurants */}
                      {isOwnerOpen && (
                        <div className="border-t border-gray-100 bg-gray-50 px-4 py-3 space-y-2">
                          {ownerRests.length === 0 ? (
                            <p className="text-sm text-gray-400 py-2">No restaurants owned yet.</p>
                          ) : (
                            ownerRests.map(r => {
                              const isRestOpen = expandedRestId === r.id
                              const tables = tablesMap[r.id] ?? []

                              return (
                                <div key={r.id} className="bg-white border border-gray-200 rounded-xl overflow-hidden">

                                  {/* Restaurant row — click to show tables */}
                                  <div
                                    onClick={() => toggleRestaurant(r.id)}
                                    className="p-3 cursor-pointer hover:bg-gray-50 flex items-center justify-between"
                                  >
                                    <div>
                                      <div className="flex items-center gap-2">
                                        <p className="font-medium text-gray-900 text-sm">{r.name}</p>
                                        <span className={`text-xs px-2 py-0.5 rounded-full border
                                          ${r.active ? 'text-green-700 bg-green-50 border-green-200' : 'text-gray-500 bg-gray-50 border-gray-200'}`}>
                                          {r.active ? 'Active' : 'Inactive'}
                                        </span>
                                      </div>
                                      <p className="text-xs text-gray-500 mt-0.5">{r.address}, {r.city}</p>
                                    </div>
                                    <span className="text-gray-400 text-xs ml-2">{isRestOpen ? '▲' : '▼'}</span>
                                  </div>

                                  {/* Expanded: table grid for this restaurant */}
                                  {isRestOpen && (
                                    <div className="border-t border-gray-100 bg-gray-50 p-3">
                                      <p className="text-xs font-medium text-gray-500 uppercase mb-2">
                                        Tables ({tables.length})
                                      </p>
                                      {tables.length === 0 ? (
                                        <p className="text-xs text-gray-400">No tables configured.</p>
                                      ) : (
                                        <div className="grid grid-cols-2 sm:grid-cols-4 gap-2">
                                          {tables.map(t => (
                                            <div key={t.tableId} className="bg-white rounded-lg border border-gray-200 p-2.5 text-xs">
                                              <p className="font-semibold text-gray-800">#{t.tableNumber}</p>
                                              <p className="text-gray-500">{t.seatingCapacity} seats</p>
                                              <p className="text-gray-500 capitalize">{t.locationType?.toLowerCase()}</p>
                                              {t.ratePerSeat > 0 && <p className="text-brand-500 font-medium">₹{t.ratePerSeat}/seat</p>}
                                              <span className={`mt-1 inline-block px-1.5 py-0.5 rounded font-medium
                                                ${t.tableStatus === 'AVAILABLE' ? 'text-green-700 bg-green-50' : 'text-red-700 bg-red-50'}`}>
                                                {t.tableStatus}
                                              </span>
                                            </div>
                                          ))}
                                        </div>
                                      )}
                                    </div>
                                  )}
                                </div>
                              )
                            })
                          )}
                        </div>
                      )}
                    </div>
                  )
                })}
              </div>
            )}
          </div>
        )}

        {/* ── CUSTOMER DIRECTORY TAB ─────────────────────────────────────── */}
        {activeTab === 'customers' && (
          <div className="bg-white rounded-2xl border border-gray-100 shadow-sm p-6">
            <h2 className="font-display text-lg font-bold text-gray-900 mb-4">
              Registered Customers
            </h2>

            {customers.length === 0 ? (
              <p className="text-center text-gray-400 text-sm py-8">No customers registered yet.</p>
            ) : (
              <div className="overflow-x-auto">
                <table className="w-full text-sm">
                  <thead>
                    <tr className="text-xs text-gray-400 uppercase border-b border-gray-100">
                      <th className="py-2 text-left">ID</th>
                      <th className="py-2 text-left">Name</th>
                      <th className="py-2 text-left">Email</th>
                      <th className="py-2 text-left">Phone</th>
                      <th className="py-2 text-left">Date of Birth</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-gray-50">
                    {customers.map(c => (
                      <tr key={c.id} className="hover:bg-gray-50">
                        <td className="py-2.5 text-gray-400 text-xs">{c.id}</td>
                        <td className="py-2.5 font-medium text-gray-900">{c.fullName}</td>
                        <td className="py-2.5 text-gray-600">{c.email}</td>
                        <td className="py-2.5 text-gray-600">{c.phoneNumber}</td>
                        <td className="py-2.5 text-gray-500 text-xs">{c.dateOfBirth || '—'}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        )}

      </div>
    </div>
  )
}
