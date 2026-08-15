import { useState, useEffect } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { mockCuisines, mockArticles } from '../dummy/dummyData'
import { restaurantService } from '../services/restaurantService'

function StarRating({ rating }) {
  return (
    <span className="flex items-center gap-1">
      <span className="text-yellow-400 text-sm">★</span>
      <span className="text-sm font-medium text-gray-700">{rating}</span>
    </span>
  )
}

function RestaurantCard({ restaurant }) {
  const navigate = useNavigate()
  return (
    <div
      className="card-hover bg-white rounded-2xl overflow-hidden border border-gray-100 cursor-pointer"
      onClick={() => navigate(`/restaurant/${restaurant.id}/book`)}
    >
      <div className="relative">
        <img src={restaurant.image} alt={restaurant.name} className="w-full h-48 object-cover" />
        {restaurant.discount && (
          <span className="absolute top-3 left-3 bg-brand-500 text-white text-xs font-bold px-2.5 py-1 rounded-lg">
            {restaurant.discount}
          </span>
        )}
        {!restaurant.isOpen && (
          <div className="absolute inset-0 bg-black/50 flex items-center justify-center">
            <span className="text-white font-medium text-sm bg-black/60 px-3 py-1 rounded-full">Currently Closed</span>
          </div>
        )}
      </div>
      <div className="p-4">
        <div className="flex items-start justify-between mb-1">
          <h3 className="font-display font-semibold text-gray-900 text-base leading-tight">{restaurant.name}</h3>
          <StarRating rating={restaurant.rating} />
        </div>
        <p className="text-sm text-gray-500 mb-2">{restaurant.cuisine}</p>
        <div className="flex items-center justify-between text-xs text-gray-400">
          <span>{restaurant.avgCost}</span>
          <span className="flex items-center gap-1">
            🕐 {restaurant.deliveryTime}
          </span>
        </div>
        <div className="flex gap-1.5 mt-3 flex-wrap">
          {restaurant.tags.map(tag => (
            <span key={tag} className="text-xs bg-gray-100 text-gray-600 px-2 py-0.5 rounded-full">{tag}</span>
          ))}
        </div>
      </div>
    </div>
  )
}

function CuisineTile({ cuisine }) {
  const [clicked, setClicked] = useState(false)
  return (
    <button
      onClick={() => { setClicked(!clicked); console.log('Filter cuisine:', cuisine.name) }}
      className={`flex flex-col items-center gap-2 p-4 rounded-2xl border-2 transition-all duration-200 cursor-pointer w-full
        ${clicked ? 'border-brand-500 bg-red-50' : 'border-gray-100 bg-white hover:border-gray-200 hover:shadow-sm'}`}
    >
      <span className="text-3xl">{cuisine.emoji}</span>
      <div className="text-center">
        <p className="text-sm font-medium text-gray-800">{cuisine.name}</p>
        <p className="text-xs text-gray-400">{cuisine.count} places</p>
      </div>
    </button>
  )
}

// Fallback images — Unsplash food photos (reliable CDN, no auth needed)
const FALLBACK_IMAGES = [
  'https://images.unsplash.com/photo-1414235077428-338989a2e8c0?w=600&fit=crop',
  'https://images.unsplash.com/photo-1555396273-367ea4eb4db5?w=600&fit=crop',
  'https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?w=600&fit=crop',
  'https://images.unsplash.com/photo-1424847651672-bf20a4b0982b?w=600&fit=crop',
  'https://images.unsplash.com/photo-1466978913421-dad2ebd01d17?w=600&fit=crop',
  'https://images.unsplash.com/photo-1552566626-52f8b828add9?w=600&fit=crop',
]

function toCardShape(dto, idx) {
  const priceBand = dto.priceBand ?? 2
  return {
    id:           dto.id,
    name:         dto.name,
    cuisine:      dto.city,
    city:         dto.city,    // kept for city-filter comparison
    address:      dto.address,
    image:        FALLBACK_IMAGES[idx % FALLBACK_IMAGES.length],
    rating:       (4.0 + (idx % 5) * 0.1).toFixed(1),
    avgCost:      `₹${priceBand * 300} for two`,
    isOpen:       dto.active ?? true,
    tags:         [dto.city, 'Dine-in'].filter(Boolean),
    discount:     idx === 0 ? '10% OFF' : null,
    deliveryTime: 'Reservation',
  }
}

