-- Eventos da etapa 1 não tinham dono; limpa dados de teste antes de adicionar a FK.
DELETE FROM events;

ALTER TABLE events
    ADD COLUMN owner_id UUID NOT NULL REFERENCES users (id);

CREATE INDEX idx_events_owner_id ON events (owner_id);
