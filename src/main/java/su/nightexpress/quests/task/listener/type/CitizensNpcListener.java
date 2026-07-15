package su.nightexpress.quests.task.listener.type;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import org.jetbrains.annotations.NotNull;

public class CitizensNpcListener implements Listener {

    private final TalkToNpcTaskListener parent;

    public CitizensNpcListener(@NotNull TalkToNpcTaskListener parent) {
        this.parent = parent;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onNpcRightClick(NPCRightClickEvent event) {
        Player player = event.getClicker();
        if (!parent.getManager().canDoTasks(player)) return;

        int npcId = event.getNPC().getId();
        parent.progress(player, "citizens:" + npcId);
    }
}
