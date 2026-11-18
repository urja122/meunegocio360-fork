import { createContext } from 'react'

/** Contexto da sessão (RNF-FE-04). Use o hook useAuth() em vez de consumir diretamente. */
export const AuthContext = createContext(null)

export const CHAVE_SESSAO = ['sessao']
