package ch.asarix.seasons;

import ch.asarix.Main;
import ch.asarix.PermLevel;
import ch.asarix.commands.Command;
import ch.asarix.commands.MessageContent;
import ch.asarix.contributions.ContribManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SeasonCommand extends Command {
    @Override
    public MessageContent run(@NotNull SlashCommandInteractionEvent event) throws Exception {
        String seasonName = getOption(event, "name");
        Season season = SeasonManager.get().getSeason(seasonName);
        if (season == null) {
            return new MessageContent("La saison " + seasonName + " n'existe pas !").setColor(Color.RED);
        }
        String from = new SimpleDateFormat("dd.MM.yy").format(new Date(season.fromMillis()));
        String to = new SimpleDateFormat("dd.MM.yy").format(new Date(season.toMillis()));
        List<User> topSeason = ContribManager.get().top(season);
        int totalSize = topSeason.size();
        topSeason = topSeason.subList(0, Math.min(topSeason.size(), 5));
        NumberFormat nf = NumberFormat.getInstance(new Locale("en", "US"));
        StringBuilder top = new StringBuilder();
        int i = 1;
        long prevContrib = 0;
        int prevIndex = i;
        for (User user : topSeason) {
            long contrib = ContribManager.get().getTotalContribValue(user);
            int index = i++;
            if (prevContrib == contrib) index = prevIndex;
            String userName = event.getGuild().isMember(user) ? user.getAsMention() : "__" + user.getAsTag() + "__";
            String line = "**" + index + ".** " + userName + " **(" + nf.format(contrib) + ")**";
            top.append(line).append("\n");
            prevContrib = contrib;
            prevIndex = index;
        }
        EmbedBuilder builder = new EmbedBuilder()
                .setTitle(season.name())
                .addField("Depuis", from, false)
                .addField("Jusqu'à", to, false)
                .addField("Contributeurs", String.valueOf(totalSize), false)
                .setDescription(top);
        return new MessageContent(builder).setAuthor(Main.asarix);
    }

    @Override
    public CommandData data() {
        OptionData seasonNameOption = new OptionData(OptionType.STRING, "name",
                "nom de la saison", true);
        return Commands.slash("season", "Créer une saison").addOptions(seasonNameOption);
    }

    @Override
    public PermLevel permLevel() {
        return PermLevel.NONE;
    }
}
