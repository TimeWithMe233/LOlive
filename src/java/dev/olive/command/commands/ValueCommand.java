package dev.olive.command.commands;

import dev.olive.Client;
import dev.olive.command.Command;
import dev.olive.module.Module;
import dev.olive.utils.DebugUtil;
import dev.olive.value.Value;
import dev.olive.value.impl.BoolValue;
import dev.olive.value.impl.ModeValue;
import dev.olive.value.impl.NumberValue;
import net.minecraft.util.EnumChatFormatting;

import java.util.ArrayList;
import java.util.List;

public class ValueCommand extends Command {

    public ValueCommand() {
        super(new String[]{"v", "value"});
    }

    @Override
    public List<String> autoComplete(int arg, String[] args) {
        return new ArrayList<>();
    }


    @Override
    public void run(String[] args, String originalMessage) {
        String[] strs = originalMessage.split(" ");
        if (strs.length < 2) {
            alert();
        } else {
            String[] values = strs[1].split("=");
            String[] infos = values[0].split("\\.");
            if (values.length != 2 || infos.length != 2) {
                alert();
            } else {
                Module module = Client.instance.moduleManager.getModule(infos[0]);
                if (module == null) {
                    DebugUtil.log(EnumChatFormatting.RED + "Module " + infos[0] + " not found.");
                } else {
                    for (Value<?> value : module.getValues()) {
                        if (value.getName().equalsIgnoreCase(infos[1])) {
                            try {
                                if (value instanceof BoolValue) {
                                    boolean b = Boolean.parseBoolean(values[1]);
                                    if (!b && !values[1].equalsIgnoreCase("false")) {
                                        throw new Exception();
                                    }
                                    ((BoolValue) value).setValue(b);
                                } else if (value instanceof NumberValue) {
                                    ((NumberValue) value).setValue(Double.valueOf(values[1]));
                                } else if (value instanceof ModeValue) {
                                    ((ModeValue) value).setMode(values[1]);
                                }
                                DebugUtil.log(EnumChatFormatting.GREEN + "Succeed to set module " + module.getName() + "'s value " + value.getName() + " to " + values[1] + ".");
                                return;
                            } catch (Exception e) {
                                DebugUtil.log(EnumChatFormatting.RED + "Cannot set module " + module.getName() + "'s value " + value.getName() + " to " + values[1] + ".");
                                return;
                            }
                        }
                    }

                    DebugUtil.log(EnumChatFormatting.RED + "Module " + module.getName() + "'s value " + infos[1] + " not found.");
                }
            }
        }
    }

    private void alert() {
        DebugUtil.log("Usage: v <Module>.<ValueName>" + "=<NewValue>");
    }
}
