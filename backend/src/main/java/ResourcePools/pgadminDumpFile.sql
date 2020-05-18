CREATE DATABASE postgres;

CREATE TABLE public.users
(
    user_id integer NOT NULL GENERATED ALWAYS AS IDENTITY ( INCREMENT 1 START 1 MINVALUE 1 MAXVALUE 2147483647 CACHE 1 ),
    last_name character varying(255) COLLATE pg_catalog."default" NOT NULL,
    is_active boolean NOT NULL DEFAULT true,
    first_name character varying(255) COLLATE pg_catalog."default" NOT NULL,
    email character varying(255) COLLATE pg_catalog."default" NOT NULL,
    created_at date NOT NULL DEFAULT CURRENT_TIMESTAMP,
    birth_date date NOT NULL,
    phone character varying COLLATE pg_catalog."default" NOT NULL,
    password character varying(255) COLLATE pg_catalog."default" NOT NULL,
    CONSTRAINT "User_pkey" PRIMARY KEY (user_id),
    CONSTRAINT "Unique_email" UNIQUE (email)
);


CREATE TABLE public.education
(
    user_id integer NOT NULL,
    institution character varying(255) COLLATE pg_catalog."default" NOT NULL,
    start_date date NOT NULL,
    end_date date,
    degree character varying(50) COLLATE pg_catalog."default" NOT NULL,
    id integer NOT NULL GENERATED ALWAYS AS IDENTITY ( INCREMENT 1 START 1 MINVALUE 1 MAXVALUE 2147483647 CACHE 1 ),
    CONSTRAINT "Education_pkey" PRIMARY KEY (id),
    CONSTRAINT "Education_pk" UNIQUE (institution, user_id, degree),
    CONSTRAINT user_id FOREIGN KEY (user_id)
        REFERENCES public.users (user_id) MATCH FULL
        ON UPDATE CASCADE
        ON DELETE CASCADE
);


----------------------------------------------------------------
-- Table: public.work

-- DROP TABLE public.work;

CREATE TABLE public.work
(
    user_id bigint NOT NULL,
    institution character varying(255) COLLATE pg_catalog."default" NOT NULL,
    start_date date NOT NULL,
    end_date date,
    job_title character varying(50) COLLATE pg_catalog."default" NOT NULL,
    id integer NOT NULL GENERATED ALWAYS AS IDENTITY ( INCREMENT 1 START 1 MINVALUE 1 MAXVALUE 2147483647 CACHE 1 ),
    CONSTRAINT "Work_pkey" PRIMARY KEY (id),
    CONSTRAINT "Work_pk" UNIQUE (institution, user_id, job_title),
    CONSTRAINT user_id FOREIGN KEY (user_id)
        REFERENCES public.users (user_id) MATCH FULL
        ON UPDATE CASCADE
        ON DELETE CASCADE
);


-----------------------------------------------------------------------------------------
-----------------------------------------------------------------------------------------
-- FUNCTION: public.add_education(integer, character varying, date, date, character varying)

-- DROP FUNCTION public.add_education(integer, character varying, date, date, character varying);

CREATE OR REPLACE FUNCTION public.add_education(user_id integer,institution character varying,start_date date,end_date date,degree character varying) RETURNS void AS $$ BEGIN INSERT INTO education VALUES ($1, $2, $3, $4, $5); END; $$

CREATE OR REPLACE FUNCTION public.add_work(
	user_id integer,
	institution character varying,
	start_date date,
	end_date date,
	job_title character varying)
    RETURNS void


AS $$
BEGIN
INSERT INTO work
VALUES ($1, $2, $3, $4, $5);
END;
$$
LANGUAGE PLPGSQL;

-----------------------------------------------------------------------
-- FUNCTION: public.deactivate(integer)

-- DROP FUNCTION public.deactivate(integer);

CREATE OR REPLACE FUNCTION public.deactivate(
	user_id integer)
    RETURNS boolean


AS $$
DECLARE is_active boolean;
BEGIN
SELECT U.is_active
INTO is_active
FROM users U
WHERE U.user_id = $1;
IF is_active
    THEN
        UPDATE users U
		SET is_active = FALSE
		WHERE U.user_id = $1;
    ELSE
        UPDATE users U
		SET is_active = TRUE
		WHERE U.user_id = $1;
    END IF;
RETURN NOT is_active;
END; $$
LANGUAGE PLPGSQL;

-----------------------------------------------------------------------
-- FUNCTION: public.delete_account(integer)

-- DROP FUNCTION public.delete_account(integer);

CREATE OR REPLACE FUNCTION public.delete_account(
	user_id integer)
    RETURNS void



AS $$
BEGIN
DELETE FROM users U
WHERE U.user_id = $1;
END;
$$
LANGUAGE PLPGSQL;

-----------------------------------------------------------------------
-- FUNCTION: public.edit_user_detail(integer, text, text)

-- DROP FUNCTION public.edit_user_detail(integer, text, text);

