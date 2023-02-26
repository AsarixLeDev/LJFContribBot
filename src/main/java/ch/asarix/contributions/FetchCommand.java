package ch.asarix.contributions;

import ch.asarix.DataManager;
import ch.asarix.Main;
import ch.asarix.PermLevel;
import ch.asarix.commands.Command;
import ch.asarix.commands.MessageContent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.jetbrains.annotations.NotNull;

import java.util.Timer;
import java.util.TimerTask;

public class FetchCommand extends Command {
    @Override
    public MessageContent run(@NotNull SlashCommandInteractionEvent event) throws Exception {
        for (DataManager dataManager : Main.dataManagers) {
            if (dataManager.finishedLoading) {
                dataManager.clearData();
                dataManager.init();
            }
            event.getChannel().sendMessage(dataManager.getClass().getSimpleName() + " : " + "-".repeat(10))
                    .queue(message -> {
                        TimerTask fetchRunnable = new TimerTask() {
                            public void run() {
                                if (dataManager.loadPercentage == 1) {
                                    message.editMessage(dataManager.getClass().getSimpleName() + " : Terminé !").queue();
                                    this.cancel();
                                    return;
                                }
                                int x = (int) Math.round(dataManager.loadPercentage * 10);
                                int percentage = (int) Math.round(dataManager.loadPercentage * 100);
                                message.editMessage(dataManager.getClass().getSimpleName() + " : "
                                        + "#".repeat(x) + "-".repeat(10 - x) + " (" + percentage + "%)").queue();
                            }
                        };

                        Timer timer = new Timer();
                        timer.schedule(fetchRunnable, 0, 1000);
                    });
        }
        return new MessageContent("Données en cours de récupération...");
    }

    @Override
    public CommandData data() {
        return Commands.slash("fetch", "Récupérer les données");
    }

    @Override
    public PermLevel permLevel() {
        return PermLevel.ADMIN;
    }
}
