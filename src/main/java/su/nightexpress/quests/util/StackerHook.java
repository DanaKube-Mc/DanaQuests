package su.nightexpress.quests.util;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

public class StackerHook {

    public static int getEntityStackAmount(@Nullable Entity entity) {
        if (!(entity instanceof LivingEntity living)) return 1;

        // 1. WildStacker API
        try {
            Class<?> apiClass = Class.forName("com.bgsoftware.wildstacker.api.WildStackerAPI");
            Object stackedEntity = apiClass.getMethod("getStackedEntity", LivingEntity.class).invoke(null, living);
            if (stackedEntity != null) {
                Object amountObj = stackedEntity.getClass().getMethod("getStackAmount").invoke(stackedEntity);
                if (amountObj instanceof Number num && num.intValue() > 0) {
                    return num.intValue();
                }
            }
        } catch (Throwable ignored) {}

        // 2. RoseStacker API
        try {
            Class<?> apiClass = Class.forName("dev.rosewood.rosestacker.api.RoseStackerAPI");
            Object instance = apiClass.getMethod("getInstance").invoke(null);
            Object stackedEntity = apiClass.getMethod("getStackedEntity", Entity.class).invoke(instance, entity);
            if (stackedEntity != null) {
                Object amountObj = stackedEntity.getClass().getMethod("getStackSize").invoke(stackedEntity);
                if (amountObj instanceof Number num && num.intValue() > 0) {
                    return num.intValue();
                }
            }
        } catch (Throwable ignored) {}

        // 3. Fallback: Entity Metadata
        if (entity.hasMetadata("wildstacker:amount")) {
            try {
                int amount = entity.getMetadata("wildstacker:amount").get(0).asInt();
                if (amount > 0) return amount;
            } catch (Throwable ignored) {}
        }
        if (entity.hasMetadata("quantity")) {
            try {
                int amount = entity.getMetadata("quantity").get(0).asInt();
                if (amount > 0) return amount;
            } catch (Throwable ignored) {}
        }

        return 1;
    }
}
