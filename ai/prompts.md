# Prompt Records

## Prompt 01

Time: 2026-06-02 17:33 CST
Tool/Model: Codex / GPT-5
Agent Role: Requirement Analyst
Related Commit: `05d011d`

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
Related Commit: `ba63aa0`, `5a6630d`, `80739be`

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
Related Commit: `6e4c439`

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

## Prompt 07

Time: 2026-06-03 15:16 CST
Tool/Model: Antigravity / Claude Opus 4.6 Thinking
Agent Role: Extra-Credit Reviewer Agent
Related Commit: Current working-tree update, not committed yet

### My Prompt

Claude Opus review request for JavaCW extra-credit implementation. Please use the coursework rubric: Extra Credit/Creativity, Java Design, Functional Completeness, AI Usage Evidence, Git Process, Documentation, Testing. Current project is a Java console Honor of Kings IMS with model/service layers, CSV persistence, recommendation fields on Hero, equipment ranking formula, manual tests, AI evidence, and previous match-validation fixes. I plan to implement four enhancements now: (1) a real recommendation engine, (2) advanced AI reflection comparing model/agent outputs, (3) automated tests without external dependency risk, and (4) one explicit extra feature, preferably combat simulation rather than GUI unless you strongly disagree. Do NOT edit files, do NOT create artifacts, do NOT run commands. Please give a concise implementation checklist: what to add, where to add it, highest risks, and what evidence/docs/tests should be updated for scoring.

### AI Response Summary

Claude recommended implementing a real `RecommendationService`, implementing combat simulation instead of GUI, using a plain Java test runner if JUnit dependencies are not bundled, documenting formulas and test evidence, and adding an advanced AI comparison file. It also warned about combat infinite loops, empty-team recommendation edge cases, test isolation, temp-file cleanup, and overscoping.

### My Decision

Accepted the direction. Implemented recommendation and combat services, added a dependency-free automated test runner, documented formulas and tests, and added `ai/model-comparison.md`. GUI was rejected because it would add runtime and scope risk without improving the core scoring as directly as combat simulation.

## Prompt 08

Time: 2026-06-03 15:58 CST
Tool/Model: Antigravity / Claude Opus 4.6 Thinking
Agent Role: Frontend Reviewer Agent
Related Commit: Current working-tree update, not committed yet

### My Prompt

Frontend extra-credit review for the current JavaCW project. Keep the existing console Main unchanged. I want to add a real local web frontend using only JDK built-ins if possible: Java HttpServer backend, static HTML/CSS/JS, REST-like endpoints for login/search/leaderboard/recommendations/combat, and a macOS one-click .command launcher that compiles, starts the web server, and opens the browser. No Maven/Gradle, no external deps. Please give a concise architecture/risk checklist and what documentation/tests should be updated. Do not write code and do not assume files beyond the current Java console project.

### AI Response Summary

Claude recommended keeping the web layer as a second entry point beside `Main`, reusing the model and service classes, serving static files from `web/`, returning JSON from local API endpoints, avoiding CORS by keeping the frontend and backend on the same server, and using a single-threaded executor to avoid mutable data concurrency issues.

### My Decision

Accepted the architecture. Implemented `web.WebMain`, `web.WebServer`, static frontend assets, and a macOS one-click launcher without adding Maven, Gradle, external Java libraries, or external JavaScript libraries.

## Prompt 09

Time: 2026-06-03 16:56 CST
Tool/Model: Antigravity / Claude Opus 4.6 Thinking
Agent Role: Frontend Design Reviewer Agent
Related Commit: Current working-tree update, not committed yet

### My Prompt

Design optimization review for the JavaCW web frontend already implemented. Current UI: single-page vanilla HTML/CSS/JS, top sticky header with brand/login, overview band with arena visual + count tiles, left vertical tool nav, central form panel, right insight panel, bottom report panel. It is functional but feels sparse in the central workspace and the visual hierarchy could be stronger. Constraints: no new dependencies, no backend changes unless absolutely necessary, keep console Main unchanged, keep coursework/professional admin-tool feel, avoid marketing hero page, keep responsive layout, no decorative gradient orbs. Please give a concise actionable frontend design checklist: layout, spacing, controls, status/report presentation, mobile behavior, and color/typography refinements. Do not write full code; focus on what to change in index.html/styles.css/app.js.

### AI Response Summary

Claude recommended an 8px spacing pass, stronger panel hierarchy, a left-accent active navigation state, compact section headers, full-width form controls, loading feedback, toast notifications, a clearer report container, contextual right-panel content, and responsive collapse from three columns to two columns to a single mobile layout. It also recommended rendering tabular data as real tables where structured data is available.

### My Decision

Accepted the low-risk frontend improvements: revised panel hierarchy, spacing, active navigation, report/status presentation, loading bar, toast notifications, contextual insight panel, and responsive behavior. Deferred table rendering because the current service endpoints intentionally return formatted reports; parsing those report strings in JavaScript would be brittle unless the backend API is extended with structured result arrays.

## Prompt 10

Time: 2026-06-03 17:18 CST
Tool/Model: Antigravity / Claude Opus 4.6 Thinking
Agent Role: Frontend Ideas Reviewer Agent
Related Commit: Current working-tree update, not committed yet

### My Prompt

Any more possible improvements? Disperse your ideas.

### AI Response Summary

Claude suggested a broad set of frontend polish and product ideas, including favicon, page title updates, hash routing, skeleton loaders, animated count-up tiles, sparklines, relative time, report copy-to-clipboard, print styles, session timeout indicators, collapsible nav, contextual placeholders, table cross-links, retry handling, raw JSON preview, connection status, hero portrait placeholders, focus-visible styling, semantic HTML audit, debounced search, and visible API latency.

### My Decision

Accepted the low-risk improvements that fit the existing dependency-free frontend: favicon, page title updates, report copy-to-clipboard, contextual placeholders, visible API latency, relative match dates, title attributes for compact rows, and keyboard focus polish. Deferred higher-risk ideas such as hash routing, structured table rendering, raw JSON preview, retry/backoff, and session timeout to isolated branch experiments.
