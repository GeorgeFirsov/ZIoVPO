create extension if not exists "pgcrypto"@@

do $$
begin
    if exists (
        select 1
        from information_schema.columns
        where table_schema = 'public'
          and table_name = 'users'
          and column_name = 'username'
    ) then
        if not exists (
            select 1
            from information_schema.tables
            where table_schema = 'public'
              and table_name = 'users_legacy'
        ) then
            alter table users rename to users_legacy;
        end if;
    end if;
end $$@@

create table if not exists users (
    id uuid primary key default gen_random_uuid(),
    name varchar(100) not null unique,
    password_hash varchar(200) not null,
    email varchar(255) unique,
    role varchar(50) not null,
    is_account_expired boolean not null default false,
    is_account_locked boolean not null default false,
    is_credentials_expired boolean not null default false,
    is_disabled boolean not null default false
)@@

do $$
begin
    if exists (
        select 1
        from information_schema.tables
        where table_schema = 'public'
          and table_name = 'users_legacy'
    ) then
        insert into users (name, password_hash, role, email, is_account_expired, is_account_locked, is_credentials_expired, is_disabled)
        select ul.username, ul.password, ul.role, null, false, false, false, false
        from users_legacy ul
        where not exists (
            select 1 from users u where u.name = ul.username
        );
    end if;
end $$@@

create table if not exists product (
    id uuid primary key default gen_random_uuid(),
    name varchar(150) not null,
    is_blocked boolean not null default false
)@@

create table if not exists license_type (
    id uuid primary key default gen_random_uuid(),
    name varchar(100) not null,
    default_duration_in_days int not null,
    description varchar(500)
)@@

create table if not exists license (
    id uuid primary key default gen_random_uuid(),
    code varchar(150) not null unique,
    user_id uuid references users(id),
    product_id uuid not null references product(id),
    type_id uuid not null references license_type(id),
    first_activation_date date,
    ending_date date,
    blocked boolean not null default false,
    device_count int not null default 1,
    owner_id uuid references users(id),
    description varchar(1000)
)@@

create table if not exists device (
    id uuid primary key default gen_random_uuid(),
    name varchar(150) not null,
    mac_address varchar(100) not null unique,
    user_id uuid not null references users(id)
)@@

create table if not exists device_license (
    id uuid primary key default gen_random_uuid(),
    license_id uuid not null references license(id),
    device_id uuid not null references device(id),
    activation_date date not null,
    unique (license_id, device_id)
)@@

create table if not exists license_history (
    id uuid primary key default gen_random_uuid(),
    license_id uuid not null references license(id),
    user_id uuid not null references users(id),
    status varchar(100) not null,
    change_date timestamp not null,
    description varchar(1000)
)@@

create table if not exists user_sessions(
    id uuid primary key,
    username varchar(50) not null,
    device_id varchar(100),
    access_token varchar(512),
    refresh_token varchar(512),
    access_token_expiry timestamp,
    refresh_token_expiry timestamp,
    status varchar(20)
)@@

create index if not exists idx_user_sessions_refresh on user_sessions(refresh_token)@@
create index if not exists idx_user_sessions_username on user_sessions(username)@@

create index if not exists idx_license_product_id on license(product_id)@@
create index if not exists idx_license_type_id on license(type_id)@@
create index if not exists idx_license_owner_id on license(owner_id)@@
create index if not exists idx_license_user_id on license(user_id)@@

create index if not exists idx_device_user_id on device(user_id)@@

create index if not exists idx_device_license_license_id on device_license(license_id)@@
create index if not exists idx_device_license_device_id on device_license(device_id)@@

create index if not exists idx_license_history_license_id on license_history(license_id)@@
create index if not exists idx_license_history_user_id on license_history(user_id)@@