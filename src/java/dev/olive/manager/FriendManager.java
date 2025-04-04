package dev.olive.manager;

import lombok.Getter;
import dev.olive.Client;
import dev.olive.ui.hud.notification.NotificationManager;
import dev.olive.ui.hud.notification.NotificationType;

import java.util.ArrayList;

@Getter
public class FriendManager {

    private final ArrayList<String> friends;

    public FriendManager() {
        friends = new ArrayList<>();
    }

    public void add(String name) {
        if (!friends.contains(name)) {
            friends.add(name);
            NotificationManager.post(NotificationType.SUCCESS, "Friend Manager", "Added friend: " + name);
            Client.instance.getConfigManager().saveConfig("friends.json");
        } else {
            NotificationManager.post(NotificationType.DISABLE, "Friend Manager", name + " is already your friend!");
        }
    }

    public void remove(String name) {
        if (friends.contains(name)) {
            friends.remove(name);
            NotificationManager.post(NotificationType.SUCCESS, "Friend Manager", "Removed friend: " + name);
            Client.instance.getConfigManager().saveConfig("friends.json");

        } else {
            NotificationManager.post(NotificationType.DISABLE, "Friend Manager", "Friend not found: " + name);

        }
    }

    public boolean isFriend(String name) {
        return friends.contains(name);
    }

}
