# AI Usage Evidence Map

Status date: 2026-06-05.

This file consolidates the AI evidence required by `requirement.pdf`. The detailed records remain in `ai/prompts.md`, `ai/agent-log.md`, `ai/reflection.md`, and `ai/model-comparison.md`.

## Required Evidence Checklist

| Requirement | Evidence |
|---|---|
| Actual prompts recorded | `ai/prompts.md` records 11 important prompts with time, tool/model, agent role, prompt text, response summary, decision, and related commit hashes. |
| At least 3 AI agent roles | Architect Agent, Implementation Agent, Testing/Reviewer Agent, Extra-Credit Reviewer Agent, Frontend Reviewer Agent, Desktop Implementation Agent. |
| Accepted, modified, rejected decisions | `ai/prompts.md` and `ai/agent-log.md` record accepted directions, rejected Gson dependency, rejected fake human commits, rejected unrelated Gemini architecture suggestions, and deferred risky frontend ideas. |
| Reflection questions | `ai/reflection.md` answers all 10 required reflection questions. |
| Advanced AI comparison | `ai/model-comparison.md` compares Gemini and Claude Opus review quality and usefulness. |
| Human accountability | `ai/agent-log.md`, `docs/submission-audit.md`, and `docs/rubric-evaluation.md` state that AI suggestions were reviewed and verified with compilation, tests, and manual review. |
| GPT/Claude traceability | The prompt records identify the tool/model used for each commit group. `docs/git-process-evidence.md` also documents the non-destructive model tag convention for `[GPT]` and `[Claude]` commit evidence. |

## Prompt-to-Commit Map

| Prompt | Agent role | Tool/model | Model tag | Main decision | Related commits |
|---|---|---|---|---|---|
| Prompt 01 | Requirement Analyst | Codex / GPT-5 | `[GPT]` | Planned staged implementation and rejected fake process evidence. | `05d011d` |
| Prompt 02 | Architect Agent | Claude Opus 4.6 Thinking | `[Claude]` | Accepted OOP/service-boundary risk review; rejected Gson to stay dependency-free. | `ba63aa0`, `5a6630d`, `80739be` |
| Prompt 03 | Implementation Agent | Codex / GPT-5 | `[GPT]` | Implemented models, services, initial data, persistence, console menus, and CSV files. | `b968786`, `dab6880`, `d13236d`, `0b86ad2`, `eb02042`, `a4c0510` |
| Prompt 04 | Testing/Reviewer Agent | Codex / GPT-5 | `[GPT]` | Fixed CSV delimiter preservation bug with a minimal change. | `ccee720` |
| Prompt 05 | Testing/Reviewer Agent | Gemini 3.5 Flash High | `[Gemini]` | Accepted useful edge cases; rejected mismatched architecture suggestions. | `6e4c439` |
| Prompt 06 | Testing/Reviewer Agent | Claude Opus 4.6 Thinking | `[Claude]` | Accepted code-grounded match validation findings. | `12948b5` |
| Prompt 07 | Extra-Credit Reviewer Agent | Claude Opus 4.6 Thinking | `[Claude]` review, `[GPT]` implementation | Implemented recommendation, combat simulation, automated tests, and model comparison. | `7410b35`, `b21c5fa`, `31200fe`, `f009858` |
| Prompt 08 | Frontend Reviewer Agent | Claude Opus 4.6 Thinking | `[Claude]` review, `[GPT]` implementation | Added optional local web frontend while preserving console `Main`. | `151414f` |
| Prompt 09 | Frontend Design Reviewer Agent | Claude Opus 4.6 Thinking | `[Claude]` review, `[GPT]` implementation | Applied low-risk frontend design improvements. | `151414f` |
| Prompt 10 | Frontend Ideas Reviewer Agent | Claude Opus 4.6 Thinking | `[Claude]` review, `[GPT]` implementation | Accepted low-risk polish and kept higher-risk ideas in branch experiments. | `151414f`, `851a81d`, `31200fe` |
| Prompt 11 | Desktop Implementation Agent | Codex / GPT-5 | `[GPT]` | Added cross-platform Swing desktop app and macOS/Windows launchers while preserving console and web entry points. | `663fc8f`, `444ae15` |

## Verification Used Against AI Output

- Repeated compilation with `javac -d out $(find src -name '*.java')`.
- Dependency-free automated tests with `java -cp out test.TestRunner`.
- Manual console test cases in `docs/test-cases.md`.
- Source review by Codex and Claude Opus for match validation, frontend scope, desktop scope, and submission evidence.
- Git history showing separated planning, implementation, review, fix, test, and documentation commits.
