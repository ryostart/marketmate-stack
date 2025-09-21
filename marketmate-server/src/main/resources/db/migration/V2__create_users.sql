create table if not exists users (
  id uuid primary key,
  email varchar(255) not null unique,
  password varchar(100) not null,
  display_name varchar(100) not null,
  created_at timestamptz not null default now()
);
