package gui;

import enums.EquipmentType;
import enums.HeroType;
import enums.Role;
import model.CombatReport;
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

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class DesktopMain {
    private static final Path DATA_DIR = Path.of("data");

    private final FileStorageService storage = new FileStorageService();
    private GameDataManager dataManager;
    private AuthenticationService authenticationService;
    private RankingService rankingService;
    private SearchService searchService;
    private RecommendationService recommendationService;
    private CombatSimulationService combatSimulationService;
    private Person currentUser;

    private JFrame frame;
    private JLabel statusLabel;
    private JLabel dataSummaryLabel;
    private JTextArea outputArea;
    private JTabbedPane tabs;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JTextField profileNameField;
    private JPasswordField profilePasswordField;
    private JComboBox<String> playerACombo;
    private JComboBox<String> heroACombo;
    private JComboBox<String> equipmentACombo;
    private JComboBox<String> playerBCombo;
    private JComboBox<String> heroBCombo;
    private JComboBox<String> equipmentBCombo;
    private JComboBox<String> adminEntityCombo;
    private JComboBox<String> adminOperationCombo;
    private JPanel adminFieldsPanel;
    private final Map<String, JTextField> adminFields = new LinkedHashMap<>();

    public static void main(String[] args) {
        if (args.length > 0 && "--smoke".equals(args[0])) {
            runSmokeCheck();
            return;
        }
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
                // Default Swing look and feel is acceptable if the platform one is unavailable.
            }
            new DesktopMain().show();
        });
    }

    private DesktopMain() {
        loadData();
    }

    private static void runSmokeCheck() {
        DesktopMain app = new DesktopMain();
        app.authenticationService.login("admin", "admin123")
                .orElseThrow(() -> new IllegalStateException("Admin login smoke check failed."));
        app.authenticationService.login("libai", "player123")
                .orElseThrow(() -> new IllegalStateException("Player login smoke check failed."));
        app.searchService.playerLookup("P001");
        app.recommendationService.heroRecommendationReport("P001", 3);
        app.combatSimulationService.simulateDuel("P001", "H001", "", "P006", "H002", "");
        Hero hero = app.dataManager.requireHero("H001");
        hero.replaceCompatibleEquipment(hero.getCompatibleEquipmentIds());
        hero.replaceRecommendedEquipment(hero.getRecommendedEquipmentIds());
        System.out.println("Desktop smoke check passed: Swing entry initialized services with "
                + app.dataManager.getPlayers().size() + " players, "
                + app.dataManager.getTeams().size() + " teams, "
                + app.dataManager.getHeroes().size() + " heroes.");
    }

    private void loadData() {
        try {
            dataManager = storage.hasDataFiles(DATA_DIR)
                    ? storage.loadAll(DATA_DIR)
                    : DataInitializer.createSampleData();
        } catch (IOException ex) {
            dataManager = DataInitializer.createSampleData();
        }
        buildServices();
    }

    private void buildServices() {
        authenticationService = new AuthenticationService(dataManager);
        rankingService = new RankingService(dataManager);
        searchService = new SearchService(dataManager, rankingService);
        recommendationService = new RecommendationService(dataManager, rankingService);
        combatSimulationService = new CombatSimulationService(dataManager, rankingService);
    }

    private void show() {
        frame = new JFrame("JavaCW Desktop");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(1100, 720));
        frame.setLayout(new BorderLayout(8, 8));
        frame.add(buildTopBar(), BorderLayout.NORTH);

        tabs = new JTabbedPane();
        tabs.addTab("Reports", buildReportsPanel());
        tabs.addTab("Recommendations", buildRecommendationPanel());
        tabs.addTab("Combat", buildCombatPanel());
        tabs.addTab("Player Tools", buildPlayerPanel());
        tabs.addTab("Admin Data", buildAdminPanel());

        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setLineWrap(false);
        outputArea.setFont(new java.awt.Font(java.awt.Font.MONOSPACED, java.awt.Font.PLAIN, 13));
        JScrollPane outputScroll = new JScrollPane(outputArea);
        outputScroll.setBorder(BorderFactory.createTitledBorder("Output"));

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tabs, outputScroll);
        splitPane.setResizeWeight(0.38);
        frame.add(splitPane, BorderLayout.CENTER);

        refreshAllControls();
        updateAccess();
        writeOutput(welcomeText());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private JPanel buildTopBar() {
        JPanel panel = new JPanel(new BorderLayout(8, 4));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 4, 8));

        JPanel loginPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        usernameField = new JTextField("admin", 10);
        passwordField = new JPasswordField("admin123", 10);
        JButton loginButton = new JButton("Login");
        JButton logoutButton = new JButton("Logout");
        JButton saveButton = new JButton("Save Data");
        JButton reloadButton = new JButton("Reload Data");

        loginButton.addActionListener(event -> login());
        logoutButton.addActionListener(event -> logout());
        saveButton.addActionListener(event -> saveDataWithMessage());
        reloadButton.addActionListener(event -> reloadData());

        loginPanel.add(new JLabel("Username"));
        loginPanel.add(usernameField);
        loginPanel.add(new JLabel("Password"));
        loginPanel.add(passwordField);
        loginPanel.add(loginButton);
        loginPanel.add(logoutButton);
        loginPanel.add(saveButton);
        loginPanel.add(reloadButton);

        statusLabel = new JLabel("Not logged in");
        dataSummaryLabel = new JLabel();
        panel.add(loginPanel, BorderLayout.WEST);
        panel.add(statusLabel, BorderLayout.CENTER);
        panel.add(dataSummaryLabel, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildReportsPanel() {
        JPanel panel = formPanel();
        JComboBox<String> actionCombo = new JComboBox<>(new String[]{
                "Player lookup",
                "Team overview",
                "Hero details",
                "Equipment statistics",
                "Player match history",
                "Team match history",
                "Leaderboard"
        });
        JTextField queryField = new JTextField("P001", 18);
        JComboBox<String> modeCombo = new JComboBox<>(new String[]{"winRate", "level", "custom"});
        JSpinner limitSpinner = new JSpinner(new SpinnerNumberModel(10, 1, 50, 1));
        JButton runButton = new JButton("Run");
        JButton listButton = new JButton("List IDs");

        runButton.addActionListener(event -> safeRun(() -> {
            requireLogin();
            String action = selectedText(actionCombo);
            String query = queryField.getText().trim();
            int limit = (Integer) limitSpinner.getValue();
            String result = switch (action) {
                case "Player lookup" -> searchService.playerLookup(query);
                case "Team overview" -> searchService.teamOverview(query);
                case "Hero details" -> searchService.heroDetails(query);
                case "Equipment statistics" -> searchService.equipmentStatistics(limit);
                case "Player match history" -> searchService.playerMatchHistory(query, limit);
                case "Team match history" -> searchService.teamMatchHistory(query, limit);
                case "Leaderboard" -> searchService.leaderboard(selectedText(modeCombo), limit);
                default -> "Unknown report action.";
            };
            writeOutput(result);
        }));
        listButton.addActionListener(event -> writeOutput(idCatalog()));

        addRow(panel, 0, "Report", actionCombo);
        addRow(panel, 1, "ID/name query", queryField);
        addRow(panel, 2, "Leaderboard mode", modeCombo);
        addRow(panel, 3, "Limit", limitSpinner);
        addButtonRow(panel, 4, runButton, listButton);
        return panel;
    }

    private JPanel buildRecommendationPanel() {
        JPanel panel = formPanel();
        JTextField playerQueryField = new JTextField("P001", 18);
        JTextField heroQueryField = new JTextField("H001", 18);
        JSpinner limitSpinner = new JSpinner(new SpinnerNumberModel(5, 1, 20, 1));
        JButton heroButton = new JButton("Recommend Heroes");
        JButton equipmentButton = new JButton("Recommend Equipment");

        heroButton.addActionListener(event -> safeRun(() -> {
            requireLogin();
            writeOutput(recommendationService.heroRecommendationReport(playerQueryField.getText().trim(),
                    (Integer) limitSpinner.getValue()));
        }));
        equipmentButton.addActionListener(event -> safeRun(() -> {
            requireLogin();
            writeOutput(recommendationService.equipmentRecommendationReport(heroQueryField.getText().trim(),
                    (Integer) limitSpinner.getValue()));
        }));

        addRow(panel, 0, "Player ID/name", playerQueryField);
        addRow(panel, 1, "Hero ID/name", heroQueryField);
        addRow(panel, 2, "Limit", limitSpinner);
        addButtonRow(panel, 3, heroButton, equipmentButton);
        return panel;
    }

    private JPanel buildCombatPanel() {
        JPanel panel = formPanel();
        playerACombo = new JComboBox<>();
        heroACombo = new JComboBox<>();
        equipmentACombo = new JComboBox<>();
        playerBCombo = new JComboBox<>();
        heroBCombo = new JComboBox<>();
        equipmentBCombo = new JComboBox<>();
        JButton simulateButton = new JButton("Simulate Duel");
        JButton refreshButton = new JButton("Refresh Choices");

        playerACombo.addActionListener(event -> refreshHeroChoices(playerACombo, heroACombo, equipmentACombo));
        playerBCombo.addActionListener(event -> refreshHeroChoices(playerBCombo, heroBCombo, equipmentBCombo));
        heroACombo.addActionListener(event -> refreshEquipmentChoices(heroACombo, equipmentACombo));
        heroBCombo.addActionListener(event -> refreshEquipmentChoices(heroBCombo, equipmentBCombo));
        refreshButton.addActionListener(event -> refreshCombatChoices());
        simulateButton.addActionListener(event -> safeRun(() -> {
            requireLogin();
            CombatReport report = combatSimulationService.simulateDuel(
                    selectedId(playerACombo),
                    selectedId(heroACombo),
                    selectedEquipmentId(equipmentACombo),
                    selectedId(playerBCombo),
                    selectedId(heroBCombo),
                    selectedEquipmentId(equipmentBCombo));
            writeOutput(report.format());
        }));

        addRow(panel, 0, "Player A", playerACombo);
        addRow(panel, 1, "Hero A", heroACombo);
        addRow(panel, 2, "Equipment A", equipmentACombo);
        addRow(panel, 3, "Player B", playerBCombo);
        addRow(panel, 4, "Hero B", heroBCombo);
        addRow(panel, 5, "Equipment B", equipmentBCombo);
        addButtonRow(panel, 6, simulateButton, refreshButton);
        return panel;
    }

    private JPanel buildPlayerPanel() {
        JPanel panel = formPanel();
        profileNameField = new JTextField(18);
        profilePasswordField = new JPasswordField(18);
        JButton loadButton = new JButton("Load My Profile");
        JButton updateButton = new JButton("Update Name/Password");

        loadButton.addActionListener(event -> safeRun(this::loadCurrentPlayerProfile));
        updateButton.addActionListener(event -> safeRun(this::updateCurrentPlayerProfile));

        addRow(panel, 0, "Name", profileNameField);
        addRow(panel, 1, "New password", profilePasswordField);
        addButtonRow(panel, 2, loadButton, updateButton);
        return panel;
    }

    private JPanel buildAdminPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        adminEntityCombo = new JComboBox<>(new String[]{"Player", "Hero", "Equipment", "Team", "Match"});
        adminOperationCombo = new JComboBox<>(new String[]{"Load", "Add", "Update", "Delete"});
        JButton applyButton = new JButton("Apply");
        JButton listButton = new JButton("List Records");
        controls.add(new JLabel("Entity"));
        controls.add(adminEntityCombo);
        controls.add(new JLabel("Operation"));
        controls.add(adminOperationCombo);
        controls.add(applyButton);
        controls.add(listButton);

        adminFieldsPanel = formPanel();
        adminEntityCombo.addActionListener(event -> rebuildAdminFields());
        applyButton.addActionListener(event -> safeRun(this::applyAdminOperation));
        listButton.addActionListener(event -> safeRun(() -> {
            requireAdmin();
            writeOutput(adminEntityList(selectedText(adminEntityCombo)));
        }));
        rebuildAdminFields();

        panel.add(controls, BorderLayout.NORTH);
        panel.add(new JScrollPane(adminFieldsPanel), BorderLayout.CENTER);
        return panel;
    }

    private void login() {
        safeRun(() -> {
            Optional<Person> user = authenticationService.login(usernameField.getText().trim(),
                    new String(passwordField.getPassword()));
            if (user.isEmpty()) {
                throw new IllegalArgumentException("Invalid username or password.");
            }
            currentUser = user.get();
            updateAccess();
            writeOutput("Logged in as " + currentUser.getName() + " (" + currentUser.getRole() + ").");
        });
    }

    private void logout() {
        currentUser = null;
        updateAccess();
        writeOutput("Logged out.");
    }

    private void saveDataWithMessage() {
        safeRun(() -> {
            saveData();
            writeOutput("Data saved to " + DATA_DIR.toAbsolutePath());
        });
    }

    private void saveData() throws IOException {
        storage.saveAll(dataManager, DATA_DIR);
    }

    private void reloadData() {
        safeRun(() -> {
            loadData();
            currentUser = null;
            refreshAllControls();
            updateAccess();
            writeOutput("Data reloaded from " + DATA_DIR.toAbsolutePath());
        });
    }

    private void loadCurrentPlayerProfile() {
        Player player = requireCurrentPlayer();
        profileNameField.setText(player.getName());
        profilePasswordField.setText("");
        writeOutput(searchService.playerReport(player)
                + System.lineSeparator()
                + "Player profile edits are limited to name and password in this desktop interface.");
    }

    private void updateCurrentPlayerProfile() throws IOException {
        Player player = requireCurrentPlayer();
        player.setName(profileNameField.getText());
        String password = new String(profilePasswordField.getPassword()).trim();
        if (!password.isEmpty()) {
            player.setPassword(password);
        }
        saveData();
        statusLabel.setText("Logged in as " + player.getName() + " (" + player.getRole() + ")");
        writeOutput("Profile updated. Team, level, win/loss record, and hero ownership remain admin-only.");
    }

    private void applyAdminOperation() throws IOException {
        requireAdmin();
        String entity = selectedText(adminEntityCombo);
        String operation = selectedText(adminOperationCombo);
        switch (operation) {
            case "Load" -> loadAdminRecord(entity);
            case "Add" -> addAdminRecord(entity);
            case "Update" -> updateAdminRecord(entity);
            case "Delete" -> deleteAdminRecord(entity);
            default -> throw new IllegalArgumentException("Unknown admin operation: " + operation);
        }
        refreshAllControls();
    }

    private void loadAdminRecord(String entity) {
        String id = field("id").getText().trim();
        switch (entity) {
            case "Player" -> {
                Player player = dataManager.requirePlayer(id);
                putField("name", player.getName());
                putField("username", player.getUsername());
                putField("password", "");
                putField("teamId", player.getTeamId());
                putField("level", String.valueOf(player.getLevel()));
                putField("wins", String.valueOf(player.getWins()));
                putField("losses", String.valueOf(player.getLosses()));
                putField("heroIds", String.join(",", player.getHeroIds()));
            }
            case "Hero" -> {
                Hero hero = dataManager.requireHero(id);
                putField("name", hero.getName());
                putField("type", hero.getType().name());
                putField("attack", String.valueOf(hero.getAttack()));
                putField("defense", String.valueOf(hero.getDefense()));
                putField("health", String.valueOf(hero.getHealth()));
                putField("difficulty", String.valueOf(hero.getDifficulty()));
                putField("compatibleEquipmentIds", String.join(",", hero.getCompatibleEquipmentIds()));
                putField("recommendedEquipmentIds", String.join(",", hero.getRecommendedEquipmentIds()));
            }
            case "Equipment" -> {
                Equipment item = dataManager.requireEquipment(id);
                putField("name", item.getName());
                putField("type", item.getType().name());
                putField("power", String.valueOf(item.getPower()));
                putField("defense", String.valueOf(item.getDefense()));
                putField("price", String.valueOf(item.getPrice()));
                putField("averageRating", String.valueOf(item.getAverageRating()));
                putField("usageCount", String.valueOf(item.getUsageCount()));
                putField("winContribution", String.valueOf(item.getWinContribution()));
            }
            case "Team" -> {
                Team team = dataManager.requireTeam(id);
                putField("name", team.getName());
            }
            case "Match" -> {
                MatchRecord record = dataManager.requireMatchRecord(id);
                putField("date", record.getDate().toString());
                putField("teamAId", record.getTeamAId());
                putField("teamBId", record.getTeamBId());
                putField("winnerTeamId", record.getWinnerTeamId());
                putField("heroPicks", formatPicks(record.getHeroPicks()));
            }
            default -> throw new IllegalArgumentException("Unknown entity: " + entity);
        }
        writeOutput(entity + " " + id + " loaded into the form.");
    }

    private void addAdminRecord(String entity) throws IOException {
        switch (entity) {
            case "Player" -> dataManager.addPlayer(new Player(
                    text("id"), text("name"), text("username"), text("password"), text("teamId"),
                    integer("level"), integer("wins"), integer("losses"), idList("heroIds")));
            case "Hero" -> {
                validateEquipmentIds(idList("compatibleEquipmentIds"));
                validateEquipmentIds(idList("recommendedEquipmentIds"));
                dataManager.addHero(new Hero(text("id"), text("name"), enumValue("type", HeroType.class),
                        integer("attack"), integer("defense"), integer("health"), integer("difficulty"),
                        idList("compatibleEquipmentIds"), idList("recommendedEquipmentIds")));
            }
            case "Equipment" -> dataManager.addEquipment(new Equipment(text("id"), text("name"),
                    enumValue("type", EquipmentType.class), integer("power"), integer("defense"),
                    integer("price"), decimal("averageRating"), integer("usageCount"), decimal("winContribution")));
            case "Team" -> dataManager.addTeam(new Team(text("id"), text("name"), List.of()));
            case "Match" -> dataManager.addMatchRecord(new MatchRecord(text("id"), LocalDate.parse(text("date")),
                    text("teamAId"), text("teamBId"), text("winnerTeamId"), picks("heroPicks")));
            default -> throw new IllegalArgumentException("Unknown entity: " + entity);
        }
        saveData();
        writeOutput(entity + " added and saved.");
    }

    private void updateAdminRecord(String entity) throws IOException {
        String id = text("id");
        switch (entity) {
            case "Player" -> {
                Player player = dataManager.requirePlayer(id);
                player.setName(text("name"));
                String password = field("password").getText().trim();
                if (!password.isEmpty()) {
                    player.setPassword(password);
                }
                if (!player.getTeamId().equals(text("teamId"))) {
                    dataManager.movePlayerToTeam(id, text("teamId"));
                }
                player.setLevel(integer("level"));
                player.setWins(integer("wins"));
                player.setLosses(integer("losses"));
                validateHeroIds(idList("heroIds"));
                player.replaceHeroes(idList("heroIds"));
            }
            case "Hero" -> {
                Hero hero = dataManager.requireHero(id);
                validateEquipmentIds(idList("compatibleEquipmentIds"));
                validateEquipmentIds(idList("recommendedEquipmentIds"));
                hero.setName(text("name"));
                hero.setType(enumValue("type", HeroType.class));
                hero.setAttack(integer("attack"));
                hero.setDefense(integer("defense"));
                hero.setHealth(integer("health"));
                hero.setDifficulty(integer("difficulty"));
                hero.replaceCompatibleEquipment(idList("compatibleEquipmentIds"));
                hero.replaceRecommendedEquipment(idList("recommendedEquipmentIds"));
            }
            case "Equipment" -> {
                Equipment item = dataManager.requireEquipment(id);
                item.setName(text("name"));
                item.setType(enumValue("type", EquipmentType.class));
                item.setPower(integer("power"));
                item.setDefense(integer("defense"));
                item.setPrice(integer("price"));
                item.setAverageRating(decimal("averageRating"));
                item.setUsageCount(integer("usageCount"));
                item.setWinContribution(decimal("winContribution"));
            }
            case "Team" -> dataManager.requireTeam(id).setName(text("name"));
            case "Match" -> dataManager.updateMatchRecord(id, LocalDate.parse(text("date")),
                    text("teamAId"), text("teamBId"), text("winnerTeamId"), picks("heroPicks"));
            default -> throw new IllegalArgumentException("Unknown entity: " + entity);
        }
        saveData();
        writeOutput(entity + " updated and saved.");
    }

    private void deleteAdminRecord(String entity) throws IOException {
        String id = text("id");
        boolean deleted = switch (entity) {
            case "Player" -> dataManager.deletePlayer(id);
            case "Hero" -> dataManager.deleteHero(id);
            case "Equipment" -> dataManager.deleteEquipment(id);
            case "Team" -> dataManager.deleteTeam(id);
            case "Match" -> dataManager.deleteMatchRecord(id);
            default -> throw new IllegalArgumentException("Unknown entity: " + entity);
        };
        saveData();
        writeOutput(deleted ? entity + " deleted and saved." : entity + " not found.");
    }

    private void rebuildAdminFields() {
        if (adminFieldsPanel == null) {
            return;
        }
        adminFields.clear();
        adminFieldsPanel.removeAll();
        String entity = selectedText(adminEntityCombo);
        List<String> fields = switch (entity) {
            case "Player" -> List.of("id", "name", "username", "password", "teamId", "level", "wins", "losses", "heroIds");
            case "Hero" -> List.of("id", "name", "type", "attack", "defense", "health", "difficulty", "compatibleEquipmentIds", "recommendedEquipmentIds");
            case "Equipment" -> List.of("id", "name", "type", "power", "defense", "price", "averageRating", "usageCount", "winContribution");
            case "Team" -> List.of("id", "name");
            case "Match" -> List.of("id", "date", "teamAId", "teamBId", "winnerTeamId", "heroPicks");
            default -> List.of("id");
        };
        int row = 0;
        for (String fieldName : fields) {
            JTextField textField = new JTextField(defaultValue(entity, fieldName), 24);
            adminFields.put(fieldName, textField);
            addRow(adminFieldsPanel, row++, fieldName, textField);
        }
        adminFieldsPanel.revalidate();
        adminFieldsPanel.repaint();
    }

    private String defaultValue(String entity, String fieldName) {
        if ("type".equals(fieldName)) {
            return "Hero".equals(entity) ? HeroType.ASSASSIN.name() : EquipmentType.ATTACK.name();
        }
        if ("date".equals(fieldName)) {
            return LocalDate.now().toString();
        }
        if ("level".equals(fieldName) || "wins".equals(fieldName) || "losses".equals(fieldName)
                || "attack".equals(fieldName) || "defense".equals(fieldName) || "health".equals(fieldName)
                || "price".equals(fieldName) || "power".equals(fieldName) || "usageCount".equals(fieldName)) {
            return "0";
        }
        if ("difficulty".equals(fieldName)) {
            return "1";
        }
        if ("averageRating".equals(fieldName) || "winContribution".equals(fieldName)) {
            return "0.0";
        }
        return "";
    }

    private void updateAccess() {
        String status = currentUser == null
                ? "Not logged in"
                : "Logged in as " + currentUser.getName() + " (" + currentUser.getRole() + ")";
        if (statusLabel != null) {
            statusLabel.setText(status);
        }
        if (tabs != null) {
            boolean player = currentUser != null && currentUser.getRole() == Role.PLAYER;
            boolean admin = currentUser != null && currentUser.getRole() == Role.ADMIN;
            tabs.setEnabledAt(3, player);
            tabs.setEnabledAt(4, admin);
        }
        updateDataSummary();
    }

    private void refreshAllControls() {
        refreshCombatChoices();
        updateDataSummary();
    }

    private void refreshCombatChoices() {
        if (playerACombo == null) {
            return;
        }
        fillCombo(playerACombo, playerLabels());
        fillCombo(playerBCombo, playerLabels());
        selectComboById(playerACombo, "P001");
        selectComboById(playerBCombo, "P006");
        refreshHeroChoices(playerACombo, heroACombo, equipmentACombo);
        refreshHeroChoices(playerBCombo, heroBCombo, equipmentBCombo);
    }

    private void refreshHeroChoices(JComboBox<String> playerCombo, JComboBox<String> heroCombo, JComboBox<String> equipmentCombo) {
        if (playerCombo == null || heroCombo == null || equipmentCombo == null || playerCombo.getItemCount() == 0) {
            return;
        }
        Player player = dataManager.requirePlayer(selectedId(playerCombo));
        List<String> heroLabels = dataManager.heroesForPlayer(player).stream()
                .map(hero -> label(hero.getId(), hero.getName()))
                .toList();
        fillCombo(heroCombo, heroLabels);
        refreshEquipmentChoices(heroCombo, equipmentCombo);
    }

    private void refreshEquipmentChoices(JComboBox<String> heroCombo, JComboBox<String> equipmentCombo) {
        if (heroCombo == null || equipmentCombo == null || heroCombo.getItemCount() == 0) {
            return;
        }
        Hero hero = dataManager.requireHero(selectedId(heroCombo));
        List<String> equipmentLabels = new ArrayList<>();
        equipmentLabels.add("AUTO - best compatible");
        for (Equipment item : dataManager.compatibleEquipmentForHero(hero)) {
            equipmentLabels.add(label(item.getId(), item.getName()));
        }
        fillCombo(equipmentCombo, equipmentLabels);
    }

    private void fillCombo(JComboBox<String> combo, List<String> values) {
        combo.removeAllItems();
        for (String value : values) {
            combo.addItem(value);
        }
    }

    private List<String> playerLabels() {
        return dataManager.getPlayers().stream()
                .map(player -> label(player.getId(), player.getName()))
                .toList();
    }

    private String adminEntityList(String entity) {
        StringBuilder output = new StringBuilder(entity).append(" records").append(System.lineSeparator());
        switch (entity) {
            case "Player" -> dataManager.getPlayers().forEach(player -> output.append(player).append(System.lineSeparator()));
            case "Hero" -> dataManager.getHeroes().forEach(hero -> output.append(hero).append(System.lineSeparator()));
            case "Equipment" -> dataManager.getEquipment().forEach(item -> output.append(item).append(System.lineSeparator()));
            case "Team" -> dataManager.getTeams().forEach(team -> output.append(team).append(System.lineSeparator()));
            case "Match" -> dataManager.getMatchRecords().forEach(record -> output.append(record).append(System.lineSeparator()));
            default -> output.append("Unknown entity.");
        }
        return output.toString();
    }

    private String idCatalog() {
        StringBuilder output = new StringBuilder();
        output.append(summaryLine()).append(System.lineSeparator()).append(System.lineSeparator());
        appendCatalog(output, "Players", dataManager.getPlayers());
        appendCatalog(output, "Teams", dataManager.getTeams());
        appendCatalog(output, "Heroes", dataManager.getHeroes());
        appendCatalog(output, "Equipment", dataManager.getEquipment());
        appendCatalog(output, "Matches", dataManager.getMatchRecords());
        return output.toString();
    }

    private void appendCatalog(StringBuilder output, String title, Collection<?> records) {
        output.append(title).append(System.lineSeparator());
        for (Object record : records) {
            output.append("- ").append(record).append(System.lineSeparator());
        }
        output.append(System.lineSeparator());
    }

    private String welcomeText() {
        return "JavaCW Desktop is a local Swing interface for the same Java services used by Main and WebMain."
                + System.lineSeparator()
                + "Login with admin/admin123 or a player account such as libai/player123."
                + System.lineSeparator()
                + summaryLine();
    }

    private String summaryLine() {
        return "Dataset: " + dataManager.getPlayers().size() + " players, "
                + dataManager.getTeams().size() + " teams, "
                + dataManager.getHeroes().size() + " heroes, "
                + dataManager.getEquipment().size() + " equipment, "
                + dataManager.getMatchRecords().size() + " matches.";
    }

    private void updateDataSummary() {
        if (dataSummaryLabel != null) {
            dataSummaryLabel.setText(summaryLine());
        }
    }

    private JPanel formPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        return panel;
    }

    private void addRow(JPanel panel, int row, String label, java.awt.Component component) {
        GridBagConstraints labelConstraints = new GridBagConstraints();
        labelConstraints.gridx = 0;
        labelConstraints.gridy = row;
        labelConstraints.anchor = GridBagConstraints.WEST;
        labelConstraints.insets = new Insets(4, 4, 4, 8);
        panel.add(new JLabel(label), labelConstraints);

        GridBagConstraints fieldConstraints = new GridBagConstraints();
        fieldConstraints.gridx = 1;
        fieldConstraints.gridy = row;
        fieldConstraints.weightx = 1.0;
        fieldConstraints.fill = GridBagConstraints.HORIZONTAL;
        fieldConstraints.insets = new Insets(4, 4, 4, 4);
        panel.add(component, fieldConstraints);
    }

    private void addButtonRow(JPanel panel, int row, JButton... buttons) {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        for (JButton button : buttons) {
            buttonPanel.add(button);
        }
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = row;
        constraints.gridwidth = 2;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(8, 4, 4, 4);
        panel.add(buttonPanel, constraints);
    }

    private void safeRun(Action action) {
        try {
            action.run();
        } catch (Exception ex) {
            writeOutput("Error: " + ex.getMessage());
            if (frame != null) {
                JOptionPane.showMessageDialog(frame, ex.getMessage(), "JavaCW Desktop", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void writeOutput(String text) {
        if (outputArea != null) {
            outputArea.setText(text);
            outputArea.setCaretPosition(0);
        } else {
            System.out.println(text);
        }
    }

    private void requireLogin() {
        if (currentUser == null) {
            throw new IllegalStateException("Please login first.");
        }
    }

    private void requireAdmin() {
        requireLogin();
        if (currentUser.getRole() != Role.ADMIN) {
            throw new IllegalStateException("Admin login is required for data management.");
        }
    }

    private Player requireCurrentPlayer() {
        requireLogin();
        if (currentUser.getRole() != Role.PLAYER) {
            throw new IllegalStateException("Player login is required for this panel.");
        }
        return dataManager.requirePlayer(currentUser.getId());
    }

    private JTextField field(String name) {
        JTextField field = adminFields.get(name);
        if (field == null) {
            throw new IllegalArgumentException("Unknown form field: " + name);
        }
        return field;
    }

    private String text(String name) {
        String value = field(name).getText().trim();
        if (value.isEmpty()) {
            throw new IllegalArgumentException(name + " cannot be blank");
        }
        return value;
    }

    private int integer(String name) {
        return Integer.parseInt(text(name));
    }

    private double decimal(String name) {
        return Double.parseDouble(text(name));
    }

    private <T extends Enum<T>> T enumValue(String name, Class<T> type) {
        return Enum.valueOf(type, text(name).toUpperCase());
    }

    private List<String> idList(String name) {
        String raw = field(name).getText().trim();
        if (raw.isEmpty()) {
            return List.of();
        }
        List<String> values = new ArrayList<>();
        for (String part : raw.split("[,;]")) {
            String value = part.trim();
            if (!value.isEmpty()) {
                values.add(value);
            }
        }
        return values;
    }

    private Map<String, String> picks(String name) {
        String raw = field(name).getText().trim();
        Map<String, String> picks = new LinkedHashMap<>();
        if (raw.isEmpty()) {
            return picks;
        }
        for (String entry : raw.split("[,;]")) {
            String[] parts = entry.trim().split(":");
            if (parts.length != 2) {
                throw new IllegalArgumentException("Hero picks must use playerId:heroId entries.");
            }
            picks.put(parts[0].trim(), parts[1].trim());
        }
        return picks;
    }

    private String formatPicks(Map<String, String> picks) {
        List<String> entries = new ArrayList<>();
        for (Map.Entry<String, String> entry : picks.entrySet()) {
            entries.add(entry.getKey() + ":" + entry.getValue());
        }
        return String.join(",", entries);
    }

    private void validateHeroIds(List<String> heroIds) {
        for (String heroId : heroIds) {
            dataManager.requireHero(heroId);
        }
    }

    private void validateEquipmentIds(List<String> equipmentIds) {
        for (String equipmentId : equipmentIds) {
            dataManager.requireEquipment(equipmentId);
        }
    }

    private void putField(String name, String value) {
        field(name).setText(value);
    }

    private String selectedText(JComboBox<String> combo) {
        Object selected = combo.getSelectedItem();
        return selected == null ? "" : selected.toString();
    }

    private String selectedId(JComboBox<String> combo) {
        String selected = selectedText(combo);
        int separator = selected.indexOf(" - ");
        return separator < 0 ? selected.trim() : selected.substring(0, separator).trim();
    }

    private String selectedEquipmentId(JComboBox<String> combo) {
        String selected = selectedId(combo);
        return "AUTO".equals(selected) ? "" : selected;
    }

    private void selectComboById(JComboBox<String> combo, String id) {
        for (int i = 0; i < combo.getItemCount(); i++) {
            if (combo.getItemAt(i).startsWith(id + " - ")) {
                combo.setSelectedIndex(i);
                return;
            }
        }
    }

    private String label(String id, String name) {
        return id + " - " + name;
    }

    @FunctionalInterface
    private interface Action {
        void run() throws Exception;
    }
}
