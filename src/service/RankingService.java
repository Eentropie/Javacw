package service;

import model.Equipment;
import model.Player;

import java.util.Comparator;
import java.util.List;

public class RankingService {
    private final GameDataManager dataManager;

    public RankingService(GameDataManager dataManager) {
        this.dataManager = dataManager;
    }

    public double equipmentScore(Equipment item) {
        int compatibleHeroCount = dataManager.countCompatibleHeroes(item.getId());
        return item.getUsageCount() * 1.5
                + item.getAverageRating() * 10
                + compatibleHeroCount * 2
                + item.getWinContribution() * 20;
    }

    public List<Equipment> topEquipment(int limit) {
        return dataManager.getEquipment().stream()
                .sorted(Comparator.comparingDouble(this::equipmentScore).reversed()
                        .thenComparing(Equipment::getName)
                        .thenComparing(Equipment::getId))
                .limit(Math.max(0, limit))
                .toList();
    }

    public double playerCustomScore(Player player) {
        return player.getWinRate() * 0.6
                + player.getLevel() * 0.3
                + player.getTotalMatches() * 0.1;
    }

    public List<Player> topPlayers(String mode, int limit) {
        Comparator<Player> comparator = switch (mode.toLowerCase()) {
            case "level" -> Comparator.comparingInt(Player::getLevel).reversed();
            case "matches" -> Comparator.comparingInt(Player::getTotalMatches).reversed();
            case "score" -> Comparator.comparingDouble(this::playerCustomScore).reversed();
            case "winrate", "win_rate", "win rate" -> Comparator.comparingDouble(Player::getWinRate).reversed();
            default -> throw new IllegalArgumentException("Unknown leaderboard mode: " + mode);
        };

        return dataManager.getPlayers().stream()
                .sorted(comparator.thenComparing(Player::getName).thenComparing(Player::getId))
                .limit(Math.max(0, limit))
                .toList();
    }
}
