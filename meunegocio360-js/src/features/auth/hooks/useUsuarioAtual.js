import { useQuery } from '@tanstack/react-query'
import { buscarUsuarioAtual } from '../api/authApi.js'

/** Dados do usuário autenticado (GET /auth/me), uma chamada autenticada real (RNF-FE-04). */
export function useUsuarioAtual() {
  return useQuery({ queryKey: ['usuario-atual'], queryFn: buscarUsuarioAtual })
}
