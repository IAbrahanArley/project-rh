create table users (
    id bigserial primary key,
    username varchar(80) not null unique,
    password varchar(255) not null,
    full_name varchar(120) not null,
    email varchar(160) not null unique,
    department varchar(80) not null,
    hire_date date not null,
    role varchar(30) not null,
    active boolean not null default true,
    created_at timestamp with time zone not null
);

create table job_openings (
    id bigserial primary key,
    title varchar(140) not null,
    description text not null,
    requirements text not null,
    department varchar(80) not null,
    location varchar(80) not null,
    status varchar(30) not null,
    created_by_id bigint not null references users(id),
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null
);

create table job_applications (
    id bigserial primary key,
    candidate_id bigint not null references users(id),
    job_opening_id bigint not null references job_openings(id),
    motivation text not null,
    status varchar(30) not null,
    feedback text,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null,
    constraint uk_job_applications_candidate_job unique (candidate_id, job_opening_id)
);

create table candidate_evaluations (
    id bigserial primary key,
    application_id bigint not null unique references job_applications(id),
    evaluator_id bigint not null references users(id),
    score integer not null check (score between 1 and 5),
    recommended boolean not null,
    comments text not null,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null
);

create table notifications (
    id bigserial primary key,
    recipient_id bigint not null references users(id),
    subject varchar(140) not null,
    message text not null,
    read_flag boolean not null default false,
    created_at timestamp with time zone not null
);

create index idx_job_openings_status on job_openings(status);
create index idx_job_applications_candidate on job_applications(candidate_id);
create index idx_job_applications_job on job_applications(job_opening_id);
create index idx_notifications_recipient on notifications(recipient_id);
