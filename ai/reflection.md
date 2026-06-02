# Reflection

## 1. Which AI tools or models did you use?

Codex / GPT-5 was used as the main execution and review assistant. Antigravity / Claude Opus 4.6 Thinking was used for architecture and code-grounded review. Gemini was briefly invoked once, but after the user requested Claude Opus only, it was not used as primary review evidence.

## 2. Which prompt was the most useful? Why?

The most useful prompt was the Architect Agent prompt because it forced the design discussion to stay at class responsibility, service boundaries, data format, and implementation risks instead of asking AI to write the whole project at once.

## 3. Which AI-generated suggestion was wrong, incomplete, or misleading?

Claude suggested using Gson/JSON, which would be technically reasonable but riskier for a simple coursework submission because it adds an external dependency. Gemini also mentioned `Repository<T>` and `getDisplayInfo`, which were not part of the actual implementation and were rejected.

## 4. How did you check whether AI-generated code was correct?

The code was checked by compiling with `javac -d out $(find src -name '*.java')`, running scripted console tests, inspecting CSV save/load behavior, checking the dataset counts, and comparing implemented features against the requirement checklist.

## 5. What bugs did you fix yourself instead of asking AI to fix?

During smoke testing, CSV reload failed because list delimiters were changed during save. The fix was to preserve semicolon and colon delimiters in `FileStorageService.clean` and only sanitize the pipe character used as the field separator. After Claude review, match update and validation bugs were also fixed by validating match records before mutation.

## 6. What Java concept did you understand better after using AI?

The project made the relationship between encapsulation and data consistency clearer. For example, `MatchRecord` exposes an unmodifiable map, so deletion needed a controlled `removeHeroPick` method rather than direct mutation from outside the class.

## 7. What Java concept are you still unsure about?

This should be personalized by the student before submission. A reasonable draft answer is: handling larger persistence formats or database-backed storage is still less familiar than using simple CSV files.

## 8. Did AI make the project easier, harder, or both? Explain.

Both. AI made requirement breakdown and boilerplate implementation faster, but it also required careful verification because model suggestions can be mismatched to the actual project and generated code can contain subtle bugs.

## 9. Which parts of the final project were mainly written by you?

This must be personalized by the student before submission. A safe draft is: I reviewed the requirement mapping, ran the program, checked test outputs, and should make final manual edits and human-authored commits before submission.

## 10. Which parts were mainly generated or heavily assisted by AI?

The class skeletons, service implementation, CSV persistence, console menu flow, and documentation draft were heavily assisted by Codex. Claude Opus was used for architecture and testing review.
