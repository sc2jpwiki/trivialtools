# --- !Ups
create sequence s_player_id;
create sequence s_reportedgame_id;

CREATE TABLE players (
	id long DEFAULT nextval('s_player_id'),
	name varchar,
	race varchar,
	rating long,
	password varchar);

CREATE TABLE reportedgames (
	id long DEFAULT nextval('s_reportedgame_id'),
	reporter long,
	opponent long,
	win long,
	lose long,
	old_rating_reporter long,
	old_rating_opponent long,
	new_rating_reporter long,
	new_rating_opponent long,
	reported_date date,
	confirmed_date date,
	status varchar);

# --- !Downs

DROP TABLE IF EXISTS players;
DROP sequence s_player_id;
DROP TABLE IF EXISTS reportedgames;
DROP sequence s_reportedgames_id;

