# Architecture Decisions

## Decision 01: Console First

The coursework recommends a console application first and treats GUI as optional extra credit. The project therefore prioritizes a reliable console UI and full requirement coverage over Swing or JavaFX.

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
- `FileStorageService`: CSV save/load.

This avoids the low-scoring pattern of one large procedural `Main` class.

## Decision 04: Relationship Storage by ID

Model classes store related object IDs rather than direct object references. This makes CSV save/load simpler and avoids cyclic serialization problems. Services resolve IDs into objects when creating reports.

## Decision 05: Tie Handling and Zero-Match Safety

Player win rate returns `0.0` when total matches are zero. Leaderboard ties are resolved by player name and then player ID. Equipment ranking ties are resolved by equipment name and ID.
