package web;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Executors;

public class WebServer {
    private final GameDataManager dataManager;
    private final Path dataDir;
    private final Path webDir;
    private final FileStorageService storageService = new FileStorageService();
    private final AuthenticationService authenticationService;
    private final RankingService rankingService;
    private final RecommendationService recommendationService;
    private final CombatSimulationService combatSimulationService;
    private final SearchService searchService;
    private final Map<String, Person> sessions = new LinkedHashMap<>();
    private HttpServer server;

    public WebServer(GameDataManager dataManager, Path dataDir, Path webDir) {
        this.dataManager = dataManager;
        this.dataDir = dataDir;
        this.webDir = webDir.toAbsolutePath().normalize();
        this.authenticationService = new AuthenticationService(dataManager);
        this.rankingService = new RankingService(dataManager);
        this.recommendationService = new RecommendationService(dataManager, rankingService);
        this.combatSimulationService = new CombatSimulationService(dataManager, rankingService);
        this.searchService = new SearchService(dataManager, rankingService);
    }

    public void start(int port) throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", port), 0);
        server.createContext("/api/", this::handleApi);
        server.createContext("/", this::handleStatic);
        server.setExecutor(Executors.newSingleThreadExecutor());
        server.start();
    }

    private void handleApi(HttpExchange exchange) throws IOException {
        try {
            String path = exchange.getRequestURI().getPath();
            switch (path) {
                case "/api/health" -> respondJson(exchange, 200, ok("\"status\":\"ready\""));
                case "/api/bootstrap" -> requireMethod(exchange, "GET", () -> respondJson(exchange, 200, bootstrapJson()));
                case "/api/login" -> requireMethod(exchange, "POST", () -> login(exchange));
                case "/api/logout" -> requireMethod(exchange, "POST", () -> logout(exchange));
                case "/api/search/player" -> requireMethod(exchange, "GET", () -> report(exchange, searchService.playerLookup(query(exchange, "q", ""))));
                case "/api/search/team" -> requireMethod(exchange, "GET", () -> report(exchange, searchService.teamOverview(query(exchange, "q", ""))));
                case "/api/search/hero" -> requireMethod(exchange, "GET", () -> report(exchange, searchService.heroDetails(query(exchange, "q", ""))));
                case "/api/equipment" -> requireMethod(exchange, "GET", () -> equipment(exchange));
                case "/api/leaderboard" -> requireMethod(exchange, "GET", () -> leaderboard(exchange));
                case "/api/matches/player" -> requireMethod(exchange, "GET", () -> playerMatches(exchange));
                case "/api/matches/team" -> requireMethod(exchange, "GET", () -> teamMatches(exchange));
                case "/api/recommend/heroes" -> requireMethod(exchange, "GET", () -> report(exchange, recommendationService.heroRecommendationReport(query(exchange, "playerId", ""), limit(exchange))));
                case "/api/recommend/equipment" -> requireMethod(exchange, "GET", () -> report(exchange, recommendationService.equipmentRecommendationReport(query(exchange, "heroId", ""), limit(exchange))));
                case "/api/combat" -> requireMethod(exchange, "POST", () -> combat(exchange));
                case "/api/save" -> requireMethod(exchange, "POST", () -> save(exchange));
                default -> respondJson(exchange, 404, fail("Unknown API endpoint: " + path));
            }
        } catch (IllegalArgumentException ex) {
            respondJson(exchange, 400, fail(ex.getMessage()));
        } catch (RuntimeException ex) {
            respondJson(exchange, 500, fail(ex.getMessage()));
        }
    }

    private void requireMethod(HttpExchange exchange, String method, WebAction action) throws IOException {
        if (!method.equals(exchange.getRequestMethod())) {
            respondJson(exchange, 405, fail("Method not allowed. Use " + method + "."));
            return;
        }
        action.run();
    }

    private void login(HttpExchange exchange) throws IOException {
        Map<String, String> form = form(exchange);
        Optional<Person> user = authenticationService.login(form.getOrDefault("username", ""), form.getOrDefault("password", ""));
        if (user.isEmpty()) {
            respondJson(exchange, 401, fail("Invalid username or password."));
            return;
        }
        String token = UUID.randomUUID().toString();
        sessions.put(token, user.get());
        respondJson(exchange, 200, ok("\"token\":" + json(token) + ",\"user\":" + userJson(user.get())));
    }

    private void logout(HttpExchange exchange) throws IOException {
        String token = token(exchange);
        if (!token.isBlank()) {
            sessions.remove(token);
        }
        respondJson(exchange, 200, ok("\"message\":\"Logged out\""));
    }

    private void report(HttpExchange exchange, String text) throws IOException {
        respondJson(exchange, 200, ok("\"report\":" + json(text)));
    }

    private void equipment(HttpExchange exchange) throws IOException {
        int limit = limit(exchange);
        if (!wantsStructuredJson(exchange)) {
            report(exchange, searchService.equipmentStatistics(limit));
            return;
        }
        respondJson(exchange, 200, ok("\"title\":\"Equipment Ranking\","
                + "\"columns\":[\"rank\",\"id\",\"name\",\"type\",\"power\",\"defense\",\"usage\",\"rating\",\"compatibleHeroes\",\"score\"],"
                + "\"rows\":" + equipmentRowsJson(limit)));
    }

    private void leaderboard(HttpExchange exchange) throws IOException {
        String mode = query(exchange, "mode", "winrate");
        int limit = limit(exchange);
        if (!wantsStructuredJson(exchange)) {
            report(exchange, searchService.leaderboard(mode, limit));
            return;
        }
        respondJson(exchange, 200, ok("\"title\":\"Leaderboard\","
                + "\"mode\":" + json(mode)
                + ",\"columns\":[\"rank\",\"id\",\"name\",\"team\",\"level\",\"wins\",\"losses\",\"matches\",\"winRate\",\"score\"],"
                + "\"rows\":" + leaderboardRowsJson(mode, limit)));
    }

    private void playerMatches(HttpExchange exchange) throws IOException {
        String playerId = query(exchange, "playerId", "");
        int limit = limit(exchange);
        if (!wantsStructuredJson(exchange)) {
            report(exchange, searchService.playerMatchHistory(playerId, limit));
            return;
        }
        Player player = dataManager.requirePlayer(playerId);
        List<MatchRecord> matches = dataManager.getMatchesNewestFirst().stream()
                .filter(record -> record.includesPlayer(playerId))
                .limit(limit)
                .toList();
        respondJson(exchange, 200, ok("\"title\":" + json("Player " + player.getName() + " Match History") + ","
                + "\"scope\":\"player\","
                + "\"subject\":" + playerSummaryJson(player) + ","
                + "\"columns\":[\"date\",\"matchId\",\"opponent\",\"result\",\"hero\"],"
                + "\"rows\":" + matchRowsJson(player.getTeamId(), playerId, matches)));
    }

    private void teamMatches(HttpExchange exchange) throws IOException {
        String teamId = query(exchange, "teamId", "");
        int limit = limit(exchange);
        if (!wantsStructuredJson(exchange)) {
            report(exchange, searchService.teamMatchHistory(teamId, limit));
            return;
        }
        Team team = dataManager.requireTeam(teamId);
        List<MatchRecord> matches = dataManager.getMatchesNewestFirst().stream()
                .filter(record -> record.includesTeam(teamId))
                .limit(limit)
                .toList();
        respondJson(exchange, 200, ok("\"title\":" + json("Team " + team.getName() + " Match History") + ","
                + "\"scope\":\"team\","
                + "\"subject\":" + teamSummaryJson(team) + ","
                + "\"columns\":[\"date\",\"matchId\",\"opponent\",\"result\",\"picks\"],"
                + "\"rows\":" + matchRowsJson(teamId, null, matches)));
    }

    private void combat(HttpExchange exchange) throws IOException {
        Map<String, String> form = form(exchange);
        String text = combatSimulationService.simulateDuel(
                form.getOrDefault("playerAId", ""),
                form.getOrDefault("heroAId", ""),
                form.getOrDefault("equipmentAId", ""),
                form.getOrDefault("playerBId", ""),
                form.getOrDefault("heroBId", ""),
                form.getOrDefault("equipmentBId", "")
        ).format();
        report(exchange, text);
    }

    private void save(HttpExchange exchange) throws IOException {
        Person user = sessions.get(token(exchange));
        if (user == null || user.getRole() == null || !"ADMIN".equals(user.getRole().name())) {
            respondJson(exchange, 403, fail("Admin login is required to save data."));
            return;
        }
        storageService.saveAll(dataManager, dataDir);
        respondJson(exchange, 200, ok("\"message\":\"Data saved to " + dataDir + "\""));
    }

    private String bootstrapJson() {
        return ok("\"counts\":{"
                + "\"players\":" + dataManager.getPlayers().size() + ","
                + "\"teams\":" + dataManager.getTeams().size() + ","
                + "\"heroes\":" + dataManager.getHeroes().size() + ","
                + "\"equipment\":" + dataManager.getEquipment().size() + ","
                + "\"matches\":" + dataManager.getMatchRecords().size()
                + "},\"players\":" + playersJson()
                + ",\"teams\":" + teamsJson()
                + ",\"heroes\":" + heroesJson()
                + ",\"equipment\":" + equipmentJson()
                + ",\"matches\":" + matchesJson());
    }

    private String playersJson() {
        StringBuilder out = new StringBuilder("[");
        boolean first = true;
        for (Player player : dataManager.getPlayers()) {
            if (!first) {
                out.append(",");
            }
            first = false;
            out.append("{\"id\":").append(json(player.getId()))
                    .append(",\"name\":").append(json(player.getName()))
                    .append(",\"username\":").append(json(player.getUsername()))
                    .append(",\"teamId\":").append(json(player.getTeamId()))
                    .append(",\"level\":").append(player.getLevel())
                    .append(",\"winRate\":").append(String.format(Locale.US, "%.1f", player.getWinRate()))
                    .append(",\"heroIds\":").append(stringListJson(player.getHeroIds()))
                    .append("}");
        }
        return out.append("]").toString();
    }

    private String teamsJson() {
        StringBuilder out = new StringBuilder("[");
        boolean first = true;
        for (Team team : dataManager.getTeams()) {
            if (!first) {
                out.append(",");
            }
            first = false;
            out.append("{\"id\":").append(json(team.getId()))
                    .append(",\"name\":").append(json(team.getName()))
                    .append(",\"playerIds\":").append(stringListJson(team.getPlayerIds()))
                    .append("}");
        }
        return out.append("]").toString();
    }

    private String heroesJson() {
        StringBuilder out = new StringBuilder("[");
        boolean first = true;
        for (Hero hero : dataManager.getHeroes()) {
            if (!first) {
                out.append(",");
            }
            first = false;
            out.append("{\"id\":").append(json(hero.getId()))
                    .append(",\"name\":").append(json(hero.getName()))
                    .append(",\"type\":").append(json(hero.getType().name()))
                    .append(",\"attack\":").append(hero.getAttack())
                    .append(",\"defense\":").append(hero.getDefense())
                    .append(",\"health\":").append(hero.getHealth())
                    .append(",\"difficulty\":").append(hero.getDifficulty())
                    .append(",\"compatibleEquipmentIds\":").append(stringListJson(hero.getCompatibleEquipmentIds()))
                    .append("}");
        }
        return out.append("]").toString();
    }

    private String equipmentJson() {
        StringBuilder out = new StringBuilder("[");
        boolean first = true;
        for (Equipment item : dataManager.getEquipment()) {
            if (!first) {
                out.append(",");
            }
            first = false;
            out.append("{\"id\":").append(json(item.getId()))
                    .append(",\"name\":").append(json(item.getName()))
                    .append(",\"type\":").append(json(item.getType().name()))
                    .append(",\"power\":").append(item.getPower())
                    .append(",\"defense\":").append(item.getDefense())
                    .append(",\"score\":").append(String.format(Locale.US, "%.2f", rankingService.equipmentScore(item)))
                    .append("}");
        }
        return out.append("]").toString();
    }

    private String matchesJson() {
        StringBuilder out = new StringBuilder("[");
        boolean first = true;
        for (MatchRecord match : dataManager.getMatchesNewestFirst()) {
            if (!first) {
                out.append(",");
            }
            first = false;
            out.append("{\"id\":").append(json(match.getId()))
                    .append(",\"date\":").append(json(match.getDate().toString()))
                    .append(",\"teamAId\":").append(json(match.getTeamAId()))
                    .append(",\"teamBId\":").append(json(match.getTeamBId()))
                    .append(",\"winnerTeamId\":").append(json(match.getWinnerTeamId()))
                    .append("}");
        }
        return out.append("]").toString();
    }

    private String leaderboardRowsJson(String mode, int limit) {
        StringBuilder out = new StringBuilder("[");
        int rank = 1;
        boolean first = true;
        for (Player player : rankingService.topPlayers(mode, limit)) {
            if (!first) {
                out.append(",");
            }
            first = false;
            Team team = dataManager.requireTeam(player.getTeamId());
            out.append("{\"rank\":").append(rank++)
                    .append(",\"id\":").append(json(player.getId()))
                    .append(",\"name\":").append(json(player.getName()))
                    .append(",\"team\":").append(json(team.getName()))
                    .append(",\"level\":").append(player.getLevel())
                    .append(",\"wins\":").append(player.getWins())
                    .append(",\"losses\":").append(player.getLosses())
                    .append(",\"matches\":").append(player.getTotalMatches())
                    .append(",\"winRate\":").append(String.format(Locale.US, "%.1f", player.getWinRate()))
                    .append(",\"score\":").append(String.format(Locale.US, "%.2f", rankingService.playerCustomScore(player)))
                    .append("}");
        }
        return out.append("]").toString();
    }

    private String equipmentRowsJson(int limit) {
        StringBuilder out = new StringBuilder("[");
        int rank = 1;
        boolean first = true;
        for (Equipment item : rankingService.topEquipment(limit)) {
            if (!first) {
                out.append(",");
            }
            first = false;
            out.append("{\"rank\":").append(rank++)
                    .append(",\"id\":").append(json(item.getId()))
                    .append(",\"name\":").append(json(item.getName()))
                    .append(",\"type\":").append(json(item.getType().name()))
                    .append(",\"power\":").append(item.getPower())
                    .append(",\"defense\":").append(item.getDefense())
                    .append(",\"usage\":").append(item.getUsageCount())
                    .append(",\"rating\":").append(String.format(Locale.US, "%.1f", item.getAverageRating()))
                    .append(",\"compatibleHeroes\":").append(dataManager.countCompatibleHeroes(item.getId()))
                    .append(",\"score\":").append(String.format(Locale.US, "%.2f", rankingService.equipmentScore(item)))
                    .append("}");
        }
        return out.append("]").toString();
    }

    private String matchRowsJson(String teamId, String playerId, List<MatchRecord> matches) {
        StringBuilder out = new StringBuilder("[");
        boolean first = true;
        for (MatchRecord match : matches) {
            if (!first) {
                out.append(",");
            }
            first = false;
            String opponentId = match.opponentForTeam(teamId);
            Team opponent = opponentId.isBlank() ? null : dataManager.requireTeam(opponentId);
            out.append("{\"date\":").append(json(match.getDate().toString()))
                    .append(",\"matchId\":").append(json(match.getId()))
                    .append(",\"opponent\":").append(json(opponent == null ? "Unknown" : opponent.getName()))
                    .append(",\"result\":").append(json(match.resultForTeam(teamId).name()));
            if (playerId == null) {
                out.append(",\"picks\":").append(json(heroPickText(match.getHeroPicks())));
            } else {
                out.append(",\"hero\":").append(json(heroName(match.getHeroPicks().get(playerId))));
            }
            out.append("}");
        }
        return out.append("]").toString();
    }

    private String playerSummaryJson(Player player) {
        return "{\"id\":" + json(player.getId())
                + ",\"name\":" + json(player.getName())
                + ",\"teamId\":" + json(player.getTeamId())
                + "}";
    }

    private String teamSummaryJson(Team team) {
        return "{\"id\":" + json(team.getId())
                + ",\"name\":" + json(team.getName())
                + "}";
    }

    private String heroPickText(Map<String, String> picks) {
        StringBuilder out = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> entry : picks.entrySet()) {
            if (!first) {
                out.append("; ");
            }
            first = false;
            Player player = dataManager.requirePlayer(entry.getKey());
            out.append(player.getName()).append(" -> ").append(heroName(entry.getValue()));
        }
        return out.toString();
    }

    private String heroName(String heroId) {
        if (heroId == null || heroId.isBlank()) {
            return "Unknown";
        }
        return dataManager.requireHero(heroId).getName();
    }

    private String userJson(Person user) {
        return "{\"id\":" + json(user.getId())
                + ",\"name\":" + json(user.getName())
                + ",\"username\":" + json(user.getUsername())
                + ",\"role\":" + json(user.getRole().name())
                + "}";
    }

    private String stringListJson(List<String> values) {
        StringBuilder out = new StringBuilder("[");
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) {
                out.append(",");
            }
            out.append(json(values.get(i)));
        }
        return out.append("]").toString();
    }

    private void handleStatic(HttpExchange exchange) throws IOException {
        if (!"GET".equals(exchange.getRequestMethod()) && !"HEAD".equals(exchange.getRequestMethod())) {
            sendBytes(exchange, 405, "text/plain; charset=utf-8", "Method not allowed".getBytes(StandardCharsets.UTF_8));
            return;
        }
        String requestPath = exchange.getRequestURI().getPath();
        if (requestPath.equals("/")) {
            requestPath = "/index.html";
        }
        Path file = webDir.resolve(requestPath.substring(1)).normalize();
        if (!file.startsWith(webDir) || !Files.isRegularFile(file)) {
            sendBytes(exchange, 404, "text/plain; charset=utf-8", "Not found".getBytes(StandardCharsets.UTF_8));
            return;
        }
        sendBytes(exchange, 200, contentType(file), Files.readAllBytes(file));
    }

    private void respondJson(HttpExchange exchange, int status, String body) throws IOException {
        Headers headers = exchange.getResponseHeaders();
        headers.set("Content-Type", "application/json; charset=utf-8");
        headers.set("Cache-Control", "no-store");
        sendBytes(exchange, status, "application/json; charset=utf-8", body.getBytes(StandardCharsets.UTF_8));
    }

    private void sendBytes(HttpExchange exchange, int status, String contentType, byte[] bytes) throws IOException {
        Headers headers = exchange.getResponseHeaders();
        headers.set("Content-Type", contentType);
        headers.set("Cache-Control", "no-store");
        exchange.sendResponseHeaders(status, bytes.length);
        try (var out = exchange.getResponseBody()) {
            out.write(bytes);
        }
    }

    private String query(HttpExchange exchange, String name, String fallback) {
        return parseParams(exchange.getRequestURI().getRawQuery()).getOrDefault(name, fallback).trim();
    }

    private int limit(HttpExchange exchange) {
        String value = query(exchange, "limit", "5");
        try {
            return Math.max(1, Math.min(20, Integer.parseInt(value)));
        } catch (NumberFormatException ex) {
            return 5;
        }
    }

    private boolean wantsStructuredJson(HttpExchange exchange) {
        return "json".equalsIgnoreCase(query(exchange, "format", ""));
    }

    private Map<String, String> form(HttpExchange exchange) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        return parseParams(body);
    }

    private Map<String, String> parseParams(String raw) {
        Map<String, String> params = new LinkedHashMap<>();
        if (raw == null || raw.isBlank()) {
            return params;
        }
        for (String pair : raw.split("&")) {
            int equals = pair.indexOf('=');
            String key = equals >= 0 ? pair.substring(0, equals) : pair;
            String value = equals >= 0 ? pair.substring(equals + 1) : "";
            params.put(decode(key), decode(value));
        }
        return params;
    }

    private String token(HttpExchange exchange) {
        String header = exchange.getRequestHeaders().getFirst("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring("Bearer ".length()).trim();
        }
        return "";
    }

    private String contentType(Path file) {
        String name = file.getFileName().toString().toLowerCase();
        if (name.endsWith(".html")) {
            return "text/html; charset=utf-8";
        }
        if (name.endsWith(".css")) {
            return "text/css; charset=utf-8";
        }
        if (name.endsWith(".js")) {
            return "text/javascript; charset=utf-8";
        }
        if (name.endsWith(".svg")) {
            return "image/svg+xml";
        }
        return "application/octet-stream";
    }

    private String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    private String ok(String fields) {
        return "{\"ok\":true" + (fields == null || fields.isBlank() ? "" : "," + fields) + "}";
    }

    private String fail(String message) {
        return "{\"ok\":false,\"message\":" + json(message == null ? "Unknown error" : message) + "}";
    }

    private String json(String value) {
        StringBuilder out = new StringBuilder("\"");
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            switch (ch) {
                case '\\' -> out.append("\\\\");
                case '"' -> out.append("\\\"");
                case '\n' -> out.append("\\n");
                case '\r' -> out.append("\\r");
                case '\t' -> out.append("\\t");
                default -> {
                    if (ch < 32) {
                        out.append(String.format("\\u%04x", (int) ch));
                    } else {
                        out.append(ch);
                    }
                }
            }
        }
        return out.append("\"").toString();
    }

    @FunctionalInterface
    private interface WebAction {
        void run() throws IOException;
    }
}
