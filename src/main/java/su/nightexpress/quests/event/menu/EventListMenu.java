package su.nightexpress.quests.event.menu;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.MenuType;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nightcore.ui.menu.MenuViewer;
import su.nightexpress.nightcore.ui.menu.type.NormalMenu;
import su.nightexpress.nightcore.util.bukkit.NightItem;
import su.nightexpress.quests.QuestsPlugin;
import su.nightexpress.quests.event.EventQuestManager;
import su.nightexpress.quests.event.definition.EventQuest;

import java.util.ArrayList;
import java.util.List;

public class EventListMenu extends NormalMenu<QuestsPlugin> {

    private final EventQuestManager manager;

    public EventListMenu(@NotNull QuestsPlugin plugin, @NotNull EventQuestManager manager) {
        super(plugin, MenuType.GENERIC_9X3, "Événements Disponibles");
        this.manager = manager;
    }

    @Override
    protected void onReady(@NotNull MenuViewer viewer, @NotNull Inventory inventory) {
        // Ready callback
    }

    @Override
    protected void onPrepare(@NotNull MenuViewer viewer, @NotNull InventoryView view) {
        Player player = viewer.getPlayer();
        List<EventQuest> quests = new ArrayList<>(this.manager.getQuests().values());

        if (quests.isEmpty()) {
            viewer.addItem(NightItem.fromType(Material.BARRIER)
                .setDisplayName("&c&lAucun Événement")
                .setLore(List.of("&7Aucun événement n'est disponible pour le moment."))
                .toMenuItem()
                .setSlots(13)
                .build()
            );
            return;
        }

        int slot = 10;
        for (EventQuest quest : quests) {
            String status = quest.isActive() ? "&aActif" : "&cTerminé";
            NightItem item = NightItem.fromType(quest.getIconMaterial())
                .setDisplayName(quest.getName())
                .setLore(List.of(
                    "&7Statut : " + status,
                    "&7Temps restant : &e" + quest.getSchedule().getTimeLeftFormatted(),
                    "",
                    "&eCliquez pour ouvrir le menu de cet événement !"
                ));

            if (quest.getCustomModelData() > 0) {
                item.setCustomModelData((float) quest.getCustomModelData());
            }

            viewer.addItem(item.toMenuItem()
                .setSlots(slot++)
                .setHandler((v, event) -> this.manager.openEventMenu(player, quest.getId()))
                .build()
            );

            if (slot > 16) break;
        }
    }
}
