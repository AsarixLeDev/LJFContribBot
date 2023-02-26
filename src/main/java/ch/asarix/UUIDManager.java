package ch.asarix;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UUIDManager extends DataManager {

    private static final UUIDManager instance = new UUIDManager();
    private final Map<UUID, String> uuidToName = new HashMap<>();
    private final Map<String, UUID> nameToUUID = new HashMap<>();

    public UUIDManager() {
        super("uuids.json");
    }

    public static UUIDManager get() {
        return instance;
    }

    @Override
    public void store(String key, JsonNode node) {
        UUID uuid = UUID.fromString(key);
        uuidToName.put(uuid, node.asText());
        nameToUUID.put(node.asText(), uuid);
    }

    public UUID getUUID(String name) {
        if (nameToUUID.containsKey(name))
            return nameToUUID.get(name);
        String uuidStr;
        UUID uuid;
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(new URL("https://api.mojang.com/users/profiles/minecraft/" + name).openStream()));
            uuidStr = (((JSONObject) new JSONParser().parse(in)).get("id")).toString().replaceAll("\"", "");
            uuidStr = uuidStr.replaceAll("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5");
            in.close();
            uuid = UUID.fromString(uuidStr);
            ObjectNode node = JsonUtil.getObjectNode(file);
            node.set(uuidStr, new TextNode(name));
            JsonUtil.write(file, node);
            uuidToName.put(uuid, name);
            nameToUUID.put(name, uuid);
        } catch (Exception e) {
            System.out.println("Unable to get UUID of: " + name + "!");
            return null;
        }
        return uuid;
    }

    public String getName(UUID uuid) {
        if (uuidToName.containsKey(uuid))
            return uuidToName.get(uuid);
        String strId = uuid.toString().replace("-", "");
        String name;
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + strId).openStream()));
            name = (((JSONObject) new JSONParser().parse(in)).get("name")).toString().replaceAll("\"", "");
            in.close();
            ObjectNode node = JsonUtil.getObjectNode(file);
            node.set(uuid.toString(), new TextNode(name));
            JsonUtil.write(file, node);
            uuidToName.put(uuid, name);
            nameToUUID.put(name, uuid);
        } catch (Exception e) {
            System.out.println("Unable to get Name of uuid: " + strId + "!");
            name = "UNKNOWN";
        }
        return name;
    }

    @Override
    public void clearData() {
        uuidToName.clear();
        nameToUUID.clear();
    }
}
