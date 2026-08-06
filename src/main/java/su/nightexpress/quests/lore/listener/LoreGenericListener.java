package su.nightexpress.quests.lore.listener;

import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.quests.QuestsPlugin;
import su.nightexpress.quests.lore.LoreManager;

public class LoreGenericListener implements Listener {

    private final QuestsPlugin plugin;
    private final LoreManager manager;

    public LoreGenericListener(@NotNull QuestsPlugin plugin, @NotNull LoreManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }
}
