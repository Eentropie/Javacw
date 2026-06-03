package test;

import enums.EquipmentType;
import model.Equipment;
import model.MatchRecord;
import model.Player;
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
import java.util.Map;
import java.util.Random;

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
        run("player deletion updates team membership", TestRunner::testDeletePlayerUpdatesTeam);
        run("leaderboard sorts by win rate", TestRunner::testLeaderboardSortsByWinRate);
        run("zero-match player win rate is safe", TestRunner::testZeroMatchWinRate);
        run("CSV save/load round trip keeps counts", TestRunner::testCsvRoundTrip);

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

        assertTrue(data.findEquipment("E999").isPresent(), "added equipment should be searchable");
        assertTrue(data.requireHero("H001").getCompatibleEquipmentIds().contains("E999"),
                "hero should reference added equipment before deletion");
        assertTrue(data.deleteEquipment("E999"), "deleteEquipment should return true");
        assertTrue(data.findEquipment("E999").isEmpty(), "deleted equipment should not remain searchable");
        assertTrue(!data.requireHero("H001").getCompatibleEquipmentIds().contains("E999"),
                "deleted equipment should be removed from compatible equipment");
        assertTrue(!data.requireHero("H001").getRecommendedEquipmentIds().contains("E999"),
                "deleted equipment should be removed from recommended equipment");
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
