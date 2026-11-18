import { useContext } from 'react'
import { AuthContext } from '../authContext.js'

/** Estado e ações da sessão: { status, usuario, aviso, iniciarSessao, sair, tentarNovamente }. */
export function useAuth() {
  const contexto = useContext(AuthContext)
  if (!contexto) {
    throw new Error('useAuth deve ser usado dentro de <AuthProvider>.')
  }
  return contexto
}
