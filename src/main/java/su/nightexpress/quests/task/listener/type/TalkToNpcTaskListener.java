package su.nightexpress.quests.task.listener.type;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.quests.QuestsPlugin;
import su.nightexpress.quests.hook.HookPlugin;
import su.nightexpress.quests.task.TaskManager;
import su.nightexpress.quests.task.TaskType;
import su.nightexpress.quests.task.adapter.AdapterFamily;
import su.nightexpress.quests.task.listener.TaskListener;
import su.nightexpress.quests.util.QuestUtils;

public class TalkToNpcTaskListener extends TaskListener<String, AdapterFamily<String>> {

    public TalkToNpcTaskListener(@NotNull QuestsPlugin plugin, @NotNull TaskManager manager, @NotNull TaskType<String, AdapterFamily<String>> taskType) {
        super(plugin, manager, taskType);
        
        if (QuestUtils.isIntegrationAvailable(HookPlugin.CITIZENS)) {
            try {
                Bukkit.getPluginManager().registerEvents(new CitizensNpcListener(this), plugin);
                plugin.info("[TalkToNpcTaskListener] Hooked Citizens successfully.");
            } catch (Throwable t) {
                plugin.warn("[TalkToNpcTaskListener] Failed to hook Citizens: " + t.getMessage());
            }
        }
        
        if (QuestUtils.isIntegrationAvailable(HookPlugin.FANCY_NPCS)) {
            try {
                Bukkit.getPluginManager().registerEvents(new FancyNpcListener(this), plugin);
                plugin.info("[TalkToNpcTaskListener] Hooked FancyNpcs successfully.");
            } catch (Throwable t) {
                plugin.warn("[TalkToNpcTaskListener] Failed to hook FancyNpcs: " + t.getMessage());
            }
        }
    }

    public void progress(@NotNull Player player, @NotNull String npcKey) {
        this.progressQuests(player, npcKey);
    }

    @NotNull
    public TaskManager getManager() {
        return this.manager;
    }
}
