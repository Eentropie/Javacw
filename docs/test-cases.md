# Manual Test Cases

Manual test cases will be updated after implementation. Actual output must be recorded from running the program.

## Test 01: Admin Login

Input:

```text
Username: admin
Password: admin123
```

Expected:

```text
Admin menu is displayed.
```

Actual: To be tested.

Result: Pending.

## Test 02: Player Login

Input:

```text
Username: libai
Password: player123
```

Expected:

```text
Player menu is displayed.
```

Actual: To be tested.

Result: Pending.

## Test 03: Player Lookup by Name

Input:

```text
Search player: Li Bai
```

Expected: Player ID, name, team, level, win rate, owned heroes, and equipment are displayed.

Actual: To be tested.

Result: Pending.

## Test 04: Team Overview

Input:

```text
Search team: Chang'an Blades
```

Expected: Members, average level, total matches, win rate, and top player are displayed.

Actual: To be tested.

Result: Pending.

## Test 05: Hero Details

Input:

```text
Search hero: Diaochan
```

Expected: Hero type, stats, compatible equipment, owners, and recommendations are displayed.

Actual: To be tested.

Result: Pending.

## Test 06: Equipment Ranking

Input:

```text
Equipment statistics
```

Expected: Equipment list is sorted by documented score.

Actual: To be tested.

Result: Pending.

## Test 07: Player Match History

Input:

```text
Player ID: P001
N: 3
```

Expected: Last three relevant matches and hero pick rate are displayed.

Actual: To be tested.

Result: Pending.

## Test 08: Leaderboard by Win Rate

Input:

```text
Top players by win rate
X: 5
```

Expected: Five players are shown in descending win-rate order with tie handling.

Actual: To be tested.

Result: Pending.

## Test 09: Admin Adds Equipment

Input:

```text
Add equipment E999 Test Blade
```

Expected: Equipment is added and visible in equipment statistics.

Actual: To be tested.

Result: Pending.

## Test 10: Player Permission Restriction

Input:

```text
Login as player and try to access data-management menu.
```

Expected: Data-management options are not available to the player.

Actual: To be tested.

Result: Pending.

## Test 11: Invalid Login

Input:

```text
Username: unknown
Password: wrong
```

Expected: Login fails without crashing.

Actual: To be tested.

Result: Pending.

## Test 12: Save and Reload

Input:

```text
Save data, restart program, search existing player.
```

Expected: Data loads from CSV and lookup still works.

Actual: To be tested.

Result: Pending.
