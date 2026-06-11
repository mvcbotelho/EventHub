CREATE TABLE events (
    id              UUID PRIMARY KEY,
    title           VARCHAR(255) NOT NULL,
    description     TEXT,
    location        VARCHAR(255) NOT NULL,
    start_date_time TIMESTAMPTZ NOT NULL,
    end_date_time   TIMESTAMPTZ NOT NULL,
    max_participants INTEGER NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_events_end_after_start CHECK (end_date_time >= start_date_time),
    CONSTRAINT chk_events_max_participants_positive CHECK (max_participants > 0)
);

CREATE INDEX idx_events_start_date_time ON events (start_date_time);
