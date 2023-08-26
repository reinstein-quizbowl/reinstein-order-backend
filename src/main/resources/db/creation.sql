create table year (
    code text primary key,
    name text not null unique,
    start_date date not null,
    end_date date not null,
    questions_ship_starting date not null,
    maximum_packet_practice_material_price numeric(8, 2) null,
    check(start_date < end_date)
);

create table packet (
    id serial primary key,
    year_code text not null references year,
    number int,
    available_for_competition boolean not null, -- pricing scheme is too complicated for a column; we'll calculate it in code for now
    price_as_practice_material numeric(8, 2) null, -- null means not available as practice material
    unique(year_code, number)
);

create table compilation (
    id serial primary key,
    name text not null unique,
    description text,
    price numeric(8, 2) not null,
    available boolean not null,
    sequence int null
);

create table school (
    id serial primary key,
    name text not null,
    short_name text not null unique, -- I'm not sure about the long-term wisdom of this unique constraint, but at least for now, it seems like a good idea since short_names are basically "IESA name or a constructed analogue"
    address text not null,
    city text not null,
    state text not null,
    postal_code text not null,
    country text not null,
    latitude numeric(8, 4) null,
    longitude numeric(8, 4) null,
    active boolean not null default true,
    coop boolean not null default false,
    iesa_id text null unique, -- Postgres properly implements the SQL standard's allowance of multiple nulls in a unique column
    note text null
);

create table booking_status (
    code text primary key,
    label text not null unique,
    assume_packet_exposure boolean not null,
    sequence int null
);

-- I wanted to call this `order`, but that's a SQL reserved word
create table booking (
    id serial primary key,
    school_id int null references school,
    name text not null,
    email_address text not null,
    authority text null,
    booking_status_code text not null references booking_status,
    ship_date date null,
    payment_received_date date null,
    requests_w9 boolean not null default false,
    external_note text null,
    internal_note text null,
    created_at timestamp not null default now(),
    creator_ip_address text null,
    creation_id text null unique,
    modified_at timestamp
);

create table booking_conference (
    id serial primary key,
    booking_id int not null references booking,
    name text not null,
    packets_requested int not null check(packets_requested >= 0)
);

create table booking_conference_school (
    id serial primary key,
    booking_conference_id int not null references booking_conference,
    school_id int not null references school,
    unique(booking_conference_id, school_id)
);

create table booking_conference_packet (
    id serial primary key,
    booking_conference_id int not null references booking_conference,
    assigned_packet_id int not null references packet,
    unique(booking_conference_id, assigned_packet_id)
);

create table non_conference_game (
    id serial primary key,
    booking_id int not null references booking,
    date date null,
    assigned_packet_id int null references packet
);

create table non_conference_game_school (
    id serial primary key,
    non_conference_game_id int not null references non_conference_game,
    school_id int not null references school,
    unique(non_conference_game_id, school_id)
);

create table booking_practice_packet_order (
    id serial primary key,
    booking_id int not null references booking,
    packet_id int not null references packet
);

create table booking_practice_compilation_order (
    id serial primary key,
    booking_id int not null references booking,
    compilation_id int not null references compilation
);

create table invoice_line (
    id serial primary key,
    booking_id int not null references booking,
    item_type text null,
    item_id int null,
    label text not null,
    quantity int not null default 1,
    unit_cost numeric(8, 2) null,
    sequence int null
);

create or replace view packet_exposure as (
    select cp.assigned_packet_id as packet_id, cs.school_id, 'Conference: ' || c.name as source, cp.booking_conference_id as source_id, b.id as booking_id, b.name as orderer_name
    from booking_conference_packet cp
        join booking_conference c on cp.booking_conference_id = c.id
        join booking_conference_school cs on cs.booking_conference_id = c.id
        join booking b on c.booking_id = b.id
    where b.booking_status_code in (select code from booking_status where assume_packet_exposure = true)

    union all

    select g.assigned_packet_id as packet_id, gs.school_id, 'Non-Conference Game' as source, g.id as source_id, b.id as booking_id, b.name as orderer_name
    from non_conference_game_school gs
        join non_conference_game g on gs.non_conference_game_id = g.id
        join booking b on g.booking_id = b.id
    where b.booking_status_code in (select code from booking_status where assume_packet_exposure = true)

    union all

    select po.packet_id, b.school_id, 'Practice Order' as source, b.id as source_id, b.id as booking_id, b.name as orderer_name
    from booking_practice_packet_order po
        join booking b on po.booking_id = b.id
    where b.booking_status_code in (select code from booking_status where assume_packet_exposure = true)

    -- THINK: Should the school that placed an order be count as exposed to all of its packets, since the coach will be getting them?
);
