package test;

import enums.EquipmentType;
import model.Equipment;
import model.Hero;
import model.MatchRecord;
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

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class TestRunner {
    private static int passed;
    private static int failed;

    public static void main(String[] args) {
        run("recommendation report ranks heroes", TestRunner::testRecommendationReport);
        run("equipment recommendation uses hero context", TestRunner::testEquipmentRecommendation);
        run("combat simulation produces a winner", TestRunner::testCombatSimulation);
        run("invalid login is rejected", TestRunner::testInvalidLogin);
        run("missing lookup returns a friendly report", TestRunner::testMissingLookupReport);
        run("match validation rejects duplicate hero picks", TestRunner::testRejectDuplicateHeroPick);
        run("match validation rejects winner outside match", TestRunner::testRejectWinnerOutsideMatch);
        run("match validation rejects player outside teams", TestRunner::testRejectPlayerOutsideTeams);
        run("equipment add and delete updates hero references", TestRunner::testEquipmentAddDeleteCascade);
        run("default ranking reports stay text based", TestRunner::testDefaultReportsStayTextBased);
        run("player lookup shows actual equipped items", TestRunner::testPlayerLookupShowsActualEquipment);
        run("team history counts only the requested team picks", TestRunner::testTeamHistoryFiltersOpponentPicks);
        run("player history keeps historical team after transfer", TestRunner::testPlayerHistoryAfterTeamTransfer);
        run("invalid player update is atomic", TestRunner::testInvalidPlayerUpdateIsAtomic);
        run("player deletion updates team membership", TestRunner::testDeletePlayerUpdatesTeam);
        run("ID-backed associations resolve to domain objects", TestRunner::testAssociationHelpersResolveObjects);
        run("leaderboard sorts by win rate", TestRunner::testLeaderboardSortsByWinRate);
        run("zero-match player win rate is safe", TestRunner::testZeroMatchWinRate);
        run("CSV save/load round trip keeps counts", TestRunner::testCsvRoundTrip);
        run("concurrent CSV saves use independent temp files", TestRunner::testConcurrentCsvSaves);

        System.out.println("Automated tests passed: " + passed + ", failed: " + failed);
        if (failed > 0) {
            throw new AssertionError("Automated test failures: " + failed);
        }
    }

    private static void testRecommendationReport() {
        GameDataManager data = sample();
        RecommendationService service = new RecommendationService(data, new RankingService(data));
        String report = service.heroRecommendationReport("P001", 3);
        assertContains(report, "Hero recommendation formula");
        assertContains(report, "Recommendations for player Li Bai");
        assertContains(report, "Reason:");
    }

    private static void testEquipmentRecommendation() {
        GameDataManager data = sample();
        RecommendationService service = new RecommendationService(data, new RankingService(data));
        String report = service.equipmentRecommendationReport("H001", 2);
        assertContains(report, "Equipment recommendation formula");
        assertContains(report, "Recommendations for hero Li Bai");
        assertContains(report, "Endless Battle");
    }

    private static void testCombatSimulation() {
        GameDataManager data = sample();
        RankingService ranking = new RankingService(data);
        CombatSimulationService service = new CombatSimulationService(data, ranking, new Random(7));
        String report = service.simulateDuel("P001", "H001", "", "P006", "H002", "").format();
        assertContains(report, "Winner:");
        assertContains(report, "Damage formula:");
    }

    private static void testInvalidLogin() {
        AuthenticationService auth = new AuthenticationService(sample());
        assertTrue(auth.login("admin", "wrong-password").isEmpty(), "wrong password should not authenticate");
        assertTrue(auth.login("missing-user", "admin123").isEmpty(), "unknown user should not authenticate");
    }

    private static void testMissingLookupReport() {
        GameDataManager data = sample();
        SearchService search = new SearchService(data, new RankingService(data));
        assertContains(search.playerLookup("Not A Player"), "No player found");
        assertContains(search.teamOverview("Not A Team"), "No team found");
        assertContains(search.heroDetails("Not A Hero"), "No hero found");
    }

    private static void testRejectDuplicateHeroPick() {
        GameDataManager data = sample();
        Map<String, String> picks = new LinkedHashMap<>();
        picks.put("P001", "H001");
        picks.put("P011", "H001");
        assertThrows(IllegalArgumentException.class, () -> data.addMatchRecord(
                new MatchRecord("M999", LocalDate.parse("2026-06-01"), "T001", "T003", "T001", picks)));
    }

    private static void testRejectWinnerOutsideMatch() {
        GameDataManager data = sample();
        Map<String, String> picks = new LinkedHashMap<>();
        picks.put("P001", "H001");
        picks.put("P006", "H002");
        assertThrows(IllegalArgumentException.class, () -> data.addMatchRecord(
                new MatchRecord("M999", LocalDate.parse("2026-06-01"), "T001", "T002", "T003", picks)));
    }

    private static void testRejectPlayerOutsideTeams() {
        GameDataManager data = sample();
        Map<String, String> picks = new LinkedHashMap<>();
        picks.put("P011", "H001");
        assertThrows(IllegalArgumentException.class, () -> data.addMatchRecord(
                new MatchRecord("M999", LocalDate.parse("2026-06-01"), "T001", "T002", "T001", picks)));
    }

    private static void testEquipmentAddDeleteCascade() {
        GameDataManager data = sample();
        Equipment item = new Equipment("E999", "Test Blade", EquipmentType.ATTACK, 80, 0, 1000, 4.2, 3, 0.30);
        data.addEquipment(item);
        data.requireHero("H001").addCompatibleEquipment("E999");
        data.requireHero("H001").addRecommendedEquipment("E999");
        data.requirePlayer("P001").replaceEquipmentLoadout("H001", List.of("E002", "E999"));

        assertTrue(data.findEquipment("E999").isPresent(), "added equipment should be searchable");
        assertTrue(data.requireHero("H001").getCompatibleEquipmentIds().contains("E999"),
                "hero should reference added equipment before deletion");
        assertTrue(data.deleteEquipment("E999"), "deleteEquipment should return true");
        assertTrue(data.findEquipment("E999").isEmpty(), "deleted equipment should not remain searchable");
        assertTrue(!data.requireHero("H001").getCompatibleEquipmentIds().contains("E999"),
                "deleted equipment should be removed from compatible equipment");
        assertTrue(!data.requireHero("H001").getRecommendedEquipmentIds().contains("E999"),
                "deleted equipment should be removed from recommended equipment");
        assertTrue(!data.requirePlayer("P001").getEquippedEquipmentIds("H001").contains("E999"),
                "deleted equipment should be removed from player loadouts");
    }

    private static void testDefaultReportsStayTextBased() {
        GameDataManager data = sample();
        SearchService search = new SearchService(data, new RankingService(data));
        String leaderboard = search.leaderboard("winrate", 2);
        String equipment = search.equipmentStatistics(2);
        assertContains(leaderboard, "Leaderboard by winrate");
        assertContains(equipment, "Equipment ranking formula");
        assertTrue(!leaderboard.contains("\"rows\""), "console leaderboard report should not expose JSON rows");
        assertTrue(!equipment.contains("\"rows\""), "console equipment report should not expose JSON rows");
    }

    private static void testDeletePlayerUpdatesTeam() {
        GameDataManager data = sample();
        assertTrue(data.requireTeam("T001").getPlayerIds().contains("P001"), "P001 should start in T001");
        assertTrue(data.deletePlayer("P001"), "deletePlayer should return true");
        assertTrue(!data.requireTeam("T001").getPlayerIds().contains("P001"), "P001 should be removed from T001");
    }

    private static void testPlayerLookupShowsActualEquipment() {
        GameDataManager data = sample();
        SearchService search = new SearchService(data, new RankingService(data));
        String report = search.playerLookup("P001");
        assertContains(report, "Equipped: [Endless Battle, Bloodweeper]");
        assertNotContains(report, "Equipped/compatible");
        assertNotContains(report, "Equipped: [Shadow Blade, Endless Battle, Jungle Blade, Bloodweeper]");
    }

    private static void testTeamHistoryFiltersOpponentPicks() {
        GameDataManager data = sample();
        SearchService search = new SearchService(data, new RankingService(data));
        String report = search.teamMatchHistory("T001", 1);
        assertContains(report, "Li Bai->Li Bai");
        assertContains(report, "Zhang Fei->Lian Po");
        assertNotContains(report, "Xiao Qiao->Angela");
        assertContains(report, "Li Bai: 20.0%");
        assertNotContains(report, "10.0%");
    }

    private static void testPlayerHistoryAfterTeamTransfer() {
        GameDataManager data = sample();
        SearchService search = new SearchService(data, new RankingService(data));
        data.movePlayerToTeam("P001", "T002");
        String report = search.playerMatchHistory("P001", 2);
        assertContains(report, "vs River Guardians result LOSS");
        assertContains(report, "vs Cloud Arena result LOSS");
        assertNotContains(report, "vs Unknown");
    }

    private static void testInvalidPlayerUpdateIsAtomic() {
        GameDataManager data = sample();
        Player player = data.requirePlayer("P001");
        List<String> originalHeroIds = List.copyOf(player.getHeroIds());
        Map<String, List<String>> originalLoadouts = player.getEquipmentLoadouts();
        String originalName = player.getName();
        String originalTeamId = player.getTeamId();

        assertThrows(IllegalArgumentException.class, () -> data.updatePlayer(
                "P001",
                "Changed Name",
                null,
                "T002",
                99,
                99,
                0,
                List.of("H001", "H999"),
                Map.of("H001", List.of("E002"))));

        assertEquals(originalName, player.getName(), "name after rejected update");
        assertEquals(originalTeamId, player.getTeamId(), "team after rejected update");
        assertTrue(originalHeroIds.equals(player.getHeroIds()), "heroes should be unchanged after rejected update");
        assertTrue(originalLoadouts.equals(player.getEquipmentLoadouts()),
                "loadouts should be unchanged after rejected update");
    }

    private static void testAssociationHelpersResolveObjects() {
        GameDataManager data = sample();
        Player player = data.requirePlayer("P001");
        Team team = data.teamForPlayer(player);
        Hero hero = data.requireHero("H001");

        assertEquals("T001", team.getId(), "player team association");
        assertTrue(data.playersForTeam(team).stream().anyMatch(member -> member.getId().equals("P001")),
                "team should resolve member player objects");
        assertTrue(data.heroesForPlayer(player).stream().anyMatch(ownedHero -> ownedHero.getId().equals("H001")),
                "player should resolve owned hero objects");
        assertTrue(data.compatibleEquipmentForHero(hero).size() >= 2,
                "hero should resolve compatible equipment objects");
        assertTrue(data.playersOwningHero(hero).stream().anyMatch(owner -> owner.getId().equals("P001")),
                "hero should resolve owning player objects");
        assertTrue(!data.matchesForTeam(team.getId()).isEmpty(), "team should resolve related matches");
        assertTrue(!data.matchesForPlayer(player.getId()).isEmpty(), "player should resolve related matches");
    }

    private static void testLeaderboardSortsByWinRate() {
        GameDataManager data = sample();
        SearchService search = new SearchService(data, new RankingService(data));
        String report = search.leaderboard("winrate", 1);
        assertContains(report, "Mulan");
    }

    private static void testZeroMatchWinRate() {
        Player player = new Player("PX", "Zero", "zero", "pw", "T001", 1, 0, 0, java.util.List.of());
        assertEquals(0.0, player.getWinRate(), "zero-match win rate");
    }

    private static void testCsvRoundTrip() throws Exception {
        GameDataManager data = sample();
        FileStorageService storage = new FileStorageService();
        Path dir = Files.createTempDirectory("javacw-test");
        storage.saveAll(data, dir);
        GameDataManager loaded = storage.loadAll(dir);
        assertEquals(data.getPlayers().size(), loaded.getPlayers().size(), "player count after round trip");
        assertEquals(data.getHeroes().size(), loaded.getHeroes().size(), "hero count after round trip");
        assertEquals(data.getMatchRecords().size(), loaded.getMatchRecords().size(), "match count after round trip");
        assertTrue(data.requirePlayer("P001").getEquipmentLoadouts()
                        .equals(loaded.requirePlayer("P001").getEquipmentLoadouts()),
                "equipment loadouts after round trip");
        assertEquals("T001", loaded.requireMatchRecord("M001").teamForPlayer("P001"),
                "historical participant team after round trip");
    }

    private static void testConcurrentCsvSaves() throws Exception {
        GameDataManager data = sample();
        FileStorageService storage = new FileStorageService();
        Path dir = Files.createTempDirectory("javacw-concurrent-save");
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<?> first = executor.submit(() -> saveUnchecked(storage, data, dir));
            Future<?> second = executor.submit(() -> saveUnchecked(storage, data, dir));
            first.get();
            second.get();
        } finally {
            executor.shutdownNow();
        }
        GameDataManager loaded = storage.loadAll(dir);
        assertEquals(data.getPlayers().size(), loaded.getPlayers().size(),
                "player count after concurrent save");
        try (var files = Files.list(dir)) {
            assertTrue(files.noneMatch(path -> path.getFileName().toString().endsWith(".tmp")),
                    "temporary save files should be cleaned up");
        }
    }

    private static void saveUnchecked(FileStorageService storage, GameDataManager data, Path dir) {
        try {
            storage.saveAll(data, dir);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private static GameDataManager sample() {
        return DataInitializer.createSampleData();
    }

    private static void run(String name, TestCase testCase) {
        try {
            testCase.run();
            passed++;
            System.out.println("PASS " + name);
        } catch (Throwable ex) {
            failed++;
            System.out.println("FAIL " + name + ": " + ex.getMessage());
        }
    }

    private static void assertContains(String actual, String expected) {
        if (!actual.contains(expected)) {
            throw new AssertionError("expected text not found: " + expected);
        }
    }

    private static void assertNotContains(String actual, String unexpected) {
        if (actual.contains(unexpected)) {
            throw new AssertionError("unexpected text found: " + unexpected);
        }
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void assertEquals(int expected, int actual, String label) {
        if (expected != actual) {
            throw new AssertionError(label + " expected " + expected + " got " + actual);
        }
    }

    private static void assertEquals(String expected, String actual, String label) {
        if (!expected.equals(actual)) {
            throw new AssertionError(label + " expected " + expected + " got " + actual);
        }
    }

    private static void assertEquals(double expected, double actual, String label) {
        if (Double.compare(expected, actual) != 0) {
            throw new AssertionError(label + " expected " + expected + " got " + actual);
        }
    }

    private static void assertThrows(Class<? extends Throwable> expectedType, TestCase testCase) {
        try {
            testCase.run();
        } catch (Throwable ex) {
            if (expectedType.isInstance(ex)) {
                return;
            }
            throw new AssertionError("expected " + expectedType.getSimpleName() + " got " + ex.getClass().getSimpleName());
        }
        throw new AssertionError("expected exception " + expectedType.getSimpleName());
    }

    @FunctionalInterface
    private interface TestCase {
        void run() throws Exception;
    }
}
