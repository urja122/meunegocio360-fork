import { useQuery, useQueryClient } from '@tanstack/react-query'
import { useCallback, useEffect, useMemo, useState } from 'react'
import { useNavigate } from 'react-router'
import { ApiError, configurarAutenticacao } from '../../../shared/lib/http.js'
import { encerrarSessao, renovarSessao } from '../api/authApi.js'
import { AuthContext, CHAVE_SESSAO } from '../authContext.js'

const RENOVAR_ANTES_MS = 60_000
const MENSAGEM_EXPIRADA = 'Sua sessão expirou. Entre novamente.'

/**
 * Restaura a sessão na inicialização. Somente 401 significa "sem sessão"; falha de rede ou 5xx
 * vira estado de erro, sem descartar nada (RF-FE-001, "Comportamentos e mensagens").
 */
async function restaurarSessao() {
  try {
    return await renovarSessao()
  } catch (erro) {
    if (erro instanceof ApiError && erro.status === 401) {
      return null
    }
    throw erro
  }
}

/**
 * Sessão do usuário (RF-FE-001, "Ciclo de vida da sessão"). O token de acesso fica apenas em
 * memória, no cache do TanStack Query; nada vai para localStorage/sessionStorage (RNF-FE-12).
 */
export function AuthProvider({ children }) {
  const queryClient = useQueryClient()
  const navigate = useNavigate()
  const [aviso, setAviso] = useState(null)

  // 1. Inicialização: POST /auth/refresh com o cookie HttpOnly (o JavaScript nunca o lê).
  const restauracao = useQuery({
    queryKey: CHAVE_SESSAO,
    queryFn: restaurarSessao,
    retry: false,
    staleTime: Infinity,
    gcTime: Infinity,
    refetchOnWindowFocus: false,
    refetchOnReconnect: false,
  })
  const sessao = restauracao.data ?? null

  const limparSessao = useCallback(() => {
    queryClient.removeQueries({ predicate: (query) => query.queryKey[0] !== CHAVE_SESSAO[0] })
    queryClient.setQueryData(CHAVE_SESSAO, null)
  }, [queryClient])

  // 3. Renovação usada pelo cliente HTTP diante de 401 e pela renovação preventiva.
  const renovar = useCallback(async () => {
    try {
      const nova = await renovarSessao()
      queryClient.setQueryData(CHAVE_SESSAO, nova)
      return nova.accessToken
    } catch (erro) {
      if (erro instanceof ApiError && erro.status === 401) {
        setAviso(MENSAGEM_EXPIRADA)
        limparSessao()
      }
      throw erro
    }
  }, [queryClient, limparSessao])

  useEffect(
    () =>
      configurarAutenticacao({
        obterToken: () => queryClient.getQueryData(CHAVE_SESSAO)?.accessToken ?? null,
        renovar,
      }),
    [queryClient, renovar],
  )

  // 4. Renovação preventiva cerca de 60 s antes de expirar; cancelada ao sair ou desmontar.
  const expiraEm = sessao?.expiraEm
  useEffect(() => {
    if (!expiraEm) {
      return undefined
    }
    const atraso = Math.max(expiraEm - Date.now() - RENOVAR_ANTES_MS, 0)
    const temporizador = setTimeout(() => {
      renovar().catch(() => {})
    }, atraso)
    return () => clearTimeout(temporizador)
  }, [expiraEm, renovar])

  // 2. Login bem-sucedido.
  const iniciarSessao = useCallback(
    (novaSessao) => {
      setAviso(null)
      queryClient.setQueryData(CHAVE_SESSAO, novaSessao)
    },
    [queryClient],
  )

  // 5. Logout: falha de rede é ignorada; descarta token e cache e vai para /login.
  const sair = useCallback(async () => {
    try {
      await encerrarSessao()
    } catch {
      // Ignorado de propósito (RF-FE-001, ciclo de vida, item 5).
    }
    setAviso(null)
    limparSessao()
    navigate('/login', { replace: true })
  }, [limparSessao, navigate])

  let status = 'anonimo'
  if (restauracao.isPending) {
    status = 'carregando'
  } else if (sessao) {
    status = 'autenticado'
  } else if (restauracao.isError) {
    status = 'erro'
  }

  const { refetch } = restauracao
  const valor = useMemo(
    () => ({
      status,
      usuario: sessao?.usuario ?? null,
      aviso,
      iniciarSessao,
      sair,
      tentarNovamente: refetch,
    }),
    [status, sessao, aviso, iniciarSessao, sair, refetch],
  )

  return <AuthContext value={valor}>{children}</AuthContext>
}
