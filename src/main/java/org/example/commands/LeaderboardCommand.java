package org.example.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.restaction.MessageEditAction;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;
import net.dv8tion.jda.api.utils.messages.MessageEditData;
import org.example.Main;
import org.example.PermLevel;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.text.NumberFormat;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class LeaderboardCommand extends Command {
    @Override
    public MessageContent run(@NotNull SlashCommandInteractionEvent event) throws Exception {
        int page = getOption(event, "page", 1);
        return page(page);
    }

    @Override
    public CommandData data() {
        OptionData pageOption = new OptionData(OptionType.INTEGER, "page",
                "numéro de la page", false);
        return Commands.slash("leaderboard", "leaderboard").addOptions(pageOption);
    }

    @Override
    public void onButton(ButtonInteractionEvent event, String id) {
        event.deferEdit().queue();
        String[] elements = id.split("-");
        int page = Integer.parseInt(elements[1]);
        int nextPage = 1;

        int maxPage = (int)Math.ceil(Main.getContribers().size() / 10d);
        if (elements[0].equalsIgnoreCase("prev")) {
            if (page == 1) {
                nextPage = maxPage;
            }
            else {
                nextPage = page - 1;
            }
        }
        else if (elements[0].equalsIgnoreCase("next")) {
            if (page < maxPage) {
                nextPage = page + 1;
            }
        }
        MessageContent newContent = page(nextPage);
        MessageEditData editAction = new MessageEditBuilder()
                .setEmbeds(newContent.getContent().build())
                .setActionRow(newContent.getButtons()).build();

        event.getMessage().editMessage(editAction).queue();
    }

    private MessageContent page(int page) {
        int start = (page-1) * 10;
        int end = page * 10;

        List<User> top = Main.getContribers();
        int totalSize = top.size();
        if (start >= top.size()) {
            return new MessageContent("Page trop grande ! Max : " + (int)Math.ceil(totalSize / 10d)).setAuthor(Main.asarix);
        }
        int max = Math.min(end, top.size());
        top = top.stream().sorted(Comparator.comparingLong(Main::getTotalContribValue).reversed()).toList().subList(start, max);
        int i = 1 + (page-1) * 10;
        NumberFormat nf = NumberFormat.getInstance(new Locale("en", "US"));
        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setTitle("Leaderboard des contribs")
                .setColor(Color.YELLOW);
        StringBuilder builder = new StringBuilder();
        long prevContrib = 0;
        int prevIndex = i;
        for (User user : top) {
            long contrib = Main.getTotalContribValue(user);
            int index = i++;
            if (prevContrib == contrib) index = prevIndex;
            String line = "**" + index + ".** " + user.getAsMention() + " **(" + nf.format(contrib) + ")**";
            builder.append(line).append("\n");
            prevContrib = contrib;
            prevIndex = index;
        }
        builder.append("\nPage ").append(page).append("/").append((int)Math.ceil(totalSize / 10d));
        embedBuilder.setDescription(builder.toString());
        return new MessageContent(embedBuilder)
                .addPrimaryButton("prev-" + page, Emoji.fromUnicode("U+25C0"), this)
                .addPrimaryButton("next-" + page, Emoji.fromUnicode("U+25B6"), this)
                .setAuthor(Main.asarix);
    }

    @Override
    public PermLevel permLevel() {
        return PermLevel.NONE;
    }
}
