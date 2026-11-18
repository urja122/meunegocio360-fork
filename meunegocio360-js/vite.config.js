import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    // RNF-FE-10 / RNF-103: o navegador chama a API pela mesma origem (/api);
    // em desenvolvimento o Vite repassa ao backend local.
    proxy: {
      '/api': 'http://localhost:8080',
    },
  },
  css: {
    preprocessorOptions: {
      scss: {
        // Oculta avisos de depreciação do Sass vindos do código do Bootstrap 5.3 (node_modules).
        quietDeps: true,
        silenceDeprecations: ['import'],
      },
    },
  },
})
