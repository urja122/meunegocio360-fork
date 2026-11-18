// Chamadas HTTP da feature de autenticação (RNF-FE-03), conforme o contrato do RF-BE-001.
import { request } from '../../../shared/lib/http.js'

/** Converte a resposta de login/refresh na sessão mantida apenas em memória (RNF-FE-12). */
function paraSessao(resposta) {
  return {
    accessToken: resposta.accessToken,
    expiraEm: Date.now() + resposta.expiresIn * 1000,
    usuario: resposta.usuario,
  }
}

export async function entrar({ email, senha }) {
  return paraSessao(
    await request('/auth/login', { method: 'POST', body: { email, senha }, autenticado: false }),
  )
}

// Uma única renovação por vez (single-flight): o refresh token é rotativo, e duas chamadas com o
// mesmo cookie derrubariam a sessão por detecção de reuso (RNF-FE-15). Também evita a chamada
// dupla do StrictMode em desenvolvimento.
let renovacaoEmAndamento = null

export function renovarSessao() {
  if (!renovacaoEmAndamento) {
    renovacaoEmAndamento = request('/auth/refresh', { method: 'POST', autenticado: false })
      .then(paraSessao)
      .finally(() => {
        renovacaoEmAndamento = null
      })
  }
  return renovacaoEmAndamento
}

export function encerrarSessao() {
  return request('/auth/logout', { method: 'POST', autenticado: false })
}

export function buscarUsuarioAtual() {
  return request('/auth/me')
}
