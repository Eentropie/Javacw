package service;

import model.Admin;
import model.Equipment;
import model.Hero;
import model.MatchRecord;
import model.Player;
import model.Team;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class GameDataManager {
    private final Map<String, Admin> admins = new LinkedHashMap<>();
    private final Map<String, Player> players = new LinkedHashMap<>();
    private final Map<String, Hero> heroes = new LinkedHashMap<>();
    private final Map<String, Equipment> equipment = new LinkedHashMap<>();
    private final Map<String, Team> teams = new LinkedHashMap<>();
    private final Map<String, MatchRecord> matchRecords = new LinkedHashMap<>();

    public void clear() {
        admins.clear();
        players.clear();
        heroes.clear();
        equipment.clear();
        teams.clear();
        matchRecords.clear();
    }

    public void addAdmin(Admin admin) {
        requireUniqueUser(admin.getId(), admin.getUsername());
        admins.put(admin.getId(), admin);
    }

    public void addPlayer(Player player) {
        requireUniqueUser(player.getId(), player.getUsername());
        validatePlayerReferences(player);
        players.put(player.getId(), player);
        teams.get(player.getTeamId()).addPlayer(player.getId());
    }

    public void addHero(Hero hero) {
        requireUnique(heroes, hero.getId(), "hero");
        for (String equipmentId : hero.getCompatibleEquipmentIds()) {
            requireEquipment(equipmentId);
        }
        for (String equipmentId : hero.getRecommendedEquipmentIds()) {
            requireEquipment(equipmentId);
        }
        heroes.put(hero.getId(), hero);
    }

    public void addEquipment(Equipment item) {
        requireUnique(equipment, item.getId(), "equipment");
        equipment.put(item.getId(), item);
    }

    public void addTeam(Team team) {
        requireUnique(teams, team.getId(), "team");
        teams.put(team.getId(), team);
    }

    public void addMatchRecord(MatchRecord record) {
        requireUnique(matchRecords, record.getId(), "match record");
        Map<String, String> participantTeamIds = validateMatchRecord(
                record.getTeamAId(),
                record.getTeamBId(),
                record.getWinnerTeamId(),
                record.getHeroPicks(),
                record.getParticipantTeamIds());
        record.replaceHeroPicks(new LinkedHashMap<>(record.getHeroPicks()), participantTeamIds);
        matchRecords.put(record.getId(), record);
    }

    public boolean deletePlayer(String playerId) {
        Player removed = players.remove(playerId);
        if (removed == null) {
            return false;
        }
        Team team = teams.get(removed.getTeamId());
        if (team != null) {
            team.removePlayer(playerId);
        }
        for (MatchRecord record : matchRecords.values()) {
            // Match history remains, but deleted player hero picks are no longer shown.
            record.removeHeroPick(playerId);
        }
        return true;
    }

    public boolean deleteHero(String heroId) {
        Hero removed = heroes.remove(heroId);
        if (removed == null) {
            return false;
        }
        for (Player player : players.values()) {
            player.removeHero(heroId);
        }
        List<String> affectedMatches = new ArrayList<>();
        for (MatchRecord record : matchRecords.values()) {
            if (record.getHeroPicks().containsValue(heroId)) {
                affectedMatches.add(record.getId());
            }
        }
        for (String matchId : affectedMatches) {
            matchRecords.remove(matchId);
        }
        return true;
    }

    public boolean deleteEquipment(String equipmentId) {
        Equipment removed = equipment.remove(equipmentId);
        if (removed == null) {
            return false;
        }
        for (Hero hero : heroes.values()) {
            hero.removeEquipment(equipmentId);
        }
        for (Player player : players.values()) {
            player.removeEquipmentFromLoadouts(equipmentId);
        }
        return true;
    }

    public boolean deleteTeam(String teamId) {
        Team team = teams.get(teamId);
        if (team == null) {
            return false;
        }
        if (!team.getPlayerIds().isEmpty()) {
            throw new IllegalStateException("Cannot delete a team while it still has players");
        }
        boolean hasMatches = matchRecords.values().stream().anyMatch(match -> match.includesTeam(teamId));
        if (hasMatches) {
            throw new IllegalStateException("Cannot delete a team while match records still reference it");
        }
        teams.remove(teamId);
        return true;
    }

    public boolean deleteMatchRecord(String matchId) {
        return matchRecords.remove(matchId) != null;
    }

    public MatchRecord requireMatchRecord(String matchId) {
        MatchRecord record = matchRecords.get(matchId);
        if (record == null) {
            throw new IllegalArgumentException("Unknown match record ID: " + matchId);
        }
        return record;
    }

    public void movePlayerToTeam(String playerId, String newTeamId) {
        Player player = requirePlayer(playerId);
        Team newTeam = requireTeam(newTeamId);
        Team oldTeam = teams.get(player.getTeamId());
        if (oldTeam != null) {
            oldTeam.removePlayer(playerId);
        }
        player.setTeamId(newTeamId);
        newTeam.addPlayer(playerId);
    }

    public void updateMatchRecord(String matchId, LocalDate date, String teamAId, String teamBId,
                                  String winnerTeamId, Map<String, String> heroPicks) {
        MatchRecord record = requireMatchRecord(matchId);
        Map<String, String> participantTeamIds = validateMatchRecord(
                teamAId, teamBId, winnerTeamId, heroPicks, Map.of());
        record.setDate(date);
        record.setTeamAId(teamAId);
        record.setTeamBId(teamBId);
        record.setWinnerTeamId(winnerTeamId);
        record.replaceHeroPicks(heroPicks, participantTeamIds);
    }

    public void updatePlayer(String playerId, String name, String password, String teamId,
                             int level, int wins, int losses, List<String> heroIds,
                             Map<String, List<String>> equipmentLoadouts) {
        Player player = requirePlayer(playerId);
        String effectivePassword = password == null || password.isBlank() ? player.getPassword() : password;
        Player proposed = new Player(
                player.getId(),
                name,
                player.getUsername(),
                effectivePassword,
                teamId,
                level,
                wins,
                losses,
                heroIds,
                equipmentLoadouts);
        validatePlayerReferences(proposed);

        player.setName(proposed.getName());
        player.setPassword(proposed.getPassword());
        if (!player.getTeamId().equals(proposed.getTeamId())) {
            movePlayerToTeam(playerId, proposed.getTeamId());
        }
        player.setLevel(proposed.getLevel());
        player.setWins(proposed.getWins());
        player.setLosses(proposed.getLosses());
        player.replaceHeroes(proposed.getHeroIds());
        player.replaceEquipmentLoadouts(proposed.getEquipmentLoadouts());
    }

    public Map<String, List<String>> defaultEquipmentLoadouts(List<String> heroIds) {
        Map<String, List<String>> loadouts = new LinkedHashMap<>();
        for (String heroId : heroIds) {
            Hero hero = requireHero(heroId);
            List<String> equipmentIds = hero.getRecommendedEquipmentIds().isEmpty()
                    ? hero.getCompatibleEquipmentIds().stream().limit(2).toList()
                    : List.copyOf(hero.getRecommendedEquipmentIds());
            loadouts.put(heroId, equipmentIds);
        }
        return loadouts;
    }

    public Optional<Player> findPlayer(String query) {
        return players.values().stream().filter(player -> player.matches(query)).findFirst();
    }

    public Optional<Team> findTeam(String query) {
        return teams.values().stream().filter(team -> team.matches(query)).findFirst();
    }

    public Optional<Hero> findHero(String query) {
        return heroes.values().stream().filter(hero -> hero.matches(query)).findFirst();
    }

    public Optional<Equipment> findEquipment(String query) {
        return equipment.values().stream().filter(item -> item.matches(query)).findFirst();
    }

    public Player requirePlayer(String playerId) {
        Player player = players.get(playerId);
        if (player == null) {
            throw new IllegalArgumentException("Unknown player ID: " + playerId);
        }
        return player;
    }

    public Hero requireHero(String heroId) {
        Hero hero = heroes.get(heroId);
        if (hero == null) {
            throw new IllegalArgumentException("Unknown hero ID: " + heroId);
        }
        return hero;
    }

    public Equipment requireEquipment(String equipmentId) {
        Equipment item = equipment.get(equipmentId);
        if (item == null) {
            throw new IllegalArgumentException("Unknown equipment ID: " + equipmentId);
        }
        return item;
    }

    public Team requireTeam(String teamId) {
        Team team = teams.get(teamId);
        if (team == null) {
            throw new IllegalArgumentException("Unknown team ID: " + teamId);
        }
        return team;
    }

    public Collection<Admin> getAdmins() {
        return admins.values();
    }

    public Collection<Player> getPlayers() {
        return players.values();
    }

    public Collection<Hero> getHeroes() {
        return heroes.values();
    }

    public Collection<Equipment> getEquipment() {
        return equipment.values();
    }

    public Collection<Team> getTeams() {
        return teams.values();
    }

    public Collection<MatchRecord> getMatchRecords() {
        return matchRecords.values();
    }

    public Team teamForPlayer(Player player) {
        return requireTeam(player.getTeamId());
    }

    public List<Player> playersForTeam(Team team) {
        return team.getPlayerIds().stream()
                .map(this::requirePlayer)
                .toList();
    }

    public List<Hero> heroesForPlayer(Player player) {
        return player.getHeroIds().stream()
                .map(this::requireHero)
                .toList();
    }

    public List<Equipment> compatibleEquipmentForHero(Hero hero) {
        return hero.getCompatibleEquipmentIds().stream()
                .map(this::requireEquipment)
                .toList();
    }

    public List<Equipment> recommendedEquipmentForHero(Hero hero) {
        return hero.getRecommendedEquipmentIds().stream()
                .map(this::requireEquipment)
                .toList();
    }

    public List<Player> playersOwningHero(Hero hero) {
        return players.values().stream()
                .filter(player -> player.ownsHero(hero.getId()))
                .toList();
    }

    public List<MatchRecord> matchesForTeam(String teamId) {
        requireTeam(teamId);
        return getMatchesNewestFirst().stream()
                .filter(record -> record.includesTeam(teamId))
                .toList();
    }

    public List<MatchRecord> matchesForPlayer(String playerId) {
        requirePlayer(playerId);
        return getMatchesNewestFirst().stream()
                .filter(record -> record.includesPlayer(playerId))
                .toList();
    }

    public List<MatchRecord> getMatchesNewestFirst() {
        return matchRecords.values().stream()
                .sorted(Comparator.comparing(MatchRecord::getDate).reversed())
                .toList();
    }

    public int countCompatibleHeroes(String equipmentId) {
        int count = 0;
        for (Hero hero : heroes.values()) {
            if (hero.getCompatibleEquipmentIds().contains(equipmentId)) {
                count++;
            }
        }
        return count;
    }

    public void rebuildTeamMembership() {
        for (Team team : teams.values()) {
            team.replacePlayers(List.of());
        }
        for (Player player : players.values()) {
            Team team = teams.get(player.getTeamId());
            if (team != null) {
                team.addPlayer(player.getId());
            }
        }
    }

    private void requireUniqueUser(String id, String username) {
        if (admins.containsKey(id) || players.containsKey(id)) {
            throw new IllegalArgumentException("Duplicate user ID: " + id);
        }
        boolean duplicateUsername = admins.values().stream().anyMatch(admin -> admin.getUsername().equals(username))
                || players.values().stream().anyMatch(player -> player.getUsername().equals(username));
        if (duplicateUsername) {
            throw new IllegalArgumentException("Duplicate username: " + username);
        }
    }

    private static void requireUnique(Map<String, ?> records, String id, String label) {
        if (records.containsKey(id)) {
            throw new IllegalArgumentException("Duplicate " + label + " ID: " + id);
        }
    }

    private void validatePlayerReferences(Player player) {
        requireTeam(player.getTeamId());
        for (String heroId : player.getHeroIds()) {
            requireHero(heroId);
        }
        for (Map.Entry<String, List<String>> entry : player.getEquipmentLoadouts().entrySet()) {
            if (!player.ownsHero(entry.getKey())) {
                throw new IllegalArgumentException(
                        "Equipment loadout references hero not owned by player: " + entry.getKey());
            }
            Hero hero = requireHero(entry.getKey());
            for (String equipmentId : entry.getValue()) {
                requireEquipment(equipmentId);
                if (!hero.getCompatibleEquipmentIds().contains(equipmentId)) {
                    throw new IllegalArgumentException(
                            "Equipment " + equipmentId + " is not compatible with hero " + hero.getId());
                }
            }
        }
    }

    private Map<String, String> validateMatchRecord(String teamAId, String teamBId, String winnerTeamId,
                                                    Map<String, String> heroPicks,
                                                    Map<String, String> suppliedParticipantTeamIds) {
        Team teamA = requireTeam(teamAId);
        Team teamB = requireTeam(teamBId);
        if (teamAId.equals(teamBId)) {
            throw new IllegalArgumentException("A match must use two different teams");
        }
        if (!winnerTeamId.equals(teamAId) && !winnerTeamId.equals(teamBId)) {
            throw new IllegalArgumentException("Winner team must be one of the two participating teams");
        }
        requireTeam(winnerTeamId);

        if (heroPicks == null) {
            throw new IllegalArgumentException("Hero picks cannot be null");
        }
        Map<String, String> participantTeamIds = suppliedParticipantTeamIds == null
                ? Map.of()
                : suppliedParticipantTeamIds;
        boolean historicalTeamsSupplied = !participantTeamIds.isEmpty();
        if (historicalTeamsSupplied && !participantTeamIds.keySet().equals(heroPicks.keySet())) {
            throw new IllegalArgumentException("Participant team mapping must match the hero pick players");
        }

        Set<String> pickedHeroIds = new HashSet<>();
        Map<String, String> normalizedParticipantTeamIds = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : heroPicks.entrySet()) {
            Player player = requirePlayer(entry.getKey());
            String heroId = entry.getValue();
            requireHero(heroId);
            String participantTeamId = historicalTeamsSupplied
                    ? participantTeamIds.get(player.getId())
                    : player.getTeamId();
            if (!teamAId.equals(participantTeamId) && !teamBId.equals(participantTeamId)) {
                throw new IllegalArgumentException("Player " + player.getId() + " is not in either participating team");
            }
            if (!historicalTeamsSupplied && !player.ownsHero(heroId)) {
                throw new IllegalArgumentException("Player " + player.getId() + " does not own hero " + heroId);
            }
            if (!pickedHeroIds.add(heroId)) {
                throw new IllegalArgumentException("Duplicate hero pick in one match: " + heroId);
            }
            normalizedParticipantTeamIds.put(player.getId(), participantTeamId);
        }
        return normalizedParticipantTeamIds;
    }
}
