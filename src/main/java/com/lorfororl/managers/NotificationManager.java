package com.lorfororl.managers;

import com.lorfororl.LorForOrlPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.entity.Player;
import org.bukkit.Sound;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.Duration;
import java.util.*;

public class NotificationManager {
    
    private final LorForOrlPlugin plugin;
    private final Map<UUID, Queue<Notification>> playerNotifications;
    private final Map<UUID, Long> lastNotificationTime;
    private BukkitRunnable notificationTask;
    
    public NotificationManager(LorForOrlPlugin plugin) {
        this.plugin = plugin;
        this.playerNotifications = new HashMap<>();
        this.lastNotificationTime = new HashMap<>();
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
        playerNotifications.clear();
        lastNotificationTime.clear();
    }
    
    private void processNotifications() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            Queue<Notification> notifications = playerNotifications.get(player.getUniqueId());
            if (notifications == null || notifications.isEmpty()) continue;
            
            // Проверяем, прошло ли достаточно времени с последнего уведомления
            long lastTime = lastNotificationTime.getOrDefault(player.getUniqueId(), 0L);
            if (System.currentTimeMillis() - lastTime < 1000) continue; // Минимум 1 секунда между уведомлениями
            
            Notification notification = notifications.poll();
            if (notification != null) {
                showNotification(player, notification);
                lastNotificationTime.put(player.getUniqueId(), System.currentTimeMillis());
            }
        }
    }
    
    private void showNotification(Player player, Notification notification) {
        switch (notification.getType()) {
            case ACTION_BAR:
                sendActionBar(player, notification.getMessage());
                break;
            case CHAT:
                sendMessage(player, notification.getMessage());
                break;
            case TITLE:
                String[] parts = notification.getMessage().split("\\|");
                String title = parts.length > 0 ? parts[0] : "";
                String subtitle = parts.length > 1 ? parts[1] : "";
                sendTitle(player, title, subtitle);
                break;
            case POPUP:
                sendPopupMessage(player, notification.getMessage());
                break;
        }
        
        if (notification.getSound() != null) {
            player.playSound(player.getLocation(), notification.getSound(), 1.0f, notification.getPitch());
        }
    }
    
    public void sendMessage(Player player, String message) {
        Component component = Component.text(message)
            .color(NamedTextColor.WHITE);
        player.sendMessage(component);
    }
    
    public void sendColoredMessage(Player player, String message, NamedTextColor color) {
        Component component = Component.text(message)
            .color(color);
        player.sendMessage(component);
    }
    
    public void sendActionBar(Player player, String message) {
        Component component = Component.text(message)
            .color(NamedTextColor.YELLOW);
        player.sendActionBar(component);
    }
    
    public void sendTitle(Player player, String title, String subtitle) {
        Component titleComponent = Component.text(title)
            .color(NamedTextColor.GOLD)
            .decorate(TextDecoration.BOLD);
        
        Component subtitleComponent = Component.text(subtitle)
            .color(NamedTextColor.YELLOW);
        
        Title titleObj = Title.title(
            titleComponent,
            subtitleComponent,
            Title.Times.times(
                Duration.ofMillis(500),  // fade in
                Duration.ofSeconds(3),   // stay
                Duration.ofMillis(500)   // fade out
            )
        );
        
        player.showTitle(titleObj);
    }
    
    public void sendPopupMessage(Player player, String message) {
        // Отправляем как сообщение в чат с особым форматированием
        Component popup = Component.text("┌─────────────────────────────────┐")
            .color(NamedTextColor.GRAY)
            .append(Component.newline())
            .append(Component.text("│ ").color(NamedTextColor.GRAY))
            .append(Component.text(message).color(NamedTextColor.WHITE))
            .append(Component.text(" │").color(NamedTextColor.GRAY))
            .append(Component.newline())
            .append(Component.text("└─────────────────────────────────┘").color(NamedTextColor.GRAY));
        
        player.sendMessage(popup);
    }
    
    public void queueNotification(Player player, String message, NotificationType type) {
        queueNotification(player, message, type, null, 1.0f);
    }
    
    public void queueNotification(Player player, String message, NotificationType type, Sound sound) {
        queueNotification(player, message, type, sound, 1.0f);
    }
    
    public void queueNotification(Player player, String message, NotificationType type, Sound sound, float pitch) {
        Queue<Notification> notifications = playerNotifications.computeIfAbsent(
            player.getUniqueId(), k -> new LinkedList<>());
        
        notifications.offer(new Notification(message, type, sound, pitch));
        
        // Ограничиваем размер очереди
        while (notifications.size() > 10) {
            notifications.poll();
        }
    }
    
    public void sendSuccessMessage(Player player, String message) {
        sendColoredMessage(player, "✓ " + message, NamedTextColor.GREEN);
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
    }
    
    public void sendErrorMessage(Player player, String message) {
        sendColoredMessage(player, "✗ " + message, NamedTextColor.RED);
        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
    }
    
    public void sendWarningMessage(Player player, String message) {
        sendColoredMessage(player, "⚠ " + message, NamedTextColor.YELLOW);
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1.0f, 1.0f);
    }
    
    public void sendInfoMessage(Player player, String message) {
        sendColoredMessage(player, "ℹ " + message, NamedTextColor.AQUA);
    }
    
    public void broadcastMessage(String message) {
        Component component = Component.text("[LorForOrl] " + message)
            .color(NamedTextColor.LIGHT_PURPLE);
        
        plugin.getServer().broadcast(component);
    }
    
    public void sendRadiationWarning(Player player, double radiationLevel) {
        if (radiationLevel <= 0) return;
        
        String message;
        Sound sound;
        NotificationType type;
        
        if (radiationLevel >= 5.0) {
            message = "§4☢ КРИТИЧЕСКАЯ РАДИАЦИЯ! ☢ " + String.format("%.1f рад/с", radiationLevel);
            sound = Sound.BLOCK_NOTE_BLOCK_BASS;
            type = NotificationType.TITLE;
        } else if (radiationLevel >= 3.0) {
            message = "§c☢ ОПАСНАЯ РАДИАЦИЯ! ☢ " + String.format("%.1f рад/с", radiationLevel);
            sound = Sound.BLOCK_NOTE_BLOCK_PLING;
            type = NotificationType.ACTION_BAR;
        } else if (radiationLevel >= 1.5) {
            message = "§e☢ Повышенная радиация ☢ " + String.format("%.1f рад/с", radiationLevel);
            sound = Sound.BLOCK_NOTE_BLOCK_CHIME;
            type = NotificationType.ACTION_BAR;
        } else {
            message = "§7☢ " + String.format("%.1f рад/с", radiationLevel);
            sound = null;
            type = NotificationType.ACTION_BAR;
        }
        
        queueNotification(player, message, type, sound);
    }
    
    public void sendEnergyWarning(Player player, int energyLevel) {
        int maxEnergy = plugin.getEnergyManager().getPlayerMaxEnergy(player);
        double percentage = (double) energyLevel / maxEnergy;
        
        if (percentage <= 0.05) {
            queueNotification(player, "§c⚡ КРИТИЧЕСКИ НИЗКИЙ ЗАРЯД! ⚡ " + energyLevel + "/" + maxEnergy, 
                NotificationType.TITLE, Sound.BLOCK_NOTE_BLOCK_BASS, 0.5f);
        } else if (percentage <= 0.15) {
            queueNotification(player, "§e⚡ Низкий заряд ⚡ " + energyLevel + "/" + maxEnergy, 
                NotificationType.ACTION_BAR, Sound.BLOCK_NOTE_BLOCK_CHIME, 1.0f);
        }
    }
    
    public void sendStructureComplete(Player player, String structureName) {
        queueNotification(player, 
            "§a§lСТРУКТУРА ЗАВЕРШЕНА!|§e" + structureName, 
            NotificationType.TITLE, 
            Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f);
    }
    
    public void sendResearchComplete(Player player, String researchName) {
        queueNotification(player, 
            "§b§lИССЛЕДОВАНИЕ ЗАВЕРШЕНО!|§e" + researchName, 
            NotificationType.TITLE, 
            Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.2f);
    }
    
    public void sendReactorWarning(Player player, int temperature, boolean overheating) {
        if (overheating) {
            if (temperature >= 950) {
                queueNotification(player, "§4⚠ РЕАКТОР ПЕРЕГРЕВАЕТСЯ! ЭВАКУИРУЙТЕСЬ! ⚠|§cТемпература: " + temperature + "°C", 
                    NotificationType.TITLE, Sound.ENTITY_ENDER_DRAGON_GROWL, 0.5f);
            } else {
                queueNotification(player, "§c⚠ Реактор перегревается! ⚠ Температура: " + temperature + "°C", 
                    NotificationType.ACTION_BAR, Sound.BLOCK_FIRE_AMBIENT, 0.8f);
            }
        }
    }
    
    public void sendAchievementUnlocked(Player player, String achievementName, String description) {
        queueNotification(player, 
            "§6§lДОСТИЖЕНИЕ РАЗБЛОКИРОВАНО!|§e" + achievementName, 
            NotificationType.TITLE, 
            Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.5f);
        
        sendSuccessMessage(player, "Достижение: " + achievementName + " - " + description);
    }
    
    public void sendSystemMessage(Player player, String system, String message) {
        String formattedMessage = String.format("§7[§b%s§7] §f%s", system, message);
        sendMessage(player, formattedMessage);
    }
    
    public void sendDebugMessage(Player player, String message) {
        if (player.hasPermission("lorfororl.debug")) {
            sendColoredMessage(player, "[DEBUG] " + message, NamedTextColor.GRAY);
        }
    }
    
    public void sendCriticalAlert(Player player, String message) {
        // Критические уведомления показываются немедленно
        sendTitle(player, "§4§lКРИТИЧЕСКОЕ ПРЕДУПРЕЖДЕНИЕ", "§c" + message);
        player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 2.0f, 0.5f);
        
        // Также добавляем в чат
        sendColoredMessage(player, "§4§l[КРИТИЧНО] §c" + message, NamedTextColor.RED);
    }
    
    public void clearNotifications(Player player) {
        Queue<Notification> notifications = playerNotifications.get(player.getUniqueId());
        if (notifications != null) {
            notifications.clear();
        }
    }
    
    public int getQueuedNotificationsCount(Player player) {
        Queue<Notification> notifications = playerNotifications.get(player.getUniqueId());
        return notifications != null ? notifications.size() : 0;
    }
    
    public enum NotificationType {
        ACTION_BAR,
        CHAT,
        TITLE,
        POPUP
    }
    
    private static class Notification {
        private final String message;
        private final NotificationType type;
        private final Sound sound;
        private final float pitch;
        
        public Notification(String message, NotificationType type, Sound sound, float pitch) {
            this.message = message;
            this.type = type;
            this.sound = sound;
            this.pitch = pitch;
        }
        
        public String getMessage() { return message; }
        public NotificationType getType() { return type; }
        public Sound getSound() { return sound; }
        public float getPitch() { return pitch; }
    }
}
