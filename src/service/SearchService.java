package service;

import model.Equipment;
import model.Hero;
import model.MatchRecord;
import model.Player;
import model.Team;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SearchService {
    private final GameDataManager dataManager;
    private final RankingService rankingService;

    public SearchService(GameDataManager dataManager, RankingService rankingService) {
        this.dataManager = dataManager;
        this.rankingService = rankingService;
    }

    public String playerLookup(String query) {
        Optional<Player> match = dataManager.findPlayer(query);
        if (match.isEmpty()) {
            return "No player found for: " + query;
        }
        return playerReport(match.get());
    }

    public String playerReport(Player player) {
        StringBuilder output = new StringBuilder();
        Team team = dataManager.requireTeam(player.getTeamId());
        output.append("Player ID: ").append(player.getId()).append(System.lineSeparator());
        output.append("Name: ").append(player.getName()).append(System.lineSeparator());
        output.append("Team: ").append(team.getName()).append(" (").append(team.getId()).append(")").append(System.lineSeparator());
        output.append("Level: ").append(player.getLevel()).append(System.lineSeparator());
        output.append("Record: ").append(player.getWins()).append("W-").append(player.getLosses()).append("L").append(System.lineSeparator());
        output.append("Win rate: ").append(formatPercent(player.getWinRate())).append(System.lineSeparator());
        output.append("Owned heroes and equipment:").append(System.lineSeparator());
        for (String heroId : player.getHeroIds()) {
            Hero hero = dataManager.requireHero(heroId);
            output.append("- ").append(hero.getName()).append(" [").append(hero.getType()).append("]").append(System.lineSeparator());
            output.append("  Equipped/compatible: ").append(namesForEquipment(hero.getCompatibleEquipmentIds())).append(System.lineSeparator());
        }
        return output.toString();
    }

    public String teamOverview(String query) {
        Optional<Team> match = dataManager.findTeam(query);
        if (match.isEmpty()) {
            return "No team found for: " + query;
        }
        Team team = match.get();
        List<Player> members = team.getPlayerIds().stream()
                .map(dataManager::requirePlayer)
                .toList();
        int totalLevel = members.stream().mapToInt(Player::getLevel).sum();
        int totalMatches = (int) dataManager.getMatchRecords().stream().filter(record -> record.includesTeam(team.getId())).count();
        int wins = (int) dataManager.getMatchRecords().stream().filter(record -> record.getWinnerTeamId().equals(team.getId())).count();
        double winRate = totalMatches == 0 ? 0.0 : wins * 100.0 / totalMatches;
        Player topPlayer = members.stream()
                .sorted((a, b) -> Double.compare(rankingService.playerCustomScore(b), rankingService.playerCustomScore(a)))
                .findFirst()
                .orElse(null);

        StringBuilder output = new StringBuilder();
        output.append("Team: ").append(team.getName()).append(" (").append(team.getId()).append(")").append(System.lineSeparator());
        output.append("Members:").append(System.lineSeparator());
        for (Player member : members) {
            output.append("- ").append(member.getName())
                    .append(" level ").append(member.getLevel())
                    .append(" win rate ").append(formatPercent(member.getWinRate()))
                    .append(System.lineSeparator());
        }
        output.append("Average level: ").append(members.isEmpty() ? "0.0" : String.format("%.1f", totalLevel * 1.0 / members.size())).append(System.lineSeparator());
        output.append("Total matches: ").append(totalMatches).append(System.lineSeparator());
        output.append("Team win rate: ").append(formatPercent(winRate)).append(System.lineSeparator());
        output.append("Top player: ").append(topPlayer == null ? "None" : topPlayer.getName()).append(System.lineSeparator());
        return output.toString();
    }

    public String heroDetails(String query) {
        Optional<Hero> match = dataManager.findHero(query);
        if (match.isEmpty()) {
            return "No hero found for: " + query;
        }
        Hero hero = match.get();
        StringBuilder output = new StringBuilder();
        output.append("Hero: ").append(hero.getName()).append(" (").append(hero.getId()).append(")").append(System.lineSeparator());
        output.append("Type: ").append(hero.getType()).append(System.lineSeparator());
        output.append("Stats: ATK ").append(hero.getAttack())
                .append(", DEF ").append(hero.getDefense())
                .append(", HP ").append(hero.getHealth())
                .append(", difficulty ").append(hero.getDifficulty())
                .append(System.lineSeparator());
        output.append("Compatible equipment: ").append(namesForEquipment(hero.getCompatibleEquipmentIds())).append(System.lineSeparator());
        output.append("Recommended equipment: ").append(namesForEquipment(hero.getRecommendedEquipmentIds())).append(System.lineSeparator());
        output.append("Players who own this hero:").append(System.lineSeparator());
        dataManager.getPlayers().stream()
                .filter(player -> player.ownsHero(hero.getId()))
                .forEach(player -> output.append("- ").append(player.getName()).append(" (").append(player.getId()).append(")").append(System.lineSeparator()));
        return output.toString();
    }

    public String equipmentStatistics(int limit) {
        StringBuilder output = new StringBuilder();
        output.append("Equipment ranking formula: usageCount * 1.5 + averageRating * 10 + compatibleHeroCount * 2 + winContribution * 20").append(System.lineSeparator());
        int rank = 1;
        for (Equipment item : rankingService.topEquipment(limit)) {
            output.append(rank++).append(". ")
                    .append(item.getName())
                    .append(" [").append(item.getType()).append("] score ")
                    .append(String.format("%.2f", rankingService.equipmentScore(item)))
                    .append(", usage ").append(item.getUsageCount())
                    .append(", rating ").append(String.format("%.1f", item.getAverageRating()))
                    .append(", compatible heroes ").append(dataManager.countCompatibleHeroes(item.getId()))
                    .append(System.lineSeparator());
        }
        return output.toString();
    }

    public String playerMatchHistory(String playerId, int limit) {
        Player player = dataManager.requirePlayer(playerId);
        List<MatchRecord> matches = dataManager.getMatchesNewestFirst().stream()
                .filter(record -> record.includesPlayer(playerId))
                .limit(Math.max(0, limit))
                .toList();
        return matchHistoryReport("Player " + player.getName(), player.getTeamId(), playerId, matches);
    }

    public String teamMatchHistory(String teamId, int limit) {
        Team team = dataManager.requireTeam(teamId);
        List<MatchRecord> matches = dataManager.getMatchesNewestFirst().stream()
                .filter(record -> record.includesTeam(teamId))
                .limit(Math.max(0, limit))
                .toList();
        return matchHistoryReport("Team " + team.getName(), teamId, null, matches);
    }

    public String leaderboard(String mode, int limit) {
        StringBuilder output = new StringBuilder();
        output.append("Leaderboard by ").append(mode).append(" (ties: name, then ID)").append(System.lineSeparator());
        int rank = 1;
        for (Player player : rankingService.topPlayers(mode, limit)) {
            output.append(rank++).append(". ").append(player.getName())
                    .append(" [").append(player.getId()).append("]")
                    .append(" level=").append(player.getLevel())
                    .append(" winRate=").append(formatPercent(player.getWinRate()))
                    .append(" matches=").append(player.getTotalMatches())
                    .append(" score=").append(String.format("%.2f", rankingService.playerCustomScore(player)))
                    .append(System.lineSeparator());
        }
        return output.toString();
    }

    private String matchHistoryReport(String title, String teamId, String playerId, List<MatchRecord> matches) {
        StringBuilder output = new StringBuilder();
        output.append(title).append(" match history").append(System.lineSeparator());
        Map<String, Integer> pickCounts = new LinkedHashMap<>();
        int wins = 0;
        int losses = 0;
        for (MatchRecord record : matches) {
            String opponentId = record.opponentForTeam(teamId);
            Team opponent = opponentId.isEmpty() ? null : dataManager.requireTeam(opponentId);
            boolean win = record.getWinnerTeamId().equals(teamId);
            if (win) {
                wins++;
            } else {
                losses++;
            }
            output.append("- ").append(record.getDate())
                    .append(" vs ").append(opponent == null ? "Unknown" : opponent.getName())
                    .append(" result ").append(win ? "WIN" : "LOSS")
                    .append(" picks ");
            if (playerId == null) {
                output.append(heroPickText(record.getHeroPicks()));
                for (String heroId : record.getHeroPicks().values()) {
                    pickCounts.merge(heroId, 1, Integer::sum);
                }
            } else {
                String heroId = record.getHeroPicks().get(playerId);
                output.append(heroName(heroId));
                pickCounts.merge(heroId, 1, Integer::sum);
            }
            output.append(System.lineSeparator());
        }
        output.append("Win/loss record in listed matches: ").append(wins).append("W-").append(losses).append("L").append(System.lineSeparator());
        output.append("Hero pick rate:").append(System.lineSeparator());
        int totalPicks = pickCounts.values().stream().mapToInt(Integer::intValue).sum();
        for (Map.Entry<String, Integer> entry : pickCounts.entrySet()) {
            double rate = totalPicks == 0 ? 0.0 : entry.getValue() * 100.0 / totalPicks;
            output.append("- ").append(heroName(entry.getKey())).append(": ").append(formatPercent(rate)).append(System.lineSeparator());
        }
        return output.toString();
    }

    private String heroPickText(Map<String, String> picks) {
        StringBuilder output = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> entry : picks.entrySet()) {
            if (!first) {
                output.append("; ");
            }
            first = false;
            Player player = dataManager.requirePlayer(entry.getKey());
            output.append(player.getName()).append("->").append(heroName(entry.getValue()));
        }
        return output.toString();
    }

    private String heroName(String heroId) {
        if (heroId == null || heroId.isBlank()) {
            return "Unknown";
        }
        return dataManager.requireHero(heroId).getName();
    }

    private String namesForEquipment(List<String> equipmentIds) {
        if (equipmentIds.isEmpty()) {
            return "None";
        }
        return equipmentIds.stream()
                .map(id -> dataManager.requireEquipment(id).getName())
                .toList()
                .toString();
    }

    public static String formatPercent(double value) {
        return String.format("%.1f%%", value);
    }
}
