package su.nightexpress.quests.event.definition;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class EventQuest {

    private final String id;
    private final String name;
    private final List<String> description;
    private final Material iconMaterial;
    private final int customModelData;
    private final EventSchedule schedule;
    private final String targetType;
    private final Set<Material> targetMaterials;
    private final Map<Integer, EventTier> tiers;
    private final String guiTitle;
    private final int guiSize;

    public EventQuest(
        @NotNull String id,
        @NotNull String name,
        @NotNull List<String> description,
        @NotNull Material iconMaterial,
        int customModelData,
        @NotNull EventSchedule schedule,
        @NotNull String targetType,
        @NotNull Set<Material> targetMaterials,
        @NotNull Map<Integer, EventTier> tiers,
        @NotNull String guiTitle,
        int guiSize
    ) {
        this.id = id;
        this.name = name;
        this.description = new ArrayList<>(description);
        this.iconMaterial = iconMaterial;
        this.customModelData = customModelData;
        this.schedule = schedule;
        this.targetType = targetType;
        this.targetMaterials = new HashSet<>(targetMaterials);
        this.tiers = new TreeMap<>(tiers);
        this.guiTitle = guiTitle;
        this.guiSize = guiSize;
    }

    @NotNull
    public static EventQuest fromYaml(@NotNull ConfigurationSection config) {
        String id = config.getString("id", "unknown_event");
        String name = config.getString("name", id);
        List<String> description = config.getStringList("description");

        String iconStr = config.getString("icon.material", "JACK_O_LANTERN");
        Material icon = Material.matchMaterial(iconStr);
        if (icon == null) icon = Material.PAPER;
        int customModelData = config.getInt("icon.custom-model-data", 0);

        // Schedule
        String modeStr = config.getString("schedule.mode", "CALENDAR").toUpperCase();
        EventSchedule schedule;
        if ("DURATION".equals(modeStr)) {
            int durationHours = config.getInt("schedule.duration-hours", 24);
            schedule = EventSchedule.createDuration(durationHours);
        } else {
            String start = config.getString("schedule.start-date", "");
            String end = config.getString("schedule.end-date", "");
            schedule = EventSchedule.createCalendar(start, end);
        }

        // Target
        String targetType = config.getString("target.type", "PICKUP_ITEM");
        List<String> matStrings = config.getStringList("target.materials");
        Set<Material> materials = new HashSet<>();
        for (String m : matStrings) {
            Material mat = Material.matchMaterial(m.trim());
            if (mat != null) {
                materials.add(mat);
            }
        }

        // Tiers
        Map<Integer, EventTier> tiers = new TreeMap<>();
        ConfigurationSection tiersSec = config.getConfigurationSection("tiers");
        if (tiersSec != null) {
            for (String key : tiersSec.getKeys(false)) {
                try {
                    int tierNum = Integer.parseInt(key);
                    long req = tiersSec.getLong(key + ".requirement", 0L);
                    String displayName = tiersSec.getString(key + ".display-name", "Palier " + tierNum);
                    String tierMatStr = tiersSec.getString(key + ".icon.material", "IRON_INGOT");
                    Material tierMat = Material.matchMaterial(tierMatStr);
                    if (tierMat == null) tierMat = Material.IRON_INGOT;
                    int tierCmd = tiersSec.getInt(key + ".icon.custom-model-data", 0);
                    List<String> rewards = tiersSec.getStringList(key + ".rewards");

                    EventTier tier = new EventTier(tierNum, req, displayName, tierMat, tierCmd, rewards);
                    tiers.put(tierNum, tier);
                } catch (NumberFormatException ignored) {}
            }
        }

        // GUI
        String guiTitle = config.getString("gui.title", "Événement");
        int guiSize = config.getInt("gui.size", 54);

        return new EventQuest(id, name, description, icon, customModelData, schedule, targetType, materials, tiers, guiTitle, guiSize);
    }

    public boolean matchesMaterial(@NotNull Material material) {
        return this.targetMaterials.contains(material);
    }

    public boolean isActive() {
        return this.schedule.isActive();
    }

    public boolean isExpired() {
        return this.schedule.isExpired();
    }

    @NotNull
    public String getId() {
        return id;
    }

    @NotNull
    public String getName() {
        return name;
    }

    @NotNull
    public List<String> getDescription() {
        return Collections.unmodifiableList(description);
    }

    @NotNull
    public Material getIconMaterial() {
        return iconMaterial;
    }

    public int getCustomModelData() {
        return customModelData;
    }

    @NotNull
    public EventSchedule getSchedule() {
        return schedule;
    }

    @NotNull
    public String getTargetType() {
        return targetType;
    }

    @NotNull
    public Set<Material> getTargetMaterials() {
        return Collections.unmodifiableSet(targetMaterials);
    }

    @NotNull
    public Map<Integer, EventTier> getTiers() {
        return Collections.unmodifiableMap(tiers);
    }

    @Nullable
    public EventTier getTier(int tierNumber) {
        return tiers.get(tierNumber);
    }

    @NotNull
    public String getGuiTitle() {
        return guiTitle;
    }

    public int getGuiSize() {
        return guiSize;
    }
}
