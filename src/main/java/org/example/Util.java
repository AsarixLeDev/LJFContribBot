package org.example;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;

public class Util {
    public static EmbedBuilder authorEmbed(User user) {
        return new EmbedBuilder().setFooter("By " + user.getAsTag(), user.getAvatarUrl());
    }

    public static EmbedBuilder withText(String text) {
        return new EmbedBuilder().setDescription(text);
    }

    public static EmbedBuilder withText(User author, String text) {
        return authorEmbed(author).setDescription(text);
    }
}
