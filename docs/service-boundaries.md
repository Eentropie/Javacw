# Service Boundaries

## `GameDataManager`

Owns all in-memory collections and consistency rules:

- stores admins, players, heroes, equipment, teams, and matches;
- rejects duplicate IDs and unknown references;
- keeps team membership synchronized when players are added or moved;
- validates player hero ownership and actual equipment loadouts before applying an update;
- removes player-owned hero references when a hero is deleted;
- removes hero-equipment and player-loadout references when equipment is deleted;
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

It resolves IDs into names, formats win rates, displays actual player equipment loadouts, and uses historical match-team membership so transfers do not rewrite old results. Team pick rates include only the requested team's picks.

## `RankingService`

Owns ranking formulas:

- equipment score;
- player custom score;
- player leaderboard sorting and tie handling.

Tie handling is intentionally centralized here so all menus use the same ranking behavior.

## `RecommendationService`

Owns recommendation formulas:

- recommends heroes for a player based on stats, owner win rate, team role gaps, difficulty fit, equipment support, and whether the player already owns the hero;
- recommends equipment for a hero based on the existing equipment score, explicit hero recommendations, compatibility, and hero-type synergy.

This keeps recommendation rules reusable by the console menu and automated tests.

## `CombatSimulationService`

Owns extra-credit combat simulation:

- validates that each player owns the selected hero;
- validates equipment compatibility;
- auto-picks compatible equipment when the user leaves the equipment prompt blank;
- runs a bounded turn loop with damage, critical hits, dodges, and a winner report.

Randomness is injected through the constructor so automated tests can use a fixed seed.

## `FileStorageService`

Owns CSV persistence:

- detects whether data files exist;
- saves all records through temporary files and rename;
- uses unique same-directory temporary files so concurrent local save attempts do not collide;
- loads records in dependency order: admins, teams, equipment, heroes, players, matches;
- migrates older player and match rows that do not yet include equipment loadouts or historical participant teams;
- rebuilds team membership after loading players.

## `Main`

Owns only console interaction:

- login menu;
- admin menu;
- player menu;
- data-management prompts;
- simple error reporting.

This division prevents the program from becoming one large procedural class.

## `web.WebMain` and `web.WebServer`

Own the optional browser frontend:

- `WebMain` is a second entry point and does not replace `Main`;
- `WebServer` serves static files from `web/`;
- API handlers call existing services for login, lookup, leaderboard, recommendations, combat simulation, and saving;
- responses are JSON so the static frontend can update the page without restarting the Java process;
- leaderboard, equipment, and match-history handlers preserve text reports by default and add `format=json` structured rows for browser tables and CSV export.

The web server uses a single-threaded executor for local-demo safety because the data manager stores mutable collections.
