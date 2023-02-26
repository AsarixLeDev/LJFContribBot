package ch.asarix.seasons;

import ch.asarix.Main;
import ch.asarix.PermLevel;
import ch.asarix.commands.Command;
import ch.asarix.commands.MessageContent;
import ch.asarix.contributions.ContribManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;
import net.dv8tion.jda.api.utils.messages.MessageEditData;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class SeasonLeaderboardCommand extends Command {
    @Override
    public MessageContent run(@NotNull SlashCommandInteractionEvent event) throws Exception {
        String seasonName = getOption(event, "name");
        Season season = SeasonManager.get().getSeason(seasonName);
        if (season == null) {
            return new MessageContent("La saison " + seasonName + " n'existe pas !").setColor(Color.RED);
        }
        int page = getOption(event, "page", 1);
        return page(event.getGuild(), season, page, true);
    }

    @Override
    public CommandData data() {
        OptionData seasonNameOption = new OptionData(OptionType.STRING, "name",
                "nom de la saison", true);
        OptionData pageOption = new OptionData(OptionType.INTEGER, "page",
                "numéro de la page", false);
        return Commands.slash("season", "Créer une saison").addOptions(seasonNameOption, pageOption);
    }

    @Override
    public PermLevel permLevel() {
        return PermLevel.NONE;
    }

    @Override
    public void onButton(ButtonInteractionEvent event, String id) {
        event.deferEdit().queue();
        String[] elements = id.split("-");
        int page = Integer.parseInt(elements[1]);
        Season season = SeasonManager.get().getSeason(elements[2]);
        if (season == null) {
            String errorMsg = "Il y a eu une erreur ! La saison " + elements[2] + " n'a pas pu être trouvée !";
            MessageEditData editAction = new MessageEditBuilder()
                    .setEmbeds(new EmbedBuilder().setDescription(errorMsg).setColor(Color.RED).build()).build();
            event.getMessage().editMessage(editAction).queue();
            return;
        }
        if (elements[0].equalsIgnoreCase("onlymembers")) {
            MessageContent newContent = page(event.getGuild(), season, page, true);
            MessageEditData editAction = new MessageEditBuilder()
                    .setEmbeds(newContent.getContent().build())
                    .setActionRow(newContent.getButtons()).build();

            event.getMessage().editMessage(editAction).queue();
            return;
        }
        if (elements[0].equalsIgnoreCase("everyuser")) {
            MessageContent newContent = page(event.getGuild(), season, page, false);
            MessageEditData editAction = new MessageEditBuilder()
                    .setEmbeds(newContent.getContent().build())
                    .setActionRow(newContent.getButtons()).build();

            event.getMessage().editMessage(editAction).queue();
            return;
        }

        boolean onlyMembers = true;
        for (Button button : event.getMessage().getButtons()) {
            if (button.getId() == null) continue;
            if (button.getId().toLowerCase().contains("onlymembers")) {
                onlyMembers = false;
                break;
            }
            if (button.getId().toLowerCase().contains("everyuser")) {
                break;
            }
        }
        int nextPage = 1;

        final boolean onlyMembersFinal = onlyMembers;

        int totalContribers = ContribManager.get().getContribers()
                .stream()
                .filter(u -> event.getGuild().isMember(u) || !onlyMembersFinal)
                .toList()
                .size();
        int maxPage = (int) Math.ceil(totalContribers / 10d);
        if (elements[0].equalsIgnoreCase("prev")) {
            if (page == 1) {
                nextPage = maxPage;
            } else {
                nextPage = page - 1;
            }
        } else if (elements[0].equalsIgnoreCase("next")) {
            if (page < maxPage) {
                nextPage = page + 1;
            }
        }
        MessageContent newContent = page(event.getGuild(), season, nextPage, onlyMembers);
        MessageEditData editAction = new MessageEditBuilder()
                .setEmbeds(newContent.getContent().build())
                .setActionRow(newContent.getButtons()).build();

        event.getMessage().editMessage(editAction).queue();
    }

    private MessageContent page(Guild guild, Season season, int page, boolean onlyMembers) {
        List<User> top = ContribManager.get().top(season).stream()
                .filter(u -> (guild.isMember(u) || !onlyMembers))
                .collect(Collectors.toList());

        int start = (page - 1) * 10;
        if (start >= top.size()) {
            page -= 1;
            start = (int) Math.ceil(top.size() / 10d);
        }
        int end = Math.min(page * 10, top.size());
        int totalSize = top.size();
        top = top.subList(start, end);

        NumberFormat nf = NumberFormat.getInstance(new Locale("en", "US"));
        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setTitle("Leaderboard des contribs")
                .setColor(Color.YELLOW);
        StringBuilder builder = new StringBuilder();
        int i = 1 + (page - 1) * 10;
        long prevContrib = 0;
        int prevIndex = i;
        for (User user : top) {
            long contrib = ContribManager.get().getTotalContribValue(user);
            int index = i++;
            if (prevContrib == contrib) index = prevIndex;
            String userName = guild.isMember(user) ? user.getAsMention() : "__" + user.getAsTag() + "__";
            String line = "**" + index + ".** " + userName + " **(" + nf.format(contrib) + ")**";
            builder.append(line).append("\n");
            prevContrib = contrib;
            prevIndex = index;
        }
        builder.append("\nPage ").append(page).append("/").append((int) Math.ceil(totalSize / 10d));
        embedBuilder.setDescription(builder.toString());
        return new MessageContent(embedBuilder)
                .addPrimaryButton("prev-" + page + "-" + season.name(), Emoji.fromUnicode("U+25C0"), this)
                .addPrimaryButton("next-" + page + "-" + season.name(), Emoji.fromUnicode("U+25B6"), this)
                .addSecondaryButton(
                        (onlyMembers ? "everyuser" : "onlymembers") + "-" + page + "-" + season.name(),
                        onlyMembers ? "Montrer tous les utilisateurs" : "Ne montrer que les membres",
                        this
                )
                .setAuthor(Main.asarix);
    }
}
