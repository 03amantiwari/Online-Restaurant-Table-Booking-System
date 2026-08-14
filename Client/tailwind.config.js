/** @type {import('tailwindcss').Config} */
export default {
  content: ["./index.html", "./src/**/*.{js,ts,jsx,tsx}"],
  theme: {
    extend: {
      colors: {
        brand: { 50:'#fff5f5', 100:'#ffe0e0', 500:'#e23744', 600:'#c0392b', 700:'#a93226' },
        dark: { 900:'#1a1a2e', 800:'#16213e', 700:'#0f3460' }
      },
      fontFamily: {
        display: ['"Playfair Display"', 'serif'],
        body: ['"DM Sans"', 'sans-serif']
      }
    }
  },
  plugins: []
}
