-- RF-BE-001: refresh tokens rotativos (RN-06 a RN-10).
-- Guarda apenas o hash SHA-256 (hex) do token, nunca o valor em claro (RN-09).
-- atualizado_em e versao seguem RNF-BE-22 (toda tabela).
CREATE TABLE refresh_token (
    id            BINARY(16)  NOT NULL,
    usuario_id    BINARY(16)  NOT NULL,
    familia_id    BINARY(16)  NOT NULL,
    token_hash    CHAR(64)    NOT NULL,
    expira_em     DATETIME(6) NOT NULL,
    revogado_em   DATETIME(6) NULL,
    criado_em     DATETIME(6) NOT NULL,
    atualizado_em DATETIME(6) NOT NULL,
    versao        BIGINT      NOT NULL DEFAULT 0,
    CONSTRAINT pk_refresh_token PRIMARY KEY (id),
    CONSTRAINT uk_refresh_token_token_hash UNIQUE (token_hash),
    CONSTRAINT fk_refresh_token_usuario FOREIGN KEY (usuario_id) REFERENCES usuario (id),
    INDEX idx_refresh_token_usuario_id (usuario_id),
    INDEX idx_refresh_token_familia_id (familia_id),
    INDEX idx_refresh_token_expira_em (expira_em),
    INDEX idx_refresh_token_revogado_em (revogado_em)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;
