package ch.asarix;

import ch.asarix.commands.CommandHandler;
import ch.asarix.contributions.ContribManager;
import ch.asarix.leaderboards.LeaderboardManager;
import ch.asarix.seasons.SeasonManager;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

public class Main extends ListenerAdapter {

    public static JDA jda;
    public static User asarix;
    public static Guild ljf;
    public static List<DataManager> dataManagers;

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
            if (Util.isBlank(botToken)) throw new NullPointerException();
        } catch (NullPointerException e) {
            System.err.println("Please specify the bot token in the variable file !");
            createVariableFile();
            shutDown();
            return;
        }
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            // Code to execute on shutdown here
            System.out.println("Bot is shutting down...");
            ContribManager contribManager = ContribManager.get();
            for (Long userId : contribManager.map.keySet()) {
                contribManager.save(UserManager.getUser(userId));
            }
            System.out.println("Done.");
            try {
                shutDown();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }));
        jda = JDABuilder.createDefault(botToken)
                .setActivity(Activity.playing("Hypixel :D"))
                .addEventListeners(new CommandHandler())
                .setChunkingFilter(ChunkingFilter.ALL)
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .enableIntents(GatewayIntent.GUILD_MEMBERS)
                .setEnableShutdownHook(true)
                .build();
        try {
            jda.awaitReady();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        asarix = jda.retrieveUserById("441284809856122891").complete();
        ljf = jda.getGuildById("604823789188022301");
        if (ljf == null) {
            System.err.println("Failed to load ljf");
            shutDown();
        }
        UserManager.addAdmin(asarix);
        dataManagers = List.of(SeasonManager.get(), ContribManager.get(), UUIDManager.get(), LeaderboardManager.get(), APIManager.get());
        dataManagers.forEach(DataManager::init);
        try {
            APIManager.get().getGuildByPlayer(UUID.fromString("8177cfe8-3a1f-4ac8-86b2-8e19dff1c156"))
                    .getGuild()
                    .getMembers()
                    .forEach(member -> UUIDManager.get().getName(member.getUuid()));
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }

    private static void createVariableFile() {
        ObjectNode node = new ObjectMapper().createObjectNode();
        node.set("API_KEY", new TextNode(""));
        node.set("BOT_TOKEN", new TextNode(""));
        try {
            JsonUtil.writeTrow(getFile("variables.json"), node);
        } catch (Exception ex) {
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
}