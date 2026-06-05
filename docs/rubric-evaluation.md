# Rubric Evaluation

Status date: 2026-06-05.

This file records a final self-audit against the 20-mark rubric in `requirement.pdf`. It is not a replacement for source code, tests, or Git history; it is an evidence map showing where each scoring area is satisfied.

## Score Estimate

| Rubric category | Marks | Current evidence | Self-audit result |
|---|---:|---|---|
| Java Design and Understanding | 5 | `src/model/`, `src/service/`, `src/contract/Searchable.java`, `src/enums/`, `docs/design.md`, `docs/uml.md` | Strong. Uses inheritance, interface, encapsulation, collections, enums, exceptions, CSV file I/O, and explicit object association helpers. |
| Functional Completeness | 4 | `src/Main.java`, `SearchService`, `RankingService`, `GameDataManager`, `AuthenticationService` | Strong. Console workflow covers lookup, team overview, hero details, equipment statistics, match history, leaderboard, data management, and login/logout. |
| AI Usage Evidence | 4 | `ai/prompts.md`, `ai/agent-log.md`, `ai/reflection.md`, `ai/model-comparison.md`, `docs/ai-usage-evidence.md` | Strong. Prompt records include model, role, prompt, response summary, decision, related commits, and GPT/Claude model tags. |
| Git Process Evidence | 3 | `git-history.txt`, Git log, `docs/git-process-evidence.md` | Strong. More than 12 commits and more than 4 human review/reflection commits are present; old specific prefixes and GPT/Claude model labels are mapped without rewriting hashes. |
| plan.md and Documentation | 2 | `plan.md`, `README.md`, `docs/design.md`, `docs/uml.md`, `docs/test-cases.md`, `docs/submission-audit.md` | Strong. Required plan sections and final run instructions are present. |
| Testing and Reliability | 1 | `docs/test-cases.md`, `src/test/TestRunner.java` | Strong. 15 manual tests documented and 15 automated checks pass. |
| Extra Credit or Creativity | 1 | `RecommendationService`, `CombatSimulationService`, `src/web/`, `ai/model-comparison.md` | Strong. Multiple extra-credit features are implemented. |

Estimated result: A-band evidence, approximately 18-19/20 depending on marker strictness.

## Verification Commands

Latest local checks:

```bash
javac -d out $(find src -name '*.java')
java -cp out test.TestRunner
node --check web/app.js
git diff --check
```

Latest automated result:

```text
Automated tests passed: 15, failed: 0
```

Dataset audit:

```text
Teams: 3, each with 5 players
Players: 15, each with at least 3 heroes
Heroes: 15, each with at least 4 compatible equipment items
Equipment: 20
Match records: 11
```

## Remaining Improvement Candidates

- Association evidence is ID-backed for CSV persistence and now exposed through object association helpers in `GameDataManager`; this is documented in `docs/design.md` and tested in `src/test/TestRunner.java`.
- Some older commit prefixes such as `[Frontend]`, `[Extra-Credit]`, `[Project]`, and `[Tests]` are more specific than the suggested prefix list. They are mapped in `docs/git-process-evidence.md`; future AI commits should keep the rubric prefix first and add a model tag second, such as `[AI-Implementation][GPT]` or `[AI-Review][Claude]`.
- Windows support has a launcher and PowerShell instructions, but final automated tests were run in the local macOS workspace. If the marker uses Windows, running the README PowerShell commands on Windows would be the strongest confirmation.
- `git-history.txt` can show all commits that existed before the history export commit. A later commit that refreshes the history file cannot include itself without another export cycle, so the repository's live Git log remains the authoritative source for the very latest commit.
