package ch.asarix.seasons;

import ch.asarix.PermLevel;
import ch.asarix.Util;
import ch.asarix.commands.Command;
import ch.asarix.commands.MessageContent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;

import java.util.Date;

public class CreateSeasonCommand extends Command {
    @Override
    public MessageContent run(@NotNull SlashCommandInteractionEvent event) throws Exception {
        String fromStr = getOption(event, "from");
        String toStr = getOption(event, "to");
        Date from = Util.tryParseDate(fromStr);
        if (from == null) {
            return new MessageContent("Date invalide !");
        }
        Date to = Util.tryParseDate(toStr);
        if (to == null) {
            return new MessageContent("Date invalide !");
        }
        if (from.getTime() >= to.getTime()) {
            return new MessageContent("La date initiale doit être strictement antérieure à la date finale !");
        }
        String name = getOption(event, "name");
        SeasonManager.get().createSeason(name, from.getTime(), to.getTime());
        return new MessageContent("Saison " + name + " créée avec succès !");
    }

    @Override
    public CommandData data() {
        OptionData seasonNameOption = new OptionData(OptionType.STRING, "name",
                "nom de la saison", false);
        OptionData fromOption = new OptionData(OptionType.STRING, "from",
                "date initiale", true);
        OptionData toOption = new OptionData(OptionType.STRING, "to",
                "date finale", true);
        return Commands.slash("createseason", "Créer une saison").addOptions(fromOption, toOption, seasonNameOption);
    }

    @Override
    public PermLevel permLevel() {
        return PermLevel.ADMIN;
    }
}
