package ch.asarix.leaderboards;

import ch.asarix.DataManager;
import ch.asarix.JsonUtil;
import ch.asarix.UUIDManager;
import ch.asarix.Util;
import ch.asarix.commands.MessageContent;
import ch.asarix.stats.Stat;
import ch.asarix.stats.Stats;
import ch.asarix.stats.StatsManager;
import ch.asarix.stats.types.Misc;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import net.dv8tion.jda.api.EmbedBuilder;

import java.text.NumberFormat;
import java.util.*;

public class LeaderboardManager extends DataManager {

    private static final LeaderboardManager instance = new LeaderboardManager();
    public Map<Stat, Map<Long, Leaderboard>> map = new HashMap<>();

    public LeaderboardManager() {
        super("leaderboards.json");
    }

    public static LeaderboardManager get() {
        return instance;
    }

    @Override
    public void store(String key, JsonNode node) {
        Stat stat = StatsManager.get().fromName(key);
        if (stat == null) {
            System.err.println("Could not get stat from name " + key);
            return;
        }
        Iterator<Map.Entry<String, JsonNode>> it = node.fields();
        while (it.hasNext()) {
            Map.Entry<String, JsonNode> entry = it.next();
            long timeMillis = Long.parseLong(entry.getKey());
            Leaderboard leaderboard = new Leaderboard();
            Iterator<Map.Entry<String, JsonNode>> userIt = entry.getValue().fields();
            while (userIt.hasNext()) {
                Map.Entry<String, JsonNode> userEntry = userIt.next();
                UUID uuid = UUID.fromString(userEntry.getKey());
                int place = userEntry.getValue().asInt();
                leaderboard.addUser(uuid, place);
            }
            save(stat, leaderboard, timeMillis);
        }
    }

    @Override
    public void clearData() {
        map.clear();
    }

    public String getRelEmoji(UUID uuid, Stat stat, int i) {
        Leaderboard latest = getLatestLeaderboard(stat);
        if (latest == null) return "\uD83D\uDD36";
        int lastPlace = latest.getPlace(uuid);
        if (lastPlace == i) return "\uD83D\uDD36";
        if (lastPlace < i) return "\uD83D\uDD3A";
        return "\uD83D\uDD3B";
    }

    private String positionEmoji(int i) {
        if (i == 1) return "🥇";
        if (i == 2) return "\uD83E\uDD48";
        if (i == 3) return "\uD83E\uDD49";
        if (i == 4) return "\uD83E\uDD0E";
        return "\uD83D\uDDA4";
    }

    public Leaderboard getLatestLeaderboard(Stat stat) {
        long latestTimeMillis = 0;
        Leaderboard latestLeaderboard = null;
        Map<Long, Leaderboard> leaderboards = map.get(stat);
        if (leaderboards == null) return null;
        for (Long time : leaderboards.keySet()) {
            if (time <= latestTimeMillis) continue;
            latestTimeMillis = time;
            latestLeaderboard = leaderboards.get(time);
        }
        return latestLeaderboard;
    }

    public MessageContent leaderboardMessage(Stat stat, List<Stats> stats) {
        EmbedBuilder builder = new EmbedBuilder()
                .setTitle(stat.niceName());
        List<Stats> top = stats.stream()
                .sorted(Comparator.comparingLong(s -> ((Stats) s).getTotalXp(stat)).reversed())
                .toList();

        StringBuilder builder1 = new StringBuilder();
        Leaderboard leaderboard = new Leaderboard();
        int i = 1;
        long prevXp = 0;
        int prevIndex = i;
        for (Stats stats1 : top.subList(0, Math.min(5, top.size()))) {
            double level = stats1.getLevelWithProgress(stat);
            long xp = stats1.getTotalXp(stat);
            int index = i++;
            if (prevXp == xp) index = prevIndex;
            UUID uuid = stats1.getUuid();
            leaderboard.addUser(uuid, index);
            String userName = UUIDManager.get().getName(uuid);
            String relEmoji = LeaderboardManager.get().getRelEmoji(uuid, stat, index);
            String line = "`" + relEmoji + positionEmoji(index) + "` #" + index;
            if (stat instanceof Misc) {
                line += " **[" + Util.round(level, 2) + "]**";
            } else {
                NumberFormat nf = NumberFormat.getInstance(new Locale("en", "US"));
                line += " [" + Util.round(level, 2) + "] **[" + nf.format(xp) + "]**";

            }
            line += " " + userName;
            builder1.append(line).append("\n");
            prevXp = xp;
            prevIndex = index;
        }
        builder.setDescription(builder1);
        save(stat, leaderboard);
        return new MessageContent(builder);
    }

    public void save(Stat stat, Leaderboard leaderboard) {
        save(stat, leaderboard, System.currentTimeMillis());
    }

    public void save(Stat stat, Leaderboard leaderboard, long timeMillis) {
        Map<Long, Leaderboard> statMap;
        if (!map.containsKey(stat)) {
            statMap = new HashMap<>();
        } else {
            statMap = map.get(stat);
        }
        statMap.put(timeMillis, leaderboard);
        map.put(stat, statMap);
        ObjectNode node = JsonUtil.getObjectNode(file);
        ObjectNode statNode;
        if (!node.has(stat.name())) {
            statNode = JsonNodeFactory.instance.objectNode();
        } else {
            statNode = (ObjectNode) node.get(stat.name());
        }
        node.set(stat.name(), statNode);
        ObjectNode userNode = JsonNodeFactory.instance.objectNode();
        for (UUID user : leaderboard.getUsers().keySet()) {
            userNode.set(user.toString(), new IntNode(leaderboard.getPlace(user)));
        }
        statNode.set(String.valueOf(timeMillis), userNode);
        JsonUtil.write(file, node);
    }
}
