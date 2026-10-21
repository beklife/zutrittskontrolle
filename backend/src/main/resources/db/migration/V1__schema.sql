CREATE TABLE player (
    id          BIGSERIAL    PRIMARY KEY,
    first_name  VARCHAR(50)  NOT NULL,
    last_name   VARCHAR(50)  NOT NULL,
    birth_date  DATE         NOT NULL,
    blocked     BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE access_attempt (
    id            BIGSERIAL    PRIMARY KEY,
    player_id     BIGINT       NOT NULL REFERENCES player (id),
    attempted_at  TIMESTAMPTZ  NOT NULL,
    granted       BOOLEAN      NOT NULL,
    reason        VARCHAR(20)  NOT NULL,
    CONSTRAINT chk_access_attempt_reason CHECK (reason IN ('GRANTED', 'UNDERAGE', 'BLOCKED'))
);

-- Das Protokoll fragt immer nach einem Zeitraum (ein Tag in Europe/Berlin):
--   WHERE attempted_at >= :from AND attempted_at < :to ORDER BY attempted_at DESC
-- Ohne Index müsste PostgreSQL bei vielen Einträgen die ganze Tabelle lesen.
-- Mit diesem B-Tree-Index findet es den Bereich direkt und liefert ihn schon sortiert.
CREATE INDEX idx_access_attempt_attempted_at ON access_attempt (attempted_at);
