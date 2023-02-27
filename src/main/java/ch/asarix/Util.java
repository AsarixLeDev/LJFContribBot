package ch.asarix;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;

import java.text.*;
import java.util.*;

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

    public static Date tryParseDate(String dateStr) {
        if (dateStr == null) return null;
        SimpleDateFormat formatter;
        Date date = null;
        String[] patterns = {
                "yyyy-MM-dd HH:mm:ss",
                "yyyy-MM-dd HH:mm",
                "yyyy-MM-dd HH",
                "yyyy-MM-dd",
                "dd/MM/yyyy"
        };
        for (String pattern : patterns) {
            formatter = new SimpleDateFormat(pattern);
            try {
                date = formatter.parse(dateStr);
                break;
            } catch (ParseException ignored) {
            }
        }
        return date;
    }

    public static String round(double numb, int dec) {
        double x = Math.pow(10.0, dec);
        double roundOff = Math.round(numb * x) / x;
        return f(roundOff);
    }

    private static String f(double numb) {
        DecimalFormat formatter = (DecimalFormat) NumberFormat.getInstance(Locale.US);
        DecimalFormatSymbols symbols = formatter.getDecimalFormatSymbols();

        symbols.setGroupingSeparator(' ');
        formatter.setDecimalFormatSymbols(symbols);
        return formatter.format(numb);
    }

    public static String firstCap(String word) {
        String fLetter = word.substring(0, 1).toUpperCase();
        return fLetter + word.substring(1).toLowerCase();
    }

    public static String getMonth() {
        int monthNumber = Calendar.getInstance().get(Calendar.MONTH);
        DateFormatSymbols dfs = new DateFormatSymbols();
        String[] months = dfs.getMonths();
        return firstCap(months[monthNumber]);
    }
}
