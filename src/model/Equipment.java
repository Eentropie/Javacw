package model;

import contract.Searchable;
import enums.EquipmentType;

public class Equipment implements Searchable {
    private final String id;
    private String name;
    private EquipmentType type;
    private int power;
    private int defense;
    private int price;
    private double averageRating;
    private int usageCount;
    private double winContribution;

    public Equipment(String id, String name, EquipmentType type, int power, int defense,
                     int price, double averageRating, int usageCount, double winContribution) {
        this.id = Person.requireText(id, "id");
        this.name = Person.requireText(name, "name");
        this.type = type;
        this.power = Person.requireNonNegative(power, "power");
        this.defense = Person.requireNonNegative(defense, "defense");
        this.price = Person.requireNonNegative(price, "price");
        this.averageRating = requireAverageRating(averageRating);
        this.usageCount = Person.requireNonNegative(usageCount, "usageCount");
        this.winContribution = requireWinContribution(winContribution);
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

    public EquipmentType getType() {
        return type;
    }

    public void setType(EquipmentType type) {
        this.type = type;
    }

    public int getPower() {
        return power;
    }

    public void setPower(int power) {
        this.power = Person.requireNonNegative(power, "power");
    }

    public int getDefense() {
        return defense;
    }

    public void setDefense(int defense) {
        this.defense = Person.requireNonNegative(defense, "defense");
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = Person.requireNonNegative(price, "price");
    }

    public double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(double averageRating) {
        this.averageRating = requireAverageRating(averageRating);
    }

    private static double requireAverageRating(double averageRating) {
        if (averageRating < 0.0 || averageRating > 5.0) {
            throw new IllegalArgumentException("averageRating must be between 0 and 5");
        }
        return averageRating;
    }

    public int getUsageCount() {
        return usageCount;
    }

    public void setUsageCount(int usageCount) {
        this.usageCount = Person.requireNonNegative(usageCount, "usageCount");
    }

    public double getWinContribution() {
        return winContribution;
    }

    public void setWinContribution(double winContribution) {
        this.winContribution = requireWinContribution(winContribution);
    }

    private static double requireWinContribution(double winContribution) {
        if (winContribution < 0.0 || winContribution > 1.0) {
            throw new IllegalArgumentException("winContribution must be between 0 and 1");
        }
        return winContribution;
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
