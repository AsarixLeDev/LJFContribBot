package ch.asarix.leaderboards;

import ch.asarix.stats.Stat;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Leaderboard {
    private final Map<UUID, Integer> users = new HashMap<>();
    private final Map<UUID, Boolean> roleGiven = new HashMap<>();
    private final Stat stat;

    public Leaderboard(Stat stat) {
        this.stat = stat;
    }

    public void addUser(UUID uuid, int place) {
        users.put(uuid, place);
        roleGiven.put(uuid, false);
    }

    public int getPlace(UUID uuid) {
        if (!users.containsKey(uuid)) return 7;
        return users.get(uuid);
    }

    public Map<UUID, Integer> getUsers() {
        return users;
    }

    public UUID getUser(int place) {
        for (UUID uuid : users.keySet()) {
            if (getPlace(uuid) == place)
                return uuid;
        }
        return null;
    }

    public Stat getStat() {
        return stat;
    }

    public boolean roleGiven(UUID uuid) {
        return roleGiven.get(uuid);
    }

    public void setRoleGiven(UUID uuid, boolean flag) {
        if (!roleGiven.containsKey(uuid)) return;
        roleGiven.put(uuid, flag);
    }
}
