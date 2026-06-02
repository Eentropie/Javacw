package service;

import model.Admin;
import model.Equipment;
import model.Hero;
import model.MatchRecord;
import model.Player;
import model.Team;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
        if (!teams.containsKey(player.getTeamId())) {
            throw new IllegalArgumentException("Unknown team ID: " + player.getTeamId());
        }
        for (String heroId : player.getHeroIds()) {
            requireHero(heroId);
        }
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
        requireTeam(record.getTeamAId());
        requireTeam(record.getTeamBId());
        requireTeam(record.getWinnerTeamId());
        for (Map.Entry<String, String> entry : record.getHeroPicks().entrySet()) {
            requirePlayer(entry.getKey());
            requireHero(entry.getValue());
        }
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
}
