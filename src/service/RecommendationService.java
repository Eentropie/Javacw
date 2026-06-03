package service;

import enums.EquipmentType;
import enums.HeroType;
import model.Equipment;
import model.Hero;
import model.Player;
import model.Team;

import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;

public class RecommendationService {
    private final GameDataManager dataManager;
    private final RankingService rankingService;

    public RecommendationService(GameDataManager dataManager, RankingService rankingService) {
        this.dataManager = dataManager;
        this.rankingService = rankingService;
    }

    public String heroRecommendationReport(String playerQuery, int limit) {
        Player player = dataManager.findPlayer(playerQuery)
                .orElseThrow(() -> new IllegalArgumentException("Player not found: " + playerQuery));

        StringBuilder output = new StringBuilder();
        output.append("Hero recommendation formula: stats + owner success + team role gap + difficulty fit + equipment support; owned heroes receive a small penalty.")
                .append(System.lineSeparator());
        output.append("Recommendations for player ").append(player.getName())
                .append(" (team ").append(dataManager.requireTeam(player.getTeamId()).getName()).append(")")
                .append(System.lineSeparator());

        int rank = 1;
        for (Hero hero : recommendHeroes(player, limit)) {
            output.append(rank++).append(". ")
                    .append(hero.getName())
                    .append(" [").append(hero.getType()).append("]")
                    .append(" score ").append(String.format("%.2f", heroRecommendationScore(player, hero)))
                    .append(player.ownsHero(hero.getId()) ? " (already owned)" : " (new option)")
                    .append(System.lineSeparator());
            output.append("   Reason: ").append(heroReason(player, hero)).append(System.lineSeparator());
        }
        return output.toString();
    }

    public String equipmentRecommendationReport(String heroQuery, int limit) {
        Hero hero = dataManager.findHero(heroQuery)
                .orElseThrow(() -> new IllegalArgumentException("Hero not found: " + heroQuery));

        StringBuilder output = new StringBuilder();
        output.append("Equipment recommendation formula: equipment ranking score + explicit recommendation bonus + hero-type synergy.")
                .append(System.lineSeparator());
        output.append("Recommendations for hero ").append(hero.getName())
                .append(" [").append(hero.getType()).append("]")
                .append(System.lineSeparator());

        int rank = 1;
        for (Equipment item : recommendEquipment(hero, limit)) {
            output.append(rank++).append(". ")
                    .append(item.getName())
                    .append(" [").append(item.getType()).append("]")
                    .append(" score ").append(String.format("%.2f", equipmentRecommendationScore(hero, item)))
                    .append(System.lineSeparator());
            output.append("   Reason: ").append(equipmentReason(hero, item)).append(System.lineSeparator());
        }
        return output.toString();
    }

    public List<Hero> recommendHeroes(Player player, int limit) {
        return dataManager.getHeroes().stream()
                .sorted(Comparator.comparingDouble((Hero hero) -> heroRecommendationScore(player, hero)).reversed()
                        .thenComparing(Hero::getName)
                        .thenComparing(Hero::getId))
                .limit(Math.max(0, limit))
                .toList();
    }

    public List<Equipment> recommendEquipment(Hero hero, int limit) {
        List<String> candidateIds = hero.getCompatibleEquipmentIds().isEmpty()
                ? dataManager.getEquipment().stream().map(Equipment::getId).toList()
                : hero.getCompatibleEquipmentIds();

        return candidateIds.stream()
                .map(dataManager::requireEquipment)
                .sorted(Comparator.comparingDouble((Equipment item) -> equipmentRecommendationScore(hero, item)).reversed()
                        .thenComparing(Equipment::getName)
                        .thenComparing(Equipment::getId))
                .limit(Math.max(0, limit))
                .toList();
    }

