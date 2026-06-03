# AI-Assisted Honor of Kings Information Management System

## 1. Project Overview

This project is a console-first Java information management system for Honor of Kings. It manages players, admins, heroes, equipment, teams, and match records. The system demonstrates Java OOP design, role-based access, searching, ranking, file I/O, and documented AI-assisted development.

## 2. How to Run

### Console version

Compile from the project root:

```bash
javac -d out $(find src -name '*.java')
```

Run:

```bash
java -cp out Main
```

The application creates CSV data files in the `data/` folder if they do not already exist.

### Optional web frontend

On macOS, double-click:

```text
Open-JavaCW-Web.command
```

This compiles the project, starts the local web server, and opens the browser automatically.

Manual web command:

```bash
javac -d out $(find src -name '*.java')
java -cp out web.WebMain
```

The original console entry point remains `Main`.

## 3. Default Login Accounts

Admin:

```text
Username: admin
Password: admin123
```

Example player:

```text
Username: libai
Password: player123
```

## 4. Implemented Features

- Login and logout with Admin and Player roles.
- Player lookup by ID or name.
- Team overview by ID or name.
- Hero details by name.
- Equipment ranking by a documented score.
- Match history for players and teams.
- Leaderboard by win rate, level, match count, or custom score.
- Recommendation engine for hero and equipment suggestions.
- Turn-based combat simulation with equipment, critical hits, dodges, and a combat report.
- Optional browser-based frontend for login, lookup, rankings, recommendations, and combat simulation.
- Admin data management for players, heroes, equipment, teams, and match records.
- Player self-service for limited personal profile edits.
- CSV save and load.

## 5. Java Concepts Used

- Inheritance: `Player` and `Admin` extend abstract `Person`.
- Interface: searchable domain objects implement `Searchable`.
- Encapsulation: model fields are private with controlled methods.
- Polymorphism: authenticated users are handled through `Person` references.
- Collections: `ArrayList`, `LinkedHashMap`, `HashMap`, and `Comparator`.
- Exception handling: invalid input, missing records, duplicates, and file loading errors are handled.
- File I/O: CSV data is saved and loaded through `FileStorageService`.
- Enums: `Role`, `HeroType`, `EquipmentType`, and `MatchResult`.

## 6. AI Usage Summary

AI assistance is recorded in `ai/prompts.md` and `ai/agent-log.md`. The submitted code remains the student's responsibility and should be reviewed before final submission.

## 7. Testing Summary

Manual test cases are documented in `docs/test-cases.md`.

Automated tests can be run without external dependencies:

```bash
javac -d out $(find src -name '*.java')
java -cp out test.TestRunner
```

Current automated coverage includes 14 service-level checks for recommendations, combat, authentication failure, missing lookups, match validation, equipment add/delete cascade behavior, leaderboard ordering, zero-match win-rate safety, text-report compatibility, and CSV round trip.

## 8. Submission Evidence Map

- Requirement coverage: `docs/requirement-checklist.md`
- Design and service boundaries: `docs/design.md`, `docs/service-boundaries.md`, `docs/architecture-decisions.md`
- Manual and automated testing: `docs/test-cases.md`, `src/test/TestRunner.java`
- AI usage evidence: `ai/prompts.md`, `ai/agent-log.md`, `ai/reflection.md`, `ai/model-comparison.md`
- Final pre-submission audit: `docs/submission-audit.md`

## 9. Known Limitations

- The project uses CSV rather than a database to keep the submission dependency-free.
- The console UI is functional rather than graphical.
- Automated tests use a plain Java runner rather than JUnit because no JUnit dependency is bundled with the repository.
