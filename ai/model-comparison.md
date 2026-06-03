# Advanced AI Reflection: Model Comparison

## Shared Review Problem

Both reviewer agents were asked to review the Java console coursework project and identify bugs, edge cases, and manual tests. The target requirement was the Honor of Kings information management system with role login, lookup, team overview, hero details, equipment ranking, match history, leaderboard, admin CRUD, player limited edit, CSV persistence, AI evidence, Git evidence, and test documentation.

## Model A: Gemini 3.5 Flash High

Gemini gave useful generic testing risks:

- CSV delimiter handling;
- orphan references after deletion;
- leaderboard division by zero;
- input buffer issues;
- enum parsing;
- invalid match structures.

However, it also mentioned architecture that was not present in this project, such as `Repository<T>` and `getDisplayInfo`. Those suggestions were rejected because they did not match the actual source code.

## Model B: Claude Opus 4.6 Thinking

Claude Opus was more useful when it was instructed to review the actual source files rather than an imagined design. It identified concrete match-record issues:

- match editing could mutate a record before validation;
- winner team was not restricted to the two participating teams;
- duplicate hero picks were possible in a single match;
- match picks needed participant-team validation;
- match picks needed player hero-ownership validation.

Those findings were accepted and fixed through centralized match validation.

## Extra-Credit Planning Review

Claude Opus was also called through Antigravity with Computer Use before the extra-credit implementation. It recommended:

- implementing a real recommendation engine rather than only showing static recommended equipment fields;
- implementing combat simulation instead of GUI because it maps directly to the PDF extra-credit criteria and avoids GUI runtime risk;
- using a plain Java automated test runner if JUnit jars are not bundled;
- documenting formulas and test evidence;
- adding this comparison file for advanced AI reflection.

## Comparison Table

| Criterion | Gemini 3.5 Flash High | Claude Opus 4.6 Thinking |
|---|---|---|
| Read actual project structure | Partly mismatched | Code-grounded |
| Hallucinated classes or methods | Yes, mentioned unrelated architecture | No major mismatch in accepted review |
| Actionable bug findings | Mostly generic risks | Concrete validation bugs |
| Helpfulness for fixes | Medium | High |
| Helpfulness for scoring strategy | Medium | High |
| Human decision | Accepted useful edge cases, rejected mismatches | Accepted code-grounded findings and extra-credit direction |

## What I Learned

The stronger review was not only about model capability. The prompt quality mattered: asking the reviewer to inspect the actual files and avoid imagined architecture produced much better feedback. The comparison also showed why AI suggestions need verification. A confident answer can still be wrong if it assumes a design that does not exist.
