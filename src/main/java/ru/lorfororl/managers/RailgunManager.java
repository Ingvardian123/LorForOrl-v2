package ru.lorfororl.managers;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;
import ru.lorfororl.systems.RailgunSystem;

public class RailgunManager implements Listener {
    private final RailgunSystem railgunSystem;
    
    public RailgunManager(RailgunSystem railgunSystem) {
        this.railgunSystem = railgunSystem;
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        
        if (item == null || !item.hasItemMeta()) {
            return;
        }
        
        String displayName = item.getItemMeta().getDisplayName();
        if (displayName != null && displayName.contains("Рельсотрон")) {
            event.setCancelled(true);
            railgunSystem.fireRailgun(player);
        }
    }
    
    public void giveRailgun(Player player) {
        railgunSystem.giveRailgun(player);
    }
    
    public long getRemainingCooldown(Player player) {
        return railgunSystem.getRemainingCooldownSeconds(player);
    }
    
    public void clearCooldown(Player player) {
        railgunSystem.clearCooldown(player);
    }
}
