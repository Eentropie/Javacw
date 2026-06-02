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
```
