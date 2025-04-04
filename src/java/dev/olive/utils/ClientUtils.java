/*
 * Decompiled with CFR 0.151.
 */
package dev.olive.utils;


import com.google.gson.JsonObject;

import java.util.List;

import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.client.resources.LanguageManager;
import net.minecraft.util.IChatComponent;

public class ClientUtils
        implements IMinecraft {
    public static void displayChatMessage(String message) {
        if (ClientUtils.mc.thePlayer == null) {
            return;
        }
        String s = "[\u00a73MCP LITE\u00a7f] " + message;
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("text", s);
        ClientUtils.mc.thePlayer.addChatMessage(IChatComponent.Serializer.jsonToComponent(jsonObject.toString()));
    }

    public static void displayClearChatMessage(String message) {
        if (ClientUtils.mc.thePlayer == null) {
            return;
        }
        String s = message;
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("text", s);
        ClientUtils.mc.thePlayer.addChatMessage(IChatComponent.Serializer.jsonToComponent(jsonObject.toString()));
    }

    public static boolean nullCheck() {
        return ClientUtils.mc.thePlayer == null || ClientUtils.mc.theWorld == null;
    }

    public static void reloadLanguage(IResourceManager reloadListener, List<IResourceManagerReloadListener> reloadListeners) {
        for (IResourceManagerReloadListener iresourcemanagerreloadlistener : reloadListeners) {
            if (!(iresourcemanagerreloadlistener instanceof LanguageManager)) continue;
            iresourcemanagerreloadlistener.onResourceManagerReload(reloadListener);
        }
    }
}

