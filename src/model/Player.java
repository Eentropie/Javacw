package model;

import enums.Role;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Player extends Person {
    private String teamId;
    private int level;
    private int wins;
    private int losses;
    private final List<String> heroIds;

    public Player(String id, String name, String username, String password,
                  String teamId, int level, int wins, int losses, List<String> heroIds) {
        super(id, name, username, password, Role.PLAYER);
        this.teamId = requireText(teamId, "teamId");
        this.level = requireNonNegative(level, "level");
        this.wins = requireNonNegative(wins, "wins");
        this.losses = requireNonNegative(losses, "losses");
        this.heroIds = new ArrayList<>();
        if (heroIds != null) {
            for (String heroId : heroIds) {
                addHero(heroId);
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
    }
}
