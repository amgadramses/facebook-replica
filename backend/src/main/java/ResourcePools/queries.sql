CREATE TABLE public.users(user_id integer NOT NULL GENERATED ALWAYS AS IDENTITY ( INCREMENT 1 START 1 MINVALUE 1 MAXVALUE 2147483647 CACHE 1 ), last_name character varying(255) COLLATE pg_catalog."default" NOT NULL, is_active boolean NOT NULL DEFAULT true, first_name character varying(255) COLLATE pg_catalog."default" NOT NULL, email character varying(255) COLLATE pg_catalog."default" NOT NULL, created_at date NOT NULL DEFAULT CURRENT_TIMESTAMP, birth_date date NOT NULL, phone character varying COLLATE pg_catalog."default" NOT NULL, password character varying(255) COLLATE pg_catalog."default" NOT NULL, CONSTRAINT "User_pkey" PRIMARY KEY (user_id), CONSTRAINT "Unique_email" UNIQUE (email));
CREATE TABLE public.education(user_id integer NOT NULL, institution character varying(255) COLLATE pg_catalog."default" NOT NULL, start_date date NOT NULL, end_date date, degree character varying(50) COLLATE pg_catalog."default" NOT NULL, id integer NOT NULL GENERATED ALWAYS AS IDENTITY ( INCREMENT 1 START 1 MINVALUE 1 MAXVALUE 2147483647 CACHE 1 ), CONSTRAINT "Education_pkey" PRIMARY KEY (id), CONSTRAINT "Education_pk" UNIQUE (institution, user_id, degree), CONSTRAINT user_id FOREIGN KEY (user_id) REFERENCES public.users (user_id) MATCH FULL ON UPDATE CASCADE ON DELETE CASCADE);
CREATE TABLE public.work(user_id bigint NOT NULL, institution character varying(255) COLLATE pg_catalog."default" NOT NULL, start_date date NOT NULL, end_date date, job_title character varying(50) COLLATE pg_catalog."default" NOT NULL, id integer NOT NULL GENERATED ALWAYS AS IDENTITY ( INCREMENT 1 START 1 MINVALUE 1 MAXVALUE 2147483647 CACHE 1 ), CONSTRAINT "Work_pkey" PRIMARY KEY (id), CONSTRAINT "Work_pk" UNIQUE (institution, user_id, job_title), CONSTRAINT user_id FOREIGN KEY (user_id) REFERENCES public.users (user_id) MATCH FULL ON UPDATE CASCADE ON DELETE CASCADE);
CREATE OR REPLACE FUNCTION public.add_education(user_id integer,institution character varying,start_date date,end_date date,degree character varying) RETURNS void AS $$ BEGIN INSERT INTO education VALUES ($1, $2, $3, $4, $5); END; $$ LANGUAGE PLPGSQL;
CREATE OR REPLACE FUNCTION public.add_work(user_id integer,institution character varying,start_date date,end_date date,job_title character varying) RETURNS void AS $$ BEGIN INSERT INTO work VALUES ($1, $2, $3, $4, $5); END;$$ LANGUAGE PLPGSQL;
CREATE OR REPLACE FUNCTION public.deactivate(user_id integer) RETURNS boolean AS $$ DECLARE is_active boolean; BEGIN SELECT U.is_active INTO is_active FROM users U WHERE U.user_id = $1; IF is_active THEN UPDATE users U SET is_active = FALSE WHERE U.user_id = $1; ELSE UPDATE users U SET is_active = TRUE WHERE U.user_id = $1; END IF; RETURN NOT is_active; END; $$ LANGUAGE PLPGSQL;
CREATE OR REPLACE FUNCTION public.delete_account(user_id integer) RETURNS void AS $$ BEGIN DELETE FROM users U WHERE U.user_id = $1; END; $$ LANGUAGE PLPGSQL;
CREATE OR REPLACE FUNCTION public.edit_user_detail(user_id integer, field text, value text) RETURNS void AS $$BEGIN EXECUTE format('UPDATE users SET %I = %L WHERE user_id = $1;', $2, $3) USING ($1); END; $$ LANGUAGE PLPGSQL;
CREATE OR REPLACE FUNCTION public.get_education(user_id integer) RETURNS refcursor AS $$ DECLARE cursor REFCURSOR := 'cur'; BEGIN OPEN cursor FOR SELECT * FROM education E WHERE E.user_id = $1; RETURN cursor; END; $$ LANGUAGE PLPGSQL;
CREATE OR REPLACE FUNCTION public.get_password(email character varying) RETURNS character varying AS $$ DECLARE encrypted_password VARCHAR(300); BEGIN SELECT U.password INTO encrypted_password FROM users U WHERE U.email = $1; RETURN encrypted_password; END; $$ LANGUAGE PLPGSQL;
CREATE OR REPLACE FUNCTION public.get_work(user_id integer) RETURNS refcursor AS $$ DECLARE cursor REFCURSOR := 'cur'; BEGIN OPEN cursor FOR SELECT * FROM work W WHERE W.user_id = $1; RETURN cursor; END; $$ LANGUAGE PLPGSQL;
CREATE OR REPLACE FUNCTION public.login(email character varying) RETURNS refcursor AS $$ DECLARE cursor REFCURSOR := 'cur'; isActive BOOLEAN; userID INTEGER; BEGIN SELECT U.user_id, U.is_active INTO userID, isActive FROM users U WHERE U.email = $1; IF NOT isActive THEN UPDATE users U SET is_active = TRUE WHERE U.user_id = userID; END IF; OPEN cursor FOR SELECT * FROM users U WHERE U.email = $1;RETURN cursor; END; $$ LANGUAGE PLPGSQL;
CREATE OR REPLACE FUNCTION public.register_user( first_name character varying, last_name character varying, email character varying, phone character varying, birth_date date, password character varying) RETURNS INTEGER AS $$ DECLARE userId INTEGER; BEGIN INSERT INTO users(first_name, last_name, email, password, birth_date, phone) VALUES($1, $2, $3, $6, $5, $4); SELECT U.user_id INTO userId FROM Users U WHERE U.email= $3; RETURN userId; END; $$ LANGUAGE PLPGSQL;
CREATE OR REPLACE FUNCTION public.remove_education(id integer)RETURNS void AS $$ BEGIN DELETE FROM education E WHERE E.id = $1; END; $$ LANGUAGE PLPGSQL;
CREATE OR REPLACE FUNCTION public.remove_work(id integer) RETURNS void AS $$ BEGIN DELETE FROM work W WHERE W.id = $1; END; $$ LANGUAGE PLPGSQL;
CREATE OR REPLACE FUNCTION public.show_profile(user_id integer) RETURNS refcursor AS $$ DECLARE cursor REFCURSOR := 'cur'; BEGIN OPEN cursor FOR (SELECT *, 1 AS type FROM Users U LEFT OUTER JOIN Education E ON E.user_id = U.user_id WHERE U.user_id = $1) UNION (SELECT *, 2 AS type FROM Users U INNER JOIN work W ON W.user_id = U.user_id WHERE U.user_id = $1); RETURN cursor; END; $$ LANGUAGE PLPGSQL;