CREATE OR REPLACE FUNCTION public.edit_user_detail(
	user_id integer,
	field text,
	value text)
    RETURNS void

AS $$BEGIN
 EXECUTE format('UPDATE users SET %I = %L WHERE user_id = $1;', $2, $3)
                USING ($1);
END;
$$
LANGUAGE PLPGSQL;
-----------------------------------------------------------------------
-- FUNCTION: public.get_education(integer)

-- DROP FUNCTION public.get_education(integer);

CREATE OR REPLACE FUNCTION public.get_education(
	user_id integer)
    RETURNS refcursor


AS $$
DECLARE cursor REFCURSOR := 'cur';

BEGIN
	OPEN cursor FOR
	SELECT * FROM education E
	WHERE E.user_id = $1;
	RETURN cursor;
END;
$$
LANGUAGE PLPGSQL;

-----------------------------------------------------------------------
-- FUNCTION: public.get_password(character varying)

-- DROP FUNCTION public.get_password(character varying);

CREATE OR REPLACE FUNCTION public.get_password(
	email character varying)
    RETURNS character varying


AS $$DECLARE encrypted_password VARCHAR(300);
BEGIN
SELECT U.password
INTO encrypted_password
FROM users U
WHERE U.email = $1;
RETURN encrypted_password;
END;
$$

LANGUAGE PLPGSQL;
-----------------------------------------------------------------------
-- FUNCTION: public.get_work(integer)

-- DROP FUNCTION public.get_work(integer);

CREATE OR REPLACE FUNCTION public.get_work(
	user_id integer)
    RETURNS refcursor

AS $$
DECLARE cursor REFCURSOR := 'cur';

BEGIN
OPEN cursor FOR
SELECT * FROM work W
WHERE W.user_id = $1;
RETURN cursor;
END;
$$
LANGUAGE PLPGSQL;

-----------------------------------------------------------------------
-- FUNCTION: public.login(character varying)

-- DROP FUNCTION public.login(character varying);

CREATE OR REPLACE FUNCTION public.login(
	email character varying)
    RETURNS refcursor


AS $$
DECLARE cursor REFCURSOR := 'cur';
BEGIN
	OPEN cursor FOR
    SELECT *
    FROM users U
    WHERE U.email = $1;
	RETURN cursor;
END;
$$
LANGUAGE PLPGSQL;
-----------------------------------------------------------------------
-- FUNCTION: public.register_user(character varying, character varying, character varying, character varying, date, character varying)

-- DROP FUNCTION public.register_user(character varying, character varying, character varying, character varying, date, character varying);

CREATE OR REPLACE FUNCTION public.register_user(
	first_name character varying,
	last_name character varying,
	email character varying,
	phone character varying,
	birth_date date,
	password character varying)
    RETURNS INTEGER


AS $$
DECLARE userId INTEGER;

BEGIN
	--RETURN QUERY -- appends the results of executing a query to the function's result set.
		INSERT INTO users(first_name, last_name, email, password, birth_date, phone)
		VALUES($1, $2, $3, $6, $5, $4);
	--	RETURNING *; --makes the INSERT statement return the inserted value
	--RETURN; --A final RETURN, which should have no argument, causes control to exit the function
SELECT U.user_id
INTO userId
FROM Users U
WHERE U.email= $3;
RETURN userId;
END;
$$
LANGUAGE PLPGSQL;

-----------------------------------------------------------------------
-- FUNCTION: public.remove_education(integer)

-- DROP FUNCTION public.remove_education(integer);

CREATE OR REPLACE FUNCTION public.remove_education(
	id integer)
    RETURNS void


AS $$
BEGIN
DELETE FROM education E
WHERE E.id = $1;
END;
$$
LANGUAGE PLPGSQL;
-----------------------------------------------------------------------
-- FUNCTION: public.remove_work(integer)

-- DROP FUNCTION public.remove_work(integer);

CREATE OR REPLACE FUNCTION public.remove_work(
	id integer)
    RETURNS void

AS $$
BEGIN
DELETE FROM work W
WHERE W.id = $1;
END;
$$
LANGUAGE PLPGSQL;


-----------------------------------------------------------------------
-- FUNCTION: public.show_profile(integer)

-- DROP FUNCTION public.show_profile(integer);

CREATE OR REPLACE FUNCTION public.show_profile(
	user_id integer)
    RETURNS refcursor

AS $$
DECLARE cursor REFCURSOR := 'cur';
BEGIN
OPEN cursor FOR
(SELECT *, 1 AS type
FROM Users U
LEFT OUTER JOIN Education E
ON E.user_id = U.user_id
WHERE U.user_id = $1)
UNION
(SELECT *, 2 AS type
FROM Users U
INNER JOIN work W
ON W.user_id = U.user_id
WHERE U.user_id = $1);
RETURN cursor;
END;
$$
LANGUAGE PLPGSQL;
-----------------------------------------------------------------------
