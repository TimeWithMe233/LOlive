package dev.olive.module;

import dev.olive.Client;
import dev.olive.event.annotations.EventTarget;
import dev.olive.event.impl.events.EventClick;
import dev.olive.event.impl.events.EventKey;
import dev.olive.event.impl.events.EventRender2D;
import dev.olive.module.impl.combat.*;
import dev.olive.module.impl.misc.*;
import dev.olive.module.impl.move.*;
import dev.olive.module.impl.player.*;
import dev.olive.module.impl.render.*;
import dev.olive.module.impl.world.*;
import dev.olive.utils.math.Fuckyou;
import dev.olive.value.Value;
import net.optifine.reflect.IFieldLocator;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;


public class ModuleManager {
    public static List<Module> modules = new ArrayList<>();
    private final Map<String, Module> moduleMap = new HashMap<>();

    private boolean enabledNeededMod = true;
    public static List<Module> getModulesInType(Category t) {
        ArrayList<Module> output = new ArrayList<>();
        for (Module m : modules) {
            if (m.getCategory() != t)
                continue;
            output.add(m);
        }
        return output;
    }
    public void init() {
        Client.instance.eventManager.register(this);
        Client.instance.hudManager.init();
        if (!(Fuckyou.isjSF == "IIS1$dkfk@@%!oas!^tasGkGfAkGasrk#^ASFDAykaAsfaw#trasfj")) {
            System.exit(0);
        }
        // combat
        addModule(new KillAura());
        addModule(new Velocity());
        addModule(new SuperKnockBack());
        addModule(new ArmorBreaker());
        addModule(new AutoSoup());
        addModule(new AutoWeapon());
        addModule(new AntiFireBall());
        addModule(new TickBase());
        addModule(new AutoProjectile());
        addModule(new Criticals());
        addModule(new AutoHead());
        addModule(new BackTrack());
        addModule(new BedBreaker());
        addModule(new Gapple());
        // movement
        addModule(new Sprint());
        addModule(new Speed());
  
        addModule(new NoWeb());
        addModule(new GuiMove());
        addModule(new NoLiquid());
        addModule(new NoSlow());
        addModule(new TargetStrafe());
        // player
        addModule(new MidPearl());

        addModule(new AutoLobby());
        addModule(new ChestStealer());
        addModule(new AntiVoid());
        addModule(new FastPlace());
        addModule(new VClip());
        addModule(new Blink());
        addModule(new SpeedMine());
        addModule(new BalanceTimer());
        addModule(new AutoTool());
        addModule(new NoFall());
        addModule(new InvManager());

        // world
        addModule(new Disabler());
        addModule(new Scaffold());
        addModule(new ChestAura());
        addModule(new Stuck());
        addModule(new PlayerTracker());

        addModule(new Ambience());


        // render
        addModule(new ClickGUI());
        addModule(new HUD());
        addModule(new ProgessBar());
        addModule(new ItemESP());
        addModule(new Chams());
        addModule(new TargetESP());
        addModule(new BlockAnimation());
        addModule(new Camera());
        addModule(new ChinaHat());
        addModule(new BetterFPS());
        addModule(new Projectile());
        addModule(new Health());
        addModule(new XRay());
        addModule(new KillEffect());
        addModule(new ItemPhysics());
        addModule(new BAHalo());
        addModule(new ESP());
        addModule(new MotionBlur());

        // misc
        addModule(new AntiBot());
        addModule(new Teams());
        addModule(new Protocol());
        addModule(new AutoPlay());
        addModule(new MemoryFix());
        addModule(new MCF());

        sortModulesByName();
    }

    public void sortModulesByName() {
        List<Map.Entry<String, Module>> entryList = new ArrayList<>(moduleMap.entrySet());
        entryList.sort(Comparator.comparing(entry -> entry.getValue().getName()));

        moduleMap.clear();
        for (Map.Entry<String, Module> entry : entryList) {
            moduleMap.put(entry.getKey(), entry.getValue());
        }
    }


    public void addModule(Module module) {
        for (final Field field : module.getClass().getDeclaredFields()) {
            try {
                field.setAccessible(true);
                final Object obj = field.get(module);
                if (obj instanceof Value) module.getValues().add((Value) obj);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        moduleMap.put(module.getClass().getSimpleName(), module);
    }

    public Map<String, Module> getModuleMap() {
        return moduleMap;
    }

    public <T extends Module> T getModule(Class<T> cls) {
        return cls.cast(moduleMap.get(cls.getSimpleName()));
    }

    public Module getModule(String name) {
        for (Module module : moduleMap.values()) {
            if (module.getName().equalsIgnoreCase(name)) {
                return module;
            }
        }
        return null;
    }

    public boolean haveModules(Category category, String key) {
        return moduleMap.values().stream()
                .filter(module -> module.getCategory() == category)
                .anyMatch(module -> module.getName().toLowerCase().replaceAll(" ", "").contains(key));
    }

    @EventTarget
    public void onKey(EventKey e) {
        moduleMap.values().stream()
                .filter(module -> module.getKey() == e.getKey() && e.getKey() != -1)
                .forEach(Module::toggle);
    }

    @EventTarget
    public void onMouse(EventClick e) {
        moduleMap.values().stream()
                .filter(module -> module.getMouseKey() != -1 && module.getMouseKey() == e.getKey() && e.getKey() != -1)
                .forEach(Module::toggle);
    }

    public List<Module> getModsByPage(Category.Pages m) {
        return moduleMap.values().stream()
                .filter(module -> module.getCategory().pages == m)
                .collect(Collectors.toList());
    }

    public List<Module> getModsByCategory(Category m) {
        return moduleMap.values().stream()
                .filter(module -> module.getCategory() == m)
                .collect(Collectors.toList());
    }

    @EventTarget
    private void on2DRender(EventRender2D e) {
        if (this.enabledNeededMod) {
            this.enabledNeededMod = false;
            moduleMap.values().stream()
                    .filter(Module::isDefaultOn)
                    .forEach(module -> module.setState(true));
        }
    }
}
