-- ============================================================
-- PMSuite - Property Management System
-- Database Schema v1.0
-- ============================================================

-- ============================================================
-- TIPOS ENUM
-- ============================================================

DO $$ BEGIN
CREATE TYPE user_role AS ENUM (
        'ADMIN', 'MANAGER', 'RECEPTIONIST', 'HOUSEKEEPER', 'MAINTENANCE', 'RESTAURATION'
    );
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

DO $$ BEGIN
CREATE TYPE room_status AS ENUM (
        'AVAILABLE', 'OCCUPIED', 'OUT_OF_SERVICE', 'BLOCKED', 'CLEANING'
    );
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

DO $$ BEGIN
CREATE TYPE reservation_status AS ENUM (
        'PENDING', 'CONFIRMED', 'CHECKED_IN', 'CHECKED_OUT', 'CANCELLED', 'NO_SHOW'
    );
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

DO $$ BEGIN

CREATE TYPE invoice_status AS ENUM (
        'PENDING', 'PAID', 'PARTIALLY_PAID', 'CANCELLED'
    );
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

DO $$ BEGIN
CREATE TYPE payment_method AS ENUM (
        'CASH', 'CREDIT_CARD', 'DEBIT_CARD', 'BANK_TRANSFER', 'OTHER'
    );
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

DO $$ BEGIN
CREATE TYPE maintenance_priority AS ENUM (
        'LOW', 'MEDIUM', 'HIGH', 'URGENT'
    );
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

DO $$ BEGIN
CREATE TYPE maintenance_status AS ENUM (
        'PENDING', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED'
    );
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

DO $$ BEGIN
CREATE TYPE alert_type AS ENUM (
        'OVERBOOKING', 'MAINTENANCE', 'INVOICE', 'CLEANING', 'CHECKIN', 'CHECKOUT', 'GENERAL'
    );
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

DO $$ BEGIN
CREATE TYPE alert_severity AS ENUM (
        'INFO', 'WARNING', 'ERROR'
    );
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

DO $$ BEGIN
CREATE TYPE order_status AS ENUM (
        'PENDING', 'PREPARING', 'DELIVERED', 'CANCELLED'
    );
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

DO $$ BEGIN
CREATE TYPE incident_status AS ENUM (
        'OPEN', 'IN_PROGRESS', 'RESOLVED', 'CLOSED'
    );
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;


-- ============================================================
-- MÓDULO: MASTERS (Configuración base del sistema)
-- ============================================================

