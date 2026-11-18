import { zodResolver } from '@hookform/resolvers/zod'
import { useEffect, useRef, useState } from 'react'
import Alert from 'react-bootstrap/Alert'
import Button from 'react-bootstrap/Button'
import Form from 'react-bootstrap/Form'
import Spinner from 'react-bootstrap/Spinner'
import { useForm } from 'react-hook-form'
import { ApiError } from '../../../shared/lib/http.js'
import { useLogin } from '../hooks/useLogin.js'
import { loginSchema } from '../schemas.js'

const BLOQUEIO_PADRAO_SEGUNDOS = 15 * 60
const MENSAGEM_CONEXAO = 'Não foi possível entrar agora. Verifique sua conexão e tente novamente.'

/** Instante (ms) em que termina o bloqueio informado pelo Retry-After. */
function fimDoBloqueio(segundos) {
  return Date.now() + segundos * 1000
}

function textoEspera(segundos) {
  const minutos = Math.max(1, Math.ceil(segundos / 60))
  return minutos === 1 ? '1 minuto' : `${minutos} minutos`
}

/**
 * Formulário de login (RF-FE-001): React Hook Form + Zod com a marcação do Bootstrap
 * (RNF-FE-20), rótulos e mensagens acessíveis (RNF-FE-21, RNF-FE-22) e decisões pelo `code`
 * do erro, nunca pelo texto (RNF-FE-14).
 */
export function LoginForm({ aviso }) {
  const {
    register,
    handleSubmit,
    setError,
    resetField,
    setFocus,
    reset,
    formState: { errors },
  } = useForm({
    resolver: zodResolver(loginSchema),
    defaultValues: { email: '', senha: '' },
    // Valida no envio e, depois do primeiro envio, a cada alteração; foca o 1º campo inválido.
    mode: 'onSubmit',
    reValidateMode: 'onChange',
    shouldFocusError: true,
  })
  const login = useLogin()
  const [alerta, setAlerta] = useState(null)
  const [bloqueadoAte, setBloqueadoAte] = useState(null)
  const alertaRef = useRef(null)

  // Reabilita o envio ao fim do bloqueio informado pelo Retry-After.
  useEffect(() => {
    if (!bloqueadoAte) {
      return undefined
    }
    const temporizador = setTimeout(
      () => {
        setBloqueadoAte(null)
        setAlerta(null)
      },
      Math.max(bloqueadoAte - Date.now(), 0),
    )
    return () => clearTimeout(temporizador)
  }, [bloqueadoAte])

  // Leva o foco à mensagem quando ela é a informação principal (RNF-FE-22).
  useEffect(() => {
    if (alerta?.focar) {
      alertaRef.current?.focus()
    }
  }, [alerta])

  function tratarErro(erro) {
    const codigo = erro instanceof ApiError ? erro.code : 'NETWORK_ERROR'

    if (codigo === 'VALIDATION_ERROR') {
      const campos = erro.errors.filter(({ field }) => field === 'email' || field === 'senha')
      if (campos.length > 0) {
        campos.forEach(({ field, message }) => setError(field, { type: 'server', message }))
        setFocus(campos[0].field)
      } else {
        setAlerta({ mensagem: 'Verifique os dados informados e tente novamente.', focar: true })
      }
      return
    }

    if (codigo === 'INVALID_CREDENTIALS') {
      // Mesma mensagem para e-mail inexistente e senha errada (RN-03 do backend).
      setAlerta({ mensagem: 'E-mail ou senha inválidos.' })
      resetField('senha')
      setFocus('senha')
      return
    }

    if (codigo === 'ACCOUNT_TEMPORARILY_LOCKED') {
      const segundos = erro.retryAfter ?? BLOQUEIO_PADRAO_SEGUNDOS
      setBloqueadoAte(fimDoBloqueio(segundos))
      setAlerta({
        mensagem: `Muitas tentativas. Tente novamente em ${textoEspera(segundos)}.`,
        focar: true,
      })
      return
    }

    // Rede, timeout, 5xx ou qualquer outro erro inesperado.
    setAlerta({ mensagem: MENSAGEM_CONEXAO, tentarNovamente: true, focar: true })
  }

  function enviar(dados) {
    setAlerta(null)
    login.mutate(dados, {
      onSuccess: () => reset(),
      onError: tratarErro,
    })
  }

  const enviarFormulario = handleSubmit(enviar)
  const bloqueado = bloqueadoAte !== null
  const desabilitado = login.isPending || bloqueado
  const mensagemTopo = alerta?.mensagem ?? aviso

  return (
    <Form noValidate onSubmit={enviarFormulario}>
      {mensagemTopo && (
        <Alert
          ref={alertaRef}
          variant={alerta ? 'danger' : 'warning'}
          role="alert"
          tabIndex={-1}
          className="d-flex flex-column gap-2"
        >
          <span>{mensagemTopo}</span>
          {alerta?.tentarNovamente && (
            <Button
              variant="outline-dark"
              className="align-self-start"
              onClick={() => enviarFormulario()}
              disabled={desabilitado}
            >
              Tentar novamente
            </Button>
          )}
        </Alert>
      )}

      <Form.Group className="mb-3" controlId="login-email">
        <Form.Label>E-mail</Form.Label>
        <Form.Control
          type="email"
          size="lg"
          autoComplete="username"
          inputMode="email"
          isInvalid={Boolean(errors.email)}
          aria-invalid={errors.email ? 'true' : 'false'}
          aria-describedby={errors.email ? 'login-email-erro' : undefined}
          {...register('email')}
        />
        <Form.Control.Feedback type="invalid" id="login-email-erro">
          {errors.email?.message}
        </Form.Control.Feedback>
      </Form.Group>

      <Form.Group className="mb-4" controlId="login-senha">
        <Form.Label>Senha</Form.Label>
        <Form.Control
          type="password"
          size="lg"
          autoComplete="current-password"
          isInvalid={Boolean(errors.senha)}
          aria-invalid={errors.senha ? 'true' : 'false'}
          aria-describedby={errors.senha ? 'login-senha-erro' : undefined}
          {...register('senha')}
        />
        <Form.Control.Feedback type="invalid" id="login-senha-erro">
          {errors.senha?.message}
        </Form.Control.Feedback>
      </Form.Group>

      <Button type="submit" variant="primary" size="lg" className="w-100" disabled={desabilitado}>
        {login.isPending ? (
          <>
            <Spinner as="span" animation="border" size="sm" aria-hidden="true" className="me-2" />
            Entrando…
          </>
        ) : (
          'Entrar'
        )}
      </Button>
    </Form>
  )
}
