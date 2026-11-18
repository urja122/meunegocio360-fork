// API pública da feature "home" (RNF-FE-01).
import { lazy } from 'react'

// Página carregada sob demanda (code splitting por rota, RNF-FE-05).
export const HomePage = lazy(() => import('./pages/HomePage.jsx'))
