package me._3000IQPlay.atrium.features.command.commands;

import me._3000IQPlay.atrium.Atrium;
import me._3000IQPlay.atrium.features.command.Command;

public class UnloadCommand
        extends Command {
    public UnloadCommand() {
        super("unload", new String[0]);
    }

    @Override
    public void execute(String[] commands) {
        Atrium.unload(true);
    }
}

