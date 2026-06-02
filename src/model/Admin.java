package model;

import enums.Role;

public class Admin extends Person {
    public Admin(String id, String name, String username, String password) {
        super(id, name, username, password, Role.ADMIN);
    }
}
