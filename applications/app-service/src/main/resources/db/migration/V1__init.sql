CREATE TABLE IF NOT EXISTS roles (
    role_id     SERIAL PRIMARY KEY,
    name      VARCHAR(20)  NOT NULL UNIQUE
        CHECK (name IN ('ADMIN','ASESOR','CLIENTE')),
    description VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS usuarios (
    user_id      BIGSERIAL PRIMARY KEY,
    name         VARCHAR(80)  NOT NULL,
    last_name    VARCHAR(80)  NOT NULL,
    document     VARCHAR(30)  NOT NULL UNIQUE,
    birth_date   DATE,
    address      VARCHAR(120),
    phone        VARCHAR(30),
    email        VARCHAR(120) NOT NULL UNIQUE,
    base_salary  NUMERIC(15,2),
    password     VARCHAR(120) NOT NULL,          -- BCrypt hash
    role_id      INTEGER     NOT NULL REFERENCES roles(role_id),
    activo       BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_usuarios_email ON usuarios (email);
CREATE INDEX IF NOT EXISTS idx_usuarios_role  ON usuarios (role_id);

INSERT INTO roles (name, description) VALUES
('ADMIN',   'Administrador del sistema'),
('ASESOR',  'Asesor de crédito'),
('CLIENTE', 'Cliente final')
ON CONFLICT (name) DO NOTHING;


INSERT INTO public.usuarios
(name, last_name, document, birth_date, address, phone, email, base_salary, password, role_id, activo, created_at)
VALUES
    ('Alicia','Admin',   '100000001','1990-01-01','Calle 1 #1-01','300000001','admin@gmail.com',   12000000.00,'$2b$10$.Ca4xrK1kQmkO.slWHrwc.RHYiGtLG8G/a.rL4f1NnlSNwfNSSoT6',1,TRUE,DEFAULT),
    ('Andres','Asesor',  '200000002','1992-02-02','Calle 2 #2-02','300000002','asesor@gmail.com',   8000000.00,'$2b$10$8UJIAHsVK8sVUvwKW5fzTuMDj3b9OEnqbV1AVh1HimV89Z5GoBWqW',2,TRUE,DEFAULT),
    ('Camila','Cliente', '300000003','1995-03-03','Calle 3 #3-03','300000003','cliente@gmail.com',  3500000.00,'$2b$10$sUJH42rH3MYlwZY9Be6OhuoS4H0vYP07W8mBVyXqpfvMOvWU874hG',3,TRUE,DEFAULT),
    ('Carol','velez','123',    '1996-04-10','Cra 1 # 23-45','444','carolvelez@gmail.com',  8000000.00,'$2a$10$z9PqLfvH6NxFBFh6FFiZYu9UpewRRERYy4XUu3uIzoM5JdgqctyhW',3,TRUE,DEFAULT),
    ('Carol','velez','1234',   '1996-04-10','Cra 1 # 23-45','444','carolvelez1@gmail.com', 8000000.00,'$2a$10$z9PqLfvH6NxFBFh6FFiZYu9UpewRRERYy4XUu3uIzoM5JdgqctyhW',3,TRUE,DEFAULT),
    ('Carol','velez','12345',  '1996-04-10','Cra 1 # 23-45','444','carolvelez2@gmail.com', 8000000.00,'$2a$10$z9PqLfvH6NxFBFh6FFiZYu9UpewRRERYy4XUu3uIzoM5JdgqctyhW',3,TRUE,DEFAULT),
    ('Carol','velez','123456', '1996-04-10','Cra 1 # 23-45','444','carolvelez3@gmail.com', 8000000.00,'$2a$10$z9PqLfvH6NxFBFh6FFiZYu9UpewRRERYy4XUu3uIzoM5JdgqctyhW',3,TRUE,DEFAULT),
    ('Carol','velez','1234567','1996-04-10','Cra 1 # 23-45','444','carolvelez4@gmail.com', 8000000.00,'$2a$10$z9PqLfvH6NxFBFh6FFiZYu9UpewRRERYy4XUu3uIzoM5JdgqctyhW',3,TRUE,DEFAULT),
    ('Carol','velez','12345678','1996-04-10','Cra 1 # 23-45','444','carolvelez5@gmail.com', 8000000.00,'$2a$10$z9PqLfvH6NxFBFh6FFiZYu9UpewRRERYy4XUu3uIzoM5JdgqctyhW',3,TRUE,DEFAULT)
    ON CONFLICT DO NOTHING;
