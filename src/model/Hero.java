package model;

import contract.Searchable;
import enums.HeroType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Hero implements Searchable {
    private final String id;
    private String name;
    private HeroType type;
    private int attack;
    private int defense;
    private int health;
    private int difficulty;
    private final List<String> compatibleEquipmentIds;
    private final List<String> recommendedEquipmentIds;

    public Hero(String id, String name, HeroType type, int attack, int defense, int health,
                int difficulty, List<String> compatibleEquipmentIds, List<String> recommendedEquipmentIds) {
        this.id = Person.requireText(id, "id");
        this.name = Person.requireText(name, "name");
        this.type = type;
        this.attack = Person.requireNonNegative(attack, "attack");
        this.defense = Person.requireNonNegative(defense, "defense");
        this.health = Person.requireNonNegative(health, "health");
        setDifficulty(difficulty);
        this.compatibleEquipmentIds = new ArrayList<>();
        this.recommendedEquipmentIds = new ArrayList<>();
        if (compatibleEquipmentIds != null) {
            compatibleEquipmentIds.forEach(this::addCompatibleEquipment);
        }
        if (recommendedEquipmentIds != null) {
            recommendedEquipmentIds.forEach(this::addRecommendedEquipment);
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

    public HeroType getType() {
        return type;
    }

    public void setType(HeroType type) {
        this.type = type;
    }

    public int getAttack() {
        return attack;
    }

    public void setAttack(int attack) {
        this.attack = Person.requireNonNegative(attack, "attack");
    }

    public int getDefense() {
        return defense;
    }

    public void setDefense(int defense) {
        this.defense = Person.requireNonNegative(defense, "defense");
    }

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = Person.requireNonNegative(health, "health");
    }

    public int getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(int difficulty) {
        if (difficulty < 1 || difficulty > 10) {
            throw new IllegalArgumentException("difficulty must be between 1 and 10");
        }
        this.difficulty = difficulty;
    }

    public List<String> getCompatibleEquipmentIds() {
        return Collections.unmodifiableList(compatibleEquipmentIds);
    }

    public List<String> getRecommendedEquipmentIds() {
        return Collections.unmodifiableList(recommendedEquipmentIds);
    }

    public void addCompatibleEquipment(String equipmentId) {
        String value = Person.requireText(equipmentId, "equipmentId");
        if (!compatibleEquipmentIds.contains(value)) {
            compatibleEquipmentIds.add(value);
        }
    }

    public void addRecommendedEquipment(String equipmentId) {
        String value = Person.requireText(equipmentId, "equipmentId");
        if (!recommendedEquipmentIds.contains(value)) {
            recommendedEquipmentIds.add(value);
        }
    }

    public boolean removeEquipment(String equipmentId) {
        boolean removed = compatibleEquipmentIds.remove(equipmentId);
        return recommendedEquipmentIds.remove(equipmentId) || removed;
    }

    public void replaceCompatibleEquipment(List<String> equipmentIds) {
        compatibleEquipmentIds.clear();
        if (equipmentIds != null) {
            equipmentIds.forEach(this::addCompatibleEquipment);
        }
    }

    public void replaceRecommendedEquipment(List<String> equipmentIds) {
        recommendedEquipmentIds.clear();
        if (equipmentIds != null) {
            equipmentIds.forEach(this::addRecommendedEquipment);
        }
    }

    @Override
    public boolean matches(String query) {
        if (query == null) {
            return false;
        }
        String normalized = query.trim().toLowerCase();
        return id.toLowerCase().contains(normalized)
                || name.toLowerCase().contains(normalized)
                || type.name().toLowerCase().contains(normalized);
    }

    @Override
    public String toString() {
        return id + " - " + name + " (" + type + ")";
    }
}
