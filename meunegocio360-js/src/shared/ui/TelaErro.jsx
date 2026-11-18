import Alert from 'react-bootstrap/Alert'
import Button from 'react-bootstrap/Button'
import Col from 'react-bootstrap/Col'
import Container from 'react-bootstrap/Container'
import Row from 'react-bootstrap/Row'

/** Tela de erro com ação de nova tentativa (RNF-062). */
export function TelaErro({ titulo, mensagem, onTentarNovamente }) {
  return (
    <main className="min-vh-100 d-flex align-items-center py-4">
      <title>Erro de conexão | MeuNegócio360</title>
      <Container>
        <Row className="justify-content-center">
          <Col xs={12} sm={10} md={8} lg={6}>
            <h1 className="h3">{titulo}</h1>
            <Alert variant="danger" role="alert">
              {mensagem}
            </Alert>
            <Button variant="primary" size="lg" onClick={onTentarNovamente}>
              Tentar novamente
            </Button>
          </Col>
        </Row>
      </Container>
    </main>
  )
}
