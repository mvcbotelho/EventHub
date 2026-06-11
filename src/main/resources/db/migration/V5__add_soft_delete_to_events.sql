ALTER TABLE events
    ADD COLUMN deleted_at TIMESTAMPTZ;

CREATE INDEX idx_events_deleted_at ON events (deleted_at);
