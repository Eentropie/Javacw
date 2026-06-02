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

Accepted the staged plan, but added an integrity rule that AI must not create fake human-authored commits.

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
