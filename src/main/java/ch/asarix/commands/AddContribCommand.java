package ch.asarix.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import ch.asarix.Contribution;
import ch.asarix.Main;
import ch.asarix.PermLevel;
import ch.asarix.Util;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AddContribCommand extends Command {
    @Override
    public MessageContent run(@NotNull SlashCommandInteractionEvent event) throws Exception {
        User user = getOption(event, "user", event.getUser());
        String name = getOption(event, "name", "Money");
        int amount = getOption(event, "amount", 1);
        Date date = null;
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateStr = getOption(event, "date", formatter.format(new Date()));
        if (dateStr == null) {
            date = new Date();
        } else {
            List<String> patterns = List.of(
                    "yyyy-MM-dd HH:mm:ss",
                    "yyyy-MM-dd HH:mm",
                    "yyyy-MM-dd HH",
                    "yyyy-MM-dd",
                    "dd/MM/yyyy"
            );

            int index = 0;
            boolean success = false;
            do {
                formatter = new SimpleDateFormat(patterns.get(index++));
                try {
                    date = formatter.parse(dateStr);
                    success = true;
                } catch (ParseException ignored) {
                }
            }
            while (!success);
        }
        if (date == null) {
            return new MessageContent("Date invalide ! Veuillez préciser le pattern minimum : yyyy-MM-dd").setAuthor(Main.asarix);
        }

        String commentary = getOption(event, "commentary", "");
        long value = getOption(event, "value", -1L);
        if (value < 0) {
            return new MessageContent(event.getOption("value").getAsString() + " : Valeur invalide !").setAuthor(Main.asarix);
        }
        Contribution contribution = Main.createContribution(user, name, amount, date, commentary, value);
        NumberFormat nf = NumberFormat.getInstance(new Locale("en", "US"));
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        EmbedBuilder builder = Util.authorEmbed(Main.asarix)
                .setTitle("Contribution sauvegardée !")
                .addField("Utilisateur", user.getAsMention(), false)
                .addField("Valeur de la contrib", nf.format(value), false)
                .addField("Nom de l'objet contribué", name, false)
                .addField("Nombre d'objets contribués", String.valueOf(amount), false)
                .addField("Commentaire", commentary, false)
                .addField("Date", df.format(date), false)
                .setColor(Color.GREEN);
        return new MessageContent(builder).addDangerButton(user.getId() + "-" + contribution.id(), "Annuler", this);
    }

    @Override
    public CommandData data() {
        OptionData userOption = new OptionData(OptionType.USER, "user",
                "utilisateur discord", false);
        OptionData nameOption = new OptionData(OptionType.STRING, "name",
                "nom de l'objet contribué", false);
        OptionData amountOption = new OptionData(OptionType.INTEGER, "amount",
                "nombre d'objets", false);
        OptionData dateOption = new OptionData(OptionType.STRING, "date",
                "date de la contribution", false);
        OptionData commOption = new OptionData(OptionType.STRING, "commentary",
                "commentaire sur la contribution", false);
        OptionData valueOption = new OptionData(OptionType.STRING, "value",
                "valeur en coins", true);
        return Commands.slash("add", "ajouter une contribution").addOptions(valueOption, userOption, nameOption, amountOption, dateOption, commOption);
    }

    @Override
    public void onButton(ButtonInteractionEvent event, String id) {
        event.deferReply(true).queue();
        String[] elements = id.split("-");
        User user = Main.jda.retrieveUserById(elements[0]).complete();
        Contribution contribution = Main.getContrib(user, elements[1]);
        Main.remove(contribution);
        event.getHook().sendMessage("Contrib de " + user.getAsMention() + " supprimée !").queue();
    }

    @Override
    public PermLevel permLevel() {
        return PermLevel.ACCESS;
    }
}
