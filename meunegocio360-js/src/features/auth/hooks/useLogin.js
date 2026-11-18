import { useMutation } from '@tanstack/react-query'
import { entrar } from '../api/authApi.js'
import { useAuth } from './useAuth.js'

/** Envio do login (POST /auth/login). Em caso de sucesso, a sessão passa a "autenticado". */
export function useLogin() {
  const { iniciarSessao } = useAuth()
  return useMutation({ mutationFn: entrar, onSuccess: iniciarSessao })
}
