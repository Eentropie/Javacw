# Git Process Evidence

Status date: 2026-06-05.

This file consolidates the Git evidence required by `requirement.pdf`. The raw Git log export remains in `git-history.txt`.

## Requirement Checklist

| Git requirement | Evidence |
|---|---|
| At least 12 meaningful commits | The repository contains far more than 12 commits, with planning, implementation, review, fixes, tests, documentation, frontend, and final audit work separated. |
| At least 4 human-authored commits | At least 7 `[Human]` commits are present, covering reflection, recommendation formula review, console verification, and documentation polish. |
| At least 3 Architect Agent commits | `[AI-Architect]` commits include `ba63aa0`, `5a6630d`, and `80739be`. |
| At least 3 Implementation Agent commits | `[AI-Implementation]` commits include model, service, data, persistence, console, CSV, and Windows launcher work. |
| At least 2 Testing/Reviewer Agent commits | `[AI-Review]` commits include `12948b5` and `6e4c439`; `[Fix]` and `[Tests]` commits also support the review/test process. |
| Git log export included | `git-history.txt` contains the command output from `git log --oneline --graph --decorate --all`. |

## Prefix Normalization

Most commits use the recommended prefixes from the assignment. A few older commits use more specific prefixes. They are not rewritten because the AI evidence files reference their existing hashes; rewriting them would weaken the traceability evidence.

| Existing prefix | Rubric interpretation |
|---|---|
| `[Frontend]` | Optional web frontend implementation evidence; treated as implementation/extra-credit process evidence. |
| `[Extra-Credit]` | Recommendation and combat implementation evidence; treated as implementation and creativity evidence. |
| `[Project]` | Project metadata/setup evidence; not counted as a required AI category. |
| `[Tests]` | Automated test implementation evidence; equivalent to the rubric's test evidence intent. |

Future commits should prefer the rubric's listed prefixes: `[Human]`, `[AI-Architect]`, `[AI-Implementation]`, `[AI-Review]`, `[AI-Refactor]`, `[Docs]`, `[Test]`, and `[Fix]`.

## AI Model Tag Convention

For new AI-assisted commits, the rubric category should remain first and the model tag should be second, for example `[AI-Implementation][GPT]` or `[AI-Review][Claude]`. This keeps the assignment prefix easy to grade while also making the assistant source visible.

Existing commits are not rewritten because changing commit messages would change hashes already referenced in `ai/prompts.md` and `docs/ai-usage-evidence.md`.

| Evidence label | Meaning | Related commit evidence |
|---|---|---|
| `[GPT]` | Codex / GPT-5 directly planned, implemented, tested, or documented the change. | `05d011d`, `b968786`, `dab6880`, `d13236d`, `0b86ad2`, `eb02042`, `ccee720`, `a4c0510`, `771c9c7`, `f3900ab`, `3a94c00` |
| `[Claude]` | Antigravity Claude Opus supplied the primary architecture or review input behind the change. | `ba63aa0`, `5a6630d`, `80739be`, `12948b5`, `6e4c439` |
| `[Claude] review + [GPT] implementation` | Claude supplied the review/design direction, then Codex implemented or documented the accepted low-risk work. | `7410b35`, `b21c5fa`, `151414f`, `31200fe`, `851a81d` |
| `[Gemini]` | Gemini was briefly used for comparison, but its mismatched architecture suggestions were not kept as primary evidence. | `6e4c439` |

## Git History Export Boundary

`git-history.txt` can include all commits that exist before the history export is committed. The final commit that only stores the refreshed `git-history.txt` cannot include itself without creating another final commit. This is a Git self-reference boundary, not missing development evidence.

The live repository history is the authoritative source for the latest commit, while `git-history.txt` provides the required exported snapshot for marking.

## Process Narrative

1. Planning and architecture evidence started with `plan.md`, requirement mapping, service boundaries, and CSV data design.
2. Implementation was split into model classes, search/ranking services, seed data, persistence, console menus, and initial CSV files.
3. Review and debugging commits fixed CSV delimiter preservation and match-record validation.
4. Extra-credit commits added recommendation, combat simulation, and automated tests.
5. Frontend work was kept optional and dependency-free, preserving the required console entry point.
6. Final documentation commits tightened AI evidence, rubric mapping, Windows compatibility notes, and Git history export evidence.
