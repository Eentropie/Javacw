package service;

import enums.EquipmentType;
import enums.HeroType;
import model.Admin;
import model.Equipment;
import model.Hero;
import model.MatchRecord;
import model.Player;
import model.Team;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class FileStorageService {
    private static final String ADMINS_FILE = "admins.csv";
    private static final String TEAMS_FILE = "teams.csv";
    private static final String EQUIPMENT_FILE = "equipment.csv";
    private static final String HEROES_FILE = "heroes.csv";
    private static final String PLAYERS_FILE = "players.csv";
    private static final String MATCHES_FILE = "matches.csv";

    public boolean hasDataFiles(Path directory) {
        return Files.exists(directory.resolve(ADMINS_FILE))
                && Files.exists(directory.resolve(TEAMS_FILE))
                && Files.exists(directory.resolve(EQUIPMENT_FILE))
                && Files.exists(directory.resolve(HEROES_FILE))
                && Files.exists(directory.resolve(PLAYERS_FILE))
                && Files.exists(directory.resolve(MATCHES_FILE));
    }

    public void saveAll(GameDataManager data, Path directory) throws IOException {
        Files.createDirectories(directory);
        writeLinesAtomically(directory.resolve(ADMINS_FILE), adminLines(data));
        writeLinesAtomically(directory.resolve(TEAMS_FILE), teamLines(data));
        writeLinesAtomically(directory.resolve(EQUIPMENT_FILE), equipmentLines(data));
        writeLinesAtomically(directory.resolve(HEROES_FILE), heroLines(data));
        writeLinesAtomically(directory.resolve(PLAYERS_FILE), playerLines(data));
        writeLinesAtomically(directory.resolve(MATCHES_FILE), matchLines(data));
    }

    public GameDataManager loadAll(Path directory) throws IOException {
        if (!hasDataFiles(directory)) {
            throw new IOException("One or more CSV data files are missing in " + directory);
        }

        GameDataManager data = new GameDataManager();
        for (String line : readDataLines(directory.resolve(ADMINS_FILE))) {
            String[] parts = split(line, 4, ADMINS_FILE);
            data.addAdmin(new Admin(parts[0], parts[1], parts[2], parts[3]));
        }
        for (String line : readDataLines(directory.resolve(TEAMS_FILE))) {
            String[] parts = split(line, 2, TEAMS_FILE);
            data.addTeam(new Team(parts[0], parts[1], List.of()));
        }
        for (String line : readDataLines(directory.resolve(EQUIPMENT_FILE))) {
            String[] parts = split(line, 9, EQUIPMENT_FILE);
            data.addEquipment(new Equipment(
                    parts[0],
                    parts[1],
                    EquipmentType.valueOf(parts[2]),
                    Integer.parseInt(parts[3]),
                    Integer.parseInt(parts[4]),
                    Integer.parseInt(parts[5]),
                    Double.parseDouble(parts[6]),
                    Integer.parseInt(parts[7]),
                    Double.parseDouble(parts[8])));
        }
        for (String line : readDataLines(directory.resolve(HEROES_FILE))) {
            String[] parts = split(line, 9, HEROES_FILE);
            data.addHero(new Hero(
                    parts[0],
                    parts[1],
                    HeroType.valueOf(parts[2]),
                    Integer.parseInt(parts[3]),
                    Integer.parseInt(parts[4]),
                    Integer.parseInt(parts[5]),
                    Integer.parseInt(parts[6]),
                    parseList(parts[7]),
                    parseList(parts[8])));
        }
        for (String line : readDataLines(directory.resolve(PLAYERS_FILE))) {
            String[] parts = split(line, 9, PLAYERS_FILE);
            data.addPlayer(new Player(
                    parts[0],
                    parts[1],
                    parts[2],
                    parts[3],
                    parts[4],
                    Integer.parseInt(parts[5]),
                    Integer.parseInt(parts[6]),
                    Integer.parseInt(parts[7]),
                    parseList(parts[8])));
        }
        for (String line : readDataLines(directory.resolve(MATCHES_FILE))) {
            String[] parts = split(line, 6, MATCHES_FILE);
            data.addMatchRecord(new MatchRecord(
                    parts[0],
                    LocalDate.parse(parts[1]),
                    parts[2],
                    parts[3],
                    parts[4],
                    parsePicks(parts[5])));
        }
        data.rebuildTeamMembership();
        return data;
    }

    private List<String> adminLines(GameDataManager data) {
        List<String> lines = new ArrayList<>();
        lines.add("# id|name|username|password");
        for (Admin admin : data.getAdmins()) {
            lines.add(join(admin.getId(), admin.getName(), admin.getUsername(), admin.getPassword()));
        }
        return lines;
    }

    private List<String> teamLines(GameDataManager data) {
        List<String> lines = new ArrayList<>();
        lines.add("# id|name");
        for (Team team : data.getTeams()) {
            lines.add(join(team.getId(), team.getName()));
        }
        return lines;
    }

    private List<String> equipmentLines(GameDataManager data) {
        List<String> lines = new ArrayList<>();
        lines.add("# id|name|type|power|defense|price|averageRating|usageCount|winContribution");
        for (Equipment item : data.getEquipment()) {
            lines.add(join(
                    item.getId(),
                    item.getName(),
                    item.getType().name(),
                    String.valueOf(item.getPower()),
                    String.valueOf(item.getDefense()),
                    String.valueOf(item.getPrice()),
                    String.valueOf(item.getAverageRating()),
                    String.valueOf(item.getUsageCount()),
                    String.valueOf(item.getWinContribution())));
        }
        return lines;
    }

    private List<String> heroLines(GameDataManager data) {
        List<String> lines = new ArrayList<>();
        lines.add("# id|name|type|attack|defense|health|difficulty|compatibleEquipmentIds|recommendedEquipmentIds");
        for (Hero hero : data.getHeroes()) {
            lines.add(join(
                    hero.getId(),
                    hero.getName(),
                    hero.getType().name(),
                    String.valueOf(hero.getAttack()),
                    String.valueOf(hero.getDefense()),
                    String.valueOf(hero.getHealth()),
                    String.valueOf(hero.getDifficulty()),
                    joinList(hero.getCompatibleEquipmentIds()),
                    joinList(hero.getRecommendedEquipmentIds())));
        }
        return lines;
    }

    private List<String> playerLines(GameDataManager data) {
        List<String> lines = new ArrayList<>();
        lines.add("# id|name|username|password|teamId|level|wins|losses|heroIds");
        for (Player player : data.getPlayers()) {
            lines.add(join(
                    player.getId(),
                    player.getName(),
                    player.getUsername(),
                    player.getPassword(),
                    player.getTeamId(),
                    String.valueOf(player.getLevel()),
                    String.valueOf(player.getWins()),
                    String.valueOf(player.getLosses()),
                    joinList(player.getHeroIds())));
        }
        return lines;
    }

    private List<String> matchLines(GameDataManager data) {
        List<String> lines = new ArrayList<>();
        lines.add("# id|date|teamAId|teamBId|winnerTeamId|playerHeroPicks");
        for (MatchRecord record : data.getMatchRecords()) {
            lines.add(join(
                    record.getId(),
                    record.getDate().toString(),
                    record.getTeamAId(),
                    record.getTeamBId(),
                    record.getWinnerTeamId(),
                    joinPicks(record.getHeroPicks())));
        }
        return lines;
    }

    private static void writeLinesAtomically(Path target, List<String> lines) throws IOException {
        Path tempFile = target.resolveSibling(target.getFileName() + ".tmp");
        Files.write(tempFile, lines, StandardCharsets.UTF_8);
        try {
            Files.move(tempFile, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException ex) {
            Files.move(tempFile, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static List<String> readDataLines(Path file) throws IOException {
        return Files.readAllLines(file, StandardCharsets.UTF_8).stream()
                .map(String::trim)
                .filter(line -> !line.isEmpty())
                .filter(line -> !line.startsWith("#"))
                .toList();
    }

    private static String[] split(String line, int expectedFields, String fileName) throws IOException {
        String[] parts = line.split("\\|", -1);
        if (parts.length != expectedFields) {
            throw new IOException("Invalid field count in " + fileName + ": " + line);
        }
        return parts;
    }

    private static String join(String... values) {
        List<String> safe = new ArrayList<>();
        for (String value : values) {
            safe.add(clean(value));
        }
        return String.join("|", safe);
    }

    private static String clean(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("|", " ")
                .replace(";", ",")
                .replace(":", "-")
                .trim();
    }

    private static String joinList(List<String> values) {
        return String.join(";", values);
    }

    private static List<String> parseList(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        return List.of(value.split(";"));
    }

    private static String joinPicks(Map<String, String> picks) {
        List<String> values = new ArrayList<>();
        for (Map.Entry<String, String> entry : picks.entrySet()) {
            values.add(entry.getKey() + ":" + entry.getValue());
        }
        return String.join(";", values);
    }

    private static Map<String, String> parsePicks(String value) throws IOException {
        Map<String, String> picks = new LinkedHashMap<>();
        if (value == null || value.isBlank()) {
            return picks;
        }
        String[] entries = value.split(";");
        for (String entry : entries) {
            String[] parts = entry.split(":", -1);
            if (parts.length != 2) {
                throw new IOException("Invalid hero pick entry: " + entry);
            }
            picks.put(parts[0], parts[1]);
        }
        return picks;
    }
}
