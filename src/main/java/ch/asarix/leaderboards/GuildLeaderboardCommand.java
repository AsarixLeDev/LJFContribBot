package ch.asarix.leaderboards;

import ch.asarix.APIManager;
import ch.asarix.PermLevel;
import ch.asarix.UUIDManager;
import ch.asarix.commands.Command;
import ch.asarix.commands.MessageContent;
import ch.asarix.stats.Stat;
import ch.asarix.stats.Stats;
import ch.asarix.stats.StatsManager;
import ch.asarix.stats.types.DungeonType;
import ch.asarix.stats.types.Misc;
import ch.asarix.stats.types.Skill;
import ch.asarix.stats.types.Slayer;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.hypixel.api.reply.GuildReply;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class GuildLeaderboardCommand extends Command {
    @Override
    public MessageContent run(@NotNull SlashCommandInteractionEvent event) throws Exception {
        String statName = getOption(event, "stat");
        List<Stat> stats;
        if (statName == null) {
            stats = new ArrayList<>();
            stats.addAll(Arrays.stream(DungeonType.values()).toList());
            stats.addAll(Arrays.stream(Misc.values()).toList());
            stats.addAll(Arrays.stream(Skill.values()).toList());
            stats.addAll(Arrays.stream(Slayer.values()).toList());
        } else {
            Stat stat = StatsManager.get().fromName(statName);
            if (stat == null) {
                return new MessageContent("Stat inconnu : " + statName);
            }
            stats = List.of(stat);
        }
        List<Stats> guildStats = new LinkedList<>();
        GuildReply.Guild guild = APIManager.get().getGuildByPlayer(UUID.fromString("8177cfe8-3a1f-4ac8-86b2-8e19dff1c156")).getGuild();
        List<GuildReply.Guild.Member> members = guild.getMembers();
        System.out.println(guild.getName() + " members : " + members.size());
        StatsManager statsManager = StatsManager.get();
        members.forEach(
                member -> {
                    UUID uuid = member.getUuid();
                    System.out.println("Fetching " + UUIDManager.get().getName(uuid) + "'s profile...");
                    try {
                        guildStats.add(statsManager.getStats(uuid));
                    } catch (RuntimeException e) {
                        e.printStackTrace();
                    }
                    System.out.println("Done.");
                }
        );
        for (Stat stat : stats) {
            event.getChannel().sendMessage(LeaderboardManager.get().leaderboardMessage(stat, guildStats).build()).queue();
        }
        return new MessageContent("finito");
    }

    @Override
    public CommandData data() {
        OptionData data = new OptionData(OptionType.STRING, "stat", "Nom du stat", false);
        return Commands.slash("skillleaderboard", "Guild leaderboard").addOptions(data);
    }

    @Override
    public PermLevel permLevel() {
        return PermLevel.ACCESS;
    }
}
