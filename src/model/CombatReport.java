package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CombatReport {
    private final String title;
    private final String winnerName;
    private final String loserName;
    private final int turns;
    private final int winnerRemainingHealth;
    private final int loserRemainingHealth;
    private final List<String> turnLog;

    public CombatReport(String title, String winnerName, String loserName, int turns,
                        int winnerRemainingHealth, int loserRemainingHealth, List<String> turnLog) {
        this.title = title;
        this.winnerName = winnerName;
        this.loserName = loserName;
        this.turns = turns;
        this.winnerRemainingHealth = Math.max(0, winnerRemainingHealth);
        this.loserRemainingHealth = Math.max(0, loserRemainingHealth);
        this.turnLog = new ArrayList<>(turnLog);
    }

    public String getWinnerName() {
        return winnerName;
    }

    public String getLoserName() {
        return loserName;
    }

    public int getTurns() {
        return turns;
    }

    public List<String> getTurnLog() {
        return Collections.unmodifiableList(turnLog);
    }

    public String format() {
        StringBuilder output = new StringBuilder();
        output.append(title).append(System.lineSeparator());
        output.append("Damage formula: max(20, attack - defense / 2); critical hit x1.5; dodge cancels damage.")
                .append(System.lineSeparator());
        for (String line : turnLog) {
            output.append(line).append(System.lineSeparator());
        }
        output.append("Winner: ").append(winnerName)
                .append(" with ").append(winnerRemainingHealth).append(" HP remaining")
                .append(System.lineSeparator());
        output.append("Loser: ").append(loserName)
                .append(" with ").append(loserRemainingHealth).append(" HP remaining")
                .append(System.lineSeparator());
        output.append("Total turns: ").append(turns).append(System.lineSeparator());
        return output.toString();
    }
}
