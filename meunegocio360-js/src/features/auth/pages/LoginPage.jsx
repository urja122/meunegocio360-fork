import Card from 'react-bootstrap/Card'
import Col from 'react-bootstrap/Col'
import Container from 'react-bootstrap/Container'
import Row from 'react-bootstrap/Row'
import { Navigate, useLocation } from 'react-router'
import logo from '../../../assets/logo-meunegocio360.png'
import { LoginForm } from '../components/LoginForm.jsx'
import { useAuth } from '../hooks/useAuth.js'

/**
 * Evita redirecionamento aberto: só aceita caminhos internos que começam com "/" (e não "//").
 * (RF-FE-001, "Pontos de atenção")
 */
function destinoSeguro(origem) {
  const caminho = origem?.pathname
  if (
    typeof caminho !== 'string' ||
    !caminho.startsWith('/') ||
    caminho.startsWith('//') ||
    caminho === '/login'
  ) {
    return '/'
  }
  return `${caminho}${origem.search ?? ''}${origem.hash ?? ''}`
}

/** Tela /login (RF-FE-001, "Telas e rotas"), centralizada em um card, mobile-first (RNF-FE-26). */
export default function LoginPage() {
  const { status, aviso } = useAuth()
  const location = useLocation()

  // Já autenticado (inclusive logo após o login): vai para a rota de origem ou para "/".
  if (status === 'autenticado') {
    return <Navigate to={destinoSeguro(location.state?.from)} replace />
  }

  return (
    <main className="min-vh-100 d-flex align-items-center py-4">
      <title>Entrar | MeuNegócio360</title>
      <Container>
        <Row className="justify-content-center">
          <Col xs={12} sm={10} md={7} lg={5} xl={4}>
            <Card className="shadow-sm">
              <Card.Body className="p-4">
                <img src={logo} alt="MeuNegócio360" className="login-logo mb-3" />
                <h1 className="h2 text-center mb-4">Entrar</h1>
                <LoginForm aviso={aviso} />
              </Card.Body>
            </Card>
          </Col>
        </Row>
      </Container>
    </main>
  )
}
