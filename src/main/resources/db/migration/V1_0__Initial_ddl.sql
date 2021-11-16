CREATE SEQUENCE puls_event_total_id_seq START WITH 1000;

CREATE TABLE puls_event_total(
    id NUMERIC(19,0) NOT NULL DEFAULT NEXTVAL('puls_event_total_id_seq'),
    oid VARCHAR(36) NOT NULL,
    type VARCHAR(255) NOT NULL,
    total NUMERIC(19,0) NOT NULL DEFAULT 1,
    properties JSONB NOT NULL,
    created TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp(),
    updated TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp(),
    PRIMARY KEY(id),
    UNIQUE (oid, type)
);

CREATE SEQUENCE  batch_run_id_seq START WITH 1000;

create table batch_run(
    id NUMERIC(19,0) NOT NULL DEFAULT NEXTVAL('batch_run_id_seq'),
    name VARCHAR(1024) NOT NULL,
    status VARCHAR(36) NOT NULL,
    updated TIMESTAMPTZ NOT NULL DEFAULT  clock_timestamp(),
    total_events INTEGER NOT NULL DEFAULT 0,
    start_time TIMESTAMPTZ NOT NULL,
    end_time TIMESTAMPTZ NOT NULL,
    PRIMARY KEY(id),
    UNIQUE(name)
);

CREATE INDEX batch_run_startime_idx ON batch_run(start_time);
CREATE INDEX batch_run_endtime_idx ON batch_run(end_time);
