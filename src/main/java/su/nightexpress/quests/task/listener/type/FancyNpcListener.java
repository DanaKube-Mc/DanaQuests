package su.nightexpress.quests.task.listener.type;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import de.oliver.fancynpcs.api.events.NpcInteractEvent;
import org.jetbrains.annotations.NotNull;

public class FancyNpcListener implements Listener {

    private final TalkToNpcTaskListener parent;

    public FancyNpcListener(@NotNull TalkToNpcTaskListener parent) {
        this.parent = parent;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onNpcInteract(NpcInteractEvent event) {
        Player player = event.getPlayer();
        if (!parent.getManager().canDoTasks(player)) return;

        String npcName = event.getNpc().getData().getName();
        parent.progress(player, "fancynpcs:" + npcName);
    }
}
