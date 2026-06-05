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
| Association | `Player`, `Hero`, and `Equipment` store stable IDs for CSV persistence; `GameDataManager` resolves them into domain-object relationships with `teamForPlayer`, `heroesForPlayer`, `compatibleEquipmentForHero`, `recommendedEquipmentForHero`, and `playersOwningHero`. |
| Aggregation/composition | `Team` stores player IDs and is rebuilt from player membership; `playersForTeam` exposes team membership as `Player` objects. |
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
| Recommendation engine | `RecommendationService` and console recommendation options. |
| Combat simulation | `CombatSimulationService`, `CombatReport`, and console combat simulation options. |
| Optional desktop app | `gui.DesktopMain` provides a Java Swing local app for login, reports, recommendations, combat, player profile edits, admin CRUD, save, and reload without using a web server. |
| Optional web frontend | `web.WebMain`, `web.WebServer`, and static assets in `web/` provide browser access to core features. |
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
| Automated tests | `src/test/TestRunner.java` runs 15 dependency-free automated service tests |
| One-click desktop launcher | `Open-JavaCW-Desktop.command` starts the optional Swing desktop app on macOS; `Open-JavaCW-Desktop.bat` provides the Windows launcher |
| One-click web launcher | `Open-JavaCW-Web.command` starts the optional browser frontend on macOS; `Open-JavaCW-Web.bat` provides the Windows launcher |
| `ai/prompts.md` | Present |
| `ai/agent-log.md` | Present |
| `ai/reflection.md` | Present, with complete draft answers |
| Advanced AI comparison | `ai/model-comparison.md` |
| Submission audit | `docs/submission-audit.md` |
| Rubric self-evaluation | `docs/rubric-evaluation.md` |
| AI usage evidence map | `docs/ai-usage-evidence.md` |
| Git process evidence map | `docs/git-process-evidence.md` |
| `git-history.txt` | To be exported after final commit |

## Extra Credit / Creativity Evidence

| Feature | Evidence |
|---|---|
| Recommendation engine | Hero and equipment recommendation formulas in `RecommendationService` and `docs/design.md`. |
| Combat simulation | Turn-based duel with compatible equipment, critical hits, dodges, bounded turns, and a `CombatReport`. |
| Desktop app | Dependency-free Swing interface with macOS and Windows one-click launchers. |
| Browser frontend | Dependency-free local web UI using JDK `HttpServer`, static HTML/CSS/JS, and a one-click launcher. |
| Advanced AI reflection | `ai/model-comparison.md` compares Gemini and Claude Opus review quality. |
| Automated tests | `src/test/TestRunner.java` covers recommendations, combat, authentication failure, missing lookups, CRUD cascade behavior, validation, object association helpers, leaderboard, win-rate edge cases, text-report compatibility, and CSV round trip. |

## Final Student Checks Before Submission

- Genuine human-authored review/reflection commits are already present in Git history. Do not add fake human commits.
- Re-run tests on the final submitted machine if using a different Java version or Windows.
- Re-export `git-history.txt` after any further commit sequence.
