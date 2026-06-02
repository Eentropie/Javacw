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
- `ccee720` preserve CSV list delimiters during save

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
