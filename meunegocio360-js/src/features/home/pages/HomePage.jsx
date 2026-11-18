import { useState } from 'react'
import Button from 'react-bootstrap/Button'
import Card from 'react-bootstrap/Card'
import Container from 'react-bootstrap/Container'
import Spinner from 'react-bootstrap/Spinner'
import { useAuth, useUsuarioAtual } from '../../auth/index.js'

const NOMES_PERFIL = { ADMIN: 'Administrador', USUARIO: 'Usuário' }

/**
 * Página inicial provisória (RF-FE-001, rota "/"): saudação, perfil e botão Sair.
 * Será substituída pelo dashboard em requisito futuro.
 */
export default function HomePage() {
  const { usuario: usuarioDaSessao, sair } = useAuth()
  // Chamada autenticada real (GET /auth/me): exercita a renovação transparente diante de 401.
  const { data: usuarioAtual } = useUsuarioAtual()
  const usuario = usuarioAtual ?? usuarioDaSessao
  const [saindo, setSaindo] = useState(false)

  async function aoSair() {
    setSaindo(true)
    await sair()
  }

  return (
    <main className="py-5">
      <title>Início | MeuNegócio360</title>
      <Container>
        <Card className="shadow-sm">
          <Card.Body className="p-4">
            <h1 className="mb-2">Olá, {usuario?.nome}!</h1>
            <p className="text-body-secondary mb-4">
              Perfil: {NOMES_PERFIL[usuario?.perfil] ?? usuario?.perfil}
            </p>
            <Button variant="outline-dark" size="lg" onClick={aoSair} disabled={saindo}>
              {saindo ? (
                <>
                  <Spinner
                    as="span"
                    animation="border"
                    size="sm"
                    aria-hidden="true"
                    className="me-2"
                  />
                  Saindo…
                </>
              ) : (
                'Sair'
              )}
            </Button>
          </Card.Body>
        </Card>
      </Container>
    </main>
  )
}
