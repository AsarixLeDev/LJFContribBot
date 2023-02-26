package ch.asarix.commands;

import ch.asarix.Main;
import ch.asarix.PermLevel;
import ch.asarix.UserManager;
import ch.asarix.Util;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.concurrent.CompletableFuture;

public abstract class Command {
    public abstract MessageContent run(@NotNull SlashCommandInteractionEvent event) throws Exception;

    public abstract CommandData data();

    public final void execute(@NotNull SlashCommandInteractionEvent event) {
        CompletableFuture.supplyAsync(() -> {
            MessageContent instant;
            try {
                if (!UserManager.hasPerm(event.getUser(), permLevel()))
                    instant = new MessageContent("Vous n'avez pas les permissions pour utiliser cette commande !").setAuthor(Main.asarix);
                else
                    instant = run(event);
            } catch (Exception e) {
                e.printStackTrace();
                event.getHook().sendMessageEmbeds(Util.withText("Il y a eu une erreur ! " + e.getMessage()).setColor(Color.RED).build()).queue();
                return "Failure.";
            }
            if (instant != null) {
//                Message message = new MessageCreateBuilder().addEmbeds(instant.build()).setActionRow();
                event.getHook().sendMessage(instant.build()).queue(instant::startCd);
//                event.getHook().sendMessageEmbeds(instant.getContent().build()).queue();
            }
            return "Done.";
        });
    }

    public void onButton(ButtonInteractionEvent event, String id) {
    }

    public abstract PermLevel permLevel();

    public <T> T getOption(SlashCommandInteractionEvent event, String name, T def) {
        OptionMapping option = event.getOption(name);
        if (option == null) return def;
        Object result = null;
        if (def == null || def instanceof String) {
            result = option.getAsString();
        } else if (def instanceof Double) {
            try {
                result = option.getAsDouble();
            } catch (IllegalStateException ignored) {
            }
        } else if (def instanceof Long) {
            System.out.println("Getting long (" + name + ")");
            try {
                result = option.getAsLong();
            } catch (IllegalStateException | NumberFormatException e) {
                System.out.println(option.getAsString());
                long parsed = parse(option.getAsString());
                System.out.println(parsed);
                if (parsed >= 0)
                    result = parsed;
            }
        } else if (def instanceof Integer) {
            try {
                result = option.getAsInt();
            } catch (IllegalStateException | NumberFormatException e) {
                System.out.println(option.getAsString());
                long parsed = parse(option.getAsString());
                System.out.println(parsed);
                if (parsed >= 0)
                    result = parsed;
            }
        } else if (def instanceof User) {
            try {
                result = option.getAsUser();
            } catch (IllegalStateException ignored) {
            }
        }
        if (result == null) {
            if (def == null) {
                return null;
            } else result = def;
        }

        if (def != null) {
            if (!(result.getClass().isInstance(def))) {
                throw new RuntimeException("L'argument doit être de type " + def.getClass().getSimpleName());
            }
        }
        return (T) result;
    }

    private long parse(String text) {
        try {
            text = text.toLowerCase();
            int bIndex = text.indexOf("b");
            double bValue = bIndex < 0 ? 0 : Double.parseDouble(text.substring(0, bIndex));
            text = text.substring(bIndex + 1);

            if (text.isEmpty()) return toLong(bValue, 0, 0, 0);

            int mIndex = text.indexOf("m");
            double mValue = mIndex < 0 ? 0 : Double.parseDouble(text.substring(0, mIndex));
            text = text.substring(mIndex + 1);

            if (text.isEmpty()) return toLong(bValue, mValue, 0, 0);

            int kIndex = text.indexOf("k");
            double kValue = kIndex < 0 ? 0 : Double.parseDouble(text.substring(0, kIndex));
            text = text.substring(kIndex + 1);

            if (text.isEmpty()) return toLong(bValue, mValue, kValue, 0);

            double cValue = Long.parseLong(text);
            return toLong(bValue, mValue, kValue, cValue);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private long toLong(double bValue, double mValue, double kValue, double cValue) {
        return (long) (1_000_000_000 * bValue + 1_000_000 * mValue + 1_000 * kValue + cValue);
    }

    public <T> T getOption(SlashCommandInteractionEvent event, String name) {
        return getOption(event, name, null);
    }
}
