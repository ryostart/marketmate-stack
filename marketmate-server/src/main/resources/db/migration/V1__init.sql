-- favorites
create table if not exists favorites (
  id uuid primary key,
  user_id uuid not null,
  place_id varchar(80) not null,
  created_at timestamptz not null default now(),
  constraint uk_fav_user_place unique(user_id, place_id)
);

-- reviews
create table if not exists reviews (
  id uuid primary key,
  user_id uuid not null,
  place_id varchar(80) not null,
  rating integer not null check (rating between 1 and 5),
  comment text not null,
  created_at timestamptz not null default now()
);
create index if not exists idx_reviews_place on reviews(place_id);

-- shopping memos
create table if not exists shopping_memos (
  id uuid primary key,
  user_id uuid not null,
  place_id varchar(80) not null,
  title varchar(120),
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);
create index if not exists idx_memo_user on shopping_memos(user_id);

create table if not exists shopping_memo_lines (
  id uuid primary key,
  memo_id uuid not null references shopping_memos(id) on delete cascade,
  pos integer not null,
  text varchar(200) not null
);
