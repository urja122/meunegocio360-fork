import Container from 'react-bootstrap/Container'
import { Link } from 'react-router'

/** Rota "*" (RF-FE-001, "Telas e rotas"). */
export default function NotFoundPage() {
  return (
    <main className="py-5">
      <title>Página não encontrada | MeuNegócio360</title>
      <Container>
        <h1>Página não encontrada</h1>
        <p>O endereço acessado não existe.</p>
        <Link to="/">Ir para a página inicial</Link>
      </Container>
    </main>
  )
}
