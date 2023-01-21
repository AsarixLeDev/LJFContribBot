package ch.asarix;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.*;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import ch.asarix.commands.CommandHandler;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class Main extends ListenerAdapter {

    public static JDA jda;
    private static File file;

    public static User asarix;

    public static Map<User, List<Contribution>> map = new HashMap<>();

    public static void main(String[] args) {
        JsonNode node;
        try {
            node = new ObjectMapper().readTree(getFile("variables.json"));
        } catch (IOException e) {
            System.err.println("Couldn't read variables file");
            createVariableFile();
            shutDown();
            return;
        }
        String botToken;
        try {
            botToken = node.get("BOT_TOKEN").asText();
            if (botToken.isBlank()) throw new NullPointerException();
        } catch (NullPointerException e) {
            System.err.println("Please specify the bot token in the variable file !");
            createVariableFile();
            shutDown();
            return;
        }
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            // Code to execute on shutdown here
            for (User user : Main.map.keySet()) {
                Main.saveAll(user, Main.map.get(user));
            }
            System.out.println("Bot is shutting down...");
            try {
                shutDown();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }));
        file = getFile("data.json");
        jda = JDABuilder.createDefault(botToken)
                .setActivity(Activity.playing("Hypixel :D"))
                .addEventListeners(new CommandHandler())
                .setEnableShutdownHook(true)
                .build();
        asarix = jda.retrieveUserById("441284809856122891").complete();
        UserManager.addAdmin(asarix);
        try {
            JsonNode node1 = new ObjectMapper().readTree(file);
            Iterator<Map.Entry<String, JsonNode>> it = node1.fields();
            while (it.hasNext()) {
                Map.Entry<String, JsonNode> entry = it.next();
                User user = jda.retrieveUserById(entry.getKey()).complete();
                ArrayNode userNode = (ArrayNode) entry.getValue();

                List<Contribution> contribs = new ArrayList<>();

                for (int i = 0; i < userNode.size(); i++) {
                    JsonNode next = userNode.get(i);
                    if (next == null || next instanceof MissingNode) continue;
                    String name = next.get("name").asText();
                    int amount = next.get("amount").asInt();
                    Date date = new Date(next.get("dateMillis").asLong());
                    String commentary = next.get("commentary").asText();
                    long value = next.get("value").asLong();

                    createContribution(user, name, amount, date, commentary, value);
                }
            }
            System.out.println("Finished loading !");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void createVariableFile() {
        ObjectNode node = new ObjectMapper().createObjectNode();
        node.set("API_KEY", new TextNode(""));
        node.set("BOT_TOKEN", new TextNode(""));
        try {
            new ObjectMapper().writeValue(getFile("variables.json"), node);
        } catch (IOException ex) {
            System.err.println("Failed to write to variable file");
            shutDown();
            throw new RuntimeException(ex);

        }
    }

    public static void shutDown() {
        if (jda != null)
            jda.shutdownNow();
        System.exit(10);
    }

    public static File getFile(String name) {
        File resourceFolder = new File(System.getProperty("user.dir"), "resources");
        if (!resourceFolder.exists())
            resourceFolder.mkdir();
        File file = new File(resourceFolder, name);
        try {
            if (!file.exists())
                file.createNewFile();
        } catch (IOException e) {
            System.err.println("Failed to create file : " + name);
            shutDown();
            throw new RuntimeException(e);
        }
        return file;
    }

    public static void save(Contribution contribution) {
        User user = contribution.user();
        List<Contribution> contribs = map.get(user);
        if (contribs == null) {
            contribs = new ArrayList<>();
        }
        contribs.add(contribution);
        map.put(user, contribs);
        saveAll(user, contribs);
    }

    public static void saveAll(User user, List<Contribution> contribs) {
        try {
            JsonNode node = new ObjectMapper().readTree(file);
            ObjectNode objectNode;
            if (node instanceof MissingNode) {
                objectNode = new ObjectNode(JsonNodeFactory.instance);
            }
            else if (node instanceof ObjectNode) {
                objectNode = (ObjectNode) node;
            }
            else {
                throw new RuntimeException("could not read file " + file.getName());
            }
            ArrayNode userNode = new ArrayNode(JsonNodeFactory.instance);

            for (Contribution contribution : contribs) {

                ObjectNode contribNode = new ObjectNode(JsonNodeFactory.instance);
                contribNode.set("name", new TextNode(contribution.contribName()));
                contribNode.set("amount", new IntNode(contribution.amount()));
                contribNode.set("dateMillis", new LongNode(contribution.date().getTime()));
                contribNode.set("commentary", new TextNode(contribution.commentary()));
                contribNode.set("value", new LongNode(contribution.value()));
                userNode.add(contribNode);
            }
            objectNode.set(user.getId(), userNode);

            new ObjectMapper().writeValue(file, objectNode);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void replaceAll(User user, List<Contribution> contribs) {
        map.put(user, contribs);
        saveAll(user, contribs);
    }

    public static List<Contribution> getContribs(User user) {
        if (!map.containsKey(user)) return List.of();
        return map.get(user);
    }

    public static List<User> getContribers() {
        return List.of(map.keySet().toArray(new User[0]));
    }

    public static long getTotalContribValue(User user) {
        long total = 0;
        for (Contribution contribution : Main.getContribs(user)) {
            total += contribution.value();
        }
        return total;
    }

    public static void remove(Contribution toRemove) {
        if (toRemove == null) return;
        User user = toRemove.user();
        List<Contribution> contribs = getContribs(user);
        for (Contribution contribution : contribs) {
            if (contribution.equals(toRemove)) {
                System.out.println("Found contrib to edit !");
                contribs.remove(contribution);
                replaceAll(user, contribs);
                return;
            }
        }
    }

    public static void edit(User user, Contribution before, Contribution after) {
        List<Contribution> contribs = getContribs(user);
        for (Contribution contribution : contribs) {
            if (contribution.equals(before)) {
                System.out.println("Found contrib to edit !");
                contribs.remove(contribution);
                contribs.add(after);
                replaceAll(user, contribs);
                return;
            }
        }
    }

    public static Contribution createContribution(User user, String contribName, int amount, Date date, String commentary, long value) {
        List<Contribution> contribs = getContribs(user);
        var ref = new Object() {
            String newId;
        };
        do {
            ref.newId = new PasswordGenerator.PasswordGeneratorBuilder().useLower(true).useUpper(true).build().generate(8);
        }
        while (contribs.stream().anyMatch(contribution -> contribution.id().equals(ref.newId)));
        Contribution contribution = new Contribution(user, contribName, amount, date, commentary, value, ref.newId);
        save(contribution);
        return contribution;
    }

    public static Contribution getContrib(User user, String id) {
        return getContribs(user).stream().filter(contribution -> contribution.id().equals(id)).findFirst().orElse(null);
    }
}