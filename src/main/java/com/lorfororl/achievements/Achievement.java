package com.lorfororl.achievements;

public class Achievement {
    
    private final String id;
    private final String name;
    private final String description;
    private final AchievementManager.AchievementType type;
    private final int requiredProgress;
    
    public Achievement(String id, String name, String description, AchievementManager.AchievementType type, int requiredProgress) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.type = type;
        this.requiredProgress = requiredProgress;
    }
    
    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public AchievementManager.AchievementType getType() { return type; }
    public int getRequiredProgress() { return requiredProgress; }
}
