package ch.asarix.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;
import net.dv8tion.jda.api.utils.messages.MessageEditData;
import net.dv8tion.jda.internal.interactions.component.StringSelectMenuImpl;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MessageContent {

    private final List<Button> buttons;
    private final List<SelectMenu> selectMenus;
    private final EmbedBuilder content;

    public MessageContent(String content) {
        this(new EmbedBuilder().setDescription(content));
    }

    public MessageContent(EmbedBuilder content) {
        this.content = content;
        this.buttons = new ArrayList<>();
        this.selectMenus = new ArrayList<>();
    }

    public MessageContent setAuthor(User user) {
        this.content.setFooter("By " + user.getAsTag(), user.getAvatarUrl());
        return this;
    }

    public MessageContent addDangerButton(String id, String label, Command command) {
        this.buttons.add(Button.danger(command.data().getName() + "-" + id, label));
        return this;
    }

    public MessageContent addPrimaryButton(String id, String label, Command command) {
        this.buttons.add(Button.primary(command.data().getName() + "-" + id, label));
        return this;
    }


    public MessageContent addPrimaryButton(String id, Emoji emoji, Command command) {
        this.buttons.add(Button.primary(command.data().getName() + "-" + id, emoji));
        return this;
    }

    public MessageContent addSecondaryButton(String id, String label, Command command) {
        this.buttons.add(Button.secondary(command.data().getName() + "-" + id, label));
        return this;
    }

    public MessageContent addSelectMenu(String id, String placeHolder, String... options) {
        List<SelectOption> options1 = new ArrayList<>();
        for (String option : options) {
            options1.add(SelectOption.of(option, option));
        }
        this.selectMenus.add(new StringSelectMenuImpl(id, placeHolder, 1, 1, false, options1));
        return this;
    }

    public MessageContent setColor(Color color) {
        content.setColor(color);
        return this;
    }

    public MessageCreateData build() {
        MessageCreateBuilder builder = new MessageCreateBuilder().addEmbeds(content.build());
        List<ItemComponent> components = new ArrayList<>();
        components.addAll(buttons);
        components.addAll(selectMenus);
        if (!components.isEmpty()) {
            builder.addActionRow(components);
        }

        return builder.build();
    }

    public void startCd(Message message) {
        new Timer().schedule(
                new TimerTask() {
                    @Override
                    public void run() {
                        MessageEditData editAction = new MessageEditBuilder()
                                .setEmbeds(getContent().build()).build();

                        message.editMessage(editAction).setComponents().queue();
                    }
                },
                30000
        );
    }

    public EmbedBuilder getContent() {
        return content;
    }

    public List<Button> getButtons() {
        return buttons;
    }
}
