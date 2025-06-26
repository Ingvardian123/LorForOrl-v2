package com.lorfororl.notifications;

import com.lorfororl.LorForOrlPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

import net.kyori.adventure.text.Component;

public class NotificationManager {
    
    private final LorForOrlPlugin plugin;
    private final Map<UUID, Queue<Notification>> playerNotifications;
    private BukkitRunnable notificationTask;
    
    public NotificationManager(LorForOrlPlugin plugin) {
        this.plugin = plugin;
        this.playerNotifications = new HashMap<>();
        startNotificationTask();
    }
    
    private void startNotificationTask() {
        notificationTask = new BukkitRunnable() {
            @Override
            public void run() {
                processNotifications();
            }
        };
        notificationTask.runTaskTimer(plugin, 0L, 20L); // Каждую секунду
    }
    
    public void shutdown() {
        if (notificationTask != null) {
            notificationTask.cancel();
        }
    }
    
    private void processNotifications() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            Queue<Notification> notifications = playerNotifications.get(player.getUniqueId());
            if (notifications == null || notifications.isEmpty()) continue;
            
            Notification notification = notifications.poll();
            if (notification != null) {
                showNotification(player, notification);
            }
        }
    }
    
    private void showNotification(Player player, Notification notification) {
        switch (notification.getType()) {
            case ACTION_BAR:
                sendActionBar(player, notification.getMessage());
                break;
            case CHAT:
                player.sendMessage(notification.getMessage());
                break;
            case TITLE:
                String[] parts = notification.getMessage().split("\\|");
                String title = parts.length > 0 ? parts[0] : "";
                String subtitle = parts.length > 1 ? parts[1] : "";
                player.sendTitle(title, subtitle, 10, 70, 20);
                break;
        }
        
        if (notification.getSound() != null) {
            player.playSound(player.getLocation(), notification.getSound(), 1.0f, 1.0f);
        }
    }
    
    private void sendActionBar(Player player, String message) {
        try {
            player.sendActionBar(net.kyori.adventure.text.Component.text(message));
        } catch (Exception e) {
            // Fallback для старых версий
            player.sendMessage(message);
        }
    }
    
    public void sendNotification(Player player, String message, NotificationType type) {
        sendNotification(player, message, type, null);
    }
    
    public void sendNotification(Player player, String message, NotificationType type, Sound sound) {
        Queue<Notification> notifications = playerNotifications.computeIfAbsent(
            player.getUniqueId(), k -> new LinkedList<>());
        
        notifications.offer(new Notification(message, type, sound));
    }
    
    public void sendRadiationWarning(Player player, double radiation) {
        String message;
        Sound sound;
        
        if (radiation >= 5.0) {
            message = "§4☢ КРИТИЧЕСКАЯ РАДИАЦИЯ! ☢";
            sound = Sound.BLOCK_NOTE_BLOCK_BASS;
        } else if (radiation >= 3.0) {
            message = "§c☢ ОПАСНАЯ РАДИАЦИЯ! ☢";
            sound = Sound.BLOCK_NOTE_BLOCK_PLING;
        } else if (radiation >= 1.5) {
            message = "§e☢ Повышенная радиация ☢";
            sound = Sound.BLOCK_NOTE_BLOCK_CHIME;
        } else {
            return; // Не показываем уведомления для низкой радиации
        }
        
        sendNotification(player, message, NotificationType.ACTION_BAR, sound);
    }
    
    public void sendEnergyWarning(Player player, int energy, int maxEnergy) {
        double percentage = (double) energy / maxEnergy;
        
        if (percentage <= 0.1) {
            sendNotification(player, "§c⚡ КРИТИЧЕСКИ НИЗКИЙ ЗАРЯД! ⚡", 
                NotificationType.ACTION_BAR, Sound.BLOCK_NOTE_BLOCK_BASS);
        } else if (percentage <= 0.25) {
            sendNotification(player, "§e⚡ Низкий заряд ⚡", 
                NotificationType.ACTION_BAR, Sound.BLOCK_NOTE_BLOCK_CHIME);
        }
    }
    
    public void sendStructureComplete(Player player, String structureName) {
        sendNotification(player, 
            "§a§lСТРУКТУРА ЗАВЕРШЕНА!|§e" + structureName, 
            NotificationType.TITLE, 
            Sound.UI_TOAST_CHALLENGE_COMPLETE);
    }
    
    public void sendResearchComplete(Player player, String researchName) {
        sendNotification(player, 
            "§b§lИССЛЕДОВАНИЕ ЗАВЕРШЕНО!|§e" + researchName, 
            NotificationType.TITLE, 
            Sound.UI_TOAST_CHALLENGE_COMPLETE);
    }
    
    public void sendReactorWarning(Player player, int temperature, boolean overheating) {
        if (overheating) {
            if (temperature >= 950) {
                sendNotification(player, "§4⚠ РЕАКТОР ПЕРЕГРЕВАЕТСЯ! ЭВАКУИРУЙТЕСЬ! ⚠", 
                    NotificationType.TITLE, Sound.ENTITY_ENDER_DRAGON_GROWL);
            } else {
                sendNotification(player, "§c⚠ Реактор перегревается! ⚠", 
                    NotificationType.ACTION_BAR, Sound.BLOCK_FIRE_AMBIENT);
            }
        }
    }
    
    public enum NotificationType {
        ACTION_BAR,
        CHAT,
        TITLE
    }
    
    private static class Notification {
        private final String message;
        private final NotificationType type;
        private final Sound sound;
        
        public Notification(String message, NotificationType type, Sound sound) {
            this.message = message;
            this.type = type;
            this.sound = sound;
        }
        
        public String getMessage() { return message; }
        public NotificationType getType() { return type; }
        public Sound getSound() { return sound; }
    }
}
