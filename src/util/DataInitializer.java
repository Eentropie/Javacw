package util;

import enums.EquipmentType;
import enums.HeroType;
import model.Admin;
import model.Equipment;
import model.Hero;
import model.MatchRecord;
import model.Player;
import model.Team;
import service.GameDataManager;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class DataInitializer {
    private DataInitializer() {
    }

    public static GameDataManager createSampleData() {
        GameDataManager data = new GameDataManager();
        addAdmins(data);
        addTeams(data);
        addEquipment(data);
        addHeroes(data);
        addPlayers(data);
        addMatches(data);
        return data;
    }

    private static void addAdmins(GameDataManager data) {
        data.addAdmin(new Admin("A001", "System Admin", "admin", "admin123"));
    }

    private static void addTeams(GameDataManager data) {
        data.addTeam(new Team("T001", "Chang'an Blades", List.of()));
        data.addTeam(new Team("T002", "River Guardians", List.of()));
        data.addTeam(new Team("T003", "Cloud Arena", List.of()));
    }

    private static void addEquipment(GameDataManager data) {
        data.addEquipment(new Equipment("E001", "Shadow Blade", EquipmentType.ATTACK, 95, 0, 2140, 4.7, 86, 0.72));
        data.addEquipment(new Equipment("E002", "Endless Battle", EquipmentType.ATTACK, 110, 0, 2140, 4.8, 92, 0.78));
        data.addEquipment(new Equipment("E003", "Breaking Dawn", EquipmentType.ATTACK, 100, 0, 3400, 4.6, 61, 0.70));
        data.addEquipment(new Equipment("E004", "Arcane Boots", EquipmentType.MOVEMENT, 20, 0, 710, 4.1, 73, 0.52));
        data.addEquipment(new Equipment("E005", "Resistance Boots", EquipmentType.MOVEMENT, 0, 35, 710, 4.4, 95, 0.66));
        data.addEquipment(new Equipment("E006", "Book of Wisdom", EquipmentType.MAGIC, 130, 0, 2990, 4.7, 70, 0.71));
        data.addEquipment(new Equipment("E007", "Staff of Nuwa", EquipmentType.MAGIC, 115, 0, 2300, 4.5, 64, 0.62));
        data.addEquipment(new Equipment("E008", "Frost Staff", EquipmentType.MAGIC, 80, 20, 2100, 4.2, 54, 0.55));
        data.addEquipment(new Equipment("E009", "Guardian Armor", EquipmentType.DEFENSE, 0, 120, 2050, 4.3, 76, 0.63));
        data.addEquipment(new Equipment("E010", "Ominous Premonition", EquipmentType.DEFENSE, 0, 140, 2180, 4.6, 83, 0.68));
        data.addEquipment(new Equipment("E011", "Red Lotus Cloak", EquipmentType.DEFENSE, 40, 110, 1830, 4.0, 58, 0.49));
        data.addEquipment(new Equipment("E012", "Sage Sanctuary", EquipmentType.DEFENSE, 0, 90, 2080, 4.8, 67, 0.75));
        data.addEquipment(new Equipment("E013", "Jungle Blade", EquipmentType.JUNGLE, 75, 0, 1500, 4.1, 47, 0.50));
        data.addEquipment(new Equipment("E014", "Patrol Axe", EquipmentType.JUNGLE, 45, 60, 1650, 4.0, 39, 0.44));
        data.addEquipment(new Equipment("E015", "Support Gem", EquipmentType.SUPPORT, 0, 30, 1200, 4.2, 51, 0.56));
        data.addEquipment(new Equipment("E016", "Guardian Medal", EquipmentType.SUPPORT, 0, 80, 1750, 4.4, 46, 0.61));
        data.addEquipment(new Equipment("E017", "Bloodweeper", EquipmentType.ATTACK, 100, 0, 1740, 4.5, 65, 0.64));
        data.addEquipment(new Equipment("E018", "Starbreaker", EquipmentType.ATTACK, 85, 0, 2060, 4.3, 59, 0.58));
        data.addEquipment(new Equipment("E019", "Holy Grail", EquipmentType.MAGIC, 90, 35, 1800, 4.2, 44, 0.53));
        data.addEquipment(new Equipment("E020", "Swift Boots", EquipmentType.MOVEMENT, 10, 0, 710, 4.0, 69, 0.48));
    }

    private static void addHeroes(GameDataManager data) {
        data.addHero(new Hero("H001", "Li Bai", HeroType.ASSASSIN, 170, 78, 3100, 8, list("E001", "E002", "E013", "E017"), list("E002", "E017")));
        data.addHero(new Hero("H002", "Diaochan", HeroType.MAGE, 155, 70, 3000, 8, list("E004", "E006", "E007", "E019"), list("E006", "E019")));
        data.addHero(new Hero("H003", "Zhao Yun", HeroType.WARRIOR, 165, 95, 3400, 6, list("E001", "E005", "E009", "E017"), list("E005", "E017")));
        data.addHero(new Hero("H004", "Luban No.7", HeroType.MARKSMAN, 180, 60, 2850, 5, list("E002", "E003", "E017", "E020"), list("E003", "E020")));
        data.addHero(new Hero("H005", "Zhang Fei", HeroType.TANK, 120, 145, 4100, 5, list("E009", "E010", "E011", "E016"), list("E010", "E016")));
        data.addHero(new Hero("H006", "Yao", HeroType.SUPPORT, 95, 85, 2950, 4, list("E005", "E015", "E016", "E019"), list("E015", "E016")));
        data.addHero(new Hero("H007", "Sun Wukong", HeroType.ASSASSIN, 175, 75, 3150, 7, list("E001", "E002", "E013", "E018"), list("E002", "E018")));
        data.addHero(new Hero("H008", "Angela", HeroType.MAGE, 160, 65, 2850, 4, list("E004", "E006", "E007", "E008"), list("E006", "E008")));
        data.addHero(new Hero("H009", "Arthur", HeroType.WARRIOR, 150, 110, 3650, 3, list("E005", "E009", "E010", "E011"), list("E005", "E010")));
        data.addHero(new Hero("H010", "Hou Yi", HeroType.MARKSMAN, 178, 58, 2800, 4, list("E002", "E003", "E017", "E020"), list("E003", "E017")));
        data.addHero(new Hero("H011", "Cai Wenji", HeroType.SUPPORT, 90, 80, 2900, 3, list("E015", "E016", "E019", "E005"), list("E015", "E019")));
        data.addHero(new Hero("H012", "Lian Po", HeroType.TANK, 125, 150, 4300, 4, list("E009", "E010", "E011", "E012"), list("E010", "E012")));
        data.addHero(new Hero("H013", "Mulan", HeroType.WARRIOR, 168, 92, 3300, 7, list("E001", "E005", "E017", "E018"), list("E005", "E018")));
        data.addHero(new Hero("H014", "Marco Polo", HeroType.MARKSMAN, 176, 62, 2860, 7, list("E003", "E017", "E018", "E020"), list("E003", "E020")));
        data.addHero(new Hero("H015", "Luna", HeroType.MAGE, 162, 72, 3050, 9, list("E004", "E006", "E007", "E013"), list("E006", "E013")));
    }

    private static void addPlayers(GameDataManager data) {
        addPlayer(data, "P001", "Li Bai", "libai", "T001", 28, 32, 12, list("H001", "H007", "H013"));
        addPlayer(data, "P002", "Chen Qian", "chenq", "T001", 24, 25, 15, list("H002", "H008", "H015"));
        addPlayer(data, "P003", "Zhao Yun", "zhaoy", "T001", 27, 30, 18, list("H003", "H009", "H013"));
        addPlayer(data, "P004", "Lu Ban", "luban", "T001", 22, 19, 17, list("H004", "H010", "H014"));
        addPlayer(data, "P005", "Zhang Fei", "zhangf", "T001", 26, 29, 16, list("H005", "H012", "H006"));
        addPlayer(data, "P006", "Xiao Qiao", "xiaoq", "T002", 25, 28, 14, list("H002", "H008", "H011"));
        addPlayer(data, "P007", "Sun Ce", "sunce", "T002", 23, 20, 20, list("H003", "H009", "H013"));
        addPlayer(data, "P008", "Hou Yi", "houyi", "T002", 29, 35, 13, list("H010", "H004", "H014"));
        addPlayer(data, "P009", "Cai Wenji", "caiw", "T002", 21, 17, 19, list("H011", "H006", "H005"));
        addPlayer(data, "P010", "Lian Po", "lianp", "T002", 24, 22, 18, list("H012", "H005", "H009"));
        addPlayer(data, "P011", "Mulan", "mulan", "T003", 30, 38, 11, list("H013", "H001", "H003"));
        addPlayer(data, "P012", "Marco Ace", "marco", "T003", 27, 31, 16, list("H014", "H004", "H010"));
        addPlayer(data, "P013", "Luna Star", "luna", "T003", 26, 27, 15, list("H015", "H002", "H008"));
        addPlayer(data, "P014", "Yao Support", "yao", "T003", 22, 21, 18, list("H006", "H011", "H005"));
        addPlayer(data, "P015", "Arthur King", "arthur", "T003", 25, 26, 17, list("H009", "H003", "H012"));
    }

    private static void addMatches(GameDataManager data) {
        data.addMatchRecord(new MatchRecord("M001", LocalDate.of(2026, 5, 1), "T001", "T002", "T001", picks("P001:H001", "P002:H002", "P003:H003", "P004:H004", "P005:H005", "P006:H008", "P007:H009", "P008:H010", "P009:H011", "P010:H012")));
        data.addMatchRecord(new MatchRecord("M002", LocalDate.of(2026, 5, 3), "T002", "T003", "T003", picks("P006:H002", "P007:H013", "P008:H010", "P009:H006", "P010:H005", "P011:H001", "P012:H014", "P013:H015", "P014:H011", "P015:H009")));
        data.addMatchRecord(new MatchRecord("M003", LocalDate.of(2026, 5, 6), "T001", "T003", "T003", picks("P001:H007", "P002:H015", "P003:H009", "P004:H010", "P005:H012", "P011:H013", "P012:H004", "P013:H002", "P014:H006", "P015:H003")));
        data.addMatchRecord(new MatchRecord("M004", LocalDate.of(2026, 5, 9), "T001", "T002", "T002", picks("P001:H013", "P002:H008", "P003:H003", "P004:H014", "P005:H006", "P006:H011", "P007:H009", "P008:H010", "P009:H005", "P010:H012")));
        data.addMatchRecord(new MatchRecord("M005", LocalDate.of(2026, 5, 12), "T002", "T003", "T002", picks("P006:H008", "P007:H013", "P008:H004", "P009:H011", "P010:H012", "P011:H003", "P012:H010", "P013:H015", "P014:H006", "P015:H009")));
        data.addMatchRecord(new MatchRecord("M006", LocalDate.of(2026, 5, 15), "T001", "T003", "T001", picks("P001:H001", "P002:H002", "P003:H013", "P004:H004", "P005:H005", "P011:H003", "P012:H014", "P013:H008", "P014:H011", "P015:H012")));
        data.addMatchRecord(new MatchRecord("M007", LocalDate.of(2026, 5, 18), "T001", "T002", "T001", picks("P001:H007", "P002:H015", "P003:H003", "P004:H010", "P005:H012", "P006:H002", "P007:H013", "P008:H014", "P009:H006", "P010:H005")));
        data.addMatchRecord(new MatchRecord("M008", LocalDate.of(2026, 5, 21), "T002", "T003", "T003", picks("P006:H011", "P007:H003", "P008:H010", "P009:H005", "P010:H009", "P011:H001", "P012:H004", "P013:H002", "P014:H006", "P015:H012")));
        data.addMatchRecord(new MatchRecord("M009", LocalDate.of(2026, 5, 24), "T001", "T003", "T003", picks("P001:H013", "P002:H008", "P003:H009", "P004:H014", "P005:H005", "P011:H001", "P012:H010", "P013:H015", "P014:H011", "P015:H003")));
        data.addMatchRecord(new MatchRecord("M010", LocalDate.of(2026, 5, 28), "T001", "T002", "T002", picks("P001:H001", "P002:H002", "P003:H003", "P004:H004", "P005:H012", "P006:H008", "P007:H009", "P008:H010", "P009:H011", "P010:H005")));
        data.addMatchRecord(new MatchRecord("M011", LocalDate.of(2026, 5, 31), "T002", "T003", "T003", picks("P006:H002", "P007:H013", "P008:H014", "P009:H006", "P010:H012", "P011:H001", "P012:H010", "P013:H015", "P014:H011", "P015:H009")));
    }

    private static List<String> list(String... values) {
        return List.of(values);
    }

    private static void addPlayer(GameDataManager data, String id, String name, String username,
                                  String teamId, int level, int wins, int losses, List<String> heroIds) {
        data.addPlayer(new Player(
                id,
                name,
                username,
                "player123",
                teamId,
                level,
                wins,
                losses,
                heroIds,
                data.defaultEquipmentLoadouts(heroIds)));
    }

    private static Map<String, String> picks(String... entries) {
        Map<String, String> picks = new LinkedHashMap<>();
        for (String entry : entries) {
            String[] parts = entry.split(":");
            picks.put(parts[0], parts[1]);
        }
        return picks;
    }
}
