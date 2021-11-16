create sequence puls_event_total_id_seq start with 1000;

create table puls_event_total(
    id numeric(19,0) not null default nextval('puls_event_total_id_seq'),
    oid varchar(36) not null,
    type varchar(255) not null,
    total numeric(19,0) not null default 1,
    properties jsonb not null,
    created timestamptz not null default clock_timestamp(),
    updated timestamptz not null default clock_timestamp(),
    primary key(id),
    unique (oid, type)
);

create sequence  batch_run_id_seq start with 1000;

create table batch_run(
    id numeric(19,0) not null default nextval('batch_run_id_seq'),
    name varchar(1024) not null,
    status varchar(36) not null,
    updated timestamptz not null default  clock_timestamp(),
    total_events integer not null default 0,
    start_time timestamptz not null,
    end_time timestamptz not null,
    primary key(id),
    unique(name)
);

create index batch_run_startime_idx on batch_run(start_time);
create index batch_run_endtime_idx on batch_run(end_time);

create sequence  outbox_id_seq start with 1000;

create table outbox(
    id  numeric(19,0) not null default nextval('outbox_id_seq'),
    oid varchar(36) not null,
    type varchar(255) not null,
    status varchar(255) not null,
    payload jsonb not null,
    updated timestamptz not null default clock_timestamp(),
    primary key (id)
);

create index outbox_status on outbox(status);
create index outbox_updated on outbox(updated);
