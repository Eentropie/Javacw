# Agent Log

## Architect Agent

Main contribution:

- Codex analyzed the requirement PDF and proposed the project structure.
- Antigravity Claude was asked to review architecture, service boundaries, data format, and implementation risks.
- Claude identified orphan references, file write reliability, scanner input problems, seed data maintenance, and leaderboard edge cases as the top implementation risks.

Human decision:

- The console-first approach and CSV persistence were selected because they match the requirement and avoid unnecessary dependency risk.
- Claude's JSON/Gson recommendation was rejected to keep the project dependency-free.
- Fake human-authored Git commits are rejected because the requirement explicitly forbids fabricated process evidence.

Related commits:

- `05d011d` create coursework plan and AI evidence skeleton
- `ba63aa0` add requirement coverage and architecture decisions
- `5a6630d` document service boundaries
- `80739be` document CSV data design

## Implementation Agent

Main contribution:

- Implemented model classes, enums, service classes, seed data, CSV persistence, console menus, and CSV data files.

Human decision:

- AI should implement selected bounded tasks, not a hidden one-shot project.
- CSV was kept dependency-free even though Claude recommended Gson.

Related commits:

- `b968786` add OOP model classes and enums
- `dab6880` implement core data search and ranking services
- `d13236d` add initial Honor of Kings dataset
- `0b86ad2` implement CSV persistence service
- `eb02042` implement console menus and role workflows
- `a4c0510` add CSV data files for initial dataset

## Testing/Reviewer Agent

Main contribution:

- Codex ran scripted smoke tests and found a CSV delimiter bug.
- Gemini suggested edge cases including delimiter handling, orphan references, division by zero, input skipping, and invalid enum parsing.
- After the user requested Claude Opus only, Claude Opus reviewed the actual source files and found concrete match-record validation issues.

Human decision:

- Actual test outputs must come from running the program, not from AI guesses.
- The CSV delimiter bug was fixed with a minimal change and recorded in the test document.
- Gemini's references to `Repository<T>` and `getDisplayInfo` were rejected because they do not match the implemented design. Gemini was not used as primary review evidence after the user requested Claude Opus only.
- Claude's match-record findings were accepted and fixed.

Related commits:

- `ccee720` preserve CSV list delimiters during save
- `12948b5` fix match record validation issues
- `6e4c439` document Claude review and test evidence

## Extra-Credit Reviewer Agent

Main contribution:

- Claude Opus reviewed the extra-credit scoring strategy through Antigravity using Computer Use.
- It recommended a real recommendation service, combat simulation instead of GUI, dependency-free automated tests if JUnit is not bundled, and a model-comparison reflection file.
- It identified risks including combat infinite loops, empty-team recommendation edge cases, random test instability, and overscoping.

Human decision:

- The combat simulation route was selected over GUI because it maps directly to the PDF extra-credit criteria and keeps the project console-first.
- A plain Java test runner was selected over JUnit because the repository has no bundled JUnit jar and the marker should be able to run tests with only `javac` and `java`.
- The model-comparison reflection uses actual Gemini and Claude review differences rather than inventing a new comparison.

Related commits:

- `7410b35` add recommendation and combat simulation.
- `b21c5fa` add dependency-free automated coverage.
- `31200fe` update submission evidence and audit.
- `f009858` update AI evidence commit links.

## Frontend Reviewer Agent

Main contribution:

- Antigravity Claude Opus reviewed the optional web frontend plan.
- It recommended a second entry point, static frontend assets, local JSON API endpoints, same-origin serving, and single-threaded server execution for mutable data safety.
- A second Claude Opus review focused on frontend design quality after implementation.
- It recommended denser spacing, clearer panel hierarchy, active navigation accents, contextual insight content, loading feedback, toast notifications, and stronger responsive behavior.
- A later Claude Opus brainstorm generated additional frontend polish and higher-risk product ideas.

Human decision:

- The original console `Main` entry point must remain unchanged.
- The web frontend is optional extra-credit evidence, not a replacement for the required console workflow.
- The implementation stays dependency-free by using JDK `HttpServer` and plain HTML/CSS/JS.
- The design pass was applied only to static frontend assets. Table rendering was initially deferred because the current API returned report strings, and parsing those strings would make the browser layer fragile.
- Low-risk polish was applied directly on `main`; higher-risk ideas were moved into fork/worktree experiments before any merge back into the main project structure.
- The structured-result fork proved a safer path by extending selected endpoints with `format=json` while preserving their default report responses. Only the stable backend structured rows, table renderer, copy-to-CSV behavior, and CSV export control were absorbed into `main`; hash routing and raw JSON preview stayed out of the mainline.
- The remaining fork worktree was preserved as branch `codex/structured-results-fork`. API smoke checks still passed, but raw JSON preview and hash routing were kept branch-only because their scoring value is lower than their UI/state risk.
- A final submission-audit pass expanded the dependency-free automated test runner from 10 to 14 checks, adding invalid-login, missing-lookup, equipment add/delete cascade, and text-report compatibility coverage. A submission evidence map was added so requirement, test, AI, and run-command evidence is easier to inspect.
- Human review of the published GitHub README identified a Windows compatibility gap: the one-click launcher was macOS-only and the documented compile command used Unix shell syntax. Codex added a Windows batch launcher and cross-platform README instructions without changing the original console entry point.

Related commits:

- `151414f` add optional local web UI and macOS launcher.
- `851a81d` preserve structured-result fork history evidence.
- `31200fe` update submission evidence and audit after accepting stable fork work.
- `771c9c7` add Windows launcher after human compatibility review.
- `b751251` refresh Git history after Windows compatibility update.