INSERT INTO users(first_name, last_name, email, birth_date, phone, password) values('mario', 'speedwagon', 'mario@fakemail.com','1990-10-10', '0102030400', 'a54e71f0e17f5aaf7946e66ab42cf3b1fd4e61d60581736c9f0eb1c3f794eb7c');
INSERT INTO users(first_name, last_name, email, birth_date, phone, password) values('petey', 'cruiser', 'petey@fakemail.com','1991-10-10', '0020304050', 'a54e71f0e17f5aaf7946e66ab42cf3b1fd4e61d60581736c9f0eb1c3f794eb7c');
INSERT INTO users(first_name, last_name, email, birth_date, phone, password) values('anna', 'sthesia', 'anna@fakemail.com','1992-10-10', '0120304050', 'a54e71f0e17f5aaf7946e66ab42cf3b1fd4e61d60581736c9f0eb1c3f794eb7c');
INSERT INTO users(first_name, last_name, email, birth_date, phone, password) values('paul', 'molive', 'paul@fakemail.com','1993-10-10', '0102304050', 'a54e71f0e17f5aaf7946e66ab42cf3b1fd4e61d60581736c9f0eb1c3f794eb7c');
INSERT INTO users(first_name, last_name, email, birth_date, phone, password) values('anna', 'maul', 'maul@fakemail.com','1994-10-10', '0102030050', 'a54e71f0e17f5aaf7946e66ab42cf3b1fd4e61d60581736c9f0eb1c3f794eb7c');
INSERT INTO users(first_name, last_name, email, birth_date, phone, password) values('gail', 'forcewind', 'gail@fakemail.com','1995-10-10', '0100304050', 'a54e71f0e17f5aaf7946e66ab42cf3b1fd4e61d60581736c9f0eb1c3f794eb7c');
INSERT INTO users(first_name, last_name, email, birth_date, phone, password) values('youtham', 'joseph', 'youtham@fakemail.com','1996-10-10', '0020304050', 'a54e71f0e17f5aaf7946e66ab42cf3b1fd4e61d60581736c9f0eb1c3f794eb7c');
INSERT INTO users(first_name, last_name, email, birth_date, phone, password) values('amgad', 'ashraf', 'amgad@fakemail.com','1997-10-10', '0102304050', 'a54e71f0e17f5aaf7946e66ab42cf3b1fd4e61d60581736c9f0eb1c3f794eb7c');
INSERT INTO users(first_name, last_name, email, birth_date, phone, password) values('akram', 'ashraf', 'akram@fakemail.com','1998-10-10', '0102304050', 'a54e71f0e17f5aaf7946e66ab42cf3b1fd4e61d60581736c9f0eb1c3f794eb7c');
INSERT INTO users(first_name, last_name, email, birth_date, phone, password) values('shady', 'younan', 'shady@fakemail.com','1999-10-10', '0102004050', 'a54e71f0e17f5aaf7946e66ab42cf3b1fd4e61d60581736c9f0eb1c3f794eb7c');
INSERT INTO education(user_id, institution, start_date, end_date, degree) values(1, 'guc', '2005-10-10', null, 'bachelor');
INSERT INTO work(user_id, institution, start_date, end_date, job_title) values(1, 'instagram', '2015-10-10', null, 'software engineer');
INSERT INTO education(user_id, institution, start_date, end_date, degree) values(1, 'nrc', '2002-10-10', '2005-10-10', 'high school');
INSERT INTO work(user_id, institution, start_date, end_date, job_title) values(2, 'facebook', '2013-10-10', '2015-10-10', 'developer');
INSERT INTO education(user_id, institution, start_date, end_date, degree) values(2, 'auc', '2010-10-10', '2012-05-05', 'bachelor');
INSERT INTO work(user_id, institution, start_date, end_date, job_title) values(2, 'google', '2015-10-11', null, 'frontend engineer');
INSERT INTO education(user_id, institution, start_date, end_date, degree) values(2, 'dls', '1998-10-10', '2000-10-10', 'high school');
INSERT INTO work(user_id, institution, start_date, end_date, job_title) values(3, 'deepmind', '2021-10-10', null, 'data scientist');
INSERT INTO education(user_id, institution, start_date, end_date, degree) values(4, 'bue', '2015-10-10', null, 'bachelor');
INSERT INTO work(user_id, institution, start_date, end_date, job_title) values(5, 'ibm', '2019-11-01', null, 'full stack developer');
INSERT INTO education(user_id, institution, start_date, end_date, degree) values(5, 'abc', '2005-06-07', null, 'phd');
INSERT INTO work(user_id, institution, start_date, end_date, job_title) values(5, 'google', '2015-10-10', '2019-10-10', 'postman');
INSERT INTO education(user_id, institution, start_date, end_date, degree) values(6, 'msa', '2017-10-06', '2020-03-03', 'bachelor');
INSERT INTO work(user_id, institution, start_date, end_date, job_title) values(6, 'nbe', '2020-05-10', null, 'teller');
INSERT INTO education(user_id, institution, start_date, end_date, degree) values(7, 'dls', '2001-10-01', '2013-05-01', 'e3dadeya');
INSERT INTO education(user_id, institution, start_date, end_date, degree) values(7, 'nrc', '2013-10-10', null, 'igcse');
INSERT INTO education(user_id, institution, start_date, end_date, degree) values(8, 'guc', '2015-10-01', null, 'bachelor');
INSERT INTO work(user_id, institution, start_date, end_date, job_title) values(8, 'microsoft', '2019-01-01', null, 'machine learning engineer');
INSERT INTO education(user_id, institution, start_date, end_date, degree) values(9, 'nrc', '2001-08-31', null, 'sanaweya');
INSERT INTO work(user_id, institution, start_date, end_date, job_title) values(9, 'sony', '2019-10-10', null, 'game developer');
INSERT INTO work(user_id, institution, start_date, end_date, job_title) values(10, 'instagram', '2018-04-03', null, 'professor');