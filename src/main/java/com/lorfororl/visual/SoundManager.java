package com.lorfororl.visual;

import com.lorfororl.LorForOrlPlugin;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public class SoundManager {
    
    private final LorForOrlPlugin plugin;
    private final Map<String, SoundSequence> activeSoundSequences;
    private final Map<String, AmbientSound> ambientSounds;
    private final Map<UUID, PlayerSoundSettings> playerSettings;
    private BukkitTask updateTask;
    
    public SoundManager(LorForOrlPlugin plugin) {
        this.plugin = plugin;
        this.activeSoundSequences = new ConcurrentHashMap<>();
        this.ambientSounds = new ConcurrentHashMap<>();
        this.playerSettings = new ConcurrentHashMap<>();
        startUpdateTask();
    }
    
    private void startUpdateTask() {
        updateTask = new BukkitRunnable() {
            @Override
            public void run() {
                updateSoundSystems();
            }
        }.runTaskTimer(plugin, 0L, 5L); // Каждые 0.25 секунды
    }
    
    private void updateSoundSystems() {
        // Обновляем звуковые последовательности
        Iterator<Map.Entry<String, SoundSequence>> seqIterator = activeSoundSequences.entrySet().iterator();
        while (seqIterator.hasNext()) {
            Map.Entry<String, SoundSequence> entry = seqIterator.next();
            SoundSequence sequence = entry.getValue();
            
            if (sequence.isFinished()) {
                seqIterator.remove();
            } else {
                sequence.update();
            }
        }
        
        // Обновляем окружающие звуки
        for (AmbientSound ambient : ambientSounds.values()) {
            ambient.update();
        }
    }
    
    public void playLaboratoryActivationSequence(Location location) {
        SoundSequence sequence = new SoundSequence("lab_activation_" + System.currentTimeMillis());
        
        // Основная последовательность активации
        sequence.addSound(0, location, Sound.BLOCK_BEACON_POWER_SELECT, 1.2f, 0.8f);
        sequence.addSound(15, location, Sound.BLOCK_CONDUIT_ACTIVATE, 1.5f, 1.0f);
        sequence.addSound(30, location, Sound.BLOCK_BEACON_ACTIVATE, 1.8f, 1.2f);
        sequence.addSound(45, location, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
        sequence.addSound(60, location, Sound.ENTITY_PLAYER_LEVELUP, 0.8f, 1.5f);
        
        // Эхо-эффекты для глубины звука
        sequence.addSound(5, location.clone().add(2, 0, 0), Sound.BLOCK_BEACON_POWER_SELECT, 0.4f, 0.6f);
        sequence.addSound(20, location.clone().add(-2, 0, 0), Sound.BLOCK_CONDUIT_ACTIVATE, 0.5f, 0.8f);
        sequence.addSound(35, location.clone().add(0, 0, 2), Sound.BLOCK_BEACON_ACTIVATE, 0.6f, 1.0f);
        
        // Дополнительные технологические звуки
        for (int i = 0; i < 8; i++) {
            sequence.addSound(10 + i * 5, location, Sound.BLOCK_NOTE_BLOCK_CHIME, 
                0.3f + (i * 0.05f), 1.5f + (i * 0.1f));
        }
        
        // Финальные гармонические звуки
        sequence.addSound(70, location, Sound.BLOCK_BELL_USE, 0.8f, 1.0f);
        sequence.addSound(75, location, Sound.BLOCK_BELL_USE, 0.6f, 1.2f);
        sequence.addSound(80, location, Sound.BLOCK_BELL_USE, 0.4f, 1.5f);
        
        activeSoundSequences.put(sequence.getId(), sequence);
    }
    
    public void playReactorMeltdownSequence(Location location) {
        SoundSequence sequence = new SoundSequence("reactor_meltdown_" + System.currentTimeMillis());
        
        // Предупредительная сирена с нарастающей интенсивностью
        for (int i = 0; i < 15; i++) {
            float volume = 1.0f + (i * 0.1f);
            float pitch = 0.5f + (i * 0.02f);
            sequence.addSound(i * 8, location, Sound.BLOCK_NOTE_BLOCK_BASS, volume, pitch);
            
            // Дополнительные тревожные звуки
            if (i % 3 == 0) {
                sequence.addSound(i * 8 + 2, location, Sound.BLOCK_REDSTONE_TORCH_BURNOUT, 0.8f, 0.3f);
            }
        }
        
        // Звуки перегрева и кипения
        for (int i = 0; i < 20; i++) {
            sequence.addSound(60 + i * 3, location, Sound.BLOCK_FIRE_AMBIENT, 
                1.2f + (i * 0.05f), 0.6f + (i * 0.02f));
            sequence.addSound(62 + i * 3, location, Sound.BLOCK_LAVA_POP, 
                0.8f, 0.8f + (i * 0.03f));
        }
        
        // Критическое состояние
        sequence.addSound(120, location, Sound.ENTITY_ENDER_DRAGON_GROWL, 2.5f, 0.3f);
        sequence.addSound(125, location, Sound.ENTITY_WITHER_SPAWN, 2.0f, 0.5f);
        
        // Взрывная последовательность
        sequence.addSound(140, location, Sound.ENTITY_GENERIC_EXPLODE, 2.5f, 0.2f);
        sequence.addSound(142, location, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 2.5f, 0.4f);
        sequence.addSound(145, location, Sound.ENTITY_DRAGON_FIREBALL_EXPLODE, 2.5f, 0.3f);
        
        // Послевзрывные звуки с затуханием
        for (int i = 0; i < 30; i++) {
            float volume = 2.0f - (i * 0.06f);
            float pitch = 0.5f + (i * 0.01f);
            sequence.addSound(150 + i * 4, location, Sound.BLOCK_FIRE_AMBIENT, 
                Math.max(0.1f, volume), pitch);
        
            // Звуки падающих обломков
            if (i % 4 == 0) {
                sequence.addSound(152 + i * 4, location, Sound.BLOCK_STONE_BREAK, 
                    Math.max(0.2f, volume * 0.8f), 0.8f + (i * 0.02f));
            }
        }
        
        activeSoundSequences.put(sequence.getId(), sequence);
    }
    
    public void playPowerArmorActivationSequence(Player player) {
        Location location = player.getLocation();
        SoundSequence sequence = new SoundSequence("armor_activation_" + player.getUniqueId());
        
        // Звуки активации брони
        sequence.addSound(0, location, Sound.ITEM_ARMOR_EQUIP_NETHERITE, 1.0f, 0.8f);
        sequence.addSound(10, location, Sound.BLOCK_BEACON_ACTIVATE, 1.2f, 1.5f);
        sequence.addSound(20, location, Sound.BLOCK_CONDUIT_ACTIVATE, 1.0f, 1.2f);
        sequence.addSound(30, location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 2.0f);
        
        // Энергетические звуки
        for (int i = 0; i < 5; i++) {
            sequence.addSound(15 + i * 3, location, Sound.BLOCK_NOTE_BLOCK_CHIME, 
                0.5f, 1.0f + (i * 0.2f));
        }
        
        activeSoundSequences.put(sequence.getId(), sequence);
    }
    
    public void playRailgunChargeSequence(Player player, String mode) {
        Location location = player.getEyeLocation();
        SoundSequence sequence = new SoundSequence("railgun_charge_" + player.getUniqueId());
        
        // Звуки зарядки в зависимости от режима
        switch (mode.toLowerCase()) {
            case "standard":
                for (int i = 0; i < 15; i++) {
                    float pitch = 0.5f + (i * 0.1f);
                    sequence.addSound(i * 2, location, Sound.BLOCK_NOTE_BLOCK_HARP, 0.3f, pitch);
                }
                sequence.addSound(30, location, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.5f, 1.2f);
                break;
                
            case "overcharge":
                for (int i = 0; i < 25; i++) {
                    float pitch = 0.3f + (i * 0.08f);
                    float volume = 0.2f + (i * 0.03f);
                    sequence.addSound(i * 2, location, Sound.BLOCK_BEACON_AMBIENT, volume, pitch);
                }
                sequence.addSound(50, location, Sound.ENTITY_DRAGON_FIREBALL_EXPLODE, 2.0f, 0.5f);
                break;
                
            case "emp":
                for (int i = 0; i < 20; i++) {
                    float pitch = 2.0f - (i * 0.05f);
                    sequence.addSound(i * 2, location, Sound.BLOCK_NOTE_BLOCK_BELL, 0.4f, pitch);
                }
                sequence.addSound(40, location, Sound.BLOCK_BEACON_DEACTIVATE, 1.5f, 0.5f);
                break;
        }
        
        activeSoundSequences.put(sequence.getId(), sequence);
    }
    
    public void playResearchCompleteSequence(Player player, String researchName) {
        Location location = player.getLocation();
        SoundSequence sequence = new SoundSequence("research_complete_" + player.getUniqueId());
        
        // Звуки завершения исследования
        sequence.addSound(0, location, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
        sequence.addSound(10, location, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.2f);
        sequence.addSound(20, location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.5f);
        
        // Магические звуки для книг знаний
        for (int i = 0; i < 8; i++) {
            sequence.addSound(5 + i * 3, location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 
                0.6f, 1.0f + (i * 0.1f));
        }
        
        // Финальный аккорд
        sequence.addSound(35, location, Sound.BLOCK_NOTE_BLOCK_CHIME, 1.0f, 2.0f);
        
        activeSoundSequences.put(sequence.getId(), sequence);
    }
    
    public void startReactorAmbientSound(Location location, String reactorId) {
        AmbientSound ambient = new AmbientSound(reactorId, location) {
            private int tick = 0;
            
            @Override
            public void update() {
                if (tick % 60 == 0) { // Каждые 3 секунды
                    playSound(Sound.BLOCK_BEACON_AMBIENT, 0.8f, 0.8f);
                }
                
                if (tick % 100 == 0) { // Каждые 5 секунд
                    playSound(Sound.BLOCK_FIRE_AMBIENT, 0.3f, 0.6f);
                }
                
                // Случайные звуки работы реактора
                if (ThreadLocalRandom.current().nextInt(200) == 0) {
                    playSound(Sound.BLOCK_PISTON_EXTEND, 0.4f, 0.5f);
                }
                
                tick++;
            }
        };
        
        ambientSounds.put(reactorId, ambient);
    }
    
    public void startLaboratoryAmbientSound(Location location, String labId) {
        AmbientSound ambient = new AmbientSound(labId, location) {
            private int tick = 0;
            
            @Override
            public void update() {
                if (tick % 80 == 0) { // Каждые 4 секунды
                    playSound(Sound.BLOCK_CONDUIT_AMBIENT, 0.5f, 1.2f);
                }
                
                if (tick % 120 == 0) { // Каждые 6 секунд
                    playSound(Sound.BLOCK_BEACON_AMBIENT, 0.3f, 1.5f);
                }
                
                // Звуки исследований
                if (ThreadLocalRandom.current().nextInt(300) == 0) {
                    playSound(Sound.BLOCK_ENCHANTMENT_TABLE_USE, 0.4f, 1.0f);
                }
                
                tick++;
            }
        };
        
        ambientSounds.put(labId, ambient);
    }
    
    public void startSolarPanelAmbientSound(Location location, String panelId) {
        AmbientSound ambient = new AmbientSound(panelId, location) {
            private int tick = 0;
            
            @Override
            public void update() {
                // Звуки только днем
                if (location.getWorld() != null && location.getWorld().getTime() < 12300) {
                    if (tick % 150 == 0) { // Каждые 7.5 секунд
                        playSound(Sound.BLOCK_NOTE_BLOCK_CHIME, 0.2f, 2.0f);
                    }
                    
                    // Тихие электрические звуки
                    if (ThreadLocalRandom.current().nextInt(400) == 0) {
                        playSound(Sound.BLOCK_REDSTONE_TORCH_BURNOUT, 0.1f, 1.8f);
                    }
                }
                
                tick++;
            }
        };
        
        ambientSounds.put(panelId, ambient);
    }
    
    public void playExplosionSequence(Location location, float power) {
        SoundSequence sequence = new SoundSequence("explosion_" + System.currentTimeMillis());
        
        // Основной взрыв
        sequence.addSound(0, location, Sound.ENTITY_GENERIC_EXPLODE, power, 0.8f);
        sequence.addSound(2, location, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, power * 0.8f, 0.6f);
        
        // Эхо
        sequence.addSound(10, location, Sound.ENTITY_GENERIC_EXPLODE, power * 0.5f, 0.6f);
        sequence.addSound(20, location, Sound.ENTITY_GENERIC_EXPLODE, power * 0.3f, 0.4f);
        
        // Обломки
        for (int i = 0; i < 10; i++) {
            sequence.addSound(5 + i * 2, location, Sound.BLOCK_STONE_BREAK, 
                0.5f + ThreadLocalRandom.current().nextFloat() * 0.5f, 
                0.8f + ThreadLocalRandom.current().nextFloat() * 0.4f);
        }
        
        activeSoundSequences.put(sequence.getId(), sequence);
    }
    
    public void play3DSound(Player player, Sound sound, Location location, float volume, float pitch) {
        PlayerSoundSettings settings = playerSettings.get(player.getUniqueId());
        if (settings != null && !settings.isEnabled()) {
            return;
        }
        
        // Расширенное 3D позиционирование
        Location playerLoc = player.getLocation();
        double distance = playerLoc.distance(location);
        
        // Рассчитываем направленность звука
        Vector toSound = location.toVector().subtract(playerLoc.toVector()).normalize();
        Vector playerDirection = playerLoc.getDirection();
        double dot = toSound.dot(playerDirection);
        
        // Применяем затухание по расстоянию
        float distanceMultiplier = Math.max(0.05f, 1.0f - (float)(distance / 48.0));
        
        // Применяем направленность (звук сзади тише)
        float directionalMultiplier = (float)(0.7f + 0.3f * Math.max(0, dot));
        
        float finalVolume = volume * distanceMultiplier * directionalMultiplier;
        
        // Применяем настройки игрока
        if (settings != null) {
            finalVolume *= settings.getVolume();
        }
        
        // Небольшая случайная вариация для реализма
        float pitchVariation = pitch + (ThreadLocalRandom.current().nextFloat() - 0.5f) * 0.1f;
        
        player.playSound(location, sound, SoundCategory.MASTER, finalVolume, pitchVariation);
    }
    
    public void setPlayerSoundSettings(Player player, boolean enabled, float volume) {
        playerSettings.put(player.getUniqueId(), new PlayerSoundSettings(enabled, volume));
    }
    
    public void stopAmbientSound(String soundId) {
        ambientSounds.remove(soundId);
    }
    
    public void stopAllPlayerSounds(Player player) {
        // Останавливаем все звуковые последовательности для игрока
        activeSoundSequences.entrySet().removeIf(entry -> 
            entry.getKey().contains(player.getUniqueId().toString()));
    }
    
    public void shutdown() {
        if (updateTask != null && !updateTask.isCancelled()) {
            updateTask.cancel();
        }
        
        activeSoundSequences.clear();
        ambientSounds.clear();
        playerSettings.clear();
    }
    
    // Классы для звуковых систем
    private class SoundSequence {
        private final String id;
        private final Map<Integer, List<ScheduledSound>> sounds;
        private int currentTick;
        private boolean finished;
        
        public SoundSequence(String id) {
            this.id = id;
            this.sounds = new HashMap<>();
            this.currentTick = 0;
            this.finished = false;
        }
        
        public void addSound(int tick, Location location, Sound sound, float volume, float pitch) {
            sounds.computeIfAbsent(tick, k -> new ArrayList<>())
                  .add(new ScheduledSound(location, sound, volume, pitch));
        }
        
        public void update() {
            List<ScheduledSound> currentSounds = sounds.get(currentTick);
            if (currentSounds != null) {
                for (ScheduledSound scheduledSound : currentSounds) {
                    scheduledSound.play();
                }
            }
            
            currentTick++;
            
            // Проверяем, закончилась ли последовательность
            if (currentTick > getMaxTick()) {
                finished = true;
            }
        }
        
        private int getMaxTick() {
            return sounds.keySet().stream().mapToInt(Integer::intValue).max().orElse(0);
        }
        
        public boolean isFinished() {
            return finished;
        }
        
        public String getId() {
            return id;
        }
    }
    
    private class ScheduledSound {
        private final Location location;
        private final Sound sound;
        private final float volume;
        private final float pitch;
        
        public ScheduledSound(Location location, Sound sound, float volume, float pitch) {
            this.location = location;
            this.sound = sound;
            this.volume = volume;
            this.pitch = pitch;
        }
        
        public void play() {
            if (location.getWorld() != null) {
                location.getWorld().playSound(location, sound, SoundCategory.MASTER, volume, pitch);
            }
        }
    }
    
    private abstract class AmbientSound {
        protected final String id;
        protected final Location location;
        
        public AmbientSound(String id, Location location) {
            this.id = id;
            this.location = location;
        }
        
        public abstract void update();
        
        protected void playSound(Sound sound, float volume, float pitch) {
            if (location.getWorld() != null) {
                location.getWorld().playSound(location, sound, SoundCategory.AMBIENT, volume, pitch);
            }
        }
        
        public String getId() {
            return id;
        }
    }
    
    private static class PlayerSoundSettings {
        private final boolean enabled;
        private final float volume;
        
        public PlayerSoundSettings(boolean enabled, float volume) {
            this.enabled = enabled;
            this.volume = volume;
        }
        
        public boolean isEnabled() {
            return enabled;
        }
        
        public float getVolume() {
            return volume;
        }
    }
}
