# Project Plan

## 1. Project Goal

The goal is to build a Java console application that manages Honor of Kings information for two kinds of users: admins and players. Admins manage all data. Players can view public information, view their own profile, view their heroes and match history, and edit limited personal information.

## 2. Requirement Analysis

- Player lookup: search by ID or name and display player profile, team, level, win rate, owned heroes, and hero equipment.
- Team overview: search by ID or name and display members, average level, total matches, win rate, and top player.
- Hero details: search by hero name and display type, base stats, compatible equipment, owners, and recommendations where available.
- Equipment statistics: rank equipment using a documented score based on usage, rating, compatible hero count, and win contribution.
- Match history: retrieve the last N matches for a player or team and display opponent, date, result, hero picks, win/loss record, and hero pick rate.
- Leaderboard: display top X players by win rate, level, match count, or custom score. Ties are handled by name and then ID.
- Recommendation engine: recommend heroes for a player and equipment for a hero using documented formulas.
- Combat simulation: run a turn-based duel using hero stats, compatible equipment, critical hits, dodges, and a combat report.
- Data management: admin users can add, delete, and edit players, heroes, equipment, teams, and match records.
- Authentication: simple username/password login with Admin and Player roles.
- Persistence: CSV files are used for save and load.

## 3. Java Concepts Used

- Inheritance: `Person` is an abstract superclass; `Player` and `Admin` extend it.
- Association: `Player` stores owned hero IDs and actual per-hero equipment loadouts; `Hero` stores compatible equipment IDs.
- Aggregation: `Team` stores member player IDs.
- Interface: `Searchable` is implemented by searchable model classes.
- Encapsulation: fields are private and changed through methods.
- Polymorphism: menu access is based on `Person` references and role checks.
- Collections: maps store records by ID; lists store relationships and sorted reports.
- Exception handling: input validation, duplicate IDs, missing records, and file loading are handled.
- File I/O: CSV files store players, heroes, equipment, teams, and matches.
- Enums: role, hero type, equipment type, and match result use enums.

## 4. Class Design

- `Person`: abstract user superclass with ID, name, username, password, and role.
- `Player`: game player with team ID, level, win/loss counts, owned heroes, and actual equipment loadouts.
- `Admin`: administrator with full data-management permission.
- `Hero`: playable hero with type, base stats, difficulty, compatible equipment, and recommended equipment.
- `Equipment`: item with type, stats, usage count, rating, and win contribution.
- `Team`: team containing multiple player IDs.
- `MatchRecord`: match result with date, teams, winner, and player hero picks.
- `GameDataManager`: central data store and data consistency rules.
- `AuthenticationService`: login and logout support.
- `SearchService`: human-readable lookup reports.
- `RankingService`: leaderboards, equipment ranking, and tie rules.
- `RecommendationService`: hero and equipment recommendation formulas.
- `CombatSimulationService`: turn-based duel simulation.
- `CombatReport`: formatted result and turn log for combat simulation.
- `FileStorageService`: CSV persistence.
- `DataInitializer`: seed data meeting the minimum dataset.
- `InputHelper`: robust console input.

## 5. UML Draft

```text
Searchable <|.. Person
Searchable <|.. Hero
Searchable <|.. Equipment
Searchable <|.. Team

Person <|-- Player
Person <|-- Admin

Player "many heroIds" --> Hero
Hero "many compatibleEquipmentIds" --> Equipment
Team "many playerIds" o-- Player
MatchRecord --> Team
MatchRecord --> Player
MatchRecord --> Hero

Main --> AuthenticationService
Main --> GameDataManager
Main --> SearchService
Main --> RankingService
Main --> RecommendationService
Main --> CombatSimulationService
GameDataManager --> FileStorageService
GameDataManager --> DataInitializer
RecommendationService --> Hero
RecommendationService --> Equipment
CombatSimulationService --> CombatReport
```

## 6. Data Design

The initial dataset includes at least 3 teams, 10 players, 15 heroes, 20 equipment items, and 10 match records. CSV is selected because it needs no external libraries and is easy to inspect. Relationship fields store IDs separated by semicolons. Player loadouts use `heroId:equipmentId,equipmentId`, and match picks use `playerId:heroId:historicalTeamId` so later transfers do not change old reports.

## 7. AI Usage Plan

- Architect Agent: class design, UML draft, service boundaries, risk review.
- Implementation Agent: selected methods and service implementation support.
- Testing/Reviewer Agent: test cases, code review, bug discovery.
- Documentation Agent: README, test documentation, and reflection improvement.

AI output must be checked manually before being accepted. Large one-shot code generation is avoided.

## 8. Prompt Strategy

Prompts should specify one role, one task, existing class names, edge cases, and whether full code is allowed. Each important prompt is recorded in `ai/prompts.md` with the response summary, decision, and related commit.

## 9. Development Timeline

1. Read requirements and create repository/document skeleton.
2. Ask Architect Agent for design feedback and revise the plan.
3. Implement model classes, enums, and interfaces.
4. Add initial data.
5. Implement search, ranking, authentication, and persistence services.
6. Implement console menu and admin/player permission flow.
7. Compile and run manual tests.
8. Use reviewer agent and fix defects.
9. Add extra-credit features: recommendation engine, combat simulation, advanced AI comparison, and automated tests.
10. Finalize AI evidence, reflection, README, and Git history.

## 10. Testing Plan

Manual test cases cover login, player lookup, team overview, hero details, equipment ranking, match history, leaderboard, admin CRUD, player permission restrictions, file save/load, recommendation output, combat simulation, and invalid input. Automated regression tests additionally cover actual equipment loadouts, team-filtered pick rates, historical results after transfers, atomic rejected updates, and concurrent saves.

Automated tests are run with:

```bash
javac -d out $(find src -name '*.java')
java -cp out test.TestRunner
```

## 11. Risk Analysis

- Risk: Too much logic in `Main`. Mitigation: keep data, search, ranking, auth, and storage in services.
- Risk: CSV parsing errors. Mitigation: use simple delimiters, validation, and fallback seed data.
- Risk: Inconsistent relationships after deletion. Mitigation: centralize delete methods in `GameDataManager`.
- Risk: Fake or weak AI evidence. Mitigation: record prompts as they are used and avoid fabricated decisions.
- Risk: Git evidence not human-authored. Mitigation: AI commits are labeled honestly; the student should add real human review/debug/refactor commits before submission.

## 12. Final Reflection Placeholder

Final reflection is maintained in `ai/reflection.md` after implementation and testing.
