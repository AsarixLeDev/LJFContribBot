package org.example.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

import java.util.ArrayList;
import java.util.List;

public class MessageContent {

    private EmbedBuilder content;
    private final List<Button> buttons;

    public MessageContent(String content) {
        this(new EmbedBuilder().setDescription(content));
    }

    public MessageContent(EmbedBuilder content) {
        this.content = content;
        buttons = new ArrayList<>();
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

    public MessageCreateData build() {
        MessageCreateBuilder builder = new MessageCreateBuilder().addEmbeds(content.build());
        if (!buttons.isEmpty()) {
            builder.addActionRow(buttons);
        }
        return builder.build();
    }

    public EmbedBuilder getContent() {
        return content;
    }

    public List<Button> getButtons() {
        return buttons;
    }
}
