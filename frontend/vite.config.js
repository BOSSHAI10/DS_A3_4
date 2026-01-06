import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vitejs.dev/config/
export default defineConfig({
    plugins: [react()],
    server: {
        // Permite accesul din afara containerului
        host: true,
        // Portul intern din container
        port: 5173,
        watch: {
            usePolling: true,
            interval: 1000,
            binaryInterval: 1000,
            ignored: ['**/node_modules/**', '**/.git/**', '**/dist/**', '**/vite.config.js']
        },
        // --- CONFIGURARE HMR OPTIMIZATĂ PENTRU TRAEFIK ---
        hmr: {
            // Forțăm protocolul WebSocket standard (nu wss încă, că nu avem HTTPS)
            protocol: 'ws',
            // Îi spunem explicit browserului să se conecteze la localhost
            host: 'localhost',
            // Portul pe care îl vede browserul (Traefik)
            clientPort: 80
        }
    },
})