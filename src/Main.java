import enums.EquipmentType;
import enums.HeroType;
import enums.Role;
import model.Equipment;
import model.Hero;
import model.MatchRecord;
import model.Person;
import model.Player;
import model.Team;
import service.AuthenticationService;
import service.CombatSimulationService;
import service.FileStorageService;
import service.GameDataManager;
import service.RankingService;
import service.RecommendationService;
import service.SearchService;
import util.DataInitializer;
import util.InputHelper;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;

public class Main {
    private static final Path DATA_DIR = Path.of("data");

    private final InputHelper input;
    private final FileStorageService storageService = new FileStorageService();
    private GameDataManager dataManager;
    private AuthenticationService authenticationService;
    private RankingService rankingService;
    private RecommendationService recommendationService;
    private CombatSimulationService combatSimulationService;
    private SearchService searchService;
    private Person currentUser;

    public Main() {
        this.input = new InputHelper(new Scanner(System.in));
        loadOrCreateData();
        wireServices();
    }

    public static void main(String[] args) {
        new Main().run();
    }

    private void run() {
        boolean exit = false;
        while (!exit) {
            if (currentUser == null) {
                exit = loginMenu();
            } else if (currentUser.getRole() == Role.ADMIN) {
                exit = adminMenu();
            } else {
                exit = playerMenu();
            }
        }
        saveData();
        System.out.println("Goodbye.");
    }

    private void loadOrCreateData() {
        try {
            if (storageService.hasDataFiles(DATA_DIR)) {
                dataManager = storageService.loadAll(DATA_DIR);
                System.out.println("Loaded data from " + DATA_DIR);
            } else {
                dataManager = DataInitializer.createSampleData();
                storageService.saveAll(dataManager, DATA_DIR);
                System.out.println("Created sample data in " + DATA_DIR);
            }
        } catch (IOException | RuntimeException ex) {
            System.out.println("Could not load CSV data: " + ex.getMessage());
            System.out.println("Using built-in sample data for this session.");
            dataManager = DataInitializer.createSampleData();
        }
    }

    private void wireServices() {
        authenticationService = new AuthenticationService(dataManager);
        rankingService = new RankingService(dataManager);
        recommendationService = new RecommendationService(dataManager, rankingService);
        combatSimulationService = new CombatSimulationService(dataManager, rankingService);
        searchService = new SearchService(dataManager, rankingService);
    }

    private boolean loginMenu() {
        System.out.println();
        System.out.println("=== Honor of Kings IMS ===");
        System.out.println("1. Login");
        System.out.println("0. Exit");
        int choice = input.readInt("Choice: ", 0, 1);
        if (choice == 0) {
            return true;
        }

        String username = input.readRequired("Username: ");
        String password = input.readRequired("Password: ");
        Optional<Person> login = authenticationService.login(username, password);
        if (login.isPresent()) {
            currentUser = login.get();
            System.out.println("Logged in as " + currentUser.getName() + " (" + currentUser.getRole() + ")");
        } else {
            System.out.println("Invalid username or password.");
        }
        return false;
    }

    private boolean adminMenu() {
        System.out.println();
        System.out.println("=== Admin Menu ===");
        printCommonOptions();
        System.out.println("9. Data management");
        System.out.println("10. Save data");
        System.out.println("11. Logout");
        System.out.println("0. Exit");
        int choice = input.readInt("Choice: ", 0, 11);
        return handleAdminChoice(choice);
    }

    private void printCommonOptions() {
        System.out.println("1. Player lookup");
        System.out.println("2. Team overview");
        System.out.println("3. Hero details");
        System.out.println("4. Equipment statistics");
        System.out.println("5. Match history");
        System.out.println("6. Leaderboard");
        System.out.println("7. Recommendation engine");
        System.out.println("8. Combat simulation");
    }

    private boolean handleAdminChoice(int choice) {
        try {
            switch (choice) {
                case 1 -> playerLookup();
                case 2 -> teamOverview();
                case 3 -> heroDetails();
                case 4 -> equipmentStatistics();
                case 5 -> matchHistory();
                case 6 -> leaderboard();
                case 7 -> recommendations();
                case 8 -> combatSimulation();
                case 9 -> dataManagementMenu();
                case 10 -> saveData();
                case 11 -> logout();
                case 0 -> {
                    return true;
                }
                default -> System.out.println("Unknown option.");
            }
        } catch (RuntimeException ex) {
            System.out.println("Error: " + ex.getMessage());
        }
        return false;
    }

