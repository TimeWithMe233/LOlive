import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.authlib.properties.PropertyMap.Serializer;
import com.yumegod.obfuscation.CallEncryption;
import com.yumegod.obfuscation.FlowObfuscate;
import com.yumegod.obfuscation.Native;
import com.yumegod.obfuscation.StringObfuscate;
import dev.olive.utils.math.Fuckyou;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import net.minecraft.client.Minecraft;
import net.minecraft.client.main.GameConfiguration;
import net.minecraft.util.Session;

import java.io.File;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.util.List;
@Native
@CallEncryption
@StringObfuscate
@FlowObfuscate
public class Main {
    public static String[] jvmoptions;

    public static void onStop() {
        System.exit(0);
    }


    public static void main(String[] p_main_0_) {
        Fuckyou.p(p_main_0_);
    }



    private static boolean isNullOrEmpty(String str) {
        return str != null && !str.isEmpty();
    }
}
