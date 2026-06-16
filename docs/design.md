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

The desktop visual layer uses the standard JDK Metal look and feel with a small `UIManager` dark-theme pass. The palette is matched to the optional web frontend's deployed tokens: dark paper and panel surfaces, gold separators, readable light text, muted data summaries, and role-colored status labels for Admin and Player logins. This was intentionally implemented as theme configuration rather than custom painting, hover listeners, or external look-and-feel dependencies so the app remains portable on Windows and macOS.

## ID-Backed Object Associations

The model stores relationships by stable IDs so CSV save/load remains simple and portable. To make the object relationships explicit in the Java design, `GameDataManager` resolves those IDs into domain-object associations through helper methods:

- `teamForPlayer(Player)` resolves a player's team.
- `playersForTeam(Team)` resolves team membership as `Player` objects.
- `heroesForPlayer(Player)` resolves owned heroes as `Hero` objects.
- `compatibleEquipmentForHero(Hero)` and `recommendedEquipmentForHero(Hero)` resolve hero-equipment associations as `Equipment` objects.
- `playersOwningHero(Hero)` resolves the reverse player-hero association.
- `matchesForTeam(String)` and `matchesForPlayer(String)` resolve match history relationships.

`SearchService` uses these association helpers when building reports. This keeps persistence ID-based while still demonstrating association and aggregation in the service layer.

## Player Equipment Loadouts

Each `Player` stores an equipment loadout for each owned hero as a map from hero ID to equipment IDs. This is separate from a hero's global compatibility and recommendation lists:

- compatible equipment describes every item the hero is allowed to use;
- recommended equipment supplies sensible defaults;
- a player loadout records the items actually equipped on that player's owned hero.

`GameDataManager` validates that a loadout only references heroes owned by the player and equipment compatible with that hero. Deleting a hero or equipment item also removes the related loadout references. Player reports therefore display actual equipped items rather than all compatible items.

## Historical Match Membership

`MatchRecord` stores both the hero picked by each player and the team represented by that player in that match. Keeping this historical participant-team mapping prevents later player transfers from changing old opponents or results. Team match reports filter picks through this mapping, so pick-rate calculations include only the requested team's heroes.

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
statScore = attack * 0.10 + defense * 0.08 + health * 0.005
ownerSuccess = average owner win rate * 0.15
teamRoleGapBonus = 12 if the role is absent, 6 if it appears once, otherwise 0
difficultyFit = max(0, 10 - abs(targetDifficulty - heroDifficulty))
equipmentSupport = average score of the best two compatible items / 10
ownedPenalty = 20 if the player already owns the hero, otherwise 0

score = statScore + ownerSuccess + teamRoleGapBonus
      + difficultyFit + equipmentSupport - ownedPenalty
```

The owned-hero penalty lowers repeated recommendations without excluding them completely. This keeps the output useful when the requested limit is larger than the number of unowned heroes.

Equipment recommendation score:

```text
explicitRecommendationBonus = 30 if listed as recommended, otherwise 0
compatibilityBonus = 12 if listed as compatible, otherwise 0
heroTypeSynergy = 1 to 10 based on hero and equipment type

score = equipmentRankingScore
      + explicitRecommendationBonus
      + compatibilityBonus
      + heroTypeSynergy
```

This reuses the documented equipment ranking formula and then adds hero-specific context. The current weights are manually tuned coursework heuristics rather than values learned from match data.

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
