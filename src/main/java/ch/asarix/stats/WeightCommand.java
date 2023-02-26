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
        JsonObject members = profile.get("members").getAsJsonObject();
        String fUuid = player.getUuid().toString().replace("-", "");
        JsonObject pProfile = members.get(fUuid).getAsJsonObject();
//        System.out.println(pProfile.toString());
        Map<String, Weight> skillWeights = new HashMap<>();
        Map<String, Weight> slayerWeights = new HashMap<>();
        Map<String, Weight> dungeonWeights = new HashMap<>();
        Stats stats = StatsManager.get().getStats(player.getUuid());
        for (Stat stat : stats.getStats().keySet()) {
            if (stat instanceof Skill) {
                skillWeights.put(stat.niceName(), stats.getWeight(stat));
            } else if (stat instanceof Slayer) {
                slayerWeights.put(stat.niceName(), stats.getWeight(stat));
            } else if (stat instanceof DungeonType) {
                dungeonWeights.put(stat.niceName(), stats.getWeight(stat));
            }
        }
        List<String> sortedSkill = new LinkedList<>();
        List<String> sortedSlayer = new LinkedList<>();
        List<String> sortedDungeon = new LinkedList<>();
        double total = 0;
        double totalBase = 0;
        for (String key : skillWeights.keySet()) {
            Weight weight = skillWeights.get(key);
            double aDouble = weight.total();
            if (aDouble == 0) continue;
            sortedSkill.add(key);
            total += aDouble;
            totalBase += weight.base();
        }
        for (String key : slayerWeights.keySet()) {
            Weight weight = slayerWeights.get(key);
            double aDouble = weight.total();
            if (aDouble == 0) continue;
            sortedSlayer.add(key);
            total += aDouble;
            totalBase += weight.base();
        }
        for (String key : dungeonWeights.keySet()) {
            Weight weight = dungeonWeights.get(key);
            double aDouble = weight.total();
            if (aDouble == 0) continue;
            sortedDungeon.add(key);
            total += aDouble;
            totalBase += weight.base();
        }


        sortedSkill = sortedSkill.stream().sorted(Comparator.comparingDouble(
                key -> skillWeights.get(key).total()).reversed()).toList();
        sortedSlayer = sortedSlayer.stream().sorted(Comparator.comparingDouble(
                key -> slayerWeights.get(key).total()).reversed()).toList();
        sortedDungeon = sortedDungeon.stream().sorted(Comparator.comparingDouble(
                key -> dungeonWeights.get(key).total()).reversed()).toList();


        EmbedBuilder builder = new EmbedBuilder();
        builder.setThumbnail("https://crafatar.com/avatars/" + player.getUuid().toString());
        builder.setTitle(player.getName(), "https://sky.shiiyu.moe/stats/" + player.getName());
        builder.addField("Weight totale", String.valueOf(Util.round(total, 2)), true);
        builder.addField("Weight sans excédent", String.valueOf(Util.round(totalBase, 2)), true);

        StringBuilder skillsValue = new StringBuilder();
        double skillTotal = 0;
        double skillBase = 0;
        for (String key : sortedSkill) {
            Weight val = skillWeights.get(key);
            skillTotal += val.total();
            skillBase += val.base();
            skillsValue.append(Util.firstCap(key))
                    .append(" : **")
                    .append(Util.round(val.total(), 2))
                    .append("**")
                    .append(" (")
                    .append(Util.round(val.base(), 2))
                    .append(")").append("\n");
        }
        String title = "Skills : " + Util.round(skillTotal, 2);
        title += " (" + Util.round(skillBase, 2) + ")";
        builder.addField(title, skillsValue.append("\n").toString(), false);

        StringBuilder slayerValue = new StringBuilder();
        double slayerTotal = 0;
        double slayerBase = 0;
        for (String key : sortedSlayer) {
            Weight val = slayerWeights.get(key);
            slayerTotal += val.total();
            slayerBase += val.base();
            slayerValue.append(Util.firstCap(key))
                    .append(" : **")
                    .append(Util.round(val.total(), 2))
                    .append("**")
                    .append(" (")
                    .append(Util.round(val.base(), 2))
                    .append(")").append("\n");
        }
        title = "Slayers : " + Util.round(slayerTotal, 2);
        title += " (" + Util.round(slayerBase, 2) + ")";
        builder.addField(title, slayerValue.append("\n").toString(), false);

        StringBuilder dungeonValue = new StringBuilder();
        double dungeonTotal = 0;
        double dungeonBase = 0;
        for (String key : sortedDungeon) {
            Weight val = dungeonWeights.get(key);
            dungeonTotal += val.total();
            dungeonBase += val.base();
            dungeonValue.append(Util.firstCap(key))
                    .append(" : **")
                    .append(Util.round(val.total(), 2))
                    .append("**")
                    .append(" (")
                    .append(Util.round(val.base(), 2))
                    .append(")").append("\n");
        }
        title = "Donjons : " + Util.round(dungeonTotal, 2);
        title += " (" + Util.round(dungeonBase, 2) + ")";
        builder.addField(title, dungeonValue.append("\n").toString(), false);

        return new MessageContent(builder).setAuthor(Main.asarix);
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
