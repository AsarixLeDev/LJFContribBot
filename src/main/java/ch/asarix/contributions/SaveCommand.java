package ch.asarix.contributions;

import ch.asarix.PermLevel;
import ch.asarix.UserManager;
import ch.asarix.commands.Command;
import ch.asarix.commands.MessageContent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.jetbrains.annotations.NotNull;

public class SaveCommand extends Command {
    @Override
    public MessageContent run(@NotNull SlashCommandInteractionEvent event) throws Exception {
        ContribManager contribManager = ContribManager.get();
        if (!contribManager.finishedLoading) {
            return new MessageContent("Veuillez attendre que les données soient toutes récupérées !");
        }
        for (Long userId : contribManager.map.keySet()) {
            contribManager.save(UserManager.getUser(userId));
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
