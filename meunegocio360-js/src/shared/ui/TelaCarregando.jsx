import Spinner from 'react-bootstrap/Spinner'

/** Indicador de carregamento acessível (role="status"), usado na restauração de sessão e no lazy. */
export function TelaCarregando({ mensagem = 'Carregando…' }) {
  return (
    <main className="min-vh-100 d-flex align-items-center justify-content-center p-3">
      <div role="status" className="d-flex align-items-center gap-2">
        <Spinner animation="border" aria-hidden="true" />
        <span>{mensagem}</span>
      </div>
    </main>
  )
}
