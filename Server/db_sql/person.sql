CREATE TABLE person(
    id serial primary key,
    name text NOT NULL,
    weight integer NOT NULL,
    eyeColor color NOT NULL,
    hairColor color,
    nationality country NOT NULL,
    CHECK(weight > 0),
    CHECK (name != '')
);