    private boolean playerMenu() {
        Player player = (Player) currentUser;
        System.out.println();
        System.out.println("=== Player Menu ===");
        System.out.println("1. View my information");
        System.out.println("2. Edit my basic information");
        System.out.println("3. Player lookup");
        System.out.println("4. Team overview");
        System.out.println("5. Hero details");
        System.out.println("6. Equipment statistics");
        System.out.println("7. My match history");
        System.out.println("8. Leaderboard");
        System.out.println("9. Recommendation engine");
        System.out.println("10. Combat simulation");
        System.out.println("11. Logout");
        System.out.println("0. Exit");
        int choice = input.readInt("Choice: ", 0, 11);
        try {
            switch (choice) {
                case 1 -> System.out.println(searchService.playerReport(player));
                case 2 -> editOwnProfile(player);
                case 3 -> playerLookup();
                case 4 -> teamOverview();
                case 5 -> heroDetails();
                case 6 -> equipmentStatistics();
                case 7 -> {
                    int limit = input.readIntMin("How many matches? ", 1);
                    System.out.println(searchService.playerMatchHistory(player.getId(), limit));
                }
                case 8 -> leaderboard();
                case 9 -> recommendations();
                case 10 -> combatSimulation();
                case 11 -> logout();
                case 0 -> {
                    return true;
                }
                default -> System.out.println("Unknown option.");
            }
        } catch (RuntimeException ex) {
            System.out.println("Error: " + ex.getMessage());
        }
        return false;
    }

    private void playerLookup() {
        String query = input.readRequired("Player ID/name: ");
        System.out.println(searchService.playerLookup(query));
    }

    private void teamOverview() {
        String query = input.readRequired("Team ID/name: ");
        System.out.println(searchService.teamOverview(query));
    }

    private void heroDetails() {
        String query = input.readRequired("Hero name/ID: ");
        System.out.println(searchService.heroDetails(query));
    }

    private void equipmentStatistics() {
        int limit = input.readIntMin("Top how many equipment items? ", 1);
        System.out.println(searchService.equipmentStatistics(limit));
    }

    private void matchHistory() {
        System.out.println("1. Player match history");
        System.out.println("2. Team match history");
        int choice = input.readInt("Choice: ", 1, 2);
        int limit = input.readIntMin("How many matches? ", 1);
        if (choice == 1) {
            String playerId = input.readRequired("Player ID: ");
            System.out.println(searchService.playerMatchHistory(playerId, limit));
        } else {
            String teamId = input.readRequired("Team ID: ");
            System.out.println(searchService.teamMatchHistory(teamId, limit));
        }
    }

    private void leaderboard() {
        System.out.println("Modes: winrate, level, matches, score");
        String mode = input.readRequired("Mode: ");
        int limit = input.readIntMin("Top how many players? ", 1);
        System.out.println(searchService.leaderboard(mode, limit));
    }

    private void recommendations() {
        System.out.println("1. Recommend heroes for a player");
        System.out.println("2. Recommend equipment for a hero");
        int choice = input.readInt("Choice: ", 1, 2);
        int limit = input.readIntMin("How many recommendations? ", 1);
        if (choice == 1) {
            String playerQuery = input.readRequired("Player ID/name: ");
            System.out.println(recommendationService.heroRecommendationReport(playerQuery, limit));
        } else {
            String heroQuery = input.readRequired("Hero ID/name: ");
            System.out.println(recommendationService.equipmentRecommendationReport(heroQuery, limit));
        }
    }

    private void combatSimulation() {
        System.out.println("Leave equipment blank to auto-pick the strongest compatible equipment.");
        printPlayers();
        printHeroes();
        printEquipment();
        String playerAId = input.readRequired("Player A ID: ");
        String heroAId = input.readRequired("Hero A ID: ");
        String equipmentAId = input.readLine("Equipment A ID: ");
        String playerBId = input.readRequired("Player B ID: ");
        String heroBId = input.readRequired("Hero B ID: ");
        String equipmentBId = input.readLine("Equipment B ID: ");
        System.out.println(combatSimulationService.simulateDuel(
                playerAId, heroAId, equipmentAId,
                playerBId, heroBId, equipmentBId).format());
    }

    private void dataManagementMenu() {
        boolean back = false;
        while (!back) {
            System.out.println();
            System.out.println("=== Data Management ===");
            System.out.println("1. Add player");
            System.out.println("2. Edit player");
            System.out.println("3. Delete player");
            System.out.println("4. Add hero");
            System.out.println("5. Edit hero");
            System.out.println("6. Delete hero");
            System.out.println("7. Add equipment");
            System.out.println("8. Edit equipment");
            System.out.println("9. Delete equipment");
            System.out.println("10. Add team");
            System.out.println("11. Edit team");
            System.out.println("12. Delete team");
            System.out.println("13. Add match record");
            System.out.println("14. Edit match record");
            System.out.println("15. Delete match record");
            System.out.println("0. Back");
            int choice = input.readInt("Choice: ", 0, 15);
            try {
                switch (choice) {
                    case 1 -> addPlayer();
                    case 2 -> editPlayer();
                    case 3 -> deletePlayer();
                    case 4 -> addHero();
                    case 5 -> editHero();
                    case 6 -> deleteHero();
                    case 7 -> addEquipment();
                    case 8 -> editEquipment();
                    case 9 -> deleteEquipment();
                    case 10 -> addTeam();
                    case 11 -> editTeam();
                    case 12 -> deleteTeam();
                    case 13 -> addMatchRecord();
                    case 14 -> editMatchRecord();
                    case 15 -> deleteMatchRecord();
                    case 0 -> back = true;
                    default -> System.out.println("Unknown option.");
                }
            } catch (RuntimeException ex) {
                System.out.println("Error: " + ex.getMessage());
            }
        }
    }

    private void addPlayer() {
        printTeams();
        printHeroes();
        String playerId = input.readRequired("Player ID: ");
        String name = input.readRequired("Name: ");
        String username = input.readRequired("Username: ");
        String password = input.readRequired("Password: ");
        String teamId = input.readRequired("Team ID: ");
        int level = input.readIntMin("Level: ", 0);
        int wins = input.readIntMin("Wins: ", 0);
        int losses = input.readIntMin("Losses: ", 0);
        List<String> heroIds = readIdList("Hero IDs separated by comma/semicolon: ");
        Map<String, List<String>> equipmentLoadouts = readEquipmentLoadouts(heroIds, true);
        Player player = new Player(
                playerId,
                name,
                username,
                password,
                teamId,
                level,
                wins,
                losses,
                heroIds,
                equipmentLoadouts);
        dataManager.addPlayer(player);
        saveData();
        System.out.println("Player added.");
    }

    private void editPlayer() {
        Player player = findPlayerPrompt();
        String name = input.readOptional("Name", player.getName());
        String password = null;
        if (input.confirm("Change password?")) {
            password = input.readRequired("New password: ");
        }
        String teamId = player.getTeamId();
        if (input.confirm("Move player to another team?")) {
            printTeams();
            teamId = input.readRequired("New team ID: ");
        }
        int level = player.getLevel();
        int wins = player.getWins();
        int losses = player.getLosses();
        if (input.confirm("Change level/win/loss numbers?")) {
            level = input.readIntMin("Level: ", 0);
            wins = input.readIntMin("Wins: ", 0);
            losses = input.readIntMin("Losses: ", 0);
        }
        List<String> heroIds = List.copyOf(player.getHeroIds());
        Map<String, List<String>> equipmentLoadouts = copyLoadouts(player.getEquipmentLoadouts());
        if (input.confirm("Replace owned hero list?")) {
            printHeroes();
            heroIds = readIdList("Hero IDs separated by comma/semicolon: ");
            Map<String, List<String>> defaultLoadouts = dataManager.defaultEquipmentLoadouts(heroIds);
            Map<String, List<String>> retainedLoadouts = new LinkedHashMap<>();
            for (String heroId : heroIds) {
                retainedLoadouts.put(heroId, equipmentLoadouts.getOrDefault(heroId, defaultLoadouts.get(heroId)));
            }
            equipmentLoadouts = retainedLoadouts;
        }
        if (input.confirm("Replace equipped item loadouts?")) {
            equipmentLoadouts = readEquipmentLoadouts(heroIds, false);
        }
        dataManager.updatePlayer(
                player.getId(), name, password, teamId, level, wins, losses, heroIds, equipmentLoadouts);
        saveData();
        System.out.println("Player updated.");
    }

    private void deletePlayer() {
        String playerId = input.readRequired("Player ID to delete: ");
        if (input.confirm("Delete player " + playerId + "?")) {
            System.out.println(dataManager.deletePlayer(playerId) ? "Player deleted." : "Player not found.");
            saveData();
        }
    }

    private void addHero() {
        printEquipment();
        Hero hero = new Hero(
                input.readRequired("Hero ID: "),
                input.readRequired("Name: "),
                input.readEnum("Hero type", HeroType.class),
                input.readIntMin("Attack: ", 0),
                input.readIntMin("Defense: ", 0),
                input.readIntMin("Health: ", 0),
                input.readInt("Difficulty 1-10: ", 1, 10),
                readIdList("Compatible equipment IDs: "),
                readIdList("Recommended equipment IDs: "));
        dataManager.addHero(hero);
        saveData();
        System.out.println("Hero added.");
    }

    private void editHero() {
        Hero hero = findHeroPrompt();
        hero.setName(input.readOptional("Name", hero.getName()));
        if (input.confirm("Change type and stats?")) {
            hero.setType(input.readEnum("Hero type", HeroType.class));
            hero.setAttack(input.readIntMin("Attack: ", 0));
            hero.setDefense(input.readIntMin("Defense: ", 0));
            hero.setHealth(input.readIntMin("Health: ", 0));
            hero.setDifficulty(input.readInt("Difficulty 1-10: ", 1, 10));
        }
        if (input.confirm("Add compatible equipment?")) {
            printEquipment();
            for (String equipmentId : readIdList("Equipment IDs: ")) {
                dataManager.requireEquipment(equipmentId);
                hero.addCompatibleEquipment(equipmentId);
            }
        }
        if (input.confirm("Add recommended equipment?")) {
            printEquipment();
            for (String equipmentId : readIdList("Equipment IDs: ")) {
                dataManager.requireEquipment(equipmentId);
                hero.addRecommendedEquipment(equipmentId);
            }
        }
        if (input.confirm("Remove equipment from this hero?")) {
            String equipmentId = input.readRequired("Equipment ID: ");
            hero.removeEquipment(equipmentId);
        }
        saveData();
        System.out.println("Hero updated.");
    }

    private void deleteHero() {
        String heroId = input.readRequired("Hero ID to delete: ");
        if (input.confirm("Delete hero " + heroId + "? Related match records with this hero pick will be removed.")) {
            System.out.println(dataManager.deleteHero(heroId) ? "Hero deleted." : "Hero not found.");
            saveData();
        }
    }

    private void addEquipment() {
        Equipment item = new Equipment(
                input.readRequired("Equipment ID: "),
                input.readRequired("Name: "),
                input.readEnum("Equipment type", EquipmentType.class),
                input.readIntMin("Power: ", 0),
                input.readIntMin("Defense: ", 0),
                input.readIntMin("Price: ", 0),
                input.readDouble("Average rating 0-5: ", 0.0, 5.0),
                input.readIntMin("Usage count: ", 0),
                input.readDouble("Win contribution 0-1: ", 0.0, 1.0));
        dataManager.addEquipment(item);
        saveData();
        System.out.println("Equipment added.");
    }

    private void editEquipment() {
        Equipment item = findEquipmentPrompt();
        item.setName(input.readOptional("Name", item.getName()));
        if (input.confirm("Change type and stats?")) {
            item.setType(input.readEnum("Equipment type", EquipmentType.class));
            item.setPower(input.readIntMin("Power: ", 0));
            item.setDefense(input.readIntMin("Defense: ", 0));
            item.setPrice(input.readIntMin("Price: ", 0));
            item.setAverageRating(input.readDouble("Average rating 0-5: ", 0.0, 5.0));
            item.setUsageCount(input.readIntMin("Usage count: ", 0));
            item.setWinContribution(input.readDouble("Win contribution 0-1: ", 0.0, 1.0));
        }
        saveData();
        System.out.println("Equipment updated.");
    }

    private void deleteEquipment() {
        String equipmentId = input.readRequired("Equipment ID to delete: ");
        if (input.confirm("Delete equipment " + equipmentId + "?")) {
            System.out.println(dataManager.deleteEquipment(equipmentId) ? "Equipment deleted." : "Equipment not found.");
            saveData();
        }
    }

    private void addTeam() {
        Team team = new Team(input.readRequired("Team ID: "), input.readRequired("Team name: "), List.of());
        dataManager.addTeam(team);
        saveData();
        System.out.println("Team added. Add or move players to fill membership.");
    }

    private void editTeam() {
        Team team = findTeamPrompt();
        team.setName(input.readOptional("Name", team.getName()));
        saveData();
        System.out.println("Team updated.");
    }

    private void deleteTeam() {
        String teamId = input.readRequired("Team ID to delete: ");
        if (input.confirm("Delete team " + teamId + "?")) {
            System.out.println(dataManager.deleteTeam(teamId) ? "Team deleted." : "Team not found.");
            saveData();
        }
    }

    private void addMatchRecord() {
        printTeams();
        printPlayers();
        printHeroes();
        MatchRecord record = new MatchRecord(
                input.readRequired("Match ID: "),
                readDate("Date yyyy-mm-dd: "),
                input.readRequired("Team A ID: "),
                input.readRequired("Team B ID: "),
                input.readRequired("Winner team ID: "),
                readPicks());
        dataManager.addMatchRecord(record);
        saveData();
        System.out.println("Match record added.");
    }

    private void editMatchRecord() {
        MatchRecord record = dataManager.requireMatchRecord(input.readRequired("Match ID: "));
        LocalDate date = record.getDate();
        String teamAId = record.getTeamAId();
        String teamBId = record.getTeamBId();
        String winnerTeamId = record.getWinnerTeamId();
        Map<String, String> heroPicks = new LinkedHashMap<>(record.getHeroPicks());
        if (input.confirm("Change date/teams/winner?")) {
            date = readDate("Date yyyy-mm-dd: ");
            teamAId = input.readRequired("Team A ID: ");
            teamBId = input.readRequired("Team B ID: ");
            winnerTeamId = input.readRequired("Winner team ID: ");
        }
        if (input.confirm("Replace hero picks?")) {
            heroPicks = readPicks();
        }
        dataManager.updateMatchRecord(record.getId(), date, teamAId, teamBId, winnerTeamId, heroPicks);
        saveData();
        System.out.println("Match record updated.");
    }

    private void deleteMatchRecord() {
        String matchId = input.readRequired("Match ID to delete: ");
        if (input.confirm("Delete match record " + matchId + "?")) {
            System.out.println(dataManager.deleteMatchRecord(matchId) ? "Match record deleted." : "Match record not found.");
            saveData();
        }
    }

    private void editOwnProfile(Player player) {
        player.setName(input.readOptional("Name", player.getName()));
        if (input.confirm("Change password?")) {
            player.setPassword(input.readRequired("New password: "));
        }
        saveData();
        System.out.println("Profile updated. Team, level, win/loss record, and heroes require admin permission.");
    }

    private Player findPlayerPrompt() {
        String query = input.readRequired("Player ID/name: ");
        return dataManager.findPlayer(query).orElseThrow(() -> new IllegalArgumentException("Player not found: " + query));
    }

    private Hero findHeroPrompt() {
        String query = input.readRequired("Hero ID/name: ");
        return dataManager.findHero(query).orElseThrow(() -> new IllegalArgumentException("Hero not found: " + query));
    }

    private Equipment findEquipmentPrompt() {
        String query = input.readRequired("Equipment ID/name: ");
        return dataManager.findEquipment(query).orElseThrow(() -> new IllegalArgumentException("Equipment not found: " + query));
    }

    private Team findTeamPrompt() {
        String query = input.readRequired("Team ID/name: ");
        return dataManager.findTeam(query).orElseThrow(() -> new IllegalArgumentException("Team not found: " + query));
    }

    private List<String> readIdList(String prompt) {
        String raw = input.readRequired(prompt);
        return Arrays.stream(raw.split("[,;]"))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .toList();
    }

    private Map<String, List<String>> readEquipmentLoadouts(List<String> heroIds, boolean defaultWhenBlank) {
        printEquipment();
        System.out.println("Enter loadouts as heroId:equipmentId,equipmentId entries separated by semicolons.");
        System.out.println("Example: H001:E002,E017;H007:E002,E018");
        String raw = input.readLine(defaultWhenBlank
                ? "Equipment loadouts (blank = recommended defaults): "
                : "Equipment loadouts: ");
        if (raw.isBlank()) {
            if (defaultWhenBlank) {
                return dataManager.defaultEquipmentLoadouts(heroIds);
            }
            throw new IllegalArgumentException("Equipment loadouts cannot be blank");
        }
        return parseEquipmentLoadouts(raw);
    }

    private Map<String, List<String>> parseEquipmentLoadouts(String raw) {
        Map<String, List<String>> loadouts = new LinkedHashMap<>();
        for (String entry : raw.split(";")) {
            String[] parts = entry.trim().split(":", -1);
            if (parts.length != 2 || parts[0].isBlank()) {
                throw new IllegalArgumentException("Invalid equipment loadout entry: " + entry);
            }
            if (loadouts.containsKey(parts[0].trim())) {
                throw new IllegalArgumentException("Duplicate hero loadout: " + parts[0].trim());
            }
            List<String> equipmentIds = parts[1].isBlank()
                    ? List.of()
                    : Arrays.stream(parts[1].split(","))
                    .map(String::trim)
                    .filter(value -> !value.isEmpty())
                    .toList();
            loadouts.put(parts[0].trim(), equipmentIds);
        }
        return loadouts;
    }

    private Map<String, List<String>> copyLoadouts(Map<String, List<String>> source) {
        Map<String, List<String>> copy = new LinkedHashMap<>();
        for (Map.Entry<String, List<String>> entry : source.entrySet()) {
            copy.put(entry.getKey(), List.copyOf(entry.getValue()));
        }
        return copy;
    }

    private Map<String, String> readPicks() {
        System.out.println("Enter picks as playerId:heroId pairs separated by semicolons.");
        System.out.println("Example: P001:H001;P002:H002");
        String raw = input.readRequired("Picks: ");
        Map<String, String> picks = new LinkedHashMap<>();
        for (String entry : raw.split(";")) {
            String[] parts = entry.trim().split(":", -1);
            if (parts.length != 2) {
                throw new IllegalArgumentException("Invalid pick entry: " + entry);
            }
            dataManager.requirePlayer(parts[0]);
            dataManager.requireHero(parts[1]);
            picks.put(parts[0], parts[1]);
        }
        return picks;
    }

    private LocalDate readDate(String prompt) {
        while (true) {
            try {
                return LocalDate.parse(input.readRequired(prompt));
            } catch (DateTimeParseException ex) {
                System.out.println("Use yyyy-mm-dd format, for example 2026-05-31.");
            }
        }
    }

    private void printTeams() {
        System.out.println("Teams:");
        for (Team team : dataManager.getTeams()) {
            System.out.println("- " + team.getId() + " " + team.getName());
        }
    }

    private void printPlayers() {
        System.out.println("Players:");
        for (Player player : dataManager.getPlayers()) {
            System.out.println("- " + player.getId() + " " + player.getName());
        }
    }

    private void printHeroes() {
        System.out.println("Heroes:");
        for (Hero hero : dataManager.getHeroes()) {
            System.out.println("- " + hero.getId() + " " + hero.getName());
        }
    }

    private void printEquipment() {
        System.out.println("Equipment:");
        for (Equipment item : dataManager.getEquipment()) {
            System.out.println("- " + item.getId() + " " + item.getName());
        }
    }

    private void saveData() {
        try {
            storageService.saveAll(dataManager, DATA_DIR);
        } catch (IOException ex) {
            System.out.println("Could not save data: " + ex.getMessage());
        }
    }

    private void logout() {
        System.out.println("Logged out " + currentUser.getName() + ".");
        currentUser = null;
    }
}
