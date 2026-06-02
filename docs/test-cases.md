# Manual Test Cases

These tests were run with:

```bash
javac -d out $(find src -name '*.java')
java -cp out Main
```

## Test 01: Admin Login

Input:

```text
1
admin
admin123
```

Expected: Admin menu is displayed.

Actual: The program printed `Logged in as System Admin (ADMIN)` and displayed the Admin menu with Data management and Save data options.

Result: Pass.

## Test 02: Player Login

Input:

```text
1
libai
player123
```

Expected: Player menu is displayed.

Actual: The program printed `Logged in as Li Bai (PLAYER)` and displayed the Player menu without data-management options.

Result: Pass.

## Test 03: Player Lookup by Name

Input:

```text
Admin login
Player lookup
Search player: Li Bai
```

Expected: The system displays Li Bai's ID, team, level, win rate, owned heroes, and equipment.

Actual: The system displayed `P001`, team `Chang'an Blades`, level `28`, win rate `72.7%`, and the owned heroes Li Bai, Sun Wukong, and Mulan with compatible equipment lists.

Result: Pass.

## Test 04: Team Overview

Input:

```text
Admin login
Team overview
Search team: T001
```

Expected: Members, average level, total matches, win rate, and top player are displayed.

Actual: The system displayed five Chang'an Blades members, average level `25.4`, total matches `7`, team win rate `42.9%`, and top player `Li Bai`.

Result: Pass.

## Test 05: Hero Details

Input:

```text
Admin login
Hero details
Search hero: Diaochan
```

Expected: Hero type, stats, compatible equipment, owners, and recommendations are displayed.

Actual: The system displayed Diaochan as `MAGE`, stats `ATK 155, DEF 70, HP 3000`, compatible equipment `[Arcane Boots, Book of Wisdom, Staff of Nuwa, Holy Grail]`, recommended equipment `[Book of Wisdom, Holy Grail]`, and owners Chen Qian, Xiao Qiao, and Luna Star.

Result: Pass.

## Test 06: Equipment Ranking

Input:

```text
Admin login
Equipment statistics
Top: 5
```

Expected: Equipment list is sorted by the documented formula.

Actual: The system printed the formula and ranked Resistance Boots, Endless Battle, Shadow Blade, Ominous Premonition, and Guardian Armor as the top five items.

Result: Pass.

## Test 07: Player Match History

Input:

```text
Admin login
Match history
Player match history
N: 3
Player ID: P001
```

Expected: Last three relevant matches and hero pick rate are displayed.

Actual: The system displayed Li Bai's three latest matches, record `1W-2L`, and pick rates for Li Bai, Mulan, and Sun Wukong.

Result: Pass.

## Test 08: Team Match History

Input:

```text
Admin login
Match history
Team match history
N: 2
Team ID: T003
```

Expected: Last two team matches, opponent, results, hero picks, win/loss record, and pick rates are displayed.

Actual: The system displayed Cloud Arena's two latest matches against River Guardians and Chang'an Blades, both wins, with hero picks and hero pick rates.

Result: Pass.

## Test 09: Leaderboard by Win Rate

Input:

```text
Admin login
Leaderboard
Mode: winrate
Top: 5
```

Expected: Five players are shown in descending win-rate order with tie handling.

Actual: The system displayed Mulan, Hou Yi, Li Bai, Xiao Qiao, and Marco Ace as the top five, including level, win rate, match count, and custom score.

Result: Pass.

## Test 10: Admin Adds and Deletes Equipment

Input:

```text
Admin login
Data management
Add equipment E999 Test Blade
Delete equipment E999
```

Expected: Equipment is added, then deleted without leaving dirty data.

Actual: The system printed `Equipment added.` and then `Equipment deleted.` A later `git status --short` showed no data file changes after the add/delete sequence.

Result: Pass.

## Test 11: Invalid Login

Input:

```text
1
unknown
wrong
```

Expected: Login fails without crashing.

Actual: The system printed `Invalid username or password.` and returned to the login menu.

Result: Pass.

## Test 12: Save and Reload

Input:

```text
Start program after CSV files exist.
0
```

Expected: Data loads from CSV and the program exits normally.

Actual: The system printed `Loaded data from data`, showed the login menu, and exited after option `0`.

Result: Pass.

## Bug Found During Testing

CSV list fields were initially saved incorrectly because the cleaning method replaced semicolons with commas. The next run failed with `Unknown equipment ID: E001,E002,E013,E017`. This was fixed in commit `ccee720` by preserving list and key-value delimiters while still sanitizing the field separator `|`.

## Bug Found During Claude Review

Claude Opus reviewed the actual source files and found that match editing could mutate an existing record before validation. It also found that match winners were not restricted to participating teams and that some seed matches had duplicate hero picks. These issues were fixed in commit `12948b5` by adding centralized match validation, safe update-before-mutation behavior, participant checks, ownership checks, and corrected seed match data.
