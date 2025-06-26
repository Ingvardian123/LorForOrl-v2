package com.lorfororl.listeners;

import com.lorfororl.LorForOrlPlugin;
import com.lorfororl.centrifuge.Centrifuge;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CentrifugeListener implements Listener {
    
    private final LorForOrlPlugin plugin;
    private final Pattern coordinatePattern = Pattern.compile("([A-C][1-3])=(-?\\d+) (-?\\d+) (-?\\d+)");
    
    public CentrifugeListener(LorForOrlPlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        if (item == null || item.getType() != Material.WRITABLE_BOOK) return;
        
        BookMeta bookMeta = (BookMeta) item.getItemMeta();
        if (bookMeta == null || !bookMeta.hasTitle()) return;
        
        // Проверяем название книги
        if (!"[LorForOrl Centrifuge]".equals(bookMeta.getTitle())) return;
        
        // Читаем содержимое книги
        if (!bookMeta.hasPages() || bookMeta.getPages().isEmpty()) return;
        
        String content = String.join("\n", bookMeta.getPages());
        
        // Парсим координаты
        Centrifuge centrifuge = parseCentrifugeFromBook(content);
        if (centrifuge != null) {
            // Активируем центрифугу
            centrifuge.activate();
            plugin.getCentrifugeManager().registerCentrifuge(centrifuge);
            
            // Визуальные эффекты активации
            Location center = centrifuge.getCenter();
            center.getWorld().spawnParticle(org.bukkit.Particle.EXPLOSION, center, 3, 0.5, 0.5, 0.5, 0.1);
            center.getWorld().playSound(center, "lorfororl.centrifuge_start", 1.0f, 1.0f);
        }
        
        event.setCancelled(true);
    }
    
    private Centrifuge parseCentrifugeFromBook(String content) {
        String[] lines = content.split("\n");
        
        Location a2 = null, b1 = null, b3 = null, c2 = null;
        boolean activate = false;
        
        for (String line : lines) {
            line = line.trim();
            
            if (line.equals("ACTIVATE=true")) {
                activate = true;
                continue;
            }
            
            Matcher matcher = coordinatePattern.matcher(line);
            if (matcher.matches()) {
                String pos = matcher.group(1);
                int x = Integer.parseInt(matcher.group(2));
                int y = Integer.parseInt(matcher.group(3));
                int z = Integer.parseInt(matcher.group(4));
                
                Location loc = new Location(null, x, y, z); // World будет установлен позже
                
                switch (pos) {
                    case "A2": a2 = loc; break;
                    case "B1": b1 = loc; break;
                    case "B3": b3 = loc; break;
                    case "C2": c2 = loc; break;
                }
            }
        }
        
        // Проверяем, что все координаты заданы и активация включена
        if (!activate || a2 == null || b1 == null || b3 == null || c2 == null) {
            return null;
        }
        
        // Вычисляем центр (должен быть B2)
        Location center = new Location(a2.getWorld(), 
            (a2.getX() + c2.getX()) / 2, 
            a2.getY(), 
            (b1.getZ() + b3.getZ()) / 2);
        
        // Создаем центрифугу
        Centrifuge centrifuge = new Centrifuge(center);
        centrifuge.setCauldronPositions(a2, b1, b3, c2);
        
        // Проверяем структуру
        if (!centrifuge.validateStructure()) {
            return null;
        }
        
        return centrifuge;
    }
}
