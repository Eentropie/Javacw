package model;

import contract.Searchable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Team implements Searchable {
    private final String id;
    private String name;
    private final List<String> playerIds;

    public Team(String id, String name, List<String> playerIds) {
        this.id = Person.requireText(id, "id");
        this.name = Person.requireText(name, "name");
        this.playerIds = new ArrayList<>();
        if (playerIds != null) {
            playerIds.forEach(this::addPlayer);
        }
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = Person.requireText(name, "name");
    }

    public List<String> getPlayerIds() {
        return Collections.unmodifiableList(playerIds);
    }

    public void addPlayer(String playerId) {
        String value = Person.requireText(playerId, "playerId");
        if (!playerIds.contains(value)) {
            playerIds.add(value);
        }
    }

    public boolean removePlayer(String playerId) {
        return playerIds.remove(playerId);
    }

    public void replacePlayers(List<String> newPlayerIds) {
        playerIds.clear();
        if (newPlayerIds != null) {
            newPlayerIds.forEach(this::addPlayer);
        }
    }

    @Override
    public boolean matches(String query) {
        if (query == null) {
            return false;
        }
        String normalized = query.trim().toLowerCase();
        return id.toLowerCase().contains(normalized)
                || name.toLowerCase().contains(normalized);
    }

    @Override
    public String toString() {
        return id + " - " + name;
    }
}
