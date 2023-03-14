package ch.asarix.stats;

import ch.asarix.APIManager;
import ch.asarix.stats.types.DungeonType;
import ch.asarix.stats.types.Misc;
import ch.asarix.stats.types.Skill;
import ch.asarix.stats.types.Slayer;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.hypixel.api.reply.skyblock.SkyBlockProfilesReply;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class StatsManager {
    private static final StatsManager instance = new StatsManager();
    public final List<Stats> statsList = new LinkedList<>();

    public static StatsManager get() {
        return instance;
    }

    public Stat fromName(String name) {
        try {
            return DungeonType.valueOf(name.toUpperCase());
        } catch (Exception ignored) {
        }
        try {
            return Misc.valueOf(name.toUpperCase());
        } catch (Exception ignored) {
        }
        try {
            return Skill.valueOf(name.toUpperCase());
        } catch (Exception ignored) {
        }
        try {
            return Slayer.valueOf(name.toUpperCase());
        } catch (Exception ignored) {
        }
        return null;
    }

    public void addStats(Stats stats) {
        Stats cachedStat = getCachedStats(stats.getUuid());
        if (cachedStat != null) statsList.remove(cachedStat);
        statsList.add(stats);
    }

    public Stats getCachedStats(UUID uuid) {
        for (Stats stats : statsList) {
            if (stats.getUuid() == uuid) {
                int FIVE_MINUTES = 30_000_000;
                if (System.currentTimeMillis() - stats.fetchedAtMillis < FIVE_MINUTES) {
                    return stats;
                }
            }
        }
        return null;
    }

    public Stats getStats(UUID uuid) {
        System.err.println("Looking for already existing stat...");
        Stats stats = getCachedStats(uuid);
        if (stats != null) return stats;
        System.err.println("Not found");
        System.err.println("Getting latest profile...");
        JsonObject profile = getLatestProfile(uuid);
        System.err.println("Done.");
        return getStats(uuid, profile);
    }

    public JsonObject getLatestProfile(UUID uuid) {
        SkyBlockProfilesReply reply = APIManager.get().getSkyBlockProfiles(uuid);
        JsonArray array = reply.getProfiles();
        JsonObject latestProfile = null;
        long latest_save = 0;
        for (int i = 0; i < array.size(); i++) {
            JsonObject profile = array.get(i).getAsJsonObject();
            if (!profile.has("last_save")) continue;
            long lastSave = profile.get("last_save").getAsLong();
            if (lastSave > latest_save) {
                latestProfile = profile;
                latest_save = lastSave;
            }
        }
        return latestProfile;
    }

    public Stats getStats(UUID uuid, JsonObject profile) {
        System.err.println("Fetching from hypixel API...");
        Stats stats = new Stats(uuid);
        JsonObject members = profile.get("members").getAsJsonObject();
        String fUuid = uuid.toString().replace("-", "");
        JsonObject pProfile = members.get(fUuid).getAsJsonObject();

        for (String key : pProfile.keySet()) {
            JsonElement element = pProfile.get(key);
            if (key.startsWith("experience_skill_")) {
                String skillName = key.replace("experience_skill_", "");
                long xp = element.getAsLong();
                stats.addStat(Skill.valueOf(skillName.toUpperCase()), xp);
            } else if (key.equals("slayer_bosses")) {
                JsonObject value = element.getAsJsonObject();
                for (String slayerName : value.keySet()) {
                    JsonElement slayerData = value.get(slayerName);
                    if (slayerData == null) continue;
                    JsonElement xpData = slayerData.getAsJsonObject().get("xp");
                    if (xpData == null) continue;
                    long xp = xpData.getAsLong();
                    stats.addStat(Slayer.valueOf(slayerName.toUpperCase()), xp);
                }
            } else if (key.equals("dungeons")) {
                JsonObject value = element.getAsJsonObject();
                for (String dungKey : value.keySet()) {
                    if (dungKey.equals("player_classes")) {
                        JsonObject classes = value.getAsJsonObject(dungKey);
                        for (Map.Entry<String, JsonElement> entry : classes.entrySet()) {
                            String name = entry.getKey();
                            JsonElement expObj = entry.getValue().getAsJsonObject().get("experience");
                            if (expObj == null) continue;
                            long xp = expObj.getAsLong();
                            stats.addStat(DungeonType.valueOf(name.toUpperCase()), xp);
                        }
                    } else if (dungKey.equals("dungeon_types")) {
                        JsonObject types = value.getAsJsonObject("dungeon_types");
                        JsonObject cata = types.getAsJsonObject("catacombs");
                        JsonElement xpElement = cata.get("experience");
                        if (xpElement == null) {
                            stats.addStat(DungeonType.CATACOMBS, 0);
                            continue;
                        }
                        long xp = xpElement.getAsLong();
                        stats.addStat(DungeonType.CATACOMBS, xp);
                    }
                }
            } else if (key.equals("leveling")) {
                JsonElement experience = element.getAsJsonObject().get("experience");
                if (experience != null) {
                    stats.addStat(Misc.SB_LEVEL, experience.getAsLong());
                }
            }
        }
        addStats(stats);
        return stats;
    }
}
