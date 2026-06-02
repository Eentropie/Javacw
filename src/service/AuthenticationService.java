package service;

import model.Admin;
import model.Person;
import model.Player;

import java.util.Optional;

public class AuthenticationService {
    private final GameDataManager dataManager;

    public AuthenticationService(GameDataManager dataManager) {
        this.dataManager = dataManager;
    }

    public Optional<Person> login(String username, String password) {
        for (Admin admin : dataManager.getAdmins()) {
            if (admin.getUsername().equals(username) && admin.checkPassword(password)) {
                return Optional.of(admin);
            }
        }
        for (Player player : dataManager.getPlayers()) {
            if (player.getUsername().equals(username) && player.checkPassword(password)) {
                return Optional.of(player);
            }
        }
        return Optional.empty();
    }
}
