# Review Findings

## Claude Opus Testing/Reviewer Agent

Claude Opus reviewed the actual source files and identified these useful issues:

| Finding | Action |
|---|---|
| `editMatchRecord` changed the existing record before validating new team IDs and winner ID. | Fixed by adding `GameDataManager.updateMatchRecord`, which validates all new values before mutating the existing record. |
| Match winner could be a team that was not one of the two participants. | Fixed in `validateMatchRecord`. |
| Duplicate hero picks were possible in a single match, and the seed data contained duplicates. | Fixed by adding duplicate-pick validation and updating built-in and CSV seed data. |
| Match picks could include a player outside the participating teams. | Fixed in `validateMatchRecord`. |
| Match picks could include a hero the player does not own. | Fixed in `validateMatchRecord`. |

Related commit:

```text
12948b5 [AI-Review] fix match record validation issues
```

## Gemini Review Note

Gemini was briefly used during the early code-review phase, after which Claude Opus became the primary code-grounded reviewer for that phase. This Gemini output was not used as primary review evidence because it mentioned non-existent project elements such as `Repository<T>` and `getDisplayInfo`. Gemini was later used again at the user's explicit request for the bounded web and Swing visual reviews recorded as Prompts 12 and 13.
