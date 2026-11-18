// API pública da feature de autenticação (RNF-FE-01): outras partes do app importam só daqui.
import { lazy } from 'react'

export { AguardarSessao } from './components/AguardarSessao.jsx'
export { AuthProvider } from './components/AuthProvider.jsx'
export { RequireAuth } from './components/RequireAuth.jsx'
export { useAuth } from './hooks/useAuth.js'
export { useUsuarioAtual } from './hooks/useUsuarioAtual.js'

// Página carregada sob demanda (code splitting por rota, RNF-FE-05).
export const LoginPage = lazy(() => import('./pages/LoginPage.jsx'))
