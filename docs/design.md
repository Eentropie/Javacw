# Design Notes

## Architecture

The application uses a layered console architecture:

- Model layer: domain objects and enums.
- Service layer: authentication, data management, searching, ranking, and storage.
- Utility layer: seed data and input handling.
- UI layer: `Main` console menus.

This keeps `Main` responsible for interaction flow rather than business rules.

## Equipment Ranking Formula

Equipment score:

```text
score = usageCount * 1.5
      + averageRating * 10
      + compatibleHeroCount * 2
      + winContribution * 20
```

This formula gives useful weight to actual usage and rating, while still rewarding compatibility and match contribution. Ties are sorted by equipment name and then equipment ID.

## Leaderboard Tie Handling

All player leaderboard modes apply the selected metric first. If two players have the same metric value, the tie is resolved by player name alphabetically and then player ID.
