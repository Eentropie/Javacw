# Requirement Coverage Checklist

## Required Classes

| Requirement | Implementation |
|---|---|
| `Person` abstract superclass | `src/model/Person.java` |
| `Player` subclass of `Person` | `src/model/Player.java` |
| `Admin` subclass of `Person` | `src/model/Admin.java` |
| `Hero` | `src/model/Hero.java` |
| `Equipment` | `src/model/Equipment.java` |
| `Team` | `src/model/Team.java` |
| `MatchRecord` | `src/model/MatchRecord.java` |

## Java Concepts

| Concept | Evidence |
|---|---|
| Inheritance | `Player` and `Admin` extend `Person`. |
| Association | `Player` stores owned hero IDs; `Hero` stores compatible equipment IDs. |
| Aggregation/composition | `Team` stores player IDs and is rebuilt from player membership. |
| Interface | `Searchable` is implemented by `Person`, `Hero`, `Equipment`, `Team`, and `MatchRecord`. |
| Encapsulation | Fields are private; list/map fields expose unmodifiable views and controlled mutation methods. |
| Polymorphism | `Main.currentUser` is a `Person`; menus branch on role. |
| Collections | `LinkedHashMap`, `ArrayList`, `List`, `Map`, and stream sorting are used. |
| Exception handling | Login, file loading, invalid IDs, duplicate IDs, invalid enum/input values, and delete conflicts are handled. |
| File I/O | `FileStorageService` saves and loads CSV files in `data/`. |
| Enums | `Role`, `HeroType`, `EquipmentType`, and `MatchResult`. |

## Dataset

| Data type | Requirement | Current count |
|---|---:|---:|
| Teams | At least 3 teams, each with 5 players | 3 teams, 5 players each |
| Players | At least 10 players, each with 3 heroes | 15 players, 3 heroes each |
| Heroes | At least 15 heroes | 15 heroes |
| Equipment | At least 20 items | 20 items |
| Match records | At least 10 records | 11 records |

## Functional Requirements

| Feature | Implementation |
|---|---|
| Player lookup | `SearchService.playerLookup` and menu option 1. |
| Team overview | `SearchService.teamOverview` and menu option 2. |
| Hero details | `SearchService.heroDetails` and menu option 3. |
| Equipment statistics | `RankingService.topEquipment`, `SearchService.equipmentStatistics`, and menu option 4. |
| Match history | `SearchService.playerMatchHistory`, `SearchService.teamMatchHistory`, and menu option 5. |
| Leaderboard | `RankingService.topPlayers`, `SearchService.leaderboard`, and menu option 6. |
| Data management | Admin data-management menu supports add/edit/delete for players, heroes, equipment, teams, and match records. |
| Authentication | `AuthenticationService` supports Admin and Player login. |
| Player permissions | Player menu excludes data management and allows only limited profile edits. |

## Documentation and Evidence

| Required artifact | Status |
|---|---|
| `plan.md` | Present |
| `README.md` | Present |
| `docs/design.md` | Present |
| UML draft | `docs/uml.md` |
| `docs/test-cases.md` | Present with actual results |
| `ai/prompts.md` | Present |
| `ai/agent-log.md` | Present |
| `ai/reflection.md` | Present, but final student personalization is still needed |
| `git-history.txt` | To be exported after final commit |

## Remaining Student Work Before Submission

- Add at least four genuine human-authored planning/debugging/refactoring commits. Do not fake these.
- Personalize `ai/reflection.md`, especially questions 7, 9, and 10.
- Re-run tests on the final submitted machine if using a different Java version.
