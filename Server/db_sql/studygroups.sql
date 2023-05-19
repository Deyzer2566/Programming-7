CREATE TABLE studygroups
(
    id     bigserial primary key,
    name   text NOT NULL,
    coords integer references coordinates,
    creationTime timestamp with time zone,
    studentCount bigint,
    expelledStudents bigint,
    shouldBeExpelled int NOT NULL,
    sem semester,
    admin integer references person,
    whoCreated integer references users NOT NULL,
    CHECK (name != ''),
    CHECK (studentCount > 0),
    CHECK (expelledStudents > 0),
    CHECK (shouldBeExpelled > 0)
);
