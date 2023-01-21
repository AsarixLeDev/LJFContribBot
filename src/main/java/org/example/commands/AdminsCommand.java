package org.example.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.example.Main;
import org.example.PermLevel;
import org.example.UserManager;
import org.example.Util;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.List;

public class AdminsCommand extends Command {
    @Override
    public MessageContent run(@NotNull SlashCommandInteractionEvent event) throws Exception {
        List<User> admins = UserManager.getAdmins();
        EmbedBuilder eBuilder = Util.authorEmbed(Main.asarix).setColor(Color.RED).setTitle("Liste des admins");
        StringBuilder builder = new StringBuilder("**__Liste des admins__**\n\n");
        for (User user : admins) {
            builder.append("**")
                    .append(user.getName())
                    .append("** (").append(user.getAsTag())
                    .append(")\n");
        }
        return new MessageContent(eBuilder.setDescription(builder.toString())).setAuthor(Main.asarix);
    }

    @Override
    public CommandData data() {
        return Commands.slash("admins", "list of admins");
    }

    @Override
    public PermLevel permLevel() {
        return PermLevel.NONE;
    }
}
