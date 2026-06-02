# Service Boundaries

## `GameDataManager`

Owns all in-memory collections and consistency rules:

- stores admins, players, heroes, equipment, teams, and matches;
- rejects duplicate IDs and unknown references;
- keeps team membership synchronized when players are added or moved;
- removes player-owned hero references when a hero is deleted;
- removes hero-equipment references when equipment is deleted;
- blocks unsafe team deletion when players or matches still reference the team.

## `AuthenticationService`

Owns login checks only. It returns a `Person` reference so `Main` can use polymorphism and role checks without knowing whether a user was found in the admin collection or player collection.

## `SearchService`

Owns human-readable reports:

- player lookup;
- team overview;
- hero details;
- equipment statistics;
- player/team match history;
- leaderboard output.

It resolves IDs into names, formats win rates, and keeps reporting logic out of `Main`.

## `RankingService`

Owns ranking formulas:

- equipment score;
- player custom score;
- player leaderboard sorting and tie handling.

Tie handling is intentionally centralized here so all menus use the same ranking behavior.

## `FileStorageService`

Owns CSV persistence:

- detects whether data files exist;
- saves all records through temporary files and rename;
- loads records in dependency order: admins, teams, equipment, heroes, players, matches;
- rebuilds team membership after loading players.

## `Main`

Owns only console interaction:

- login menu;
- admin menu;
- player menu;
- data-management prompts;
- simple error reporting.

This division prevents the program from becoming one large procedural class.
