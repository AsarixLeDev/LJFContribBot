package ch.asarix.leaderboards;

import ch.asarix.*;
import ch.asarix.commands.MessageContent;
import ch.asarix.stats.Stat;
import ch.asarix.stats.Stats;
import ch.asarix.stats.StatsManager;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Iterators;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;

import java.util.*;
import java.util.concurrent.CompletableFuture;

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
            long xp = stats1.getTotalXp(stat);
            int index = i++;
            if (prevXp == xp) index = prevIndex;
            UUID uuid = stats1.getUuid();
            leaderboard.addUser(uuid, index);
            String userName = UUIDManager.get().getName(uuid);
            String relEmoji = LeaderboardManager.get().getRelEmoji(uuid, stat, index);
            String line = "`" + relEmoji + positionEmoji(index) + "` #" + index + " ";
            line += stat.formatLine(stats1);
            line += " " + UserManager.tryGetMention(userName);
            builder1.append(line).append("\n");
            prevXp = xp;
            prevIndex = index;
        }
        builder.setDescription(builder1);
        save(stat, leaderboard);
        return new MessageContent(builder);
    }

    public void displayRoles(Stats stats, List<Stat> toInclude) {
        Guild guild = Main.jda.getGuildById("604823789188022301");
        if (guild == null) return;
        System.out.println("got guild");
        Role firstRole = guild.getRoleById("744303376962945034");
        if (firstRole == null) {
            System.err.println("Unable to find firstRole");
            return;
        }
        Role secondRole = guild.getRoleById("744562546807144508");
        if (secondRole == null) {
            System.err.println("Unable to find secondRole");
            return;
        }
        Role thirdRole = guild.getRoleById("744564749307740200");
        if (thirdRole == null) {
            System.err.println("Unable to find thirdRole");
            return;
        }
        Stat bestStat = null;
        int bestPosition = 6;
        for (Stat stat : stats.getStats().keySet()) {
            if (!toInclude.contains(stat)) continue;
            Leaderboard leaderboard = getLatestLeaderboard(stat);
            int place = leaderboard.getPlace(stats.getUuid());
            if (place > 0 && place < bestPosition) {
                bestPosition = place;
                bestStat = stat;
            }
        }
//        System.out.println(bestPosition);
        if (bestPosition > 3) return;
//        System.out.println("pos ok");
        int pos = bestPosition;
        Stat stat = bestStat;
        User user = UserManager.getUserByUuid(stats.getUuid());
        if (user == null) return;
        System.out.println("user ok");
        Member member = guild.getMember(user);
        if (member == null) return;
        System.out.println("member ok");
        CompletableFuture<Void> firstRemove = CompletableFuture.runAsync(() -> guild.removeRoleFromMember(member, firstRole).complete());
        CompletableFuture<Void> secRemove = CompletableFuture.runAsync(() -> guild.removeRoleFromMember(member, secondRole).complete());
        CompletableFuture<Void> thirdRemove = CompletableFuture.runAsync(() -> guild.removeRoleFromMember(member, thirdRole).complete());
        CompletableFuture.allOf(firstRemove, secRemove, thirdRemove).thenRun(() -> {
            if (pos == 1) {
                guild.addRoleToMember(member, firstRole).queue();
            } else if (pos == 2) {
                guild.addRoleToMember(member, secondRole).queue();
            } else {
                guild.addRoleToMember(member, thirdRole).queue();
            }

            String nickName = member.getNickname();
            if (nickName == null)
                nickName = member.getEffectiveName();
            boolean changed = false;
            for (int i = 1; i < 4; i++) {
                if (nickName.contains(positionEmoji(i))) {
                    nickName = nickName.replace(positionEmoji(i), positionEmoji(pos));
                    changed = true;
                }
            }
            if (!changed) nickName += positionEmoji(pos);
            guild.modifyNickname(member, nickName).queue();
            String statName = stat == null ? "null" : stat.niceName();
            System.out.println("Modified user " + member.getEffectiveName() + " #" + pos + " in " + statName);
        });
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
        if (Iterators.size(statNode.fieldNames()) > 2) {
            long earliest = -1;
            for (Iterator<String> it = statNode.fieldNames(); it.hasNext(); ) {
                String fieldName = it.next();
                try {
                    long tMillis = Long.parseLong(fieldName);
                    if (earliest < 0 || tMillis < earliest) earliest = tMillis;
                } catch (NumberFormatException e) {
                    System.err.println("Could not parse long " + fieldName);
                }
            }
            if (earliest > 0)
                statNode.remove(String.valueOf(earliest));
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
