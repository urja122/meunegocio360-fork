// Cliente HTTP único da aplicação (RNF-FE-10, RNF-FE-11, RNF-FE-15).
// Não conhece nenhuma feature (RNF-FE-01): a autenticação é plugada por configurarAutenticacao().

const BASE_URL = import.meta.env.VITE_API_BASE_URL ?? '/api/v1'
const TIMEOUT_MS = 15000

if (!BASE_URL.startsWith('/') || BASE_URL.startsWith('//')) {
  throw new Error('VITE_API_BASE_URL deve ser um caminho relativo, como /api/v1 (RNF-FE-10).')
}

/** Erro tipado a partir de application/problem+json. A lógica do cliente usa `code` (RNF-FE-14). */
export class ApiError extends Error {
  constructor({ status, code, detail = null, errors = [], retryAfter = null }) {
    super(detail ?? code)
    this.name = 'ApiError'
    this.status = status
    this.code = code
    this.detail = detail
    this.errors = errors
    this.retryAfter = retryAfter
  }
}

let autenticacao = {
  obterToken: () => null,
  renovar: null,
}

/**
 * Liga o cliente à sessão: `obterToken()` devolve o token de acesso em memória e `renovar()`
 * renova a sessão e devolve o novo token (ou lança erro). Chamado pelo provedor de autenticação.
 */
export function configurarAutenticacao({ obterToken, renovar }) {
  autenticacao = { obterToken, renovar }
  return () => {
    autenticacao = { obterToken: () => null, renovar: null }
  }
}

// Renovação única e compartilhada (single-flight): várias requisições com 401 ao mesmo tempo
// aguardam a mesma chamada ao /auth/refresh (RNF-FE-15).
let renovacaoEmAndamento = null

function renovarUmaVez() {
  if (!renovacaoEmAndamento) {
    renovacaoEmAndamento = Promise.resolve()
      .then(() => autenticacao.renovar())
      .finally(() => {
        renovacaoEmAndamento = null
      })
  }
  return renovacaoEmAndamento
}

/**
 * Faz uma requisição à API.
 * @param {string} caminho caminho relativo à base, ex.: '/auth/me'
 * @param {object} opcoes
 * @param {boolean} [opcoes.autenticado=true] envia o token de acesso e trata 401 renovando a sessão
 */
export async function request(caminho, { method = 'GET', body, autenticado = true } = {}) {
  let resposta = await enviar(caminho, { method, body, autenticado })

  // Renova uma única vez e repete a requisição original (RNF-FE-11). Requisições não
  // autenticadas (como as de /auth/*) nunca entram aqui, o que evita laço de renovação.
  if (resposta.status === 401 && autenticado && autenticacao.renovar) {
    await renovarUmaVez()
    resposta = await enviar(caminho, { method, body, autenticado })
  }

  return interpretar(resposta)
}

async function enviar(caminho, { method, body, autenticado }) {
  const headers = {
    Accept: 'application/json, application/problem+json',
    'X-Request-Id': crypto.randomUUID(),
  }
  if (body !== undefined) {
    headers['Content-Type'] = 'application/json'
  }
  const token = autenticado ? autenticacao.obterToken() : null
  if (token) {
    headers.Authorization = `Bearer ${token}`
  }

  const controle = new AbortController()
  const temporizador = setTimeout(() => controle.abort(), TIMEOUT_MS)
  try {
    return await fetch(`${BASE_URL}${caminho}`, {
      method,
      headers,
      body: body !== undefined ? JSON.stringify(body) : undefined,
      // Mesma origem: o navegador envia o cookie HttpOnly de refresh aos endpoints /auth/*.
      credentials: 'same-origin',
      signal: controle.signal,
    })
  } catch (erro) {
    if (erro.name === 'AbortError') {
      throw new ApiError({ status: 0, code: 'TIMEOUT' })
    }
    throw new ApiError({ status: 0, code: 'NETWORK_ERROR' })
  } finally {
    clearTimeout(temporizador)
  }
}

async function interpretar(resposta) {
  const tipo = resposta.headers.get('Content-Type') ?? ''
  const corpo = tipo.includes('json') ? await resposta.json().catch(() => null) : null

  if (resposta.ok) {
    return corpo
  }

  const segundos = Number.parseInt(resposta.headers.get('Retry-After') ?? '', 10)
  throw new ApiError({
    status: resposta.status,
    code: corpo?.code ?? `HTTP_${resposta.status}`,
    detail: corpo?.detail ?? null,
    errors: Array.isArray(corpo?.errors) ? corpo.errors : [],
    retryAfter: Number.isFinite(segundos) ? segundos : null,
  })
}
