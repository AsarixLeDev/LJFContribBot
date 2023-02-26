package ch.asarix.commands;

import ch.asarix.commands.perms.AccessCommand;
import ch.asarix.commands.perms.AdminCommand;
import ch.asarix.commands.perms.AdminsCommand;
import ch.asarix.contributions.*;
import ch.asarix.leaderboards.GuildLeaderboardCommand;
import ch.asarix.seasons.CreateSeasonCommand;
import ch.asarix.seasons.SeasonCommand;
import ch.asarix.seasons.SeasonLeaderboardCommand;
import ch.asarix.stats.WeightCommand;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class CommandHandler extends ListenerAdapter {
    private final HashMap<String, Command> commandMap = new HashMap<>();

    @Override
    public void onGuildReady(@NotNull GuildReadyEvent event) {
        Guild guild = event.getGuild();
        registerCommand(new AddContribCommand(), guild);
        registerCommand(new LeaderboardCommand(), guild);
        registerCommand(new ContribsCommand(), guild);
        registerCommand(new AccessCommand(), guild);
        registerCommand(new AdminCommand(), guild);
        registerCommand(new AdminsCommand(), guild);
        registerCommand(new SaveCommand(), guild);
        registerCommand(new FetchCommand(), guild);
        registerCommand(new CreateSeasonCommand(), guild);
        registerCommand(new SeasonCommand(), guild);
        registerCommand(new SeasonLeaderboardCommand(), guild);
        registerCommand(new WeightCommand(), guild);
        registerCommand(new GuildLeaderboardCommand(), guild);
    }

    private void registerCommand(Command command, Guild guild) {
        CommandData data = command.data();
        this.commandMap.put(data.getName(), command);
        guild.upsertCommand(data).queue();
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        Command command = commandMap.get(event.getName());
        if (command == null) {
            event.getHook().sendMessage("Commande non trouvée ! Est-ce un bug ?").queue();
            return;
        }
        command.execute(event);
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        String id = event.getButton().getId();
        if (id == null) return;
        int sepIndex = id.indexOf("-");
        if (sepIndex < 0) return;
        String commandName = id.substring(0, sepIndex);
        Command command = commandMap.get(commandName);
        if (command == null) {
            return;
        }
        command.onButton(event, id.substring(sepIndex + 1));
    }
}
