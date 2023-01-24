package ch.asarix;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;

import java.util.ArrayList;
import java.util.List;

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

    public static boolean isBlank(String input) {
        char[] chars = input.toCharArray();
        for (char c : chars) {
            if (c != ' ')
                return false;
        }
        return true;
    }

    public static List<User> toUserList(Object[] list) {
        List<User> users = new ArrayList<>();
        for (Object object : list) {
            users.add((User) object);
        }
        return users;
    }

    public static List<Contribution> toContribList(Object[] list) {
        List<Contribution> contribs = new ArrayList<>();
        for (Object object : list) {
            contribs.add((Contribution) object);
        }
        return contribs;
    }
}
