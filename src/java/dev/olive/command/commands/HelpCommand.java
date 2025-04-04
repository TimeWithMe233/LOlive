package dev.olive.command.commands;

import dev.olive.Client;
import dev.olive.command.Command;
import dev.olive.utils.DebugUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HelpCommand extends Command {
    public HelpCommand() {
        super("help", "h");
    }

    @Override
    public List<String> autoComplete(int arg, String[] args) {
        return new ArrayList<>();
    }

    @Override
    public void run(String[] args, String originalMessag) {
        DebugUtil.log("§a[Commands]:§f");
        for (Command command : Client.instance.commandManager.getCommands()) {
            DebugUtil.log("§e." + Arrays.toString(command.getNames()));
        }
    }
}
