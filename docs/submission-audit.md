# Submission Audit

Status date: 2026-06-05.

## Entry Points

- **Required** console entry point: `src/Main.java`. This is the primary deliverable and should be graded first.
- **Optional** web entry point: `src/web/WebMain.java`. This is extra-credit evidence, not a replacement for the console workflow.
- macOS one-click launcher: `Open-JavaCW-Web.command`.
- Windows one-click launcher: `Open-JavaCW-Web.bat`.

## Requirement Evidence

| Area | Evidence |
|---|---|
| Required model classes | `src/model/Person.java`, `Player.java`, `Admin.java`, `Hero.java`, `Equipment.java`, `Team.java`, `MatchRecord.java` |
| OOP concepts | `docs/requirement-checklist.md`, `docs/uml.md`, `docs/design.md` |
| Console features | `src/Main.java`, `src/service/SearchService.java`, `RankingService.java`, `GameDataManager.java` |
| File I/O | `src/service/FileStorageService.java`, `docs/data-design.md` |
| Extra-credit features | `RecommendationService`, `CombatSimulationService`, `src/web/`, `Open-JavaCW-Web.command`, `Open-JavaCW-Web.bat` |
| AI evidence | `ai/prompts.md`, `ai/agent-log.md`, `ai/reflection.md`, `ai/model-comparison.md` |
| Tests | `docs/test-cases.md`, `src/test/TestRunner.java` — 14 automated checks, all passing |
| Rubric audit | `docs/rubric-evaluation.md` maps the final project against the 20-mark rubric |

## Latest Verification Commands

```bash
javac -d out $(find src -name '*.java')
java -cp out test.TestRunner
node --check web/app.js
git diff --check
```

Windows PowerShell compile/test equivalent:

```powershell
$files = Get-ChildItem -Recurse src -Filter *.java | ForEach-Object { $_.FullName }
javac -d out $files
java -cp out test.TestRunner
```

Latest automated test result:

```text
Automated tests passed: 14, failed: 0
```

## Current Risk Review

- The console workflow is the safest grading path and should be demonstrated first.
- The optional web frontend is extra evidence only; it does not replace `Main`.
- Human review of the GitHub README found that the first web launcher documented only macOS and used Unix shell compile syntax. The follow-up added a Windows `.bat` launcher and Windows PowerShell commands.
- Core Java code uses standard JDK APIs and `Path`/`Files` for data paths, so there is no intentional macOS-only code path. The local verification environment is macOS; Windows execution should still be re-run on the final Windows machine if that platform is used for marking.
- The fork branch `codex/structured-results-fork` preserves higher-risk UI experiments. Only structured tables and CSV controls were accepted into `main`; hash routing and raw JSON preview remain out of the mainline.
- `git-history.txt` has been regenerated after the final commit sequence.

## Personal Verification

I personally ran all console flows (login, lookup, team overview, hero details, equipment ranking, match history, leaderboard, recommendation engine, combat simulation, data management add/delete, save/load) and confirmed 14/14 automated tests pass before this final submission.

## Final Submission Checklist

- [x] Re-run the verification commands above.
- [x] Confirm `README.md` run commands work on the submission machine.
- [x] Export final Git history after commits are complete.
- [x] Review AI files for accuracy; no invented prompts or fabricated model outputs.
- [x] Add human-authored commits with personalized reflection, formula review, and test verification.
