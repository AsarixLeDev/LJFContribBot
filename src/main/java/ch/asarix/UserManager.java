package ch.asarix;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class UserManager {

    private static final Map<Long, User> userCache = new HashMap<>();
    private static final Map<String, String> userMentionCache = new HashMap<>();

    public static boolean hasAccess(User user) {
        userCache.put(user.getIdLong(), user);
        try {
            JsonNode node = new ObjectMapper().readTree(Main.getFile("access.json"));
            Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                long id = entry.getValue().asLong();
                if (user.getIdLong() == id)
                    return true;
            }
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void addAccess(User user) {
        userCache.put(user.getIdLong(), user);
        if (hasAccess(user)) return;
        try {
            ObjectMapper mapper = new ObjectMapper();
            File file = Main.getFile("access.json");
            ObjectNode oNode;
            JsonNode node = mapper.readTree(file);
            if (node instanceof ObjectNode)
                oNode = (ObjectNode) node;
            else
                oNode = new ObjectMapper().createObjectNode();
            oNode.put(user.getName(), user.getIdLong());

            if (!file.createNewFile()) {
                if (!file.exists()) {
                    throw new RuntimeException("Couldn't create file " + file.getName());
                }
            }
            mapper.writeValue(file, oNode);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean hasAdmin(User user) {
        userCache.put(user.getIdLong(), user);
        try {
            JsonNode node = new ObjectMapper().readTree(Main.getFile("admin.json"));
            Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                long id = entry.getValue().asLong();
                if (user.getIdLong() == id)
                    return true;
            }
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void addAdmin(User user) {
        userCache.put(user.getIdLong(), user);
        if (hasAdmin(user)) return;
        addAccess(user);
        try {
            ObjectMapper mapper = new ObjectMapper();
            File file = Main.getFile("admin.json");
            ObjectNode oNode;
            JsonNode node = mapper.readTree(file);
            if (node instanceof ObjectNode)
                oNode = (ObjectNode) node;
            else
                oNode = new ObjectMapper().createObjectNode();
            oNode.put(user.getName(), user.getIdLong());

            if (!file.createNewFile()) {
                if (!file.exists()) {
                    throw new RuntimeException("Couldn't create file " + file.getName());
                }
            }
            mapper.writeValue(file, oNode);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void updateDiscordNames() throws IOException {
        uDNFile("access.json");
        uDNFile("admin.json");
    }

    private static void uDNFile(String fileName) throws IOException {
        File file = Main.getFile(fileName);
        JsonNode node = new ObjectMapper().readTree(file);
        Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
        Map<String, Long> ids = new HashMap<>();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> entry = fields.next();
            long id = entry.getValue().asLong();
            User user = getUser(id);
            if (user == null) continue;
            String userName = user.getName();
            ids.put(userName, id);
        }
        ObjectMapper mapper = new ObjectMapper();

        if (!file.createNewFile()) {
            if (!file.exists()) {
                throw new RuntimeException("Couldn't create file " + file.getName());
            }
        }
        mapper.writeValue(file, ids);
    }

    public static List<User> getAccess() {
        try {
            List<User> access = new ArrayList<>();
            JsonNode node = new ObjectMapper().readTree(Main.getFile("access.json"));
            Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                long id = entry.getValue().asLong();
                access.add(getUser(id));
            }
            return access;
        } catch (IOException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public static List<User> getAdmins() {
        try {
            List<User> access = new ArrayList<>();
            JsonNode node = new ObjectMapper().readTree(Main.getFile("admin.json"));
            Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                long id = entry.getValue().asLong();
                access.add(getUser(id));
            }
            return access;
        } catch (IOException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public static boolean hasPerm(User user, PermLevel permLevel) {
        userCache.put(user.getIdLong(), user);
        if (permLevel == PermLevel.NONE)
            return true;
        if (permLevel == PermLevel.ACCESS)
            return hasAccess(user);
        return hasAdmin(user);
    }

    public static User getUser(long id) {
        if (userCache.containsKey(id))
            return userCache.get(id);
        User user = Main.jda.retrieveUserById(id).complete();
        if (user == null) return null;
        userCache.put(id, user);
        return user;
    }

    public static User getUser(String id) {
        long idLong;
        try {
            idLong = Long.parseLong(id);
        } catch (NumberFormatException e) {
            System.err.println("Wrong id : " + id);
            return null;
        }
        return getUser(idLong);
    }

    public static String tryGetMention(String userName) {
        String lowerName = userName.toLowerCase();
        if (userMentionCache.containsKey(lowerName))
            return userMentionCache.get(lowerName);

        Guild guild = Main.jda.getGuildById("604823789188022301");
        if (guild == null) {
            System.err.println("Could not get guild");
            return null;
        }
        for (Member member : guild.getMembers()) {
            String nickName = member.getNickname();
            if (nickName == null) continue;
            if (nickName.toLowerCase().contains(lowerName)) {
                String mention = member.getAsMention();
                userMentionCache.put(lowerName, mention);
                return mention;
            }
        }
        return userName;
    }
}
