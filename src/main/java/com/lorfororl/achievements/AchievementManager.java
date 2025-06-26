package com.lorfororl.achievements;

import com.lorfororl.LorForOrlPlugin;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.*;

public class AchievementManager {
    
    private final LorForOrlPlugin plugin;
    private final Map<String, Achievement> achievements;
    private final Map<UUID, Set<String>> playerAchievements;
    private final Map<UUID, Map<String, Integer>> playerProgress;
    
    public AchievementManager(LorForOrlPlugin plugin) {
        this.plugin = plugin;
        this.achievements = new HashMap<>();
        this.playerAchievements = new HashMap<>();
        this.playerProgress = new HashMap<>();
        
        initializeAchievements();
    }
    
    private void initializeAchievements() {
        // Радиационные достижения
        achievements.put("first_radiation", new Achievement(
            "first_radiation",
            "§cПервое облучение",
            "§7Получите первую дозу радиации",
            AchievementType.RADIATION,
            1
        ));
        
        achievements.put("radiation_survivor", new Achievement(
            "radiation_survivor",
            "§6Выживший в радиации",
            "§7Выживите с радиацией выше 3.0 у.е.",
            AchievementType.RADIATION,
            1
        ));
        
        achievements.put("radiation_immune", new Achievement(
            "radiation_immune",
            "§aИммунитет к радиации",
            "§7Снизьте радиацию с 5.0 до 0.1 у.е.",
            AchievementType.RADIATION,
            1
        ));
        
        // Производственные достижения
        achievements.put("first_centrifuge", new Achievement(
            "first_centrifuge",
            "§eПервая центрифуга",
            "§7Создайте свою первую центрифугу",
            AchievementType.PRODUCTION,
            1
        ));
        
        achievements.put("uranium_collector", new Achievement(
            "uranium_collector",
            "§2Коллекционер урана",
            "§7Соберите 100 урановой пыли",
            AchievementType.PRODUCTION,
            100
        ));
        
        achievements.put("uranium_master", new Achievement(
            "uranium_master",
            "§4Мастер урана",
            "§7Соберите 1000 урановой пыли",
            AchievementType.PRODUCTION,
            1000
        ));
        
        // Исследовательские достижения
        achievements.put("first_research", new Achievement(
            "first_research",
            "§bПервое исследование",
            "§7Завершите первое исследование",
            AchievementType.RESEARCH,
            1
        ));
        
        achievements.put("researcher", new Achievement(
            "researcher",
            "§5Исследователь",
            "§7Завершите 5 исследований",
            AchievementType.RESEARCH,
            5
        ));
        
        achievements.put("mad_scientist", new Achievement(
            "mad_scientist",
            "§cБезумный ученый",
            "§7Завершите все исследования",
            AchievementType.RESEARCH,
            10
        ));
        
        // Энергетические достижения
        achievements.put("first_reactor", new Achievement(
            "first_reactor",
            "§4Первый реактор",
            "§7Постройте свой первый ядерный реактор",
            AchievementType.ENERGY,
            1
        ));
        
        achievements.put("power_plant", new Achievement(
            "power_plant",
            "§6Электростанция",
            "§7Выработайте 100,000 единиц энергии",
            AchievementType.ENERGY,
            100000
        ));
        
        achievements.put("nuclear_meltdown", new Achievement(
            "nuclear_meltdown",
            "§cЯдерная катастрофа",
            "§7Допустите взрыв реактора",
            AchievementType.ENERGY,
            1
        ));
        
        // Боевые достижения
        achievements.put("first_railgun_kill", new Achievement(
            "first_railgun_kill",
            "§cПервое убийство из рельсотрона",
            "§7Убейте моба из рельсотрона",
            AchievementType.COMBAT,
            1
        ));
        
        achievements.put("sniper", new Achievement(
            "sniper",
            "§eСнайпер",
            "§7Убейте цель с расстояния 100+ блоков",
            AchievementType.COMBAT,
            1
        ));
        
        achievements.put("emp_master", new Achievement(
            "emp_master",
            "§bМастер ЭМИ",
            "§7Отключите 10 силовых броней ЭМИ выстрелами",
            AchievementType.COMBAT,
            10
        ));
        
        // Строительные достижения
        achievements.put("architect", new Achievement(
            "architect",
            "§6Архитектор",
            "§7Постройте все типы структур",
            AchievementType.BUILDING,
            4
        ));
        
        achievements.put("city_builder", new Achievement(
            "city_builder",
            "§aГрадостроитель",
            "§7Постройте 20 структур",
            AchievementType.BUILDING,
            20
        ));
    }
    
    public void checkAchievement(Player player, String achievementId, int progress) {
        UUID playerId = player.getUniqueId();
        
        // Проверяем, не получено ли уже достижение
        if (hasAchievement(player, achievementId)) {
            return;
        }
        
        Achievement achievement = achievements.get(achievementId);
        if (achievement == null) {
            return;
        }
        
        // Обновляем прогресс
        Map<String, Integer> playerProgressMap = playerProgress.computeIfAbsent(playerId, k -> new HashMap<>());
        int currentProgress = playerProgressMap.getOrDefault(achievementId, 0);
        int newProgress = Math.max(currentProgress, progress);
        playerProgressMap.put(achievementId, newProgress);
        
        // Проверяем выполнение
        if (newProgress >= achievement.getRequiredProgress()) {
            unlockAchievement(player, achievement);
        } else {
            // Показываем прогресс
            if (newProgress > currentProgress) {
                player.sendMessage(String.format("§e[Достижение] %s: %d/%d", 
                    achievement.getName(), newProgress, achievement.getRequiredProgress()));
            }
        }
    }
    
    private void unlockAchievement(Player player, Achievement achievement) {
        UUID playerId = player.getUniqueId();
        
        // Добавляем достижение
        playerAchievements.computeIfAbsent(playerId, k -> new HashSet<>()).add(achievement.getId());
        
        // Уведомление
        player.sendMessage("§a§l[ДОСТИЖЕНИЕ ПОЛУЧЕНО!]");
        player.sendMessage("§e" + achievement.getName());
        player.sendMessage("§7" + achievement.getDescription());
        
        // Звуковые эффекты
        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
        
        // Награда (если настроена экономика)
        giveAchievementReward(player, achievement);
    }
    
    private void giveAchievementReward(Player player, Achievement achievement) {
        // Здесь можно добавить интеграцию с экономическими плагинами
        // Пока что просто выдаем опыт
        int expReward = achievement.getRequiredProgress() * 10;
        player.giveExp(expReward);
        player.sendMessage("§a+§e" + expReward + " §aопыта!");
    }
    
    public boolean hasAchievement(Player player, String achievementId) {
        Set<String> achievements = playerAchievements.get(player.getUniqueId());
        return achievements != null && achievements.contains(achievementId);
    }
    
    public int getProgress(Player player, String achievementId) {
        Map<String, Integer> progress = playerProgress.get(player.getUniqueId());
        return progress != null ? progress.getOrDefault(achievementId, 0) : 0;
    }
    
    public List<Achievement> getPlayerAchievements(Player player) {
        Set<String> playerAchievementIds = playerAchievements.get(player.getUniqueId());
        if (playerAchievementIds == null) {
            return new ArrayList<>();
        }
        
        return playerAchievementIds.stream()
            .map(achievements::get)
            .filter(Objects::nonNull)
            .toList();
    }
    
    public List<Achievement> getAllAchievements() {
        return new ArrayList<>(achievements.values());
    }
    
    // Специальные методы для проверки достижений
    public void onRadiationGained(Player player, double radiation) {
        checkAchievement(player, "first_radiation", 1);
        
        if (radiation >= 3.0) {
            checkAchievement(player, "radiation_survivor", 1);
        }
    }
    
    public void onUraniumCollected(Player player, int amount) {
        int currentAmount = getProgress(player, "uranium_collector") + amount;
        checkAchievement(player, "uranium_collector", currentAmount);
        checkAchievement(player, "uranium_master", currentAmount);
    }
    
    public void onResearchCompleted(Player player) {
        int currentResearch = getProgress(player, "first_research") + 1;
        checkAchievement(player, "first_research", currentResearch);
        checkAchievement(player, "researcher", currentResearch);
        checkAchievement(player, "mad_scientist", currentResearch);
    }
    
    public void onStructureBuilt(Player player, String structureType) {
        if ("nuclear_reactor".equals(structureType)) {
            checkAchievement(player, "first_reactor", 1);
        }
        
        int totalStructures = getProgress(player, "architect") + 1;
        checkAchievement(player, "city_builder", totalStructures);
        
        // Проверяем архитектора (все типы структур)
        Set<String> builtTypes = new HashSet<>();
        // Здесь нужно отслеживать типы построенных структур
    }
    
    public void onRailgunKill(Player player, double distance) {
        checkAchievement(player, "first_railgun_kill", 1);
        
        if (distance >= 100) {
            checkAchievement(player, "sniper", 1);
        }
    }
    
    public void onEMPHit(Player player) {
        int empHits = getProgress(player, "emp_master") + 1;
        checkAchievement(player, "emp_master", empHits);
    }
    
    public void onReactorExplosion(Player player) {
        checkAchievement(player, "nuclear_meltdown", 1);
    }
    
    public enum AchievementType {
        RADIATION("§cРадиация"),
        PRODUCTION("§eПроизводство"),
        RESEARCH("§bИсследования"),
        ENERGY("§6Энергетика"),
        COMBAT("§cБой"),
        BUILDING("§aСтроительство");
        
        private final String displayName;
        
        AchievementType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() { return displayName; }
    }
}
