package su.nightexpress.quests.island.definition;

import org.bukkit.Material;
import java.util.Map;

public class IslandResourceGroup {
    private final String id;
    private final String name;
    private final Map<Material, Double> materials;

    public IslandResourceGroup(String id, String name, Map<Material, Double> materials) {
        this.id = id;
        this.name = name;
        this.materials = materials;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
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