export default function Home() {
  const [accordionOpen, setAccordionOpen] = useState(null)
  // All restaurants fetched from backend
  const [restaurants, setRestaurants]   = useState([])
  const [loadingRest, setLoadingRest]   = useState(true)
  const [restError,   setRestError]     = useState(null)
  // Search and city filter — applied locally on fetched data
  const [searchQuery,  setSearchQuery]  = useState('')
  const [selectedCity, setSelectedCity] = useState('All')

  useEffect(() => {
    restaurantService.getAll()
      .then(({ data }) => setRestaurants(data.map(toCardShape)))
      .catch(() => setRestError('Could not load restaurants. Is the backend running?'))
      .finally(() => setLoadingRest(false))
  }, [])

  // Filter restaurants by city and name — runs on every keystroke (client-side, no API call)
  const filteredRestaurants = restaurants.filter(r => {
    const matchesCity   = selectedCity === 'All' || r.city?.toLowerCase() === selectedCity.toLowerCase()
    const matchesSearch = r.name.toLowerCase().includes(searchQuery.toLowerCase())
    return matchesCity && matchesSearch
  })

  const faqs = [
    { q: 'How do I book a table?', a: 'Choose your date, time, and number of guests, then pick an available table from our visual grid and confirm your booking.' },
    { q: 'Can I cancel my reservation?', a: 'Yes, cancellations are free up to 2 hours before your reserved time. Head to My Bookings to manage your reservations.' },
    { q: 'Is there a booking fee?', a: 'TableBook is completely free to use. No booking fees, no hidden charges.' },
    { q: 'How do I know my booking is confirmed?', a: 'You\'ll receive a confirmation screen and email immediately after booking. Your booking will appear under My Bookings.' },
  ]

  return (
    <div className="bg-gray-50 min-h-screen">

      {/* Hero Banner */}
      <div className="relative bg-gradient-to-br from-gray-900 via-gray-800 to-brand-700 overflow-hidden">
        <div className="absolute inset-0 opacity-20"
          style={{ backgroundImage: 'url(https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?w=1200&fit=crop)', backgroundSize: 'cover', backgroundPosition: 'center' }}
        />
        <div className="relative max-w-7xl mx-auto px-4 py-16 text-center">
          <p className="text-brand-200 text-sm font-medium uppercase tracking-widest mb-3">Pune's Favourite Table Booking App</p>
          <h1 className="text-white text-4xl md:text-5xl font-display font-bold mb-4 leading-tight">
            Discover & Reserve<br />Your Perfect Table
          </h1>
          <p className="text-gray-300 text-lg mb-8 max-w-xl mx-auto">
            1,200+ restaurants in Pune. Real-time availability. Instant confirmation.
          </p>
          <Link to="/"
            className="inline-block bg-brand-500 text-white px-8 py-3.5 rounded-xl font-medium text-base hover:bg-brand-600 transition-colors shadow-lg">
            Book a Table Now →
          </Link>
        </div>
      </div>

      {/* Quick Filters
      <div className="max-w-7xl mx-auto px-4 py-6">
        <div className="flex gap-3 overflow-x-auto scrollbar-hide pb-1">
          {quickFilters.map(filter => (
            <button
              key={filter.id}
              onClick={() => { setActiveFilter(activeFilter === filter.id ? null : filter.id); console.log('Filter:', filter.label) }}
              className={`flex items-center gap-2 px-5 py-2.5 rounded-full border-2 whitespace-nowrap transition-all text-sm font-medium shrink-0
                ${activeFilter === filter.id ? 'bg-brand-500 border-brand-500 text-white' : 'bg-white border-gray-200 text-gray-700 hover:border-brand-300'}`}
            >
              <span>{filter.icon}</span>
              <span>{filter.label}</span>
              <span className={`text-xs ${activeFilter === filter.id ? 'text-red-100' : 'text-gray-400'}`}>{filter.time}</span>
            </button>
          ))}
        </div>
      </div> */}

      {/* Search + City Filter bar */}
      <section className="max-w-7xl mx-auto px-4 pt-6 pb-2">
        <div className="flex flex-col sm:flex-row gap-3">
          {/* City dropdown — filters restaurants by city field */}
          <select
            value={selectedCity}
            onChange={e => setSelectedCity(e.target.value)}
            className="border border-gray-200 rounded-xl px-4 py-2.5 text-sm text-gray-700 focus:outline-none focus:border-brand-500 bg-white"
          >
            {['All', 'Pune', 'Lucknow', 'Mumbai', 'Delhi'].map(c => (
              <option key={c}>{c}</option>
            ))}
          </select>
          {/* Search input — filters restaurants by name */}
          <input
            type="text"
            value={searchQuery}
            onChange={e => setSearchQuery(e.target.value)}
            placeholder="Search restaurants by name..."
            className="flex-1 border border-gray-200 rounded-xl px-4 py-2.5 text-sm focus:outline-none focus:border-brand-500"
          />
        </div>
      </section>

      {/* Restaurants Near You */}
      <section className="max-w-7xl mx-auto px-4 py-4">
        <div className="flex items-center justify-between mb-5">
          <div>
            <h2 className="text-2xl font-display font-bold text-gray-900">Restaurants Near You</h2>
            <p className="text-gray-500 text-sm mt-0.5">
              {filteredRestaurants.length} result{filteredRestaurants.length !== 1 ? 's' : ''}
              {selectedCity !== 'All' ? ` in ${selectedCity}` : ''}
              {searchQuery ? ` for "${searchQuery}"` : ''}
            </p>
          </div>
        </div>
        {loadingRest && (
          <div className="text-center py-16 text-gray-400">Loading restaurants...</div>
        )}
        {restError && (
          <div className="text-center py-8 text-red-500 text-sm">{restError}</div>
        )}
        {!loadingRest && !restError && restaurants.length === 0 && (
          <div className="text-center py-8 text-gray-400">No restaurants found yet.</div>
        )}
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-5">
          {filteredRestaurants.map(r => <RestaurantCard key={r.id} restaurant={r} />)}
        </div>
      </section>

      {/* Cuisine Mood Grid */}
      <section className="max-w-7xl mx-auto px-4 py-8">
        <div className="mb-5">
          <h2 className="text-2xl font-display font-bold text-gray-900">What's Your Mood?</h2>
          <p className="text-gray-500 text-sm mt-0.5">Explore by cuisine · Click to filter</p>
        </div>
        <div className="grid grid-cols-4 sm:grid-cols-8 gap-3">
          {mockCuisines.map(c => <CuisineTile key={c.id} cuisine={c} />)}
        </div>
      </section>

      {/* Editorial Feature Cards */}
      <section className="max-w-7xl mx-auto px-4 py-8">
        <div className="flex items-center justify-between mb-5">
          <div>
            <h2 className="text-2xl font-display font-bold text-gray-900">Food Trends</h2>
            <p className="text-gray-500 text-sm mt-0.5">Curated by our food critics</p>
          </div>
        </div>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-5">
          {mockArticles.map(article => (
            <div key={article.id} className="card-hover bg-white rounded-2xl overflow-hidden border border-gray-100 cursor-pointer"
              onClick={() => console.log('Read article:', article.title)}>
              <img src={article.image} alt={article.title} className="w-full h-40 object-cover" />
              <div className="p-4">
                <span className="text-xs text-brand-500 font-medium uppercase tracking-wide">{article.category}</span>
                <h3 className="font-display font-semibold text-gray-900 mt-1 mb-2 leading-tight">{article.title}</h3>
                <div className="flex items-center justify-between text-xs text-gray-400">
                  <span>By {article.author}</span>
                  <span>{article.readTime}</span>
                </div>
              </div>
            </div>
          ))}
        </div>
      </section>

      

      {/* Footer */}
      <footer className="bg-gray-900 text-gray-300 mt-8">
        <div className="max-w-7xl mx-auto px-4 py-12 grid grid-cols-2 md:grid-cols-4 gap-8">
          <div>
            <div className="flex items-center gap-2 mb-4">
              <div className="w-7 h-7 bg-brand-500 rounded-lg flex items-center justify-center">
                <span className="text-white text-xs font-bold">TB</span>
              </div>
              <span className="font-display font-bold text-white text-lg">EasySeat</span>
            </div>
            <p className="text-sm text-gray-500 leading-relaxed">Pune's finest restaurant reservation platform.</p>
          </div>
          {/* Footer nav — uses real routes now that static pages exist */}
          <div>
            <h4 className="text-white font-medium mb-3 text-sm">Company</h4>
            <ul className="space-y-2">
              <li><Link to="/about" className="text-sm text-gray-500 hover:text-white transition-colors">About Us</Link></li>
            </ul>
          </div>
          <div>
            <h4 className="text-white font-medium mb-3 text-sm">Support</h4>
            <ul className="space-y-2">
              <li><Link to="/help"    className="text-sm text-gray-500 hover:text-white transition-colors">Help Center</Link></li>
              <li><Link to="/contact" className="text-sm text-gray-500 hover:text-white transition-colors">Contact Us</Link></li>
            </ul>
          </div>
        </div>
        <div className="border-t border-gray-800 px-4 py-4 max-w-7xl mx-auto flex flex-col sm:flex-row items-center justify-between gap-2">
          <p className="text-xs text-gray-600">© 2026 EasySeat. All rights reserved.</p>
         
        </div>
      </footer>
    </div>
  )
}
