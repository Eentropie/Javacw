# Reflection

## 1. Which AI tools or models did you use?

I used Codex (GPT-5) as the main code generation assistant and Antigravity with Claude Opus 4.6 Thinking for architecture review and code-level bug finding. Gemini 3.5 Flash was briefly tried once for a review task, but its output referenced classes that did not exist in my project, so I switched to Claude Opus only for the rest of the development.

## 2. Which prompt was the most useful? Why?

The Architect Agent prompt (Prompt 02 in prompts.md) was the most useful because it forced me to think about class responsibilities and service boundaries before writing any code. Without it I would probably have put too much logic into Main.java. The prompt also surfaced risks I had not considered, like orphan references after deletion and leaderboard division-by-zero edge cases.

## 3. Which AI-generated suggestion was wrong, incomplete, or misleading?

Two things stood out. First, Claude suggested using Gson/JSON for persistence — technically fine, but it would add an external dependency that a marker might not have, so I rejected it and kept plain CSV. Second, Gemini mentioned `Repository<T>` and `getDisplayInfo` in its review, neither of which existed in my actual code. That taught me that AI can review an imagined version of your project rather than the real one if you do not point it at the actual source files.

## 4. How did you check whether AI-generated code was correct?

I compiled the project with `javac -d out $(find src -name '*.java')` after every major change to catch syntax and type errors immediately. Then I ran the console program manually, tested every menu option at least once, and compared the CSV output before and after save/load to make sure data was not corrupted. For the automated tests, I read each test method to confirm it was actually checking something meaningful rather than always passing.

## 5. What bugs did you fix yourself instead of asking AI to fix?

The biggest one I caught myself was the CSV delimiter bug: after saving, semicolons inside list fields were being converted to commas, so the next load treated "E001,E002" as a single unknown equipment ID. I traced this by reading the CSV file line by line and comparing it to the original seed data. The fix was a one-line change in `FileStorageService.clean` to only sanitize the pipe character. Claude later found match-record validation bugs (editing mutated a record before validation, winner was not restricted to the two participating teams), but the CSV bug was the one I diagnosed and fixed on my own.

## 6. What Java concept did you understand better after using AI?

Encapsulation and data consistency. Before this project I knew encapsulation means "private fields with getters/setters", but I did not have a concrete example of why it matters. In this project, `MatchRecord` returns an unmodifiable copy of its hero picks map. When I needed to remove a hero pick during deletion, I had to add a controlled `removeHeroPick` method instead of mutating the map directly. That made the reason for encapsulation click for me in a way textbook examples never did.

## 7. What Java concept are you still unsure about?

I am still not fully confident with exception handling strategy — specifically, where to catch exceptions versus where to let them propagate. In this project most exceptions are caught in the Main menu loop and printed as error messages, which works fine for a console app, but I am not sure how to design exception handling for a larger application with multiple layers. I also want to learn more about persistence beyond CSV, like how to use JDBC or a real database.

## 8. Did AI make the project easier, harder, or both? Explain.

Both. The initial code scaffolding was much faster with AI — model classes, CSV parsing, and menu boilerplate that would have taken me hours were generated in minutes. But verifying AI output took significant time too. I had to read every generated file, run the program, and check edge cases. The Gemini review that referenced non-existent classes showed me that AI confidence does not equal correctness. Overall I think AI saved time on the mechanical parts but did not save me from needing to understand what the code does.

## 9. Which parts of the final project were mainly written by you?

I was mainly responsible for: deciding the overall architecture (CSV over JSON, console-first, no external dependencies), reviewing and accepting/rejecting every AI suggestion, diagnosing the CSV delimiter bug, running all manual tests and recording actual outputs, writing the final reflection and documentation edits, and making the judgment calls on which extra-credit features to implement (combat simulation over GUI, dependency-free test runner over JUnit). The human commits in the Git history represent work I personally reviewed, edited, and verified.

## 10. Which parts were mainly generated or heavily assisted by AI?

The model classes (Person, Player, Admin, Hero, Equipment, Team, MatchRecord), service-layer implementations (GameDataManager, SearchService, RankingService, FileStorageService, RecommendationService, CombatSimulationService), CSV data files, console menu flow in Main.java, the automated test runner, the web frontend, and initial drafts of all documentation were generated or heavily assisted by Codex. Claude Opus was used as a reviewer — it identified real bugs in match validation that I then confirmed and accepted. I treated all AI output as draft material that needed my review before it could be considered correct.