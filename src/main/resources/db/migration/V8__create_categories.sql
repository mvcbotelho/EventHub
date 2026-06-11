CREATE TABLE categories (
    id         UUID PRIMARY KEY,
    name       VARCHAR(100) NOT NULL UNIQUE,
    slug       VARCHAR(100) NOT NULL UNIQUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

INSERT INTO categories (id, name, slug) VALUES
    ('11111111-1111-1111-1111-111111111101', 'Meetup', 'meetup'),
    ('11111111-1111-1111-1111-111111111102', 'Workshop', 'workshop'),
    ('11111111-1111-1111-1111-111111111103', 'Conference', 'conference');

ALTER TABLE events
    ADD COLUMN category_id UUID REFERENCES categories (id);

CREATE INDEX idx_events_category_id ON events (category_id);
