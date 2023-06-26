package ch.asarix.leaderboards;

import ch.asarix.*;
import ch.asarix.commands.Command;
import ch.asarix.commands.MessageContent;
import ch.asarix.stats.StatType;
import ch.asarix.stats.Stats;
import ch.asarix.stats.StatsManager;
import ch.asarix.stats.types.DungeonType;
import ch.asarix.stats.types.Misc;
import ch.asarix.stats.types.Skill;
import ch.asarix.stats.types.Slayer;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.hypixel.api.reply.GuildReply;
import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class GuildLeaderboardCommand extends Command {
    @Override
    public MessageContent run(@NotNull SlashCommandInteractionEvent event) throws Exception {
        String statName = getOption(event, "stat");
        List<StatType> statsList;
        if (statName == null) {
            statsList = new ArrayList<>();
            statsList.add(DungeonType.CATACOMBS);
//            statsList.addAll(Arrays.stream(DungeonType.values()).toList());
            statsList.addAll(Arrays.stream(Misc.values()).toList());
            List<StatType> skills = Arrays.stream(Skill.values()).collect(Collectors.toList());
            skills.remove(Skill.SOCIAL2);
            skills.remove(Skill.RUNECRAFTING);
            skills.remove(Skill.CARPENTRY);
            statsList.addAll(skills);
            statsList.addAll(Arrays.stream(Slayer.values()).toList());
            statsList.remove(Slayer.UNKNOWN);
        } else {
            StatType statType = StatsManager.get().fromName(statName);
            if (statType == null) {
                return new MessageContent("Stat inconnu : " + statName);
            }
            statsList = List.of(statType);
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
                    Stats stats = statsManager.getStats(uuid);
                    if (stats == null) {
                        System.err.println("No stats (likely due to no profile)");
                        return;
                    }
                    try {
                        guildStats.add(stats);
                    } catch (RuntimeException e) {
                        e.printStackTrace();
                    }
                    System.out.println("Done.");
                }
        );
        List<Member> previousPlayers = new ArrayList<>();
        for (StatType statType : statsList) {
            Leaderboard leaderboard = LeaderboardManager.get().getLatestLeaderboard(statType);
            if (leaderboard == null) continue;
            for (UUID uuid : leaderboard.getUsers().keySet()) {
                if (leaderboard.getPlace(uuid) > 3) continue;
                User user = UserManager.getUserByUuid(uuid);
                if (user == null) {
                    System.err.println("Could not find user with uuid : " + uuid);
                    continue;
                }
                Member member = Main.ljf.getMember(user);
                if (member == null) {
                    System.err.println("User " + user.getAsTag() + " is not in guild " + Main.ljf.getName());
                    continue;
                }
                if (!previousPlayers.contains(member))
                    previousPlayers.add(member);
            }
        }
        for (Member member : previousPlayers) {
            LeaderboardManager.get().removeRoles(member);
        }
        StringBuilder message = new StringBuilder();
        for (StatType statType : statsList) {
            String leaderboardMessage = LeaderboardManager.get().leaderboardMessage(statType, guildStats);
            if ((message + leaderboardMessage).length() > 1000) {
                System.out.println("Sending...");
                event.getChannel().sendMessage(message).queue();
                message = new StringBuilder(leaderboardMessage);
            } else {
                message.append(leaderboardMessage);
            }
        }
        event.getChannel().sendMessage(message).queue();
        for (StatType statType : statsList) {
            LeaderboardManager.get().displayRoles(statType, statsList);
        }
        SimpleDateFormat formatter = new SimpleDateFormat("dd");
        String msg = "Leaderboard de guild de " + Util.getMonth() + ", semaine du " + formatter.format(System.currentTimeMillis());
//        String msg = "```diff Leaderboard de la guilde du " + formatter.format(System.currentTimeMillis()) + " Skills```";
        return new MessageContent(new EmbedBuilder().setTitle(msg)).setAuthor(Main.asarix);
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
