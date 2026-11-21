import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// In development, /api calls go to the Spring Boot backend on port 8080.
export default defineConfig({
  test: { environment: 'jsdom', globals: true, setupFiles: './src/test-setup.js' },
  plugins: [react()],
  server: { proxy: { '/api': 'http://localhost:8080' } },
})
