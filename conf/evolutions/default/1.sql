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
	oldratingreporter long,
	oldratingopponent long,
	newratingreporter long,
	newratingopponent long,
	reporteddate date,
	confirmeddate date,
	status varchar);

# --- !Downs

DROP TABLE IF EXISTS players;
DROP sequence s_player_id;
DROP TABLE IF EXISTS reportedgames;
DROP sequence s_reportedgames_id;

