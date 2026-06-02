# Data Design

## Files

The application stores data in the `data/` folder.

| File | Fields |
|---|---|
| `admins.csv` | `id`, `name`, `username`, `password` |
| `teams.csv` | `id`, `name` |
| `equipment.csv` | `id`, `name`, `type`, `power`, `defense`, `price`, `averageRating`, `usageCount`, `winContribution` |
| `heroes.csv` | `id`, `name`, `type`, `attack`, `defense`, `health`, `difficulty`, `compatibleEquipmentIds`, `recommendedEquipmentIds` |
| `players.csv` | `id`, `name`, `username`, `password`, `teamId`, `level`, `wins`, `losses`, `heroIds` |
| `matches.csv` | `id`, `date`, `teamAId`, `teamBId`, `winnerTeamId`, `playerHeroPicks` |

## Delimiters

- `|` separates CSV fields.
- `;` separates ID lists.
- `:` separates player/hero pairs in match records.

Example:

```text
P001:H001;P002:H002
```

This means player `P001` picked hero `H001`, and player `P002` picked hero `H002`.

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
- A hero's compatible and recommended equipment IDs must reference existing equipment.
- A match must reference two existing teams, one existing winner team, existing players, and existing heroes.
- A team's player list is rebuilt from players after loading.

## Save Strategy

Each file is written to a temporary file first and then moved to the final CSV path. This reduces the chance of a partially written data file if saving is interrupted.

## Known Tradeoff

CSV is simple and dependency-free, but it does not support arbitrary delimiter characters inside names. The current implementation replaces the field delimiter `|` in text values before saving. Semicolons and colons are preserved because they are needed for list and map fields.
