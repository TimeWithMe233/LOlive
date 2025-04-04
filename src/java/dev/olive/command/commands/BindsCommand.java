package dev.olive.command.commands;

import org.lwjglx.input.Keyboard;
import dev.olive.Client;
import dev.olive.command.Command;
import dev.olive.module.Module;
import dev.olive.utils.DebugUtil;

import java.util.ArrayList;
import java.util.List;

public class BindsCommand extends Command {
    public BindsCommand() {
        super("binds");
    }

    @Override
    public List<String> autoComplete(int arg, String[] args) {
        return new ArrayList<>();
    }

    @Override
    public void run(String[] args, String originalMessag) {
        for (Module module : Client.instance.moduleManager.getModuleMap().values()) {

            if (module.getKey() == -1)
                continue;
            DebugUtil.log("§a[Binds]§f" + module.name + " :" + Keyboard.getKeyName(module.key));
        }
    }
}
