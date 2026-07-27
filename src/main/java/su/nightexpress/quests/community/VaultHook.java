package su.nightexpress.quests.community;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class VaultHook {

    public static boolean hasVault() {
        return Bukkit.getPluginManager().isPluginEnabled("Vault");
    }

    public static boolean withdraw(Player player, double amount) {
        if (!hasVault()) {
            // Fallback for environment without Vault: accept deposit
            return true;
        }

        try {
            Class<?> economyClass = Class.forName("net.milkbowl.vault.economy.Economy");
            Object rsp = Bukkit.getServicesManager().getRegistration(economyClass);
            if (rsp != null) {
                Object provider = rsp.getClass().getMethod("getProvider").invoke(rsp);
                double balance = (double) economyClass.getMethod("getBalance", org.bukkit.OfflinePlayer.class).invoke(provider, player);
                if (balance >= amount) {
                    Object resp = economyClass.getMethod("withdrawPlayer", org.bukkit.OfflinePlayer.class, double.class).invoke(provider, player, amount);
                    Boolean success = (Boolean) resp.getClass().getMethod("transactionSuccess").invoke(resp);
                    return success != null && success;
                }
            }
        } catch (Throwable e) {
            // Fallback if reflection fails
            return true;
        }
        return false;
    }

    public static double getBalance(Player player) {
        if (!hasVault()) {
            return 1000000.0;
        }

        try {
            Class<?> economyClass = Class.forName("net.milkbowl.vault.economy.Economy");
            Object rsp = Bukkit.getServicesManager().getRegistration(economyClass);
            if (rsp != null) {
                Object provider = rsp.getClass().getMethod("getProvider").invoke(rsp);
                return (double) economyClass.getMethod("getBalance", org.bukkit.OfflinePlayer.class).invoke(provider, player);
            }
        } catch (Throwable ignored) {}
        return 0.0;
    }
}
