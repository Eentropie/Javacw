# Data Design

## Files

The application stores data in the `data/` folder.

| File | Fields |
|---|---|
| `admins.csv` | `id`, `name`, `username`, `password` |
| `teams.csv` | `id`, `name` |
| `equipment.csv` | `id`, `name`, `type`, `power`, `defense`, `price`, `averageRating`, `usageCount`, `winContribution` |
| `heroes.csv` | `id`, `name`, `type`, `attack`, `defense`, `health`, `difficulty`, `compatibleEquipmentIds`, `recommendedEquipmentIds` |
| `players.csv` | `id`, `name`, `username`, `password`, `teamId`, `level`, `wins`, `losses`, `heroIds`, `equipmentLoadouts` |
| `matches.csv` | `id`, `date`, `teamAId`, `teamBId`, `winnerTeamId`, `playerHeroTeamPicks` |

## Delimiters

- `|` separates CSV fields.
- `;` separates ID lists.
- `:` separates keys and values in loadouts and match records.
- `,` separates equipment IDs within one hero loadout.

Player loadout example:

```text
H001:E002,E017;H007:E002,E018
```

This means that the player's `H001` hero has `E002` and `E017` equipped, while `H007` has `E002` and `E018`.

Match pick example:

```text
P001:H001:T001;P006:H008:T002
```

This stores the player, selected hero, and historical team for each pick. The loader also accepts the older `playerId:heroId` form and derives the team during migration.

## Load Order

Data is loaded in this order:

1. Admins
2. Teams
3. Equipment
4. Heroes
5. Players
6. Match records

This order prevents references from being checked before the referenced records exist.

## Relationship Rules

- A player must reference an existing team.
- A player-owned hero ID must reference an existing hero.
- A player equipment loadout must reference an owned hero and equipment compatible with that hero.
- A hero's compatible and recommended equipment IDs must reference existing equipment.
- A match must reference two existing teams, one participating winner team, existing players, existing heroes, and a historical participant team for each pick.
- A team's player list is rebuilt from players after loading.

## Save Strategy

Each file is written to a uniquely named temporary file in the same directory and then moved to the final CSV path. Unique names prevent two running interfaces from competing for one fixed `.tmp` file, while same-directory atomic replacement reduces the chance of a partially written data file.

## Known Tradeoff

CSV is simple and dependency-free, but it does not support arbitrary delimiter characters inside names. The current implementation replaces the field delimiter `|` in text values before saving. Semicolons and colons are preserved because they are needed for list and map fields.
