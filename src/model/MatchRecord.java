package model;

import contract.Searchable;
import enums.MatchResult;

import java.time.LocalDate;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class MatchRecord implements Searchable {
    private final String id;
    private LocalDate date;
    private String teamAId;
    private String teamBId;
    private String winnerTeamId;
    private final Map<String, String> heroPicks;
    private final Map<String, String> participantTeamIds;

    public MatchRecord(String id, LocalDate date, String teamAId, String teamBId,
                       String winnerTeamId, Map<String, String> heroPicks) {
        this(id, date, teamAId, teamBId, winnerTeamId, heroPicks, Map.of());
    }

    public MatchRecord(String id, LocalDate date, String teamAId, String teamBId,
                       String winnerTeamId, Map<String, String> heroPicks,
                       Map<String, String> participantTeamIds) {
        this.id = Person.requireText(id, "id");
        this.date = requireDate(date);
        this.teamAId = Person.requireText(teamAId, "teamAId");
        this.teamBId = Person.requireText(teamBId, "teamBId");
        this.winnerTeamId = Person.requireText(winnerTeamId, "winnerTeamId");
        this.heroPicks = new LinkedHashMap<>();
        this.participantTeamIds = new LinkedHashMap<>();
        if (heroPicks != null) {
            this.heroPicks.putAll(heroPicks);
        }
        if (participantTeamIds != null) {
            this.participantTeamIds.putAll(participantTeamIds);
        }
    }

    public String getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = requireDate(date);
    }

    public String getTeamAId() {
        return teamAId;
    }

    public void setTeamAId(String teamAId) {
        this.teamAId = Person.requireText(teamAId, "teamAId");
    }

    public String getTeamBId() {
        return teamBId;
    }

    public void setTeamBId(String teamBId) {
        this.teamBId = Person.requireText(teamBId, "teamBId");
    }

    public String getWinnerTeamId() {
        return winnerTeamId;
    }

    public void setWinnerTeamId(String winnerTeamId) {
        this.winnerTeamId = Person.requireText(winnerTeamId, "winnerTeamId");
    }

    public Map<String, String> getHeroPicks() {
        return Collections.unmodifiableMap(heroPicks);
    }

    public Map<String, String> getParticipantTeamIds() {
        return Collections.unmodifiableMap(participantTeamIds);
    }

    public void putHeroPick(String playerId, String heroId) {
        heroPicks.put(Person.requireText(playerId, "playerId"), Person.requireText(heroId, "heroId"));
    }

    public void putHeroPick(String playerId, String heroId, String teamId) {
        String participantId = Person.requireText(playerId, "playerId");
        heroPicks.put(participantId, Person.requireText(heroId, "heroId"));
        participantTeamIds.put(participantId, Person.requireText(teamId, "teamId"));
    }

    public boolean removeHeroPick(String playerId) {
        participantTeamIds.remove(playerId);
        return heroPicks.remove(playerId) != null;
    }

    public void clearHeroPicks() {
        heroPicks.clear();
        participantTeamIds.clear();
    }

    public void replaceHeroPicks(Map<String, String> newHeroPicks, Map<String, String> newParticipantTeamIds) {
        clearHeroPicks();
        for (Map.Entry<String, String> entry : newHeroPicks.entrySet()) {
            String teamId = newParticipantTeamIds.get(entry.getKey());
            putHeroPick(entry.getKey(), entry.getValue(), teamId);
        }
    }

    public boolean includesTeam(String teamId) {
        return teamAId.equals(teamId) || teamBId.equals(teamId);
    }

    public boolean includesPlayer(String playerId) {
        return heroPicks.containsKey(playerId);
    }

    public String teamForPlayer(String playerId) {
        return participantTeamIds.getOrDefault(playerId, "");
    }

    public String opponentForTeam(String teamId) {
        if (teamAId.equals(teamId)) {
            return teamBId;
        }
        if (teamBId.equals(teamId)) {
            return teamAId;
        }
        return "";
    }

    public MatchResult resultForTeam(String teamId) {
        return winnerTeamId.equals(teamId) ? MatchResult.WIN : MatchResult.LOSS;
    }

    @Override
    public boolean matches(String query) {
        if (query == null) {
            return false;
        }
        String normalized = query.trim().toLowerCase();
        return id.toLowerCase().contains(normalized)
                || teamAId.toLowerCase().contains(normalized)
                || teamBId.toLowerCase().contains(normalized)
                || winnerTeamId.toLowerCase().contains(normalized);
    }

    @Override
    public String toString() {
        return id + " " + date + " " + teamAId + " vs " + teamBId + " winner=" + winnerTeamId;
    }

    private static LocalDate requireDate(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("date cannot be null");
        }
        return date;
    }
}
