# Prompt Records

## Prompt 01

Time: 2026-06-02 17:33 CST
Tool/Model: Codex / GPT-5
Agent Role: Requirement Analyst
Related Commit: pending

### My Prompt

Read the Java coursework requirement PDF and current project. Provide an execution plan using Codex plus Antigravity.

### AI Response Summary

Codex extracted the coursework requirements, identified that the current project is an empty IntelliJ template, and proposed a staged plan covering OOP design, console features, CSV persistence, AI evidence, Git evidence, and testing.

### My Decision

Accepted the staged plan, but added an integrity rule that AI must not create fake human-authored commits. Related first documentation commit: `05d011d`.

## Prompt 02

Time: 2026-06-02 17:33 CST
Tool/Model: Antigravity / Claude Opus 4.6 Thinking
Agent Role: Architect Agent
Related Commit: pending

### My Prompt

Architect Agent task for Java coursework. Requirement summary: build a console-first Java OOP Honor of Kings information management system with Person abstract superclass, Player/Admin subclasses, Hero, Equipment, Team, MatchRecord; use interface, encapsulation, polymorphism, collections, exception handling, file I/O, enums; initial dataset 3 teams/10 players/15 heroes/20 equipment/10 matches; features: login roles, player lookup, team overview, hero details, equipment ranking, match history, leaderboard, admin CRUD, player limited edit; docs: plan.md, design/UML, ai prompts/agent log/reflection, test cases. Current repo is an empty IntelliJ Java template. Please give a concise architecture review: class responsibilities, service boundaries, data format choice, and top 5 implementation risks. Do not write full code.

### AI Response Summary

Claude recommended a layered design, warned about orphan references on deletion, file corruption during writes, scanner newline bugs, seed data maintenance, and leaderboard zero-division or tie edge cases. It suggested JSON with Gson as the preferred data format, with CSV as the fallback if external JARs are not desired.

### My Decision

Accepted the risk list and mitigation ideas. Rejected the Gson recommendation because a dependency-free CSV approach is easier to compile on a marker's machine and still satisfies the file I/O requirement.

## Prompt 03

Time: 2026-06-02 17:35 CST
Tool/Model: Codex / GPT-5
Agent Role: Implementation Agent
Related Commit: `b968786`, `dab6880`, `d13236d`, `0b86ad2`, `eb02042`, `a4c0510`

### My Prompt

Implement the Java coursework project from the accepted plan. Keep logic out of one giant Main class, use required OOP concepts, add seed data, CSV file I/O, console menus, authentication, search, ranking, match history, admin CRUD, and player limited edit.

### AI Response Summary

Codex implemented the model layer, service layer, data initializer, CSV persistence, generated CSV data files, and console menus.

### My Decision

Accepted the implementation structure after compilation and smoke testing. The project uses CSV rather than JSON to avoid external library setup.

## Prompt 04

Time: 2026-06-02 17:48 CST
Tool/Model: Codex / GPT-5
Agent Role: Testing/Reviewer Agent
Related Commit: `ccee720`

### My Prompt

Run scripted console tests for login, player lookup, equipment ranking, leaderboard, player flow, and save/load. Identify any failure and fix only the minimal broken code.

### AI Response Summary

Codex found a CSV reload bug: list fields had semicolons converted to commas during save, causing hero compatible equipment IDs to load as a single invalid ID.

### My Decision

Accepted the minimal fix. The CSV cleaner now only sanitizes the field separator `|` and preserves `;` and `:` for list and map fields.

## Prompt 05

Time: 2026-06-02 17:53 CST
Tool/Model: Antigravity / Gemini 3.5 Flash High
Agent Role: Testing/Reviewer Agent
Related Commit: pending

### My Prompt

Testing/Reviewer Agent task. Current Java console project now has model classes, GameDataManager, AuthenticationService, SearchService, RankingService, FileStorageService, seed data, CSV data files, and Main menus. Requirement features: role login, lookup, team overview, hero details, equipment ranking, match history, leaderboard, admin CRUD, player limited edit, file save/load, AI/Git/test docs. Please give a concise review checklist: top bugs to test, edge cases, and 10 manual test cases. Do not write code.

### AI Response Summary

Gemini suggested testing CSV delimiter handling, orphan references after deletion, leaderboard division by zero, input buffer skipping, enum parsing, empty ID lists, double deletes, and invalid match structures. It also mentioned architecture items such as `Repository<T>` and `getDisplayInfo` that are not part of this project.

### My Decision

Accepted the useful testing risks and rejected the unrelated architecture suggestions. After the user requested Claude Opus only, this Gemini output was not used as the primary review evidence. Manual test documentation was updated using actual console output rather than AI-generated expected results.

## Prompt 06

Time: 2026-06-02 17:56 CST
Tool/Model: Antigravity / Claude Opus 4.6 Thinking
Agent Role: Testing/Reviewer Agent
Related Commit: `12948b5`

### My Prompt

Testing/Reviewer Agent task using Claude Opus only. Review the current actual Java console project, not an imagined architecture. It has model classes Person/Player/Admin/Hero/Equipment/Team/MatchRecord, services GameDataManager/AuthenticationService/SearchService/RankingService/FileStorageService, seed CSV data, and Main console menus. No Repository<T> and no getDisplayInfo method exist. Requirement features: role login, lookup, team overview, hero details, equipment ranking, match history, leaderboard, admin CRUD, player limited edit, file save/load, AI/Git/test docs. Give a concise review checklist: top bugs to test, edge cases, and 10 manual test cases. Do not write code.

### AI Response Summary

Claude read the actual source files and reported that the code was generally solid, especially input handling, encapsulation, atomic CSV writes, and deletion cascades. It found real match-record issues: edit flow mutated records before validating, winner team was not restricted to participants, duplicate hero picks existed in seed data, and match picks needed participant/ownership validation.

### My Decision

Accepted the code-grounded findings. Added centralized match validation, safe update-before-mutation behavior, winner participant checks, player team membership checks, player hero ownership checks, and corrected seed match data.
