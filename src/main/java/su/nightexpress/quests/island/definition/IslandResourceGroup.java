package su.nightexpress.quests.island.definition;

import org.bukkit.Material;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class IslandResourceGroup {
    private final String id;
    private final String name;
    private final Material icon;
    private final Map<Material, Double> materials;

    public IslandResourceGroup(String id, String name, Map<Material, Double> materials) {
        this(id, name, null, materials);
    }

    public IslandResourceGroup(String id, String name, @Nullable Material icon, Map<Material, Double> materials) {
        this.id = id;
        this.name = name;
        this.icon = icon;
        this.materials = materials;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Nullable
    public Material getIcon() {
        return icon;
    }

    public Map<Material, Double> getMaterials() {
        return materials;
    }

    public double getWeight(Material material) {
        return materials.getOrDefault(material, 0.0);
    }

    public boolean contains(Material material) {
        return materials.containsKey(material);
    }
}
