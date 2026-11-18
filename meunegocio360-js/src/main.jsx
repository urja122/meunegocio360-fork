import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
// Fontes auto-hospedadas, apenas os pesos usados (docs/requisitos-fe.md, 2.3)
import '@fontsource/barlow/400.css'
import '@fontsource/barlow/500.css'
import '@fontsource/barlow/600.css'
import '@fontsource/barlow-condensed/500.css'
import '@fontsource/barlow-condensed/600.css'
import '@fontsource/barlow-condensed/700.css'
import './shared/styles/theme.scss'
import App from './app/App.jsx'

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <App />
  </StrictMode>,
)
