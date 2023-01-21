package ch.asarix.commands;

import ch.asarix.PermLevel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import ch.asarix.Main;
import org.jetbrains.annotations.NotNull;

public class SaveCommand extends Command {
    @Override
    public MessageContent run(@NotNull SlashCommandInteractionEvent event) throws Exception {
        for (User user : Main.map.keySet()) {
            Main.saveAll(user, Main.map.get(user));
        }
        return new MessageContent("Données sauvegardées !");
    }

    @Override
    public CommandData data() {
        return Commands.slash("save", "Sauvegarder les données");
    }

    @Override
    public PermLevel permLevel() {
        return PermLevel.ADMIN;
    }
}
