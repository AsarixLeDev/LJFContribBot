package org.example.commands;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.example.Main;
import org.example.PermLevel;
import org.example.UserManager;
import org.example.Util;
import org.jetbrains.annotations.NotNull;

public class AdminCommand extends Command {
    @Override
    public MessageContent run(@NotNull SlashCommandInteractionEvent event) throws Exception {
        OptionMapping optionMapping = event.getOption("user");
        if (optionMapping == null) {
            return new MessageContent("Veuillez spécifier un utilisateur !").setAuthor(Main.asarix);
        }
        User user = optionMapping.getAsUser();
        if (UserManager.hasAdmin(user))
            return new MessageContent(user.getAsMention() + " : ce joueur a déjà les permissions administrateur !").setAuthor(Main.asarix);
        UserManager.addAdmin(user);
        return new MessageContent(user.getAsMention() + " a désormais les permissions administrateur !").setAuthor(Main.asarix);
    }

    @Override
    public CommandData data() {
        OptionData data = new OptionData(OptionType.USER, "user",
                "utilisateur", true);
        return Commands.slash("admin", "add admin perms to user").addOptions(data);
    }

    @Override
    public PermLevel permLevel() {
        return PermLevel.ADMIN;
    }
}
