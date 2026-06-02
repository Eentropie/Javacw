package model;

import contract.Searchable;
import enums.Role;

public abstract class Person implements Searchable {
    private final String id;
    private String name;
    private final String username;
    private String password;
    private final Role role;

    protected Person(String id, String name, String username, String password, Role role) {
        this.id = requireText(id, "id");
        this.name = requireText(name, "name");
        this.username = requireText(username, "username");
        this.password = requireText(password, "password");
        this.role = role;
    }

    protected static String requireText(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " cannot be blank");
        }
        return value.trim();
    }

    protected static int requireNonNegative(int value, String fieldName) {
        if (value < 0) {
            throw new IllegalArgumentException(fieldName + " cannot be negative");
        }
        return value;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = requireText(name, "name");
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = requireText(password, "password");
    }

    public Role getRole() {
        return role;
    }

    public boolean checkPassword(String password) {
        return this.password.equals(password);
    }

    @Override
    public boolean matches(String query) {
        if (query == null) {
            return false;
        }
        String normalized = query.trim().toLowerCase();
        return id.toLowerCase().contains(normalized)
                || name.toLowerCase().contains(normalized)
                || username.toLowerCase().contains(normalized);
    }

    @Override
    public String toString() {
        return id + " - " + name + " (" + role + ")";
    }
}
