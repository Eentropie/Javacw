package service;

import model.CombatReport;
import model.Equipment;
import model.Hero;
import model.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CombatSimulationService {
    private static final int MAX_TURNS = 100;

    private final GameDataManager dataManager;
    private final RankingService rankingService;
    private final Random random;

    public CombatSimulationService(GameDataManager dataManager, RankingService rankingService) {
        this(dataManager, rankingService, new Random());
    }

    public CombatSimulationService(GameDataManager dataManager, RankingService rankingService, Random random) {
        this.dataManager = dataManager;
        this.rankingService = rankingService;
        this.random = random;
    }

    public CombatReport simulateDuel(String playerAId, String heroAId, String equipmentAId,
                                     String playerBId, String heroBId, String equipmentBId) {
        Combatant first = buildCombatant(playerAId, heroAId, equipmentAId);
        Combatant second = buildCombatant(playerBId, heroBId, equipmentBId);
        List<String> log = new ArrayList<>();

        Combatant attacker = first;
        Combatant defender = second;
        int turn = 0;
        // The turn cap prevents an infinite duel if two defensive builds are nearly equal.
        while (first.health > 0 && second.health > 0 && turn < MAX_TURNS) {
            turn++;
            resolveTurn(turn, attacker, defender, log);
            Combatant nextAttacker = defender;
            defender = attacker;
            attacker = nextAttacker;
        }

        Combatant winner = first.health >= second.health ? first : second;
        Combatant loser = winner == first ? second : first;
        if (turn == MAX_TURNS && first.health > 0 && second.health > 0) {
            log.add("Turn limit reached; higher remaining HP decides the winner.");
        }

        return new CombatReport(
                first.label() + " vs " + second.label(),
                winner.label(),
                loser.label(),
                turn,
                winner.health,
                loser.health,
                log);
    }

    private Combatant buildCombatant(String playerId, String heroId, String equipmentId) {
        Player player = dataManager.requirePlayer(playerId);
        Hero hero = dataManager.requireHero(heroId);
        if (!player.ownsHero(heroId)) {
            throw new IllegalArgumentException("Player " + playerId + " does not own hero " + heroId);
        }
        Equipment equipment = equipmentId == null || equipmentId.isBlank()
                ? bestEquipmentFor(hero)
                : dataManager.requireEquipment(equipmentId);
        if (!hero.getCompatibleEquipmentIds().contains(equipment.getId())) {
            throw new IllegalArgumentException("Equipment " + equipment.getId() + " is not compatible with hero " + heroId);
        }
        return new Combatant(player, hero, equipment);
    }

    private Equipment bestEquipmentFor(Hero hero) {
        return hero.getCompatibleEquipmentIds().stream()
                .map(dataManager::requireEquipment)
                .sorted((a, b) -> Double.compare(rankingService.equipmentScore(b), rankingService.equipmentScore(a)))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Hero has no compatible equipment: " + hero.getId()));
    }

    private void resolveTurn(int turn, Combatant attacker, Combatant defender, List<String> log) {
        int before = defender.health;
        double dodgeChance = Math.min(0.25, 0.04 + defender.hero.getDifficulty() * 0.01);
        if (random.nextDouble() < dodgeChance) {
            log.add("Turn " + turn + ": " + defender.label() + " dodged " + attacker.label() + "'s attack.");
            return;
        }

        int damage = Math.max(20, attacker.attack - defender.defense / 2);
        double critChance = Math.min(0.30, 0.05 + attacker.hero.getDifficulty() * 0.015);
        boolean critical = random.nextDouble() < critChance;
        if (critical) {
            damage = (int) Math.round(damage * 1.5);
        }
        defender.health = Math.max(0, defender.health - damage);
        log.add("Turn " + turn + ": " + attacker.label()
                + " dealt " + damage + " damage"
                + (critical ? " (CRITICAL)" : "")
                + " to " + defender.label()
                + " HP " + before + " -> " + defender.health + ".");
    }

    private static class Combatant {
        private final Player player;
        private final Hero hero;
        private final Equipment equipment;
        private final int attack;
        private final int defense;
        private int health;

        private Combatant(Player player, Hero hero, Equipment equipment) {
            this.player = player;
            this.hero = hero;
            this.equipment = equipment;
            this.attack = hero.getAttack() + equipment.getPower() + player.getLevel() * 2;
            this.defense = hero.getDefense() + equipment.getDefense() + player.getLevel();
            this.health = hero.getHealth() + equipment.getDefense() * 8 + player.getLevel() * 25;
        }

        private String label() {
            return player.getName() + "/" + hero.getName() + "/" + equipment.getName();
        }
    }
}
