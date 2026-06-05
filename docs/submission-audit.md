# Submission Audit

Status date: 2026-06-04.

## Entry Points

- **Required** console entry point: `src/Main.java`. This is the primary deliverable and should be graded first.
- **Optional** web entry point: `src/web/WebMain.java`. This is extra-credit evidence, not a replacement for the console workflow.
- macOS one-click launcher: `Open-JavaCW-Web.command`.

## Requirement Evidence

| Area | Evidence |
|---|---|
| Required model classes | `src/model/Person.java`, `Player.java`, `Admin.java`, `Hero.java`, `Equipment.java`, `Team.java`, `MatchRecord.java` |
| OOP concepts | `docs/requirement-checklist.md`, `docs/uml.md`, `docs/design.md` |
| Console features | `src/Main.java`, `src/service/SearchService.java`, `RankingService.java`, `GameDataManager.java` |
| File I/O | `src/service/FileStorageService.java`, `docs/data-design.md` |
| Extra-credit features | `RecommendationService`, `CombatSimulationService`, `src/web/`, `Open-JavaCW-Web.command` |
| AI evidence | `ai/prompts.md`, `ai/agent-log.md`, `ai/reflection.md`, `ai/model-comparison.md` |
| Tests | `docs/test-cases.md`, `src/test/TestRunner.java` — 14 automated checks, all passing |

## Latest Verification Commands

```bash
javac -d out $(find src -name '*.java')
java -cp out test.TestRunner
node --check web/app.js
git diff --check
```