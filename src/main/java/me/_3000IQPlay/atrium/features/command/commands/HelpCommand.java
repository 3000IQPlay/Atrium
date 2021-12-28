package me._3000IQPlay.atrium.features.command.commands;

import me._3000IQPlay.atrium.Atrium;
import me._3000IQPlay.atrium.features.command.Command;

public class HelpCommand
        extends Command {
    public HelpCommand() {
        super("commands");
    }

    @Override
    public void execute(String[] commands) {
        HelpCommand.sendMessage("You can use following commands: ");
        for (Command command : Atrium.commandManager.getCommands()) {
            HelpCommand.sendMessage(Atrium.commandManager.getPrefix() + command.getName());
        }
    }
}

