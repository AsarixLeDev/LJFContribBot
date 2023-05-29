package ch.asarix.leaderboards;

import ch.asarix.*;
import ch.asarix.stats.StatType;
import ch.asarix.stats.Stats;
import ch.asarix.stats.StatsManager;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Iterators;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;

import java.util.*;

public class LeaderboardManager extends DataManager {

    private static final LeaderboardManager instance = new LeaderboardManager();
    public Map<StatType, Map<Long, Leaderboard>> map = new HashMap<>();
    private Role firstRole;
    private Role secondRole;
    private Role thirdRole;

    public LeaderboardManager() {
        super("leaderboards.json");
    }

    public static LeaderboardManager get() {
        return instance;
    }

    @Override
    public void init() {
        super.init();
        final Guild guild = Main.ljf;
        firstRole = guild.getRoleById("744303376962945034");
        secondRole = guild.getRoleById("744562546807144508");
        thirdRole = guild.getRoleById("744564749307740200");
    }

    @Override
    public void store(String key, JsonNode node) {
        StatType statType = StatsManager.get().fromName(key);
        if (statType == null) {
            System.err.println("Could not get stat from name " + key);
            return;
        }
        Iterator<Map.Entry<String, JsonNode>> it = node.fields();
        while (it.hasNext()) {
            Map.Entry<String, JsonNode> entry = it.next();
            long timeMillis = Long.parseLong(entry.getKey());
            Leaderboard leaderboard = new Leaderboard(statType);
            Iterator<Map.Entry<String, JsonNode>> userIt = entry.getValue().fields();
            while (userIt.hasNext()) {
                Map.Entry<String, JsonNode> userEntry = userIt.next();
                UUID uuid = UUID.fromString(userEntry.getKey());
                int place = userEntry.getValue().asInt();
                leaderboard.addUser(uuid, place);
            }
            save(statType, leaderboard, timeMillis);
        }
    }

    @Override
    public void clearData() {
        map.clear();
    }

    public String getRelEmoji(UUID uuid, StatType statType, int i) {
        Leaderboard latest = getLatestLeaderboard(statType);
        if (latest == null) return "\uD83D\uDD36";
        int lastPlace = latest.getPlace(uuid);
        if (lastPlace == i) return "\uD83D\uDD36";
        if (lastPlace < i) return "\uD83D\uDD3B";
        return "\uD83D\uDD3A";
    }

    private String positionEmoji(int i) {
        if (i == 1) return "🥇";
        if (i == 2) return "\uD83E\uDD48";
        if (i == 3) return "\uD83E\uDD49";
        if (i == 4) return "\uD83E\uDD0E";
        return "\uD83D\uDDA4";
    }

    private Role positionRole(int i) {
        if (i == 1)
            return firstRole;
        if (i == 2)
            return secondRole;
        if (i == 3)
            return thirdRole;
        return null;
    }

    public Leaderboard getLatestLeaderboard(StatType statType) {
        long latestTimeMillis = 0;
        Leaderboard latestLeaderboard = null;
        Map<Long, Leaderboard> leaderboards = map.get(statType);
        if (leaderboards == null) return null;
        for (Long time : leaderboards.keySet()) {
            if (time <= latestTimeMillis) continue;
            latestTimeMillis = time;
            latestLeaderboard = leaderboards.get(time);
        }
        return latestLeaderboard;
    }

    public String leaderboardMessage(StatType statType, List<Stats> stats) {
        StringBuilder builder = new StringBuilder("[**" + statType.niceName() + "**]\n");
        List<Stats> top = stats.stream()
                .sorted(Comparator.comparingLong(s -> ((Stats) s).getTotalXp(statType)).reversed())
                .toList();

        Leaderboard leaderboard = new Leaderboard(statType);
        int i = 1;
        long prevXp = 0;
        int prevIndex = i;
        for (Stats stats1 : top.subList(0, Math.min(5, top.size()))) {
            long xp = stats1.getTotalXp(statType);
            int index = i++;
            if (prevXp == xp) index = prevIndex;
            UUID uuid = stats1.getUuid();
            leaderboard.addUser(uuid, index);
            String userName = UUIDManager.get().getName(uuid);
            String relEmoji = LeaderboardManager.get().getRelEmoji(uuid, statType, index);
            String line = "`" + relEmoji + positionEmoji(index) + "` #" + index + " ";
            line += statType.formatLine(stats1);
            line += " " + UserManager.tryGetMention(userName);
            builder.append(line).append("\n");
            prevXp = xp;
            prevIndex = index;
        }
        save(statType, leaderboard);
        return builder.toString();
    }

