CREATE TABLE event_waitlist (
    id         UUID PRIMARY KEY,
    event_id   UUID NOT NULL REFERENCES events (id) ON DELETE CASCADE,
    user_id    UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    joined_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (event_id, user_id)
);

CREATE INDEX idx_event_waitlist_event_joined ON event_waitlist (event_id, joined_at);
