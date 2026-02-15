create table if not exists users(
    id bigserial primary key,
    username varchar(50) not null unique,
    password varchar(200) not null,
    role varchar(20) not null
);

create table if not exists user_sessions(
    id uuid primary key,
    username varchar(50) not null,
    device_id varchar(100),
    access_token varchar(512),
    refresh_token varchar(512),
    access_token_expiry timestamp,
    refresh_token_expiry timestamp,
    status varchar(20)
);

create index if not exists idx_user_sessions_refresh on user_sessions(refresh_token);
create index if not exists idx_user_sessions_username on user_sessions(username);
