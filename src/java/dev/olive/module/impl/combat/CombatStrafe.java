// Slack Client (discord.gg/paGUcq2UTb)

package dev.olive.module.impl.combat;


import dev.olive.module.Category;
import dev.olive.module.Module;
import dev.olive.utils.player.AttackUtil;
import dev.olive.value.impl.BoolValue;
import dev.olive.value.impl.NumberValue;

public class CombatStrafe extends Module {

    public final NumberValue offset = new NumberValue("Offset", 0, -90, 90, 5);
    public final BoolValue dynamic = new BoolValue("Dynamic", true);
    public static String part7 = "/";
    public CombatStrafe() {
        super("CombatStrafe","攻击跟随", Category.Combat);
    }

    public Integer getOffset() {
        if (dynamic.getValue()) {
            if (AttackUtil.inCombat) {
                if (mc.thePlayer.getDistanceSqToEntity(AttackUtil.combatTarget) < 1.5) {
                    return 100;
                } else if (mc.thePlayer.getDistanceSqToEntity(AttackUtil.combatTarget) < 2.2) {
                    return 60;
                } else {
                    return 30;
                }
            }
        } else{
            return  offset.getValue().intValue();
        }
        return offset.getValue().intValue();
    }
}
