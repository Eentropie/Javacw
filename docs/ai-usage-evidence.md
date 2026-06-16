# AI Usage Evidence Map

Status date: 2026-06-16.

This file consolidates the AI evidence required by `requirement.pdf`. The detailed records remain in `ai/prompts.md`, `ai/agent-log.md`, `ai/reflection.md`, and `ai/model-comparison.md`.

## Required Evidence Checklist

| Requirement | Evidence |
|---|---|
| Actual prompts recorded | `ai/prompts.md` records 15 important prompts with time, tool/model, agent role, prompt text, response summary, decision, and related commit hashes. |
| At least 3 AI agent roles | Architect Agent, Implementation Agent, Testing/Reviewer Agent, Extra-Credit Reviewer Agent, Frontend Reviewer Agent, Desktop Implementation Agent, Desktop Design Reviewer Agent, Final Requirement Audit Agent, Human-Guided Usability Fix Agent. |
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
| Prompt 04 | Testing/Reviewer Agent | Human diagnosis/fix, then Codex / GPT-5 verification | `[Human]` diagnosis and fix, `[GPT]` verification | The user traced and fixed the CSV delimiter corruption; Codex reran the scripted save/load flows and documented the verified result. | `ccee720` |
| Prompt 05 | Testing/Reviewer Agent | Gemini 3.5 Flash High | `[Gemini]` | Accepted useful edge cases; rejected mismatched architecture suggestions. | `6e4c439` |
| Prompt 06 | Testing/Reviewer Agent | Claude Opus 4.6 Thinking | `[Claude]` | Accepted code-grounded match validation findings. | `12948b5` |
| Prompt 07 | Extra-Credit Reviewer Agent | Claude Opus 4.6 Thinking | `[Claude]` review, `[GPT]` implementation | Implemented recommendation, combat simulation, automated tests, and model comparison. | `7410b35`, `b21c5fa`, `31200fe`, `f009858` |
| Prompt 08 | Frontend Reviewer Agent | Claude Opus 4.6 Thinking | `[Claude]` review, `[GPT]` implementation | Added optional local web frontend while preserving console `Main`. | `151414f` |
| Prompt 09 | Frontend Design Reviewer Agent | Claude Opus 4.6 Thinking | `[Claude]` review, `[GPT]` implementation | Applied low-risk frontend design improvements. | `151414f` |
| Prompt 10 | Frontend Ideas Reviewer Agent | Claude Opus 4.6 Thinking | `[Claude]` review, `[GPT]` implementation | Accepted low-risk polish and kept higher-risk ideas in branch experiments. | `151414f`, `851a81d`, `31200fe` |
| Prompt 11 | Desktop Implementation Agent | Codex / GPT-5 | `[GPT]` | Added cross-platform Swing desktop app and macOS/Windows launchers while preserving console and web entry points. | `663fc8f`, `444ae15` |
| Prompt 12 | Frontend Design Reviewer Agent | Gemini 3.1 Pro High, then Codex / GPT-5 | `[Gemini]` review, `[GPT]` implementation | Applied a dependency-free arena/control-panel visual polish to the optional web frontend while preserving console, web APIs, and JavaScript behavior. | `602d0b7` |
| Prompt 13 | Desktop Design Reviewer Agent | Gemini 3.1 Pro High, Claude Opus 4.6 Thinking, then Codex / GPT-5 | `[Gemini]` review, `[Claude]` approval, `[GPT]` implementation | Applied a Claude-approved, dependency-free dark Metal Swing theme to the optional local desktop app while preserving console, web, service, CSV, launcher, and smoke behavior. | `70498f6` |
| Prompt 14 | Final Requirement Audit Agent | Codex / GPT-5, Computer Use | `[GPT]` audit and implementation | Audited requirement, Codex threads, Antigravity conversations, evidence attribution, implementation consistency, tests, and local Swing launch; corrected final attribution and added final repairs. | `e6a79ba`, `81f5a9d`, `1437354`, `4b1ec04` |
| Prompt 15 | Human-Guided Usability Fix Agent | Codex / GPT-5 | `[Human]` discovery and direction, `[GPT]` implementation | The user found that blind `P00x`/`T00x` console entry was not user-friendly; Codex added numbered player/team choices while preserving ID/name input. | `b58e1d2` |

## Verification Used Against AI Output

- Repeated compilation with `javac -d out $(find src -name '*.java')`.
- Dependency-free automated tests with `java -cp out test.TestRunner` (latest result: `20 passed, 0 failed`).
- Manual console test cases in `docs/test-cases.md`.
- Source review by Codex, Claude Opus, and Gemini for match validation, frontend scope, desktop scope, frontend visual polish, and submission evidence.
- macOS GUI/screenshot check confirming the polished Swing window title `Honor of Kings IMS - Desktop` and visible login controls, Save/Reload buttons, report/recommendation/combat tabs, and dataset summary.
- Git history showing separated planning, implementation, review, fix, test, and documentation commits.
- `docs/codex-conversation-audit.md` maps the JavaCW Codex threads and the relevant Antigravity conversations used to correct attribution.
