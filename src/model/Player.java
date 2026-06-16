package model;

import enums.Role;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Player extends Person {
    private String teamId;
    private int level;
    private int wins;
    private int losses;
    private final List<String> heroIds;
    private final Map<String, List<String>> equipmentLoadouts;

    public Player(String id, String name, String username, String password,
                  String teamId, int level, int wins, int losses, List<String> heroIds) {
        this(id, name, username, password, teamId, level, wins, losses, heroIds, Map.of());
    }

    public Player(String id, String name, String username, String password,
                  String teamId, int level, int wins, int losses, List<String> heroIds,
                  Map<String, List<String>> equipmentLoadouts) {
        super(id, name, username, password, Role.PLAYER);
        this.teamId = requireText(teamId, "teamId");
        this.level = requireNonNegative(level, "level");
        this.wins = requireNonNegative(wins, "wins");
        this.losses = requireNonNegative(losses, "losses");
        this.heroIds = new ArrayList<>();
        this.equipmentLoadouts = new LinkedHashMap<>();
        if (heroIds != null) {
            for (String heroId : heroIds) {
                String value = requireText(heroId, "heroId");
                if (!this.heroIds.contains(value)) {
                    this.heroIds.add(value);
                }
            }
        }
        if (equipmentLoadouts != null) {
            for (Map.Entry<String, List<String>> entry : equipmentLoadouts.entrySet()) {
                String ownedHeroId = requireText(entry.getKey(), "heroId");
                if (!this.heroIds.contains(ownedHeroId)) {
                    throw new IllegalArgumentException("Player does not own hero " + ownedHeroId);
                }
                List<String> values = new ArrayList<>();
                if (entry.getValue() != null) {
                    for (String equipmentId : entry.getValue()) {
                        String value = requireText(equipmentId, "equipmentId");
                        if (!values.contains(value)) {
                            values.add(value);
                        }
                    }
                }
                this.equipmentLoadouts.put(ownedHeroId, values);
            }
        }
    }

    public String getTeamId() {
        return teamId;
    }

    public void setTeamId(String teamId) {
        this.teamId = requireText(teamId, "teamId");
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = requireNonNegative(level, "level");
    }

    public int getWins() {
        return wins;
    }

    public void setWins(int wins) {
        this.wins = requireNonNegative(wins, "wins");
    }

    public int getLosses() {
        return losses;
    }

    public void setLosses(int losses) {
        this.losses = requireNonNegative(losses, "losses");
    }

    public int getTotalMatches() {
        return wins + losses;
    }

    public double getWinRate() {
        int total = getTotalMatches();
        if (total == 0) {
            return 0.0;
        }
        return wins * 100.0 / total;
    }

    public List<String> getHeroIds() {
        return Collections.unmodifiableList(heroIds);
    }

    public void addHero(String heroId) {
        String value = requireText(heroId, "heroId");
        if (!heroIds.contains(value)) {
            heroIds.add(value);
        }
    }

    public boolean removeHero(String heroId) {
        equipmentLoadouts.remove(heroId);
        return heroIds.remove(heroId);
    }

    public boolean ownsHero(String heroId) {
        return heroIds.contains(heroId);
    }

    public void replaceHeroes(List<String> newHeroIds) {
        heroIds.clear();
        if (newHeroIds != null) {
            for (String heroId : newHeroIds) {
                addHero(heroId);
            }
        }
        equipmentLoadouts.keySet().removeIf(heroId -> !heroIds.contains(heroId));
    }

    public Map<String, List<String>> getEquipmentLoadouts() {
        Map<String, List<String>> copy = new LinkedHashMap<>();
        for (Map.Entry<String, List<String>> entry : equipmentLoadouts.entrySet()) {
            copy.put(entry.getKey(), List.copyOf(entry.getValue()));
        }
        return Collections.unmodifiableMap(copy);
    }

    public List<String> getEquippedEquipmentIds(String heroId) {
        List<String> equipmentIds = equipmentLoadouts.get(heroId);
        return equipmentIds == null ? List.of() : Collections.unmodifiableList(equipmentIds);
    }

    public void replaceEquipmentLoadout(String heroId, List<String> equipmentIds) {
        String ownedHeroId = requireText(heroId, "heroId");
        if (!ownsHero(ownedHeroId)) {
            throw new IllegalArgumentException("Player does not own hero " + ownedHeroId);
        }
        List<String> values = new ArrayList<>();
        if (equipmentIds != null) {
            for (String equipmentId : equipmentIds) {
                String value = requireText(equipmentId, "equipmentId");
                if (!values.contains(value)) {
                    values.add(value);
                }
            }
        }
        equipmentLoadouts.put(ownedHeroId, values);
    }

    public void replaceEquipmentLoadouts(Map<String, List<String>> newLoadouts) {
        equipmentLoadouts.clear();
        if (newLoadouts != null) {
            for (Map.Entry<String, List<String>> entry : newLoadouts.entrySet()) {
                replaceEquipmentLoadout(entry.getKey(), entry.getValue());
            }
        }
    }

    public void removeEquipmentFromLoadouts(String equipmentId) {
        for (List<String> equipmentIds : equipmentLoadouts.values()) {
            equipmentIds.remove(equipmentId);
        }
    }
}
