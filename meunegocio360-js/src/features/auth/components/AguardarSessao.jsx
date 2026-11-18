import { TelaCarregando } from '../../../shared/ui/TelaCarregando.jsx'
import { TelaErro } from '../../../shared/ui/TelaErro.jsx'
import { useAuth } from '../hooks/useAuth.js'

/**
 * Enquanto a sessão é restaurada, não renderiza telas protegidas nem o login (evita o "flash" da
 * tela de login). Falha de rede/5xx mostra erro com "Tentar novamente", sem ir ao login.
 */
export function AguardarSessao({ children }) {
  const { status, tentarNovamente } = useAuth()

  if (status === 'carregando') {
    return <TelaCarregando mensagem="Verificando sua sessão…" />
  }
  if (status === 'erro') {
    return (
      <TelaErro
        titulo="Não foi possível conectar"
        mensagem="Não conseguimos verificar sua sessão agora. Verifique sua conexão e tente novamente."
        onTentarNovamente={() => tentarNovamente()}
      />
    )
  }
  return children
}
