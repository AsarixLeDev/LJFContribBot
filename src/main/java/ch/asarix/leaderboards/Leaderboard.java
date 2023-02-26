package ch.asarix.leaderboards;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Leaderboard {
    private final Map<UUID, Integer> users = new HashMap<>();

    public void addUser(UUID uuid, int place) {
        users.put(uuid, place);
    }

    public int getPlace(UUID uuid) {
        if (!users.containsKey(uuid)) return -1;
        return users.get(uuid);
    }

    public Map<UUID, Integer> getUsers() {
        return users;
    }
}
