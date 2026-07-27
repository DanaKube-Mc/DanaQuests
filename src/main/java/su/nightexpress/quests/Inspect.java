package su.nightexpress.quests;

import java.lang.reflect.Method;

public class Inspect {
    static {
        try {
            System.err.println("=== INSPECT LiteralNodeBuilder ===");
            for (Method m : Class.forName("su.nightexpress.nightcore.commands.builder.LiteralNodeBuilder").getMethods()) {
                System.err.println("LNB: " + m.getReturnType().getSimpleName() + " " + m.getName() + "(" + java.util.Arrays.toString(m.getParameterTypes()) + ")");
            }
            System.err.println("=== INSPECT MenuItem ===");
            for (Method m : Class.forName("su.nightexpress.nightcore.ui.menu.item.MenuItem").getMethods()) {
                System.err.println("MI: " + m.getReturnType().getSimpleName() + " " + m.getName() + "(" + java.util.Arrays.toString(m.getParameterTypes()) + ")");
            }
            System.err.println("=== INSPECT NightItem ===");
            for (Method m : Class.forName("su.nightexpress.nightcore.util.bukkit.NightItem").getMethods()) {
                System.err.println("NI: " + m.getReturnType().getSimpleName() + " " + m.getName() + "(" + java.util.Arrays.toString(m.getParameterTypes()) + ")");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
