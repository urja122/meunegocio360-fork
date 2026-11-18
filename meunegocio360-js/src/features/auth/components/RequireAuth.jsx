import { Navigate, useLocation } from 'react-router'
import { useAuth } from '../hooks/useAuth.js'

/**
 * Guarda de rota (RNF-FE-13): sem sessão, envia ao /login preservando a rota de origem.
 * É só UX; a autorização real é do backend.
 */
export function RequireAuth({ children }) {
  const { status } = useAuth()
  const location = useLocation()

  if (status !== 'autenticado') {
    return <Navigate to="/login" replace state={{ from: location }} />
  }
  return children
}