-- Hotel principal
CREATE TABLE IF NOT EXISTS hotel (
                                     id          SERIAL PRIMARY KEY,
                                     name        VARCHAR(100) NOT NULL,
    address     TEXT,
    city        VARCHAR(100),
    postal_code VARCHAR(10),
    country     VARCHAR(100) DEFAULT 'España',
    phone       VARCHAR(20),
    email       VARCHAR(100),
    website     VARCHAR(200),
    nif         VARCHAR(20),
    iban        VARCHAR(50),
    logo_path   VARCHAR(500),
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

-- Usuarios del sistema (personal)
CREATE TABLE IF NOT EXISTS users (
                                     id              SERIAL PRIMARY KEY,
                                     username        VARCHAR(50) UNIQUE NOT NULL,
    email           VARCHAR(100) UNIQUE NOT NULL,
    password_hash   VARCHAR(255) NOT NULL,
    full_name       VARCHAR(100) NOT NULL,
    role            user_role NOT NULL DEFAULT 'RECEPTIONIST',
    active          BOOLEAN DEFAULT TRUE,
    last_login      TIMESTAMP,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

-- Tipos de habitación
CREATE TABLE IF NOT EXISTS room_types (
                                          id              SERIAL PRIMARY KEY,
                                          code            VARCHAR(20) UNIQUE NOT NULL,  -- SGL, DBL, SUITE, etc.
    name            VARCHAR(100) NOT NULL,         -- Single Estándar, Doble Vista Mar, etc.
    description     TEXT,
    max_occupancy   INT NOT NULL DEFAULT 2,
    base_price      NUMERIC(10,2) NOT NULL,
    amenities       TEXT                           -- JSON o lista de servicios
    );

-- Temporadas / Períodos de precio
CREATE TABLE IF NOT EXISTS seasons (
                                       id          SERIAL PRIMARY KEY,
                                       name        VARCHAR(100) NOT NULL,
    start_date  DATE NOT NULL,
    end_date    DATE NOT NULL,
    multiplier  NUMERIC(4,2) DEFAULT 1.00,
    CONSTRAINT chk_season_dates CHECK (end_date > start_date)
    );

-- Planes de tarifas por tipo de habitación y temporada
CREATE TABLE IF NOT EXISTS rate_plans (
                                          id              SERIAL PRIMARY KEY,
                                          room_type_id    INT NOT NULL REFERENCES room_types(id) ON DELETE CASCADE,
    season_id       INT REFERENCES seasons(id) ON DELETE SET NULL,
    name            VARCHAR(100),
    price_per_night NUMERIC(10,2) NOT NULL,
    min_nights      INT DEFAULT 1,
    max_nights      INT DEFAULT NULL,
    includes_breakfast BOOLEAN DEFAULT FALSE,
    UNIQUE (room_type_id, season_id)
    );

-- Precios especiales por día y tipo de habitación (override)
CREATE TABLE IF NOT EXISTS daily_rates (
                                           id           SERIAL PRIMARY KEY,
                                           room_type_id INT NOT NULL REFERENCES room_types(id) ON DELETE CASCADE,
    rate_date    DATE NOT NULL,
    price        NUMERIC(10,2) NOT NULL,
    notes        TEXT,
    UNIQUE (room_type_id, rate_date)
    );

-- Promociones / descuentos con código
CREATE TABLE IF NOT EXISTS promotions (
                                          id             SERIAL PRIMARY KEY,
                                          name           VARCHAR(100) NOT NULL,
    code           VARCHAR(30) UNIQUE NOT NULL,
    discount_type  VARCHAR(10) NOT NULL CHECK (discount_type IN ('PERCENT','FIXED')),
    discount_value NUMERIC(10,2) NOT NULL,
    start_date     DATE NOT NULL,
    end_date       DATE NOT NULL,
    active         BOOLEAN DEFAULT TRUE,
    notes          TEXT
    );

-- Paquetes turísticos
CREATE TABLE IF NOT EXISTS packages (
                                        id          SERIAL PRIMARY KEY,
                                        name        VARCHAR(100) NOT NULL,
    description TEXT,
    base_price  NUMERIC(10,2) NOT NULL,
    contents    TEXT,
    active      BOOLEAN DEFAULT TRUE
    );


-- ============================================================
-- MÓDULO: HABITACIONES
-- ============================================================

-- Habitaciones del hotel
CREATE TABLE IF NOT EXISTS rooms (
                                     id              SERIAL PRIMARY KEY,
                                     number          VARCHAR(10) UNIQUE NOT NULL,
    floor           INT,
    room_type_id    INT REFERENCES room_types(id) ON DELETE SET NULL,
    status          room_status DEFAULT 'AVAILABLE',
    notes           TEXT,
    last_cleaned_at TIMESTAMP,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );


-- ============================================================
-- MÓDULO: CLIENTES / HUÉSPEDES
-- ============================================================

-- Datos personales de huéspedes
CREATE TABLE IF NOT EXISTS guests (
                                      id              SERIAL PRIMARY KEY,
                                      nif             VARCHAR(20) UNIQUE,
    first_name      VARCHAR(50) NOT NULL,
    last_name       VARCHAR(100) NOT NULL,
    email           VARCHAR(100),
    phone           VARCHAR(20),
    nationality     VARCHAR(50),
    country         VARCHAR(100),
    address         TEXT,
    city            VARCHAR(100),
    postal_code     VARCHAR(20),
    birth_date      DATE,
    notes           TEXT,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

-- Grupos de reserva (familias, empresas, etc.)
CREATE TABLE IF NOT EXISTS guest_groups (
                                            id          SERIAL PRIMARY KEY,
                                            name        VARCHAR(100) NOT NULL,
    contact_id  INT REFERENCES guests(id) ON DELETE SET NULL,
    notes       TEXT,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );


-- ============================================================
-- MÓDULO: RESERVAS / PLANNING
-- ============================================================

-- Reservas de habitaciones
CREATE TABLE IF NOT EXISTS reservations (
                                            id                  SERIAL PRIMARY KEY,
                                            reservation_number  VARCHAR(20) UNIQUE NOT NULL,
    room_id             INT REFERENCES rooms(id) ON DELETE SET NULL,
    guest_id            INT REFERENCES guests(id) ON DELETE SET NULL,
    group_id            INT REFERENCES guest_groups(id) ON DELETE SET NULL,
    rate_plan_id        INT REFERENCES rate_plans(id) ON DELETE SET NULL,
    check_in_date       DATE NOT NULL,
    check_out_date      DATE NOT NULL,
    adults              INT DEFAULT 1,
    children            INT DEFAULT 0,
    status              reservation_status DEFAULT 'PENDING',
    total_price         NUMERIC(10,2),
    deposit_paid        NUMERIC(10,2) DEFAULT 0,
    special_requests    TEXT,
    notes               TEXT,
    created_by          INT REFERENCES users(id) ON DELETE SET NULL,
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_reservation_dates CHECK (check_out_date > check_in_date),
    CONSTRAINT chk_reservation_guests CHECK (adults > 0)
    );

-- Historial de cambios de estado de reservas
CREATE TABLE IF NOT EXISTS reservation_history (
                                                   id              SERIAL PRIMARY KEY,
                                                   reservation_id  INT NOT NULL REFERENCES reservations(id) ON DELETE CASCADE,
    old_status      reservation_status,
    new_status      reservation_status NOT NULL,
    changed_by      INT REFERENCES users(id) ON DELETE SET NULL,
    notes           TEXT,
    changed_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );


-- ============================================================
-- MÓDULO: RECEPCIÓN (Check-in / Check-out)
-- ============================================================

-- Registro de check-in
CREATE TABLE IF NOT EXISTS checkins (
                                        id              SERIAL PRIMARY KEY,
                                        reservation_id  INT NOT NULL REFERENCES reservations(id) ON DELETE CASCADE,
    checked_in_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    checked_in_by   INT REFERENCES users(id) ON DELETE SET NULL,
    notes           TEXT
    );

-- Registro de check-out
CREATE TABLE IF NOT EXISTS checkouts (
                                         id              SERIAL PRIMARY KEY,
                                         reservation_id  INT NOT NULL REFERENCES reservations(id) ON DELETE CASCADE,
    checked_out_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    checked_out_by  INT REFERENCES users(id) ON DELETE SET NULL,
    final_amount    NUMERIC(10,2),
    notes           TEXT
    );

-- Incidencias en recepción
CREATE TABLE IF NOT EXISTS incidents (
                                         id              SERIAL PRIMARY KEY,
                                         reservation_id  INT REFERENCES reservations(id) ON DELETE SET NULL,
    room_id         INT REFERENCES rooms(id) ON DELETE SET NULL,
    title           VARCHAR(200) NOT NULL,
    description     TEXT,
    status          incident_status DEFAULT 'OPEN',
    reported_by     INT REFERENCES users(id) ON DELETE SET NULL,
    resolved_by     INT REFERENCES users(id) ON DELETE SET NULL,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    resolved_at     TIMESTAMP
    );


-- ============================================================
-- MÓDULO: ADMINISTRACIÓN / FACTURACIÓN
-- ============================================================

-- Facturas
CREATE TABLE IF NOT EXISTS invoices (
                                        id              SERIAL PRIMARY KEY,
                                        invoice_number  VARCHAR(20) UNIQUE NOT NULL,
    reservation_id  INT REFERENCES reservations(id) ON DELETE SET NULL,
    guest_id        INT REFERENCES guests(id) ON DELETE SET NULL,
    issue_date      DATE NOT NULL DEFAULT CURRENT_DATE,
    due_date        DATE,
    subtotal        NUMERIC(10,2) NOT NULL DEFAULT 0,
    tax_rate        NUMERIC(5,2) DEFAULT 10.00,   -- IVA en %
    tax_amount      NUMERIC(10,2) NOT NULL DEFAULT 0,
    total_amount    NUMERIC(10,2) NOT NULL DEFAULT 0,
    paid_amount     NUMERIC(10,2) DEFAULT 0,
    status          invoice_status DEFAULT 'PENDING',
    payment_method  payment_method,
    notes           TEXT,
    -- Datos de facturación alternativos (empresa/tercero que paga por el huésped)
    billing_name        VARCHAR(200),
    billing_nif         VARCHAR(30),
    billing_address     VARCHAR(300),
    billing_city        VARCHAR(100),
    billing_postal_code VARCHAR(10),
    billing_country     VARCHAR(100),
    created_by      INT REFERENCES users(id) ON DELETE SET NULL,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

-- Líneas de factura (conceptos)
CREATE TABLE IF NOT EXISTS invoice_lines (
                                             id              SERIAL PRIMARY KEY,
                                             invoice_id      INT NOT NULL REFERENCES invoices(id) ON DELETE CASCADE,
    description     VARCHAR(200) NOT NULL,
    quantity        NUMERIC(8,2) NOT NULL DEFAULT 1,
    unit_price      NUMERIC(10,2) NOT NULL,
    total_price     NUMERIC(10,2) NOT NULL,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

-- Pagos y cobros
CREATE TABLE IF NOT EXISTS payments (
                                        id              SERIAL PRIMARY KEY,
                                        invoice_id      INT NOT NULL REFERENCES invoices(id) ON DELETE CASCADE,
    amount          NUMERIC(10,2) NOT NULL,
    method          payment_method NOT NULL,
    reference       VARCHAR(100),              -- Referencia bancaria / TPV
    paid_at         TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    processed_by    INT REFERENCES users(id) ON DELETE SET NULL,
    notes           TEXT
    );


-- ============================================================
-- MÓDULO: MANTENIMIENTO
-- ============================================================

-- Solicitudes de mantenimiento
CREATE TABLE IF NOT EXISTS maintenance_requests (
                                                    id              SERIAL PRIMARY KEY,
                                                    room_id         INT REFERENCES rooms(id) ON DELETE SET NULL,
    title           VARCHAR(200) NOT NULL,
    description     TEXT,
    priority        maintenance_priority DEFAULT 'MEDIUM',
    status          maintenance_status DEFAULT 'PENDING',
    reported_by     INT REFERENCES users(id) ON DELETE SET NULL,
    assigned_to     INT REFERENCES users(id) ON DELETE SET NULL,
    reported_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    started_at      TIMESTAMP,
    completed_at    TIMESTAMP,
    notes           TEXT
    );


-- ============================================================
-- MÓDULO: RESTAURACIÓN
-- ============================================================

-- Categorías de carta
CREATE TABLE IF NOT EXISTS menu_categories (
                                               id          SERIAL PRIMARY KEY,
                                               name        VARCHAR(100) NOT NULL,
    description TEXT,
    color       VARCHAR(20) DEFAULT '#607D8B',
    active      BOOLEAN DEFAULT TRUE
    );
ALTER TABLE menu_categories ADD COLUMN IF NOT EXISTS color VARCHAR(20) DEFAULT '#607D8B';
ALTER TABLE restaurant_orders ADD COLUMN IF NOT EXISTS payment_method VARCHAR(20);

-- Carta / Menú
CREATE TABLE IF NOT EXISTS menu_items (
                                          id              SERIAL PRIMARY KEY,
                                          category_id     INT REFERENCES menu_categories(id) ON DELETE SET NULL,
    name            VARCHAR(100) NOT NULL,
    description     TEXT,
    price           NUMERIC(8,2) NOT NULL,
    available       BOOLEAN DEFAULT TRUE,
    allergens       TEXT
    );

-- Pedidos de restauración
CREATE TABLE IF NOT EXISTS restaurant_orders (
                                                 id              SERIAL PRIMARY KEY,
                                                 reservation_id  INT REFERENCES reservations(id) ON DELETE SET NULL,
    room_id         INT REFERENCES rooms(id) ON DELETE SET NULL,
    table_number    VARCHAR(10),
    status          order_status DEFAULT 'PENDING',
    notes           TEXT,
    created_by      INT REFERENCES users(id) ON DELETE SET NULL,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    delivered_at    TIMESTAMP
    );

-- Líneas de pedido de restauración
CREATE TABLE IF NOT EXISTS order_lines (
                                           id              SERIAL PRIMARY KEY,
                                           order_id        INT NOT NULL REFERENCES restaurant_orders(id) ON DELETE CASCADE,
    menu_item_id    INT REFERENCES menu_items(id) ON DELETE SET NULL,
    quantity        INT NOT NULL DEFAULT 1,
    unit_price      NUMERIC(8,2) NOT NULL,
    notes           TEXT
    );

-- Mesas del restaurante (gestión visual)
CREATE TABLE IF NOT EXISTS restaurant_tables (
                                                 id         SERIAL PRIMARY KEY,
                                                 zone       VARCHAR(50)  NOT NULL,
    name       VARCHAR(30)  NOT NULL,
    capacity   INT          DEFAULT 4,
    sort_order INT          DEFAULT 0,
    pos_x      FLOAT        DEFAULT 10,
    pos_y      FLOAT        DEFAULT 10,
    active     BOOLEAN      DEFAULT TRUE
    );
ALTER TABLE restaurant_tables ADD COLUMN IF NOT EXISTS pos_x FLOAT DEFAULT 10;
ALTER TABLE restaurant_tables ADD COLUMN IF NOT EXISTS pos_y FLOAT DEFAULT 10;

-- Add unique constraint to prevent duplicates on repeated startup
DO $$
BEGIN
  -- Remove duplicate rows, keeping the one with the lowest id per (zone, name)
DELETE FROM restaurant_tables
WHERE id NOT IN (
    SELECT MIN(id) FROM restaurant_tables GROUP BY zone, name
    );
-- Add unique constraint if it doesn't already exist
IF NOT EXISTS (
      SELECT 1 FROM pg_constraint WHERE conname = 'uniq_restaurant_tables_zone_name'
  ) THEN
ALTER TABLE restaurant_tables
    ADD CONSTRAINT uniq_restaurant_tables_zone_name UNIQUE (zone, name);
END IF;
END;$$;

INSERT INTO restaurant_tables (zone, name, capacity, sort_order, pos_x, pos_y) VALUES
                                                                                   ('BARRA','BARRA 1',2,1,10,10),('BARRA','BARRA 2',2,2,120,10),('BARRA','BARRA 3',2,3,230,10),
                                                                                   ('BARRA','BARRA 4',2,4,340,10),('BARRA','BARRA 5',2,5,450,10),
                                                                                   ('BARRA','BARRA 6',2,6,10,90), ('BARRA','BARRA 7',2,7,120,90),('BARRA','BARRA 8',2,8,230,90),
                                                                                   ('BARRA','BARRA 9',2,9,340,90),('BARRA','BARRA 10',2,10,450,90),
                                                                                   ('SALÓN','MESA 1',4,1,10,10), ('SALÓN','MESA 2',4,2,130,10),('SALÓN','MESA 3',4,3,250,10),
                                                                                   ('SALÓN','MESA 4',4,4,370,10),('SALÓN','MESA 5',4,5,10,110),
                                                                                   ('SALÓN','MESA 6',4,6,130,110),('SALÓN','MESA 7',4,7,250,110),('SALÓN','MESA 8',4,8,370,110),
                                                                                   ('SALÓN','MESA 9',4,9,10,210),('SALÓN','MESA 10',4,10,130,210),
                                                                                   ('TERRAZA','TERRAZA 1',4,1,10,10),('TERRAZA','TERRAZA 2',4,2,130,10),
                                                                                   ('TERRAZA','TERRAZA 3',4,3,250,10),('TERRAZA','TERRAZA 4',4,4,370,10),
                                                                                   ('TERRAZA','TERRAZA 5',4,5,10,110),('TERRAZA','TERRAZA 6',4,6,130,110),
                                                                                   ('TERRAZA','TERRAZA 7',4,7,250,110),('TERRAZA','TERRAZA 8',4,8,370,110),
                                                                                   ('TERRAZA','TERRAZA 9',4,9,10,210),('TERRAZA','TERRAZA 10',4,10,130,210)
    ON CONFLICT (zone, name) DO NOTHING;


-- ============================================================
-- MÓDULO: COMUNICACIÓN
-- ============================================================

-- Mensajes / Comunicaciones con huéspedes
CREATE TABLE IF NOT EXISTS communications (
                                              id              SERIAL PRIMARY KEY,
                                              guest_id        INT REFERENCES guests(id) ON DELETE SET NULL,
    reservation_id  INT REFERENCES reservations(id) ON DELETE SET NULL,
    subject         VARCHAR(200),
    body            TEXT NOT NULL,
    channel         VARCHAR(50),   -- EMAIL, SMS, WHATSAPP, INTERNAL
    direction       VARCHAR(10),   -- INBOUND, OUTBOUND
    sent_at         TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    sent_by         INT REFERENCES users(id) ON DELETE SET NULL,
    read            BOOLEAN DEFAULT FALSE
    );

-- Plantillas de correo electrónico
CREATE TABLE IF NOT EXISTS email_templates (
                                               id          SERIAL PRIMARY KEY,
                                               name        VARCHAR(100) NOT NULL,
    type        VARCHAR(50) NOT NULL,   -- CONFIRMATION, WELCOME, INVOICE, HOUSEKEEPING, CUSTOM
    subject     VARCHAR(200) NOT NULL,
    body        TEXT NOT NULL,
    active      BOOLEAN DEFAULT TRUE,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

-- Configuración de aplicación (clave-valor)
CREATE TABLE IF NOT EXISTS app_config (
                                          key         VARCHAR(100) PRIMARY KEY,
    value       TEXT,
    description VARCHAR(200)
    );

-- Datos por defecto: plantillas de correo
INSERT INTO email_templates (name, type, subject, body) VALUES
                                                            ('Confirmación de reserva', 'CONFIRMATION',
                                                             'Confirmación de su reserva {{reservation_number}}',
                                                             E'Estimado/a {{guest_name}},\n\nNos complace confirmar su reserva con los siguientes datos:\n\nNúmero de reserva: {{reservation_number}}\nFecha de entrada: {{check_in}}\nFecha de salida: {{check_out}}\nHabitación: {{room_number}}\nImporte total: {{total_price}} €\n\nSi tiene alguna pregunta, no dude en contactarnos.\n\nAtentamente,\nEl equipo del hotel'),
                                                            ('Bienvenida', 'WELCOME',
                                                             'Bienvenido/a, {{guest_name}}',
                                                             E'Estimado/a {{guest_name}},\n\nEs un placer tenerle con nosotros. Le informamos que su habitación {{room_number}} está lista.\n\nHorario de desayuno: 7:30 - 10:30\nWi-Fi: disponible en todo el establecimiento\n\nEstamos a su disposición en recepción.\n\nAtentamente,\nEl equipo del hotel'),
                                                            ('Envío de factura', 'INVOICE',
                                                             'Factura {{invoice_number}} - {{hotel_name}}',
                                                             E'Estimado/a {{guest_name}},\n\nAdjunto encontrará la factura {{invoice_number}} correspondiente a su estancia.\n\nImporte: {{total_amount}} €\nFecha: {{issue_date}}\n\nGracias por su visita. Esperamos verle de nuevo pronto.\n\nAtentamente,\nEl equipo del hotel'),
                                                            ('Notificación housekeeping', 'HOUSEKEEPING',
                                                             '[Housekeeping] Habitación {{room_number}} - {{task}}',
                                                             E'Estimado/a equipo,\n\nSe requiere atención en la habitación {{room_number}}:\n\nTarea: {{task}}\nPrioridad: {{priority}}\nNotas: {{notes}}\n\nGracias.')
    ON CONFLICT DO NOTHING;

-- Datos por defecto: configuración SMTP (vacíos, el usuario los rellena en la UI)
INSERT INTO app_config (key, value, description) VALUES
                                                     ('smtp.host',     '',      'Servidor SMTP (ej: smtp.gmail.com)'),
                                                     ('smtp.port',     '587',   'Puerto SMTP (587 para TLS, 465 para SSL)'),
                                                     ('smtp.user',     '',      'Usuario/email SMTP'),
                                                     ('smtp.password', '',      'Contraseña SMTP'),
                                                     ('smtp.from',     '',      'Dirección de envío (From:)'),
                                                     ('smtp.from_name','Hotel', 'Nombre del remitente'),
                                                     ('smtp.tls',      'true',  'Usar STARTTLS (true/false)')
    ON CONFLICT DO NOTHING;


-- ============================================================
-- MÓDULO: ALERTAS DEL SISTEMA
-- ============================================================

-- Alertas y notificaciones del sistema
CREATE TABLE IF NOT EXISTS alerts (
                                      id              SERIAL PRIMARY KEY,
                                      type            alert_type NOT NULL,
                                      severity        alert_severity DEFAULT 'INFO',
                                      title           VARCHAR(200) NOT NULL,
    description     TEXT,
    related_room_id         INT REFERENCES rooms(id) ON DELETE SET NULL,
    related_reservation_id  INT REFERENCES reservations(id) ON DELETE SET NULL,
    resolved        BOOLEAN DEFAULT FALSE,
    resolved_by     INT REFERENCES users(id) ON DELETE SET NULL,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    resolved_at     TIMESTAMP
    );


-- ============================================================
-- ÍNDICES para rendimiento
-- ============================================================

CREATE INDEX IF NOT EXISTS idx_rooms_status ON rooms(status);
CREATE INDEX IF NOT EXISTS idx_rooms_type ON rooms(room_type_id);
CREATE INDEX IF NOT EXISTS idx_reservations_dates ON reservations(check_in_date, check_out_date);
CREATE INDEX IF NOT EXISTS idx_reservations_status ON reservations(status);
CREATE INDEX IF NOT EXISTS idx_reservations_room ON reservations(room_id);
CREATE INDEX IF NOT EXISTS idx_reservations_guest ON reservations(guest_id);
CREATE INDEX IF NOT EXISTS idx_guests_nif ON guests(nif);
CREATE INDEX IF NOT EXISTS idx_guests_name ON guests(last_name, first_name);
CREATE INDEX IF NOT EXISTS idx_invoices_status ON invoices(status);
CREATE INDEX IF NOT EXISTS idx_invoices_reservation ON invoices(reservation_id);
CREATE INDEX IF NOT EXISTS idx_maintenance_status ON maintenance_requests(status);
CREATE INDEX IF NOT EXISTS idx_maintenance_room ON maintenance_requests(room_id);
CREATE INDEX IF NOT EXISTS idx_alerts_resolved ON alerts(resolved);
CREATE INDEX IF NOT EXISTS idx_alerts_type ON alerts(type);


-- ============================================================
-- FUNCIÓN: Actualizar updated_at automáticamente
-- ============================================================

CREATE OR REPLACE FUNCTION update_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_reservations_updated_at ON reservations;
CREATE TRIGGER trg_reservations_updated_at
    BEFORE UPDATE ON reservations
    FOR EACH ROW EXECUTE FUNCTION update_updated_at();


-- ============================================================
-- FUNCIÓN: Generar número de reserva automático
-- ============================================================

CREATE OR REPLACE FUNCTION generate_reservation_number()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.reservation_number IS NULL OR NEW.reservation_number = '' THEN
        NEW.reservation_number := 'RES-' || TO_CHAR(CURRENT_DATE, 'YYYYMMDD') || '-' || LPAD(NEW.id::TEXT, 4, '0');
END IF;
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_reservation_number ON reservations;
CREATE TRIGGER trg_reservation_number
    BEFORE INSERT ON reservations
    FOR EACH ROW EXECUTE FUNCTION generate_reservation_number();


-- ============================================================
-- FUNCIÓN: Generar número de factura automático
-- ============================================================

CREATE OR REPLACE FUNCTION generate_invoice_number()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.invoice_number IS NULL OR NEW.invoice_number = '' THEN
        NEW.invoice_number := 'FAC-' || TO_CHAR(CURRENT_DATE, 'YYYYMMDD') || '-' || LPAD(NEW.id::TEXT, 4, '0');
END IF;
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_invoice_number ON invoices;
CREATE TRIGGER trg_invoice_number
    BEFORE INSERT ON invoices
    FOR EACH ROW EXECUTE FUNCTION generate_invoice_number();


-- ============================================================
-- DATOS BASE (configuración mínima del sistema)
-- ============================================================

-- Hotel principal
INSERT INTO hotel (name, address, city, country, phone, email)
VALUES ('PMSuite Hotel', 'Calle Mayor 1', 'Madrid', 'España', '+34 912 345 678', 'info@pmsuite.es')
    ON CONFLICT DO NOTHING;

-- Usuario administrador por defecto
INSERT INTO users (username, email, password_hash, full_name, role)
VALUES ('nacho', 'nacho@ofi.es', 'cambiar_password_hash', 'Nacho Administrador', 'ADMIN')
    ON CONFLICT (username) DO NOTHING;

-- Tipos de habitación (según la UI: SGL Economy, SGL Superior, DBL Standard, DBL Vista Mar, Suite Junior)
INSERT INTO room_types (code, name, description, max_occupancy, base_price) VALUES
                                                                                ('SGL-ECO',  'Single Económica',    'Habitación individual estándar',                    1, 65.00),
                                                                                ('SGL-SUP',  'Single Superior',     'Habitación individual con mejores acabados',         1, 85.00),
                                                                                ('DBL-STD',  'Doble Estándar',      'Habitación doble con dos camas o cama de matrimonio',2, 110.00),
                                                                                ('DBL-MAR',  'Doble Vista Mar',     'Habitación doble con vistas al mar',                 2, 150.00),
                                                                                ('SJR',      'Suite Junior',        'Suite con sala de estar y vistas panorámicas',       3, 220.00)
    ON CONFLICT (code) DO NOTHING;

-- Tipos de habitación adicionales para demo
INSERT INTO room_types (code, name, description, max_occupancy, base_price) VALUES
                                                                                ('DBL-SUP',  'Doble Superior',  'Habitación doble con mejores acabados',  2, 135.00),
                                                                                ('STE-PRE',  'Suite Premium',   'Suite de lujo con terraza y jacuzzi',    3, 320.00)
    ON CONFLICT (code) DO NOTHING;

-- Juego completo de habitaciones del hotel demo
INSERT INTO rooms (number, floor, room_type_id, status) VALUES
                                                            ('101', 1, (SELECT id FROM room_types WHERE code = 'SGL-ECO'),  'OCCUPIED'),
                                                            ('102', 1, (SELECT id FROM room_types WHERE code = 'SGL-ECO'),  'OCCUPIED'),
                                                            ('103', 1, (SELECT id FROM room_types WHERE code = 'DBL-STD'),  'OCCUPIED'),
                                                            ('104', 1, (SELECT id FROM room_types WHERE code = 'DBL-STD'),  'OCCUPIED'),
                                                            ('105', 1, (SELECT id FROM room_types WHERE code = 'DBL-STD'),  'AVAILABLE'),
                                                            ('106', 1, (SELECT id FROM room_types WHERE code = 'DBL-STD'),  'AVAILABLE'),
                                                            ('201', 2, (SELECT id FROM room_types WHERE code = 'DBL-STD'),  'OCCUPIED'),
                                                            ('202', 2, (SELECT id FROM room_types WHERE code = 'DBL-STD'),  'AVAILABLE'),
                                                            ('203', 2, (SELECT id FROM room_types WHERE code = 'DBL-MAR'),  'OCCUPIED'),
                                                            ('204', 2, (SELECT id FROM room_types WHERE code = 'DBL-MAR'),  'BLOCKED'),
                                                            ('205', 2, (SELECT id FROM room_types WHERE code = 'DBL-MAR'),  'AVAILABLE'),
                                                            ('206', 2, (SELECT id FROM room_types WHERE code = 'DBL-SUP'),  'OCCUPIED'),
                                                            ('207', 2, (SELECT id FROM room_types WHERE code = 'DBL-SUP'),  'AVAILABLE'),
                                                            ('301', 3, (SELECT id FROM room_types WHERE code = 'DBL-SUP'),  'OCCUPIED'),
                                                            ('302', 3, (SELECT id FROM room_types WHERE code = 'DBL-SUP'),  'BLOCKED'),
                                                            ('303', 3, (SELECT id FROM room_types WHERE code = 'SJR'),      'AVAILABLE'),
                                                            ('304', 3, (SELECT id FROM room_types WHERE code = 'SJR'),      'AVAILABLE'),
                                                            ('305', 3, (SELECT id FROM room_types WHERE code = 'SJR'),      'OCCUPIED'),
                                                            ('306', 3, (SELECT id FROM room_types WHERE code = 'SJR'),      'OUT_OF_SERVICE'),
                                                            ('401', 4, (SELECT id FROM room_types WHERE code = 'SJR'),      'AVAILABLE'),
                                                            ('402', 4, (SELECT id FROM room_types WHERE code = 'STE-PRE'),  'OCCUPIED'),
                                                            ('403', 4, (SELECT id FROM room_types WHERE code = 'STE-PRE'),  'AVAILABLE')
    ON CONFLICT (number) DO UPDATE SET
    status       = EXCLUDED.status,
                                floor        = EXCLUDED.floor,
                                room_type_id = EXCLUDED.room_type_id;

-- Huéspedes de demostración
INSERT INTO guests (nif, first_name, last_name, email, phone, nationality) VALUES
                                                                               ('ES00000001', 'Luis',   'García Ruiz',  'luis.garcia@example.com',   '+34 612 001 001', 'Española'),
                                                                               ('ES00000002', 'Pablo',  'Ferrer Mas',   'pablo.ferrer@example.com',  '+34 612 001 002', 'Española'),
                                                                               ('GB00000001', 'John',   'Smith',        'john.smith@example.com',    '+44 7700 900001', 'Inglesa'),
                                                                               ('ES00000003', 'Carmen', 'López Sanz',   'carmen.lopez@example.com',  '+34 612 001 003', 'Española'),
                                                                               ('DE00000001', 'Hans',   'Müller',       'hans.muller@example.com',   '+49 170 1234567', 'Alemana'),
                                                                               ('JP00000001', 'Kenji',  'Tanaka',       'kenji.tanaka@example.com',  '+81 90 12345678', 'Japonesa'),
                                                                               ('US00000001', 'Thomas', 'Wilson',       'thomas.wilson@example.com', '+1 555 0100',     'Americana'),
                                                                               ('FR00000001', 'Marie',  'Dubois',       'marie.dubois@example.com',  '+33 6 12 345678', 'Francesa'),
                                                                               ('IT00000001', 'Marco',  'Rossi',        'marco.rossi@example.com',   '+39 340 1234567', 'Italiana'),
                                                                               ('US00000002', 'Sarah',  'Brown',        'sarah.brown@example.com',   '+1 555 0200',     'Americana'),
                                                                               ('US00000003', 'Emma',   'Anderson',     'emma.anderson@example.com', '+1 555 0300',     'Americana'),
                                                                               ('ES00000004', 'Juan',   'Pérez Gómez',  'juan.perez@example.com',    '+34 612 001 004', 'Española'),
                                                                               ('CN00000001', 'Wei',    'Chen',         'wei.chen@example.com',      '+86 131 0000001', 'China'),
                                                                               ('CZ00000001', 'Karel',  'Novak',        'karel.novak@example.com',   '+420 700 123456', 'Checa'),
                                                                               ('ES00000005', 'Ana',    'Santos',       'ana.santos@example.com',    '+34 612 001 005', 'Española')
    ON CONFLICT (nif) DO NOTHING;

-- Reservas de demostración
-- DM-001..DM-010: estancias activas (CHECKED_IN)
-- DM-003+DM-004: sobrereserva en habitación 103
-- DM-ARR1,ARR2:  llegadas de hoy
-- DM-DEP1,DEP2:  salidas de hoy (CHECKED_IN con check_out=hoy)
-- DM-CHK1,CHK2:  estancias completadas (CHECKED_OUT)
INSERT INTO reservations
(reservation_number, room_id, guest_id, check_in_date, check_out_date, adults, status, total_price)
VALUES
    ('DM-001',
     (SELECT id FROM rooms WHERE number='101'),
     (SELECT id FROM guests WHERE nif='ES00000001'),
     CURRENT_DATE - 10, CURRENT_DATE + 2, 1, 'CHECKED_IN',  520.00),
    ('DM-002',
     (SELECT id FROM rooms WHERE number='102'),
     (SELECT id FROM guests WHERE nif='US00000001'),
     CURRENT_DATE - 5,  CURRENT_DATE + 3, 1, 'CHECKED_IN',  520.00),
    -- Sobrereserva: dos reservas solapadas en hab. 103
    ('DM-003',
     (SELECT id FROM rooms WHERE number='103'),
     (SELECT id FROM guests WHERE nif='GB00000001'),
     CURRENT_DATE - 8,  CURRENT_DATE + 2, 2, 'CHECKED_IN',  1100.00),
    ('DM-004',
     (SELECT id FROM rooms WHERE number='103'),
     (SELECT id FROM guests WHERE nif='ES00000003'),
     CURRENT_DATE - 2,  CURRENT_DATE + 5, 2, 'CONFIRMED',   770.00),
    ('DM-005',
     (SELECT id FROM rooms WHERE number='104'),
     (SELECT id FROM guests WHERE nif='IT00000001'),
     CURRENT_DATE - 6,  CURRENT_DATE + 4, 2, 'CHECKED_IN',  1100.00),
    ('DM-006',
     (SELECT id FROM rooms WHERE number='203'),
     (SELECT id FROM guests WHERE nif='US00000002'),
     CURRENT_DATE - 3,  CURRENT_DATE + 5, 2, 'CHECKED_IN',  1200.00),
    ('DM-007',
     (SELECT id FROM rooms WHERE number='206'),
     (SELECT id FROM guests WHERE nif='US00000003'),
     CURRENT_DATE - 12, CURRENT_DATE + 2, 2, 'CHECKED_IN',  1890.00),
    ('DM-008',
     (SELECT id FROM rooms WHERE number='301'),
     (SELECT id FROM guests WHERE nif='ES00000004'),
     CURRENT_DATE - 8,  CURRENT_DATE + 4, 2, 'CHECKED_IN',  1890.00),
    ('DM-009',
     (SELECT id FROM rooms WHERE number='402'),
     (SELECT id FROM guests WHERE nif='CZ00000001'),
     CURRENT_DATE - 15, CURRENT_DATE + 4, 3, 'CHECKED_IN',  5120.00),
    -- Llegadas de hoy
    ('DM-ARR1',
     (SELECT id FROM rooms WHERE number='106'),
     (SELECT id FROM guests WHERE nif='ES00000002'),
     CURRENT_DATE,      CURRENT_DATE + 4, 2, 'CONFIRMED',   440.00),
    ('DM-ARR2',
     (SELECT id FROM rooms WHERE number='207'),
     (SELECT id FROM guests WHERE nif='DE00000001'),
     CURRENT_DATE,      CURRENT_DATE + 6, 1, 'PENDING',     810.00),
    -- Salidas de hoy (check_out = hoy, aún CHECKED_IN)
    ('DM-DEP1',
     (SELECT id FROM rooms WHERE number='201'),
     (SELECT id FROM guests WHERE nif='FR00000001'),
     CURRENT_DATE - 4,  CURRENT_DATE,     2, 'CHECKED_IN',  440.00),
    ('DM-DEP2',
     (SELECT id FROM rooms WHERE number='305'),
     (SELECT id FROM guests WHERE nif='JP00000001'),
     CURRENT_DATE - 7,  CURRENT_DATE,     2, 'CHECKED_IN',  1540.00),
    -- Estancias completadas
    ('DM-CHK1',
     (SELECT id FROM rooms WHERE number='202'),
     (SELECT id FROM guests WHERE nif='CN00000001'),
     CURRENT_DATE - 20, CURRENT_DATE - 13, 2, 'CHECKED_OUT', 770.00),
    ('DM-CHK2',
     (SELECT id FROM rooms WHERE number='303'),
     (SELECT id FROM guests WHERE nif='ES00000005'),
     CURRENT_DATE - 12, CURRENT_DATE - 7,  2, 'CHECKED_OUT', 1100.00),
    -- Reservas futuras confirmadas
    ('DM-F001',
     (SELECT id FROM rooms WHERE number='105'),
     (SELECT id FROM guests WHERE nif='ES00000005'),
     CURRENT_DATE + 3,  CURRENT_DATE + 8,  2, 'CONFIRMED',  550.00),
    ('DM-F002',
     (SELECT id FROM rooms WHERE number='202'),
     (SELECT id FROM guests WHERE nif='ES00000002'),
     CURRENT_DATE + 5,  CURRENT_DATE + 10, 2, 'CONFIRMED',  550.00),
    ('DM-F003',
     (SELECT id FROM rooms WHERE number='303'),
     (SELECT id FROM guests WHERE nif='GB00000001'),
     CURRENT_DATE + 4,  CURRENT_DATE + 11, 2, 'CONFIRMED',  1540.00),
    ('DM-F004',
     (SELECT id FROM rooms WHERE number='304'),
     (SELECT id FROM guests WHERE nif='DE00000001'),
     CURRENT_DATE + 3,  CURRENT_DATE + 10, 2, 'PENDING',    1540.00),
    ('DM-F005',
     (SELECT id FROM rooms WHERE number='401'),
     (SELECT id FROM guests WHERE nif='IT00000001'),
     CURRENT_DATE + 8,  CURRENT_DATE + 15, 2, 'CONFIRMED',  1760.00),
    ('DM-F006',
     (SELECT id FROM rooms WHERE number='403'),
     (SELECT id FROM guests WHERE nif='US00000001'),
     CURRENT_DATE + 10, CURRENT_DATE + 17, 2, 'CONFIRMED',  2240.00)
    ON CONFLICT (reservation_number) DO NOTHING;

-- Solicitudes de mantenimiento para habitaciones bloqueadas/fuera de servicio
INSERT INTO maintenance_requests (room_id, title, description, priority, status)
SELECT (SELECT id FROM rooms WHERE number='306'),
       'Avería sistema climatización',
       'El sistema de aire acondicionado no funciona. Requiere revisión técnica urgente.',
       'HIGH', 'IN_PROGRESS'
    WHERE NOT EXISTS (
    SELECT 1 FROM maintenance_requests mr
    JOIN rooms r ON r.id = mr.room_id
    WHERE r.number = '306' AND mr.title = 'Avería sistema climatización'
);

INSERT INTO maintenance_requests (room_id, title, description, priority, status)
SELECT (SELECT id FROM rooms WHERE number='302'),
       'Limpieza profunda y desinfección',
       'Protocolo de limpieza profunda programado tras larga estancia.',
       'MEDIUM', 'IN_PROGRESS'
    WHERE NOT EXISTS (
    SELECT 1 FROM maintenance_requests mr
    JOIN rooms r ON r.id = mr.room_id
    WHERE r.number = '302' AND mr.title = 'Limpieza profunda y desinfección'
);

INSERT INTO maintenance_requests (room_id, title, description, priority, status)
SELECT (SELECT id FROM rooms WHERE number='204'),
       'Revisión instalación eléctrica',
       'Revisión preventiva del cuadro eléctrico y enchufes.',
       'MEDIUM', 'PENDING'
    WHERE NOT EXISTS (
    SELECT 1 FROM maintenance_requests mr
    JOIN rooms r ON r.id = mr.room_id
    WHERE r.number = '204' AND mr.title = 'Revisión instalación eléctrica'
);

-- ============================================================
-- SEED: FACTURAS Y EXTRAS PARA RESERVAS ACTIVAS
-- ============================================================

-- DM-001 (hab. 101): minibar + desayuno buffet
INSERT INTO invoices (invoice_number, reservation_id, guest_id, issue_date, subtotal, tax_rate, tax_amount, total_amount, paid_amount, status)
SELECT 'FAC-0001',
       (SELECT id FROM reservations WHERE reservation_number='DM-001'),
       (SELECT guest_id FROM reservations WHERE reservation_number='DM-001'),
       CURRENT_DATE - 5, 527.27, 10.00, 52.73, 580.00, 200.00, 'PARTIALLY_PAID'
    WHERE NOT EXISTS (SELECT 1 FROM invoices WHERE invoice_number='FAC-0001');

INSERT INTO invoice_lines (invoice_id, description, quantity, unit_price, total_price)
SELECT (SELECT id FROM invoices WHERE invoice_number='FAC-0001'), 'Alojamiento 12 noches', 12, 40.00, 480.00
    WHERE NOT EXISTS (SELECT 1 FROM invoice_lines il JOIN invoices i ON i.id=il.invoice_id WHERE i.invoice_number='FAC-0001' AND il.description='Alojamiento 12 noches');

INSERT INTO invoice_lines (invoice_id, description, quantity, unit_price, total_price)
SELECT (SELECT id FROM invoices WHERE invoice_number='FAC-0001'), 'Minibar - Agua mineral x2, Refresco x3', 1, 12.00, 12.00
    WHERE NOT EXISTS (SELECT 1 FROM invoice_lines il JOIN invoices i ON i.id=il.invoice_id WHERE i.invoice_number='FAC-0001' AND il.description LIKE 'Minibar%');

INSERT INTO invoice_lines (invoice_id, description, quantity, unit_price, total_price)
SELECT (SELECT id FROM invoices WHERE invoice_number='FAC-0001'), 'Desayuno buffet x2 personas', 3, 28.00, 84.00
    WHERE NOT EXISTS (SELECT 1 FROM invoice_lines il JOIN invoices i ON i.id=il.invoice_id WHERE i.invoice_number='FAC-0001' AND il.description LIKE 'Desayuno%');

-- DM-003 (hab. 103): room service + parking
INSERT INTO invoices (invoice_number, reservation_id, guest_id, issue_date, subtotal, tax_rate, tax_amount, total_amount, paid_amount, status)
SELECT 'FAC-0003',
       (SELECT id FROM reservations WHERE reservation_number='DM-003'),
       (SELECT guest_id FROM reservations WHERE reservation_number='DM-003'),
       CURRENT_DATE - 3, 1127.27, 10.00, 112.73, 1240.00, 1240.00, 'PAID'
    WHERE NOT EXISTS (SELECT 1 FROM invoices WHERE invoice_number='FAC-0003');

INSERT INTO invoice_lines (invoice_id, description, quantity, unit_price, total_price)
SELECT (SELECT id FROM invoices WHERE invoice_number='FAC-0003'), 'Alojamiento 10 noches', 10, 100.00, 1000.00
    WHERE NOT EXISTS (SELECT 1 FROM invoice_lines il JOIN invoices i ON i.id=il.invoice_id WHERE i.invoice_number='FAC-0003' AND il.description='Alojamiento 10 noches');

INSERT INTO invoice_lines (invoice_id, description, quantity, unit_price, total_price)
SELECT (SELECT id FROM invoices WHERE invoice_number='FAC-0003'), 'Restaurante - Cena romántica para 2', 1, 95.00, 95.00
    WHERE NOT EXISTS (SELECT 1 FROM invoice_lines il JOIN invoices i ON i.id=il.invoice_id WHERE i.invoice_number='FAC-0003' AND il.description LIKE 'Restaurante%');

INSERT INTO invoice_lines (invoice_id, description, quantity, unit_price, total_price)
SELECT (SELECT id FROM invoices WHERE invoice_number='FAC-0003'), 'Parking vehículo 10 días', 10, 15.00, 150.00
    WHERE NOT EXISTS (SELECT 1 FROM invoice_lines il JOIN invoices i ON i.id=il.invoice_id WHERE i.invoice_number='FAC-0003' AND il.description LIKE 'Parking%');

-- DM-005 (hab. 104): spa + masaje
INSERT INTO invoices (invoice_number, reservation_id, guest_id, issue_date, subtotal, tax_rate, tax_amount, total_amount, paid_amount, status)
SELECT 'FAC-0005',
       (SELECT id FROM reservations WHERE reservation_number='DM-005'),
       (SELECT guest_id FROM reservations WHERE reservation_number='DM-005'),
       CURRENT_DATE - 2, 1272.73, 10.00, 127.27, 1400.00, 500.00, 'PARTIALLY_PAID'
    WHERE NOT EXISTS (SELECT 1 FROM invoices WHERE invoice_number='FAC-0005');

INSERT INTO invoice_lines (invoice_id, description, quantity, unit_price, total_price)
SELECT (SELECT id FROM invoices WHERE invoice_number='FAC-0005'), 'Alojamiento 10 noches', 10, 100.00, 1000.00
    WHERE NOT EXISTS (SELECT 1 FROM invoice_lines il JOIN invoices i ON i.id=il.invoice_id WHERE i.invoice_number='FAC-0005' AND il.description='Alojamiento 10 noches');

INSERT INTO invoice_lines (invoice_id, description, quantity, unit_price, total_price)
SELECT (SELECT id FROM invoices WHERE invoice_number='FAC-0005'), 'Spa - Circuito termal para 2', 2, 65.00, 130.00
    WHERE NOT EXISTS (SELECT 1 FROM invoice_lines il JOIN invoices i ON i.id=il.invoice_id WHERE i.invoice_number='FAC-0005' AND il.description LIKE 'Spa%');

INSERT INTO invoice_lines (invoice_id, description, quantity, unit_price, total_price)
SELECT (SELECT id FROM invoices WHERE invoice_number='FAC-0005'), 'Masaje relajante 60 min x2', 2, 85.00, 170.00
    WHERE NOT EXISTS (SELECT 1 FROM invoice_lines il JOIN invoices i ON i.id=il.invoice_id WHERE i.invoice_number='FAC-0005' AND il.description LIKE 'Masaje%');

-- DM-006 (hab. 203): desayuno + minibar
INSERT INTO invoices (invoice_number, reservation_id, guest_id, issue_date, subtotal, tax_rate, tax_amount, total_amount, paid_amount, status)
SELECT 'FAC-0006',
       (SELECT id FROM reservations WHERE reservation_number='DM-006'),
       (SELECT guest_id FROM reservations WHERE reservation_number='DM-006'),
       CURRENT_DATE - 1, 1309.09, 10.00, 130.91, 1440.00, 0.00, 'PENDING'
    WHERE NOT EXISTS (SELECT 1 FROM invoices WHERE invoice_number='FAC-0006');

INSERT INTO invoice_lines (invoice_id, description, quantity, unit_price, total_price)
SELECT (SELECT id FROM invoices WHERE invoice_number='FAC-0006'), 'Alojamiento 8 noches', 8, 150.00, 1200.00
    WHERE NOT EXISTS (SELECT 1 FROM invoice_lines il JOIN invoices i ON i.id=il.invoice_id WHERE i.invoice_number='FAC-0006' AND il.description='Alojamiento 8 noches');

INSERT INTO invoice_lines (invoice_id, description, quantity, unit_price, total_price)
SELECT (SELECT id FROM invoices WHERE invoice_number='FAC-0006'), 'Desayuno buffet x2 personas', 4, 28.00, 112.00
    WHERE NOT EXISTS (SELECT 1 FROM invoice_lines il JOIN invoices i ON i.id=il.invoice_id WHERE i.invoice_number='FAC-0006' AND il.description LIKE 'Desayuno%');

INSERT INTO invoice_lines (invoice_id, description, quantity, unit_price, total_price)
SELECT (SELECT id FROM invoices WHERE invoice_number='FAC-0006'), 'Minibar - Vino tinto, snacks, agua', 1, 38.00, 38.00
    WHERE NOT EXISTS (SELECT 1 FROM invoice_lines il JOIN invoices i ON i.id=il.invoice_id WHERE i.invoice_number='FAC-0006' AND il.description LIKE 'Minibar%');

-- DM-009 (hab. 402 Suite Premium): minibar + cena + spa + parking
INSERT INTO invoices (invoice_number, reservation_id, guest_id, issue_date, subtotal, tax_rate, tax_amount, total_amount, paid_amount, status)
SELECT 'FAC-0009',
       (SELECT id FROM reservations WHERE reservation_number='DM-009'),
       (SELECT guest_id FROM reservations WHERE reservation_number='DM-009'),
       CURRENT_DATE - 10, 5490.91, 10.00, 549.09, 6040.00, 3000.00, 'PARTIALLY_PAID'
    WHERE NOT EXISTS (SELECT 1 FROM invoices WHERE invoice_number='FAC-0009');

INSERT INTO invoice_lines (invoice_id, description, quantity, unit_price, total_price)
SELECT (SELECT id FROM invoices WHERE invoice_number='FAC-0009'), 'Alojamiento 19 noches Suite Premium', 19, 280.00, 5320.00
    WHERE NOT EXISTS (SELECT 1 FROM invoice_lines il JOIN invoices i ON i.id=il.invoice_id WHERE i.invoice_number='FAC-0009' AND il.description LIKE 'Alojamiento%');

INSERT INTO invoice_lines (invoice_id, description, quantity, unit_price, total_price)
SELECT (SELECT id FROM invoices WHERE invoice_number='FAC-0009'), 'Minibar - Premium surtido (5 días)', 5, 22.00, 110.00
    WHERE NOT EXISTS (SELECT 1 FROM invoice_lines il JOIN invoices i ON i.id=il.invoice_id WHERE i.invoice_number='FAC-0009' AND il.description LIKE 'Minibar%');

INSERT INTO invoice_lines (invoice_id, description, quantity, unit_price, total_price)
SELECT (SELECT id FROM invoices WHERE invoice_number='FAC-0009'), 'Restaurante - Cena gourmet x3 noches', 3, 120.00, 360.00
    WHERE NOT EXISTS (SELECT 1 FROM invoice_lines il JOIN invoices i ON i.id=il.invoice_id WHERE i.invoice_number='FAC-0009' AND il.description LIKE 'Restaurante%');

INSERT INTO invoice_lines (invoice_id, description, quantity, unit_price, total_price)
SELECT (SELECT id FROM invoices WHERE invoice_number='FAC-0009'), 'Spa - Pack Bienestar Total (3 sesiones)', 3, 95.00, 285.00
    WHERE NOT EXISTS (SELECT 1 FROM invoice_lines il JOIN invoices i ON i.id=il.invoice_id WHERE i.invoice_number='FAC-0009' AND il.description LIKE 'Spa%');

INSERT INTO invoice_lines (invoice_id, description, quantity, unit_price, total_price)
SELECT (SELECT id FROM invoices WHERE invoice_number='FAC-0009'), 'Parking cubierto VIP 19 días', 19, 20.00, 380.00
    WHERE NOT EXISTS (SELECT 1 FROM invoice_lines il JOIN invoices i ON i.id=il.invoice_id WHERE i.invoice_number='FAC-0009' AND il.description LIKE 'Parking%');

-- Pagos para facturas PAID y PARTIAL
INSERT INTO payments (invoice_id, amount, method, reference, paid_at)
SELECT (SELECT id FROM invoices WHERE invoice_number='FAC-0001'), 200.00, 'CREDIT_CARD', 'TPV-20250422-001', CURRENT_TIMESTAMP - INTERVAL '5 days'
WHERE NOT EXISTS (SELECT 1 FROM payments p JOIN invoices i ON i.id=p.invoice_id WHERE i.invoice_number='FAC-0001');

INSERT INTO payments (invoice_id, amount, method, reference, paid_at)
SELECT (SELECT id FROM invoices WHERE invoice_number='FAC-0003'), 1240.00, 'CASH', NULL, CURRENT_TIMESTAMP - INTERVAL '2 days'
WHERE NOT EXISTS (SELECT 1 FROM payments p JOIN invoices i ON i.id=p.invoice_id WHERE i.invoice_number='FAC-0003');

INSERT INTO payments (invoice_id, amount, method, reference, paid_at)
SELECT (SELECT id FROM invoices WHERE invoice_number='FAC-0005'), 500.00, 'DEBIT_CARD', 'TPV-20250428-005', CURRENT_TIMESTAMP - INTERVAL '1 day'
WHERE NOT EXISTS (SELECT 1 FROM payments p JOIN invoices i ON i.id=p.invoice_id WHERE i.invoice_number='FAC-0005');

INSERT INTO payments (invoice_id, amount, method, reference, paid_at)
SELECT (SELECT id FROM invoices WHERE invoice_number='FAC-0009'), 3000.00, 'BANK_TRANSFER', 'TRANS-20250416-009', CURRENT_TIMESTAMP - INTERVAL '10 days'
WHERE NOT EXISTS (SELECT 1 FROM payments p JOIN invoices i ON i.id=p.invoice_id WHERE i.invoice_number='FAC-0009');
