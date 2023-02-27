alter table budget
add author_id int,
add constraint fk_author_budget foreign key (author_id) references author (id)
    on delete cascade;