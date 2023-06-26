package ch.asarix;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import net.hypixel.api.HypixelAPI;
import net.hypixel.api.apache.ApacheHttpClient;
import net.hypixel.api.reply.GuildReply;
import net.hypixel.api.reply.PlayerReply;
import net.hypixel.api.reply.skyblock.SkyBlockProfilesReply;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.locks.ReentrantLock;

public class APIManager extends DataManager {

    private static final APIManager instance = new APIManager();

    private final CustomLock lock = new CustomLock();

    private final Map<HypixelAPI, Map.Entry<Integer, Long>> apis = new HashMap<>();

    public APIManager() {
        super("apis.json");
    }

    public static APIManager get() {
        return instance;
    }

    @Override
    public void store(String key, JsonNode node) {
        ArrayNode apiKeys = (ArrayNode) node;
        for (JsonNode apiNode : apiKeys) {
            String apiKey = System.getProperty("apiKey", apiNode.asText());
            HypixelAPI API = new HypixelAPI(new ApacheHttpClient(UUID.fromString(apiKey)));
            apis.put(API, new AbstractMap.SimpleEntry<>(0, System.currentTimeMillis()));
        }
    }

    @Override
    public void clearData() {
        apis.clear();
    }

    @Deprecated
    public HypixelAPI getApi() {
        for (HypixelAPI api : apis.keySet()) {
            Map.Entry<Integer, Long> usageState = apis.get(api);
            long deltaMillis = System.currentTimeMillis() - usageState.getValue();
            if ((deltaMillis > 60000) || (usageState.getKey() == 0)) {
                apis.put(api, new AbstractMap.SimpleEntry<>(1, System.currentTimeMillis()));
                return api;
            }
            if (usageState.getKey() < 100) {
                apis.put(api, new AbstractMap.SimpleEntry<>(usageState.getKey() + 1, usageState.getValue()));
                return api;
            }
        }
        return getNextAvailableApi();
    }

    public PlayerReply getPlayerByUuid(UUID uuid) {
        List<HypixelAPI> bestApis = apis.keySet().stream().sorted(Comparator.comparingInt(api -> apis.get(api).getKey())).toList();
        for (HypixelAPI api : bestApis) {
            try {
                return api.getPlayerByUuid(uuid).get();
            } catch (ExecutionException | InterruptedException ignored) {
            }
        }
        try {
            return getNextAvailableApi().getPlayerByUuid(uuid).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            throw new RuntimeException("Il y a eu un problème en récupérant l'instance du joueur : " + uuid.toString());
        }
    }

    public GuildReply getGuildByPlayer(UUID uuid) {
        List<HypixelAPI> bestApis = apis.keySet().stream().sorted(Comparator.comparingInt(api -> apis.get(api).getKey())).toList();
        for (HypixelAPI api : bestApis) {
            try {
                return api.getGuildByPlayer(uuid).get();
            } catch (ExecutionException | InterruptedException ignored) {
            }
        }
        try {
            return getNextAvailableApi().getGuildByPlayer(uuid).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            throw new RuntimeException("Il y a eu un problème en récupérant la guild du joueur : " + uuid.toString());
        }
    }

    public SkyBlockProfilesReply getSkyBlockProfiles(UUID uuid) {
        if (uuid == null) {
            System.err.println("Tried to fetch profiles from null uuid");
            return null;
        }
        List<HypixelAPI> bestApis = apis.keySet().stream().sorted(Comparator.comparingInt(api -> apis.get(api).getKey())).toList();
        for (HypixelAPI api : bestApis) {
            try {
                return api.getSkyBlockProfiles(uuid).get();
            } catch (ExecutionException | InterruptedException ignored) {
            }
        }
        try {
            return getNextAvailableApi().getSkyBlockProfiles(uuid).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            throw new RuntimeException("Il y a eu un problème en récupérant le profile du joueur : " + uuid.toString());
        }
    }

    private HypixelAPI getNextAvailableApi() {
        for (int i = 0; i < 20; i++) {
            List<HypixelAPI> bestApis = apis.keySet().stream().sorted(Comparator.comparingInt(api -> apis.get(api).getKey())).toList();
            for (HypixelAPI api : bestApis) {
                try {
                    api.getPlayerByUuid("8177cfe8-3a1f-4ac8-86b2-8e19dff1c156").get();
                    return api;
                } catch (ExecutionException | InterruptedException ignored) {
                }
            }
            try {
                lock.lock();
                try {
                    Thread ownerThread = getOwnerThread();
                    System.out.println("Owner thread: " + ownerThread.getName());
                    ownerThread.wait(10_000);
                } finally {
                    lock.unlock();
                }
            } catch (InterruptedException ignored) {
            }
        }
        throw new RuntimeException("La récupération d'une clé d'api valable a pris trop de temps !");
    }

    private Thread getOwnerThread() {
        return lock.getOwnerThread();
    }

    public static class CustomLock extends ReentrantLock {
        public Thread getOwnerThread() {
            return super.getOwner();
        }
    }
}
