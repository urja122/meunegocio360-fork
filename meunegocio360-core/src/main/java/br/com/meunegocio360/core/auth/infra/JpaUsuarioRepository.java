package br.com.meunegocio360.core.auth.infra;

import br.com.meunegocio360.core.auth.domain.Usuario;
import br.com.meunegocio360.core.auth.domain.UsuarioRepository;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Implementação JPA (Spring Data) da porta {@link UsuarioRepository}. */
interface JpaUsuarioRepository extends JpaRepository<Usuario, UUID>, UsuarioRepository {

  @Override
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select u from Usuario u where u.email = :email")
  Optional<Usuario> buscarPorEmailParaAtualizacao(@Param("email") String email);

  boolean existsByEmail(String email);

  @Override
  default Optional<Usuario> buscarPorId(UUID id) {
    return findById(id);
  }

  @Override
  default boolean existePorEmail(String email) {
    return existsByEmail(email);
  }

  @Override
  default Usuario salvar(Usuario usuario) {
    return save(usuario);
  }
}
