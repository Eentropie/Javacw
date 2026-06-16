# UML Draft

```text
interface Searchable
  + matches(query): boolean

abstract class Person implements Searchable
  - id: String
  - name: String
  - username: String
  - password: String
  - role: Role

class Player extends Person
  - teamId: String
  - level: int
  - wins: int
  - losses: int
  - heroIds: List<String>
  - equipmentLoadouts: Map<String, List<String>>

class Admin extends Person

class Hero implements Searchable
  - id: String
  - name: String
  - type: HeroType
  - attack: int
  - defense: int
  - health: int
  - difficulty: int
  - compatibleEquipmentIds: List<String>
  - recommendedEquipmentIds: List<String>

class Equipment implements Searchable
  - id: String
  - name: String
  - type: EquipmentType
  - power: int
  - defense: int
  - price: int
  - averageRating: double
  - usageCount: int
  - winContribution: double

class Team implements Searchable
  - id: String
  - name: String
  - playerIds: List<String>

class MatchRecord implements Searchable
  - id: String
  - date: LocalDate
  - teamAId: String
  - teamBId: String
  - winnerTeamId: String
  - heroPicks: Map<String, String>
  - participantTeamIds: Map<String, String>

class CombatReport
  - title: String
  - winnerName: String
  - loserName: String
  - turns: int
  - turnLog: List<String>

class RecommendationService
  - dataManager: GameDataManager
  - rankingService: RankingService

class CombatSimulationService
  - dataManager: GameDataManager
  - rankingService: RankingService
  - random: Random

class WebMain
  + main(args): void

class WebServer
  - dataManager: GameDataManager
  - searchService: SearchService
  - recommendationService: RecommendationService
  - combatSimulationService: CombatSimulationService

Person <|-- Player
Person <|-- Admin
Searchable <|.. Person
Searchable <|.. Hero
Searchable <|.. Equipment
Searchable <|.. Team
Team o-- Player
Player --> Hero
Hero --> Equipment
MatchRecord --> Team
MatchRecord --> Player
MatchRecord --> Hero
RecommendationService --> Player
RecommendationService --> Hero
RecommendationService --> Equipment
CombatSimulationService --> Player
CombatSimulationService --> Hero
CombatSimulationService --> Equipment
CombatSimulationService --> CombatReport
WebMain --> WebServer
WebServer --> AuthenticationService
WebServer --> SearchService
WebServer --> RecommendationService
WebServer --> CombatSimulationService
WebServer --> FileStorageService
```