    public void removeRoles(Member member) {
        final Guild guild = Main.ljf;
        if (guild == null) {
            System.err.println("Failed to get guild");
            return;
        }
        guild.removeRoleFromMember(member, firstRole).complete();
        guild.removeRoleFromMember(member, secondRole).complete();
        guild.removeRoleFromMember(member, thirdRole).complete();
        guild.modifyNickname(member, getGuildName(member)
                .replace(positionEmoji(1), "")
                .replace(positionEmoji(2), "")
                .replace(positionEmoji(3), "")).complete();
        System.out.println("Removed roles from member " + member.getAsMention());
    }

    public Map.Entry<Integer, Leaderboard> getLatestBestPlace(Stats stats, List<StatType> included) {
        int bestPlace = 7;
        Leaderboard bestLb = null;
        for (StatType statType : stats.getStats().keySet()) {
            if (!included.contains(statType)) continue;
            Leaderboard leaderboard = getLatestLeaderboard(statType);
            if (leaderboard == null) {
                System.err.println("Could not find latest leaderboard of stat " + statType.niceName());
                continue;
            }
            int place = leaderboard.getPlace(stats.getUuid());
            if (place < bestPlace) {
                bestPlace = place;
                bestLb = leaderboard;
            }
        }
        return new AbstractMap.SimpleEntry<>(bestPlace, bestLb);
    }

    public void displayRoles(StatType statType, List<StatType> statTypes) {
        final Guild guild = Main.ljf;
        if (guild == null) {
            System.err.println("Failed to get guild");
            return;
        }
        Leaderboard leaderboard = getLatestLeaderboard(statType);
        for (int i = 1; i < 4; i++) {
            UUID uuid = leaderboard.getUser(i);
            Stats stats1 = StatsManager.get().getStats(uuid);
            if (stats1 == null) continue;
            Map.Entry<Integer, Leaderboard> bestPlace = getLatestBestPlace(stats1, statTypes);
            if (bestPlace.getKey() < i) {
                System.err.println(1);
                continue;
            }
            if (bestPlace.getKey() == i && bestPlace.getValue().roleGiven(uuid)) {
                System.err.println(2);
                continue;
            }
            bestPlace.getValue().setRoleGiven(uuid, true);
            if (uuid == null) {
                System.err.println("Could not find #" + i + " user of stat " + statType.niceName());
                continue;
            }
            User user = UserManager.getUserByUuid(uuid);
            if (user == null) {
                System.err.println("Could not find user with uuid : " + uuid);
                continue;
            }
            Member member = guild.getMember(user);
            if (member == null) {
                System.err.println("User " + user.getAsTag() + " is not in guild " + guild.getName());
                continue;
            }
            Role role = positionRole(i);
            if (role == null) {
                System.err.println("Could not get role #" + i);
                continue;
            }
            guild.addRoleToMember(member, role).complete();
            guild.modifyNickname(member, getGuildName(member) + positionEmoji(i)).complete();
            System.out.println("Modified user " + member.getEffectiveName() + " #" + i + " in " + statType.niceName());
        }
    }

    private String getGuildName(Member member) {
        String nickName = member.getNickname();
        if (nickName == null)
            nickName = member.getEffectiveName();
        return nickName;
    }

    public void save(StatType statType, Leaderboard leaderboard) {
        save(statType, leaderboard, System.currentTimeMillis());
    }

    public void save(StatType statType, Leaderboard leaderboard, long timeMillis) {
        Map<Long, Leaderboard> statMap;
        if (!map.containsKey(statType)) {
            statMap = new HashMap<>();
        } else {
            statMap = map.get(statType);
        }
        statMap.put(timeMillis, leaderboard);
        map.put(statType, statMap);
        ObjectNode node = JsonUtil.getObjectNode(file);
        ObjectNode statNode;
        if (!node.has(statType.name())) {
            statNode = JsonNodeFactory.instance.objectNode();
        } else {
            statNode = (ObjectNode) node.get(statType.name());
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
        node.set(statType.name(), statNode);
        ObjectNode userNode = JsonNodeFactory.instance.objectNode();
        for (UUID user : leaderboard.getUsers().keySet()) {
            userNode.set(user.toString(), new IntNode(leaderboard.getPlace(user)));
        }
        statNode.set(String.valueOf(timeMillis), userNode);
        JsonUtil.write(file, node);
    }
}
