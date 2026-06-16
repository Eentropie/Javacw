# Architecture Decisions

## Decision 01: Console First

The coursework recommends a console application first and treats GUI as optional extra credit. The project therefore prioritizes a reliable console UI and full requirement coverage over Swing or JavaFX.

For the first extra-credit pass, combat simulation was selected before GUI because it directly used the existing hero, equipment, and player data while avoiding GUI toolkit setup risk on a marker's machine. After the console, services, tests, and documentation were stable, optional Swing and web entry points were added as separate extra-credit interfaces without replacing `Main`.

## Decision 02: Dependency-Free CSV Persistence

Claude suggested JSON with Gson as a clean format for nested data. This project uses CSV instead because:

- it satisfies the file I/O requirement;
- it compiles without external libraries;
- it is easy for a marker to inspect;
- the data model stores relationships by ID, which maps cleanly to delimited fields.

The risk is delimiter handling. The implementation uses `|` between fields, `;` for lists, and `:` for match hero picks. A smoke test found a delimiter bug, which was fixed in commit `ccee720`.

## Decision 03: Service Layer Boundaries

`Main` owns console flow only. Business logic is split into:

- `GameDataManager`: record maps, validation, CRUD, and relationship consistency.
- `AuthenticationService`: login.
- `SearchService`: report text for lookups and history.
- `RankingService`: leaderboard and equipment score.
- `RecommendationService`: hero and equipment recommendation formulas.
- `CombatSimulationService`: extra-credit turn-based duel simulation.
- `FileStorageService`: CSV save/load.

This avoids the low-scoring pattern of one large procedural `Main` class.

## Decision 04: Relationship Storage by ID

Model classes store related object IDs rather than direct object references. This makes CSV save/load simpler and avoids cyclic serialization problems. Services resolve IDs into objects when creating reports.

Player equipment is stored as an ID-backed map from owned hero IDs to actually equipped item IDs. Match records also preserve the team represented by each player in that match. The historical team snapshot is necessary because a later player transfer must not rewrite old opponents, results, or team pick-rate reports.

## Decision 05: Tie Handling and Zero-Match Safety

Player win rate returns `0.0` when total matches are zero. Leaderboard ties are resolved by player name and then player ID. Equipment ranking ties are resolved by equipment name and ID.

## Decision 06: Dependency-Free Automated Tests

The coursework mentions that meaningful JUnit tests may receive bonus credit. This repository does not include a JUnit jar or build tool, so automated tests are implemented as a plain Java runner in `src/test/TestRunner.java`. This keeps the tests executable with the same `javac` and `java` commands used for the application.

## Decision 07: Optional Local Web Frontend

The console `Main` entry point remains unchanged. The optional web frontend is added as a second entry point through `web.WebMain`, using JDK `HttpServer` and static HTML/CSS/JS files under `web/`.

This keeps the extra-credit GUI dependency-free:

- no Maven or Gradle setup is required;
- no external JavaScript framework is required;
- the web layer reuses existing services instead of duplicating business logic;
- the one-click macOS launcher compiles, starts the server, and opens the browser.

`WebServer` uses a single-threaded executor because `GameDataManager` stores mutable in-memory collections. This avoids concurrency risk in a local coursework demo.

## Decision 08: Validate Before Mutation

Admin player updates are assembled as a proposed state and validated against teams, heroes, equipment, compatibility, and numeric constraints before the live `Player` object is changed. This prevents a rejected hero or equipment ID from leaving an in-memory partial update that could be saved later.

CSV files use unique temporary filenames rather than one fixed `<file>.tmp` name. This keeps separate local entry points from colliding if they save at nearly the same time.
