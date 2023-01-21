package ch.asarix;

import net.dv8tion.jda.api.entities.User;

import java.util.Date;
import java.util.Objects;

public record Contribution(User user, String contribName, int amount, Date date, String commentary, long value, String id) {
    @Override
    public String toString() {
        return "Contribution{" +
                "user=" + user +
                ", contribName='" + contribName + '\'' +
                ", amount=" + amount +
                ", date=" + dateMillis() +
                ", commentary='" + commentary + '\'' +
                ", value=" + value +
                '}';
    }

    public long dateMillis() {
        return date.getTime();
    }

//    public static Contribution parse(String text) throws ParseException {
//        if (!text.startsWith("Contribution")) throw new ParseException("Could not parse", 0);
//        User user = null;
//        String contribName = null;
//        Integer amount = null;
//        Date date = null;
//        String commentary = null;
//        Long value = null;
//        Pattern MY_PATTERN = Pattern.compile("\\(id=(.*?)\\)");
//        Matcher m = MY_PATTERN.matcher(text);
//        while (m.find()) {
//            String s = m.group(1);
//            try {
//                user = Main.jda.retrieveUserById(Long.parseLong(s)).complete();
//                break;
//            }
//            catch (NumberFormatException ignored) {}
//        }
//        MY_PATTERN = Pattern.compile("contribName=(.*?)\\,");
//        m = MY_PATTERN.matcher(text);
//        while (m.find()) {
//            contribName = m.group(1);
//        }
//        MY_PATTERN = Pattern.compile("amount=(.*?)\\,");
//        m = MY_PATTERN.matcher(text);
//        while (m.find()) {
//            String s = m.group(1);
//            try {
//                amount = Integer.parseInt(s);
//                break;
//            }
//            catch (NumberFormatException ignored) {}
//        }
//        MY_PATTERN = Pattern.compile("date=(.*?)\\,");
//        m = MY_PATTERN.matcher(text);
//        while (m.find()) {
//            try {
//                long dateMillis = Long.parseLong(m.group(1));
//                date = new Date(dateMillis);
//                break;
//            }
//            catch (NumberFormatException ignored) {}
//        }
//        MY_PATTERN = Pattern.compile("commentary=(.*?)\\,");
//        m = MY_PATTERN.matcher(text);
//        while (m.find()) {
//            commentary = m.group(1);
//        }
//        MY_PATTERN = Pattern.compile("value=(.*?)\\}");
//        m = MY_PATTERN.matcher(text);
//        while (m.find()) {
//            try {
//                value = Long.parseLong(m.group(1));
//                break;
//            }
//            catch (NumberFormatException ignored) {}
//        }
//        if (user == null || contribName == null || amount == null || date == null || commentary == null || value == null) {
//            throw new ParseException("Could not parse", -1);
//        }
//        return new Contribution(user, contribName, amount, date, commentary, value);
//    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Contribution that = (Contribution) o;
        if (that.toString().equals(toString())) return true;
        return amount == that.amount && value == that.value && user.equals(that.user) && contribName.equals(that.contribName) && date.equals(that.date) && commentary.equals(that.commentary);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user, contribName, amount, date, commentary, value);
    }
}
