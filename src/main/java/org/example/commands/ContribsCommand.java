package org.example.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.example.Contribution;
import org.example.Main;
import org.example.PermLevel;
import org.example.Util;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class ContribsCommand extends Command {
    @Override
    public MessageContent run(@NotNull SlashCommandInteractionEvent event) throws Exception {
        User user = getOption(event, "user", event.getUser());
        List<User> users = Main.getContribers().stream().sorted(Comparator.comparingLong(Main::getTotalContribValue).reversed()).toList();
        int place = users.indexOf(user);
        if (place < 0) {
            EmbedBuilder builder = Util.authorEmbed(Main.asarix)
                    .setTitle("Contributions")
                    .setDescription(user.getAsMention() + "\n\nAucune contribution !")
                    .setColor(Color.ORANGE);
            return new MessageContent(builder).setAuthor(Main.asarix);
        }
        List<Contribution> contribs = Main.getContribs(user).stream().sorted(Comparator.comparingLong(Contribution::dateMillis).reversed()).toList();
        int i = 1;
        NumberFormat nf = NumberFormat.getInstance(new Locale("en", "US"));
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        EmbedBuilder embedBuilder = Util.authorEmbed(Main.asarix)
                .setTitle("Contributions")
                .setColor(Color.BLUE);
        StringBuilder contribBuilder = new StringBuilder(user.getAsMention() + "\n\n`\uD83C\uDFC6` #" + (place + 1) + "   -   `\uD83D\uDCB0` **" + nf.format(Main.getTotalContribValue(user)) + "**\n\n");
        for (Contribution contribution : contribs) {
            String dateStr = df.format(contribution.date());
            String line = "(" + dateStr + ") " + contribution.contribName();
            if (contribution.amount() > 1)
                line +=  " " + "**x" + contribution.amount() + "**";
            if (!contribution.commentary().isEmpty())
                line += " - " + contribution.commentary();
            contribBuilder.append("` ").append(i++)
                    .append(" `  **")
                    .append(nf.format(contribution.value()))
                    .append("** ").append(line).append("\n");
            //embedBuilder.addField(i++ + ". " + nf.format(contribution.value()), line, false);
        }
        return new MessageContent(embedBuilder.setDescription(contribBuilder.toString())).setAuthor(Main.asarix);
    }

    @Override
    public CommandData data() {
        OptionData userOption = new OptionData(OptionType.USER, "user",
                "valeur en coins", false);
        return Commands.slash("contribs", "liste des contribs").addOptions(userOption);
    }

    @Override
    public PermLevel permLevel() {
        return PermLevel.NONE;
    }
}
