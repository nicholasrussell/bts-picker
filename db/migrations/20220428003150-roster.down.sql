ALTER TABLE rosters
DROP CONSTRAINT IF EXISTS fk_player;
--;;
ALTER TABLE rosters
DROP CONSTRAINT IF EXISTS fk_team;
--;;
DROP TABLE IF EXISTS rosters;
