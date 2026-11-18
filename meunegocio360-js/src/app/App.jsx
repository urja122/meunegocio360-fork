import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { lazy, Suspense } from 'react'
import { BrowserRouter, Route, Routes } from 'react-router'
import { AguardarSessao, AuthProvider, LoginPage, RequireAuth } from '../features/auth/index.js'
import { HomePage } from '../features/home/index.js'
import { ApiError } from '../shared/lib/http.js'
import { TelaCarregando } from '../shared/ui/TelaCarregando.jsx'

const NotFoundPage = lazy(() => import('./NotFoundPage.jsx'))

// Estado de servidor no TanStack Query (RNF-FE-04). Erros 4xx não são repetidos.
const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      refetchOnWindowFocus: false,
      retry: (tentativas, erro) =>
        tentativas < 1 && !(erro instanceof ApiError && erro.status >= 400 && erro.status < 500),
    },
  },
})

/** Raiz da aplicação: providers, guarda de sessão e rotas (RF-FE-001, "Telas e rotas"). */
export default function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <AuthProvider>
          <AguardarSessao>
            <Suspense fallback={<TelaCarregando />}>
              <Routes>
                <Route path="/login" element={<LoginPage />} />
                <Route
                  path="/"
                  element={
                    <RequireAuth>
                      <HomePage />
                    </RequireAuth>
                  }
                />
                <Route path="*" element={<NotFoundPage />} />
              </Routes>
            </Suspense>
          </AguardarSessao>
        </AuthProvider>
      </BrowserRouter>
    </QueryClientProvider>
  )
}
