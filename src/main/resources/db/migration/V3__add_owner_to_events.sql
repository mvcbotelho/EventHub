-- Stage 1 events had no owner; clear test data before adding the FK.
DELETE FROM events;

ALTER TABLE events
    ADD COLUMN owner_id UUID NOT NULL REFERENCES users (id);

CREATE INDEX idx_events_owner_id ON events (owner_id);
