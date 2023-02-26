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

public class APIManager extends DataManager {

    private static final APIManager instance = new APIManager();

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
        throw new RuntimeException("L'api est trop surchargée ! Veuillez réessayer plus tard.");
    }

    public PlayerReply getPlayerByUuid(UUID uuid) {
        List<HypixelAPI> bestApis = apis.keySet().stream().sorted(Comparator.comparingInt(api -> apis.get(api).getKey())).toList();
        for (HypixelAPI api : bestApis) {
            try {
                return api.getPlayerByUuid(uuid).get();
            } catch (ExecutionException | InterruptedException ignored) {
            }
        }
        throw new RuntimeException("L'api est trop surchargée ! Veuillez réessayer plus tard.");
    }

    public GuildReply getGuildByPlayer(UUID uuid) {
        List<HypixelAPI> bestApis = apis.keySet().stream().sorted(Comparator.comparingInt(api -> apis.get(api).getKey())).toList();
        for (HypixelAPI api : bestApis) {
            try {
                return api.getGuildByPlayer(uuid).get();
            } catch (ExecutionException | InterruptedException ignored) {
            }
        }
        throw new RuntimeException("L'api est trop surchargée ! Veuillez réessayer plus tard.");
    }

    public SkyBlockProfilesReply getSkyBlockProfiles(UUID uuid) {
        List<HypixelAPI> bestApis = apis.keySet().stream().sorted(Comparator.comparingInt(api -> apis.get(api).getKey())).toList();
        for (HypixelAPI api : bestApis) {
            try {
                return api.getSkyBlockProfiles(uuid).get();
            } catch (ExecutionException | InterruptedException ignored) {
            }
        }
        throw new RuntimeException("L'api est trop surchargée ! Veuillez réessayer plus tard.");
    }
}
