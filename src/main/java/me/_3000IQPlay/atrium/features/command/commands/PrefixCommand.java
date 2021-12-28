package me._3000IQPlay.atrium.features.command.commands;

import me._3000IQPlay.atrium.Atrium;
import me._3000IQPlay.atrium.features.command.Command;
import me._3000IQPlay.atrium.features.modules.client.ClickGui;

public class PrefixCommand
        extends Command {
    public PrefixCommand() {
        super("prefix", new String[]{"<char>"});
    }

    @Override
    public void execute(String[] commands) {
        if (commands.length == 1) {
            Command.sendMessage("\u00a7cSpecify a new prefix.");
            return;
        }
        Atrium.moduleManager.getModuleByClass(ClickGui.class).prefix.setValue(commands[0]);
        Command.sendMessage("Prefix set to \u00a7a" + Atrium.commandManager.getPrefix());
    }
}

