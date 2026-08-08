create table candidate_resumes (
    id uuid primary key default gen_random_uuid(),
    candidate_id uuid not null unique references users(id),
    file_name varchar(180) not null,
    content_type varchar(80) not null,
    size_bytes bigint not null,
    storage_key varchar(500) not null unique,
    uploaded_at timestamp with time zone not null
);

create index idx_candidate_resumes_candidate on candidate_resumes(candidate_id);
