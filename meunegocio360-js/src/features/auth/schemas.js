import { z } from 'zod'

const MENSAGEM_TAMANHO_SENHA = 'A senha deve ter entre 8 e 72 caracteres.'

/**
 * Validação do formulário de login (RF-FE-001, "Campos e validações"). Espelha as regras do
 * backend (RN-01, RN-11 do RF-BE-001), mas não as substitui (RNF-020).
 */
export const loginSchema = z.object({
  email: z
    .string()
    .trim()
    .min(1, { error: 'Informe seu e-mail.' })
    .max(160, { error: 'O e-mail deve ter no máximo 160 caracteres.' })
    .pipe(z.email({ error: 'Informe um e-mail válido.' })),
  senha: z
    .string()
    .min(1, { error: 'Informe sua senha.' })
    .min(8, { error: MENSAGEM_TAMANHO_SENHA })
    .max(72, { error: MENSAGEM_TAMANHO_SENHA }),
})
