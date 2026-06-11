CREATE TABLE event_registrations (
    id            UUID PRIMARY KEY,
    event_id      UUID NOT NULL REFERENCES events (id) ON DELETE CASCADE,
    user_id       UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    status        VARCHAR(20) NOT NULL,
    registered_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_event_registrations_event_user UNIQUE (event_id, user_id),
    CONSTRAINT chk_event_registrations_status CHECK (status IN ('CONFIRMED', 'CANCELED'))
);

CREATE INDEX idx_event_registrations_event_id ON event_registrations (event_id);
CREATE INDEX idx_event_registrations_user_id ON event_registrations (user_id);
CREATE INDEX idx_event_registrations_status ON event_registrations (event_id, status);
