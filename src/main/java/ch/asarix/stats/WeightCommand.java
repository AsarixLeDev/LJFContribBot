package ch.asarix.stats;

import ch.asarix.*;
import ch.asarix.commands.Command;
import ch.asarix.commands.MessageContent;
import ch.asarix.stats.types.DungeonType;
import ch.asarix.stats.types.Skill;
import ch.asarix.stats.types.Slayer;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.hypixel.api.reply.PlayerReply;
import net.hypixel.api.reply.skyblock.SkyBlockProfilesReply;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class WeightCommand extends Command {

    @Override
    public MessageContent run(@NotNull SlashCommandInteractionEvent event) throws InterruptedException, ExecutionException {
        OptionMapping optionMapping = event.getOption("player");
        if (optionMapping == null) {
            return new MessageContent("Veuillez spécifier un joueur !");
        }
        String playerName = optionMapping.getAsString();
        optionMapping = event.getOption("profile");
        String profileName = optionMapping == null ? null : optionMapping.getAsString();
        UUID uuid = UUIDManager.get().getUUID(playerName);
        if (uuid == null) {
            return new MessageContent(playerName + " : Ce joueur n'a pas pu être trouvé !");
        }
        PlayerReply.Player player = APIManager.get().getPlayerByUuid(uuid).getPlayer();
        SkyBlockProfilesReply reply = APIManager.get().getSkyBlockProfiles(uuid);
        JsonArray array = reply.getProfiles();
        JsonObject latestProfile = null;
        long latest_save = 0;
        for (int i = 0; i < array.size(); i++) {
            JsonObject profile = array.get(i).getAsJsonObject();
            if (profileName != null) {
                String name = profile.get("cute_name").getAsString();
                if (name.equalsIgnoreCase(profileName)) {
                    return analyseWeight(player, profile);
                }
            }
            if (!profile.has("last_save")) continue;
            long lastSave = profile.get("last_save").getAsLong();
            if (lastSave > latest_save) {
                latestProfile = profile;
                latest_save = lastSave;
            }
        }
        if (profileName != null)
            return new MessageContent("Ce profile n'existe pas !");
        if (latestProfile == null)
            return new MessageContent("Ce joueur n'a pas de profile SkyBlock !");
        return analyseWeight(player, latestProfile);
    }

    private MessageContent analyseWeight(PlayerReply.Player player, JsonObject profile) {
        Map<String, Weight> skillWeights = new HashMap<>();
        Map<String, Weight> slayerWeights = new HashMap<>();
        Map<String, Weight> dungeonWeights = new HashMap<>();
        Stats stats = StatsManager.get().getStats(player.getUuid(), profile);
        for (StatType statType : stats.getStats().keySet()) {
            if (statType instanceof Skill) {
                if (((Skill) statType).isCosmetic()) continue;
                skillWeights.put(statType.niceName(), stats.getWeight(statType));
            } else if (statType instanceof Slayer) {
                slayerWeights.put(statType.niceName(), stats.getWeight(statType));
            } else if (statType instanceof DungeonType) {
                dungeonWeights.put(statType.niceName(), stats.getWeight(statType));
            }
        }

        List<String> sortedSkill = skillWeights.keySet().stream()
                .filter(key -> skillWeights.get(key).total() != 0)
                .sorted(Comparator.comparingDouble(key -> skillWeights.get(key).total()).reversed())
                .collect(Collectors.toList());
        List<String> sortedSlayer = slayerWeights.keySet().stream()
                .filter(key -> slayerWeights.get(key).total() != 0)
                .sorted(Comparator.comparingDouble(key -> slayerWeights.get(key).total()).reversed())
                .collect(Collectors.toList());
        List<String> sortedDungeon = dungeonWeights.keySet().stream()
                .filter(key -> dungeonWeights.get(key).total() != 0)
                .sorted(Comparator.comparingDouble(key -> dungeonWeights.get(key).total()).reversed())
                .collect(Collectors.toList());

        double skillTotal = skillWeights.values().stream().map(Weight::total).reduce(0d, Double::sum);
        double skillBase = skillWeights.values().stream().map(Weight::base).reduce(0d, Double::sum);

        double slayerTotal = slayerWeights.values().stream().map(Weight::total).reduce(0d, Double::sum);
        double slayerBase = slayerWeights.values().stream().map(Weight::base).reduce(0d, Double::sum);

        double dungeonTotal = dungeonWeights.values().stream().map(Weight::total).reduce(0d, Double::sum);
        double dungeonBase = dungeonWeights.values().stream().map(Weight::base).reduce(0d, Double::sum);

        double total = skillTotal + slayerTotal + dungeonTotal;
        double totalBase = skillBase + slayerBase + dungeonBase;

        EmbedBuilder builder = new EmbedBuilder();
        builder.setThumbnail("https://crafatar.com/avatars/" + player.getUuid().toString());
        builder.setTitle(player.getName(), "https://sky.shiiyu.moe/stats/" + player.getName());
        builder.addField("Weight totale", String.valueOf(Util.round(total, 2)), true);
        builder.addField("Weight sans excédent", String.valueOf(Util.round(totalBase, 2)), true);

        String title = "Skills : " + Util.round(skillTotal, 2)
                + " (" + Util.round(skillBase, 2) + ")";
        builder.addField(title, getField(sortedSkill, skillWeights), false);

        title = "Slayers : " + Util.round(slayerTotal, 2)
                + " (" + Util.round(slayerBase, 2) + ")";
        builder.addField(title, getField(sortedSlayer, slayerWeights), false);

        title = "Donjons : " + Util.round(dungeonTotal, 2)
                + " (" + Util.round(dungeonBase, 2) + ")";
        builder.addField(title, getField(sortedDungeon, dungeonWeights), false);

        return new MessageContent(builder).setAuthor(Main.asarix);
    }

    private String getField(List<String> sortedKeys, Map<String, Weight> map) {
        StringBuilder builder = new StringBuilder();
        for (String key : sortedKeys) {
            Weight val = map.get(key);
            builder.append(Util.firstCap(key))
                    .append(" : **")
                    .append(Util.round(val.total(), 2))
                    .append("**")
                    .append(" (")
                    .append(Util.round(val.base(), 2))
                    .append(")").append("\n");
        }
        return builder.append("\n").toString();
    }

    @Override
    public CommandData data() {
        OptionData data = new OptionData(OptionType.STRING, "player", "Nom ou UUID du joueur", true);
        OptionData data1 = new OptionData(OptionType.STRING, "profile", "Nom du profile", false);
        return Commands.slash("weight", "Weight infos").addOptions(data, data1);
    }

    private Weight skillWeight(String skillName, long xp) {
        CalculatedStat skill = Skill.valueOf(skillName.toUpperCase()).calculate(xp);
        return skill.getWeight();
    }

    @Override
    public PermLevel permLevel() {
        return PermLevel.ACCESS;
    }
}
