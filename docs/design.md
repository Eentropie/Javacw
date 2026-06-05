# Design Notes

## Architecture

The application uses a layered architecture:

- Model layer: domain objects and enums.
- Service layer: authentication, data management, searching, ranking, and storage.
- Utility layer: seed data and input handling.
- UI layer: `Main` console menus, optional `gui.DesktopMain` Swing desktop app, and optional `web.WebMain` browser frontend.

This keeps `Main` responsible for interaction flow rather than business rules.

## Optional Swing Desktop App

`gui.DesktopMain` is a dependency-free local desktop interface built with Java Swing. It is a separate entry point and does not replace the required console `Main` class. It loads and saves the same CSV data through `FileStorageService` and delegates business behavior to the existing services:

- `AuthenticationService` for Admin and Player login.
- `SearchService` for lookup, team overview, hero details, equipment statistics, match history, and leaderboards.
- `RecommendationService` for hero and equipment recommendations.
- `CombatSimulationService` for turn-based duel reports.
- `GameDataManager` for admin CRUD and player profile updates.

The desktop app includes a `--smoke` mode so the entry point can be verified from a terminal without opening a graphical window. macOS and Windows launchers compile the same source tree and start `gui.DesktopMain`.

## ID-Backed Object Associations

The model stores relationships by stable IDs so CSV save/load remains simple and portable. To make the object relationships explicit in the Java design, `GameDataManager` resolves those IDs into domain-object associations through helper methods:

- `teamForPlayer(Player)` resolves a player's team.
- `playersForTeam(Team)` resolves team membership as `Player` objects.
- `heroesForPlayer(Player)` resolves owned heroes as `Hero` objects.
- `compatibleEquipmentForHero(Hero)` and `recommendedEquipmentForHero(Hero)` resolve hero-equipment associations as `Equipment` objects.
- `playersOwningHero(Hero)` resolves the reverse player-hero association.
- `matchesForTeam(String)` and `matchesForPlayer(String)` resolve match history relationships.

`SearchService` uses these association helpers when building reports. This keeps persistence ID-based while still demonstrating association and aggregation in the service layer.

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

## Recommendation Engine

The recommendation engine is implemented in `RecommendationService` and has two report types.

### Why this formula

The hero recommendation score combines several independent signals so that each recommendation can be justified with a reason line rather than just a bare number. The idea is that a useful recommendation should consider more than raw stats.

Hero recommendation score:

```text
score = statScore
      + ownerSuccess
      + teamRoleGapBonus
      + difficultyFit
      + equipmentSupport
      - ownedPenalty
```

This reuses the existing equipment ranking formula and adds hero-specific context.

## Combat Simulation

`CombatSimulationService` provides a deterministic-testable, turn-based duel. A player must own the selected hero, and selected equipment must be compatible with that hero. If equipment is left blank in the console, the service auto-picks the strongest compatible equipment by the existing equipment score.

Each turn uses:

```text
damage = max(20, attackerAttack - defenderDefense / 2)
critical chance = min(0.30, 0.05 + attackerDifficulty * 0.015)
dodge chance = min(0.25, 0.04 + defenderDifficulty * 0.01)
```

A critical hit multiplies damage by 1.5. A dodge cancels the turn's damage. A 100-turn cap prevents infinite loops if two defensive builds are very close.

## Optional Web Frontend

The web frontend is implemented as a second UI layer:

- `web.WebMain` loads CSV data using the same `FileStorageService` fallback rules as the console app, starts the local server, and opens the browser.
- `web.WebServer` uses JDK `HttpServer` to serve static files from `web/` and expose local API endpoints under `/api/`.
- Static assets in `web/index.html`, `web/styles.css`, and `web/app.js` provide login, lookup, leaderboard, match history, recommendations, and combat simulation.

The web API returns JSON report objects but delegates the actual business behavior to `AuthenticationService`, `SearchService`, `RankingService`, `RecommendationService`, `CombatSimulationService`, and `FileStorageService`. This keeps the browser frontend aligned with the console behavior. Ranking, equipment, and match-history endpoints also support `format=json`, which returns structured `columns` and `rows` for real table rendering while preserving the original text-report response by default.

The frontend design is intentionally dashboard-like rather than marketing-like: a compact overview band, left tool navigation, central task panel, contextual insight sidebar, and bottom report panel. The static assets also include request loading feedback, status badges, toast messages, responsive breakpoints, table output for structured endpoints, copy-to-CSV behavior, and CSV export controls so the optional browser interface feels like a real local control panel while preserving the required console workflow.

A later polish pass added low-risk usability details directly to the main frontend: a local SVG favicon, browser color-scheme metadata, visible API latency, report copy-to-clipboard, contextual lookup placeholders, relative match dates, and stronger keyboard focus styling. A branch experiment then proved that structured result tables were feasible without parsing report strings, so only the stable `format=json`, table rendering, and CSV portions were migrated into `main`. Higher-risk ideas such as hash routing, raw JSON preview, retry/backoff, and session timeout remain branch-only candidates.

A final Gemini 3.1 Pro design review was used only for visual polish. The accepted changes were limited to static theme and layout CSS: dark battlefield surfaces, gold action accents, crisp panel borders, dark form controls, scoreboard-style table headers, and a wide-screen insight-panel height cap so the report area remains visible sooner. No external game assets, CDNs, JavaScript dependencies, backend API changes, or console-entry changes were introduced.
