package su.nightexpress.quests.event.menu;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.MenuType;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nightcore.ui.menu.MenuViewer;
import su.nightexpress.nightcore.ui.menu.item.MenuItem;
import su.nightexpress.nightcore.ui.menu.type.NormalMenu;
import su.nightexpress.nightcore.util.bukkit.NightItem;
import su.nightexpress.quests.QuestsPlugin;
import su.nightexpress.quests.event.EventQuestManager;
import su.nightexpress.quests.event.data.EventLeaderboard;
import su.nightexpress.quests.event.data.EventUserData;
import su.nightexpress.quests.event.definition.EventQuest;
import su.nightexpress.quests.event.definition.EventTier;
import su.nightexpress.quests.util.MenuUtils;

import java.util.*;

public class EventQuestMenu extends NormalMenu<QuestsPlugin> {

    public enum TierState {
        LOCKED,
        CLAIMABLE,
        CLAIMED
    }

    private final EventQuestManager manager;
    private final EventQuest quest;

    private static final int[] TIER_SLOTS = new int[]{19, 20, 21, 22, 23, 24, 25};

    public EventQuestMenu(@NotNull QuestsPlugin plugin, @NotNull EventQuestManager manager, @NotNull EventQuest quest) {
        super(plugin, MenuType.GENERIC_9X6, quest.getGuiTitle());
        this.manager = manager;
        this.quest = quest;
    }

    @NotNull
    public static TierState resolveTierState(@NotNull EventUserData data, @NotNull EventTier tier) {
        if (data.isTierClaimed(tier.getTierNumber())) {
            return TierState.CLAIMED;
        }
        if (data.canClaim(tier)) {
            return TierState.CLAIMABLE;
        }
        return TierState.LOCKED;
    }

    @Override
    protected void onReady(@NotNull MenuViewer viewer, @NotNull Inventory inventory) {
        // Ready callback
    }

    @Override
    protected void onPrepare(@NotNull MenuViewer viewer, @NotNull InventoryView view) {
        Player player = viewer.getPlayer();
        EventUserData data = this.manager.getUserData(player.getUniqueId(), this.quest.getId());
        EventLeaderboard leaderboard = this.manager.getLeaderboard(this.quest.getId());

        long score = data.getTotalScore();
        int rank = leaderboard.getPlayerRank(player.getUniqueId(), score);
        String rankStr = rank > 0 ? "#" + rank : "Non classé";
        String statusStr = this.quest.isActive() ? "&aActif" : "&cTerminé";
        String timeLeftStr = this.quest.getSchedule().getTimeLeftFormatted();

        // 1. En-tête / Header (Slot 4)
        List<String> headerLore = new ArrayList<>();
        headerLore.add("&7Statut : " + statusStr);
        headerLore.add("&7Temps restant : &e" + timeLeftStr);
        headerLore.add("");
        headerLore.add("&fVotre Score : &6" + String.format("%,d", score));
        headerLore.add("&fVotre Rang : &e" + rankStr);
        headerLore.add("");
        for (String desc : this.quest.getDescription()) {
            headerLore.add(desc);
        }

        NightItem headerItem = NightItem.fromType(this.quest.getIconMaterial())
            .setDisplayName(this.quest.getName())
            .setLore(headerLore);
        if (this.quest.getCustomModelData() > 0) {
            headerItem.setCustomModelData((float) this.quest.getCustomModelData());
        }

        viewer.addItem(headerItem.toMenuItem().setSlots(4).build());

        // 2. Ligne des Paliers
        List<EventTier> tierList = new ArrayList<>(this.quest.getTiers().values());
        for (int i = 0; i < tierList.size(); i++) {
            if (i >= TIER_SLOTS.length) break;

            EventTier tier = tierList.get(i);
            int slot = TIER_SLOTS[i];
            TierState state = resolveTierState(data, tier);

            NightItem tierItem = NightItem.fromType(tier.getIconMaterial())
                .setDisplayName(tier.getDisplayName());
            if (tier.getCustomModelData() > 0) {
                tierItem.setCustomModelData((float) tier.getCustomModelData());
            }

            List<String> lore = new ArrayList<>();
            lore.add("&7Exigence : &6" + String.format("%,d", tier.getRequirement()) + " points");
            lore.add("");

            switch (state) {
                case LOCKED -> {
                    double progress = Math.min(1.0, (double) score / tier.getRequirement());
                    String bar = MenuUtils.buildProgressBar(progress);
                    lore.add("&c&l🔒 Bloqué");
                    lore.add("&7Progression : &e" + String.format("%,d", score) + " &7/ &6" + String.format("%,d", tier.getRequirement()));
                    lore.add(bar);
                    lore.add("");
                    lore.add("&7Récompenses :");
                    for (String r : tier.getRewards()) {
                        lore.add(" &8• &f" + r);
                    }
                }
                case CLAIMABLE -> {
                    lore.add("&e&l★ Débloqué (À Réclamer !)");
                    lore.add("&aCliquez pour récupérer votre récompense !");
                    lore.add("");
                    lore.add("&7Récompenses :");
                    for (String r : tier.getRewards()) {
                        lore.add(" &8• &f" + r);
                    }
                    tierItem.setEnchantGlint(true);
                }
                case CLAIMED -> {
                    lore.add("&a&l✔ Réclamé");
                    lore.add("&7Vous avez déjà collecté les récompenses de ce palier.");
                }
            }

            tierItem.setLore(lore);

            MenuItem.Builder builder = tierItem.toMenuItem().setSlots(slot);
            if (state == TierState.CLAIMABLE) {
                builder.setHandler((v, event) -> {
                    if (this.manager.claimTier(player, this.quest, tier.getTierNumber())) {
                        this.open(player);
                    }
                });
            }
            viewer.addItem(builder.build());
        }

        // 3. Panneau Leaderboard (Slot 49)
        List<String> lbLore = new ArrayList<>();
        lbLore.add("&7Top 10 des meilleurs contributeurs :");
        lbLore.add("");
        for (int rankIdx = 1; rankIdx <= 10; rankIdx++) {
            String pName = leaderboard.getTopPlayerName(rankIdx);
            long pScore = leaderboard.getTopPlayerScore(rankIdx);
            if (!"---".equals(pName)) {
                lbLore.add("&e#" + rankIdx + " &f" + pName + " &7- &6" + String.format("%,d", pScore) + " pts");
            } else {
                lbLore.add("&8#" + rankIdx + " ---");
            }
        }
        lbLore.add("");
        lbLore.add("&7Votre position : &e" + rankStr);

        NightItem lbItem = NightItem.fromType(Material.PLAYER_HEAD)
            .setDisplayName("&6&lClassement de l'Événement")
            .setLore(lbLore);

        viewer.addItem(lbItem.toMenuItem().setSlots(49).build());

        // 4. Bouton Fermer (Slot 53)
        viewer.addItem(NightItem.fromType(Material.BARRIER)
            .setDisplayName("&c&lFermer")
            .toMenuItem()
            .setSlots(53)
            .setHandler((v, event) -> player.closeInventory())
            .build()
        );
    }
}