    public double heroRecommendationScore(Player player, Hero hero) {
        // Combine independent signals so the report can justify each recommendation.
        double statScore = hero.getAttack() * 0.10 + hero.getDefense() * 0.08 + hero.getHealth() * 0.005;
        double ownerSuccess = averageOwnerWinRate(hero) * 0.15;
        double roleGapBonus = teamRoleGapBonus(player, hero.getType());
        double difficultyFit = difficultyFit(player, hero);
        double equipmentSupport = recommendEquipment(hero, 2).stream()
                .mapToDouble(rankingService::equipmentScore)
                .average()
                .orElse(0.0) / 10.0;
        double ownedPenalty = player.ownsHero(hero.getId()) ? 20.0 : 0.0;
        return statScore + ownerSuccess + roleGapBonus + difficultyFit + equipmentSupport - ownedPenalty;
    }

    public double equipmentRecommendationScore(Hero hero, Equipment item) {
        // Start from the existing ranking formula, then add hero-specific context.
        double explicitBonus = hero.getRecommendedEquipmentIds().contains(item.getId()) ? 30.0 : 0.0;
        double compatibilityBonus = hero.getCompatibleEquipmentIds().contains(item.getId()) ? 12.0 : 0.0;
        return rankingService.equipmentScore(item)
                + explicitBonus
                + compatibilityBonus
                + typeSynergy(hero.getType(), item.getType());
    }

    private String heroReason(Player player, Hero hero) {
        String ownership = player.ownsHero(hero.getId()) ? "already owned, lower priority" : "not owned by player";
        return ownership
                + ", role gap bonus " + String.format("%.1f", teamRoleGapBonus(player, hero.getType()))
                + ", difficulty fit " + String.format("%.1f", difficultyFit(player, hero));
    }

    private String equipmentReason(Hero hero, Equipment item) {
        StringBuilder reason = new StringBuilder();
        if (hero.getRecommendedEquipmentIds().contains(item.getId())) {
            reason.append("listed as recommended; ");
        }
        if (hero.getCompatibleEquipmentIds().contains(item.getId())) {
            reason.append("compatible with hero; ");
        }
        reason.append("type synergy ").append(String.format("%.1f", typeSynergy(hero.getType(), item.getType())));
        return reason.toString();
    }

    private double averageOwnerWinRate(Hero hero) {
        OptionalDouble average = dataManager.getPlayers().stream()
                .filter(player -> player.ownsHero(hero.getId()))
                .mapToDouble(Player::getWinRate)
                .average();
        return average.orElse(0.0);
    }

    private double teamRoleGapBonus(Player player, HeroType heroType) {
        Team team = dataManager.requireTeam(player.getTeamId());
        if (team.getPlayerIds().isEmpty()) {
            return 0.0;
        }
        // Count owned hero types across the team to reward missing or rare roles.
        Map<HeroType, Integer> counts = new EnumMap<>(HeroType.class);
        for (String playerId : team.getPlayerIds()) {
            Player member = dataManager.requirePlayer(playerId);
            for (String heroId : member.getHeroIds()) {
                counts.merge(dataManager.requireHero(heroId).getType(), 1, Integer::sum);
            }
        }
        int count = counts.getOrDefault(heroType, 0);
        if (count == 0) {
            return 12.0;
        }
        if (count == 1) {
            return 6.0;
        }
        return 0.0;
    }

    private double difficultyFit(Player player, Hero hero) {
        int targetDifficulty = Math.min(10, Math.max(1, 3 + player.getLevel() / 5));
        return Math.max(0, 10 - Math.abs(targetDifficulty - hero.getDifficulty()));
    }

    private double typeSynergy(HeroType heroType, EquipmentType equipmentType) {
        return switch (equipmentType) {
            case ATTACK -> heroType == HeroType.MARKSMAN || heroType == HeroType.ASSASSIN || heroType == HeroType.WARRIOR ? 10.0 : 2.0;
            case MAGIC -> heroType == HeroType.MAGE || heroType == HeroType.SUPPORT ? 10.0 : 1.0;
            case DEFENSE -> heroType == HeroType.TANK || heroType == HeroType.SUPPORT || heroType == HeroType.WARRIOR ? 8.0 : 2.0;
            case MOVEMENT -> 4.0;
            case JUNGLE -> heroType == HeroType.ASSASSIN || heroType == HeroType.WARRIOR ? 6.0 : 1.0;
            case SUPPORT -> heroType == HeroType.SUPPORT ? 10.0 : 2.0;
        };
    }
}
