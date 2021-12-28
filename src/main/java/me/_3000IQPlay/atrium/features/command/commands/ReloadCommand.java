package me._3000IQPlay.atrium.features.command.commands;

import me._3000IQPlay.atrium.Atrium;
import me._3000IQPlay.atrium.features.command.Command;

public class ReloadCommand
        extends Command {
    public ReloadCommand() {
        super("reload", new String[0]);
    }

    @Override
    public void execute(String[] commands) {
        Atrium.reload();
    }
}

