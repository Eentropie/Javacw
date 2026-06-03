# Submission Audit

Status date: 2026-06-04.

## Entry Points

- Required console entry point remains `src/Main.java`.
- Optional web entry point is `src/web/WebMain.java`.
- macOS one-click launcher is `Open-JavaCW-Web.command`.

## Requirement Evidence

| Area | Evidence |
|---|---|
| Required model classes | `src/model/Person.java`, `Player.java`, `Admin.java`, `Hero.java`, `Equipment.java`, `Team.java`, `MatchRecord.java` |
| OOP concepts | `docs/requirement-checklist.md`, `docs/uml.md`, `docs/design.md` |
| Console features | `src/Main.java`, `src/service/SearchService.java`, `RankingService.java`, `GameDataManager.java` |
| File I/O | `src/service/FileStorageService.java`, `docs/data-design.md` |
| Extra-credit features | `RecommendationService`, `CombatSimulationService`, `src/web/`, `Open-JavaCW-Web.command` |
| AI evidence | `ai/prompts.md`, `ai/agent-log.md`, `ai/reflection.md`, `ai/model-comparison.md` |
| Tests | `docs/test-cases.md`, `src/test/TestRunner.java` |

## Latest Verification Commands

```bash
javac -d out $(find src -name '*.java')
java -cp out test.TestRunner
node --check web/app.js
git diff --check
```

Latest automated test result:

```text
Automated tests passed: 14, failed: 0
```

## Current Risk Review

- The console workflow is still the safest grading path and should be demonstrated first.
- The optional web frontend is extra evidence only; it does not replace `Main`.
- The fork branch `codex/structured-results-fork` preserves higher-risk UI experiments. Only structured tables and CSV controls were accepted into `main`; hash routing and raw JSON preview remain out of the mainline.
- `git-history.txt` should be regenerated after the final commit sequence.

## Final Submission Checklist

- Re-run the verification commands above.
- Confirm `README.md` run commands work on the submission machine.
- Export final Git history after commits are complete.
- Review AI files for accuracy; do not include invented prompts or fabricated model outputs.
