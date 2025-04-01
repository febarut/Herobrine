package net.saganetwork.herobrine;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class HerobrineSceneListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player p = event.getPlayer();

        if (HerobrineSceneManager.isSceneRunning() && HerobrineSceneManager.hasPreviousLocation(p.getUniqueId())) {
            p.teleport(HerobrineSceneManager.getPreviousLocation(p.getUniqueId()));
            p.sendMessage("§7Herobrine sahnesinden döndün.");
        }
    }
}
