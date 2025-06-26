package com.lorfororl;

import org.bukkit.plugin.java.JavaPlugin;
import com.lorfororl.config.ConfigManager;
import com.lorfororl.data.DataManager;
import com.lorfororl.commands.LorForOrlCommand;
import com.lorfororl.commands.AchievementCommand;
import com.lorfororl.listeners.*;
import com.lorfororl.achievements.AchievementManager;
import com.lorfororl.automation.AutoMinerManager;
import com.lorfororl.balance.BalanceManager;
import com.lorfororl.centrifuge.CentrifugeManager;
import com.lorfororl.energy.EnergyManager;
import com.lorfororl.energy.SolarPanelManager;
import com.lorfororl.equipment.EquipmentManager;
import com.lorfororl.equipment.PowerArmorSystem;
import com.lorfororl.gui.GuiManager;
import com.lorfororl.gui.AnimatedGuiManager;
import com.lorfororl.items.*;
import com.lorfororl.laboratory.LaboratoryManager;
import com.lorfororl.notifications.NotificationManager;
import com.lorfororl.protection.EnergyShield;
import com.lorfororl.radiation.RadiationManager;
import com.lorfororl.radiation.RadiationZoneManager;
import com.lorfororl.reactor.ReactorManager;
import com.lorfororl.research.ResearchManager;
import com.lorfororl.research.AdvancedResearchManager;
import com.lorfororl.research.ResearchEconomy;
import com.lorfororl.research.ResearchProgression;
import com.lorfororl.security.SecurityManager;
import com.lorfororl.structures.StructureBuilder;
import com.lorfororl.visual.*;
import com.lorfororl.weapons.NuclearBombManager;
import com.lorfororl.weapons.RailgunManager;
import com.lorfororl.weapons.RailgunSystem;

public class LorForOrlPlugin extends JavaPlugin {
    
    private static LorForOrlPlugin instance;
    private long startTime;
    
    // Managers
    private ConfigManager configManager;
    private DataManager dataManager;
    private AchievementManager achievementManager;
    private AutoMinerManager autoMinerManager;
    private BalanceManager balanceManager;
    private CentrifugeManager centrifugeManager;
    private EnergyManager energyManager;
    private SolarPanelManager solarPanelManager;
    private EquipmentManager equipmentManager;
    private PowerArmorSystem powerArmorSystem;
    private GuiManager guiManager;
    private AnimatedGuiManager animatedGuiManager;
    private LaboratoryManager laboratoryManager;
    private NotificationManager notificationManager;
    private RadiationManager radiationManager;
    private RadiationZoneManager radiationZoneManager;
    private ReactorManager reactorManager;
    private ResearchManager researchManager;
    private AdvancedResearchManager advancedResearchManager;
    private ResearchEconomy researchEconomy;
    private ResearchProgression researchProgression;
    private SecurityManager securityManager;
    private StructureBuilder structureBuilder;
    private NuclearBombManager nuclearBombManager;
    private RailgunManager railgunManager;
    private RailgunSystem railgunSystem;
    
    // Visual Managers
    private CinematicManager cinematicManager;
    private EffectManager effectManager;
    private HologramManager hologramManager;
    private HudManager hudManager;
    private ParticleSystemManager particleSystemManager;
    private SoundManager soundManager;
    
    // Items
    private UraniumItems uraniumItems;
    private LaboratoryItems laboratoryItems;
    private StructureItems structureItems;
    private AdvancedItems advancedItems;
    
    @Override
    public void onEnable() {
        instance = this;
        startTime = System.currentTimeMillis();
        
        getLogger().info("Загрузка LorForOrl v2.0 - Ядерные технологии будущего!");
        
        // Инициализация менеджеров
        initializeManagers();
        
        // Регистрация команд
        registerCommands();
        
        // Регистрация слушателей
        registerListeners();
        
        // Инициализация предметов
        initializeItems();
        
        getLogger().info("LorForOrl v2.0 успешно загружен!");
    }
    
    @Override
    public void onDisable() {
        getLogger().info("Выгрузка LorForOrl v2.0...");
        
        // Сохранение данных
        if (dataManager != null) {
            dataManager.saveData();
        }
        
        getLogger().info("LorForOrl v2.0 выгружен!");
    }
    
    private void initializeManagers() {
        configManager = new ConfigManager(this);
        dataManager = new DataManager(this);
        achievementManager = new AchievementManager(this);
        autoMinerManager = new AutoMinerManager(this);
        balanceManager = new BalanceManager(this);
        centrifugeManager = new CentrifugeManager(this);
        energyManager = new EnergyManager(this);
        solarPanelManager = new SolarPanelManager(this);
        equipmentManager = new EquipmentManager(this);
        powerArmorSystem = new PowerArmorSystem(this);
        guiManager = new GuiManager(this);
        animatedGuiManager = new AnimatedGuiManager(this);
        laboratoryManager = new LaboratoryManager(this);
        notificationManager = new NotificationManager(this);
        radiationManager = new RadiationManager(this);
        radiationZoneManager = new RadiationZoneManager(this);
        reactorManager = new ReactorManager(this);
        researchManager = new ResearchManager();
        advancedResearchManager = new AdvancedResearchManager(this);
        researchEconomy = new ResearchEconomy(this);
        researchProgression = new ResearchProgression(researchManager);
        securityManager = new SecurityManager(this);
        structureBuilder = new StructureBuilder(this);
        nuclearBombManager = new NuclearBombManager(this);
        railgunManager = new RailgunManager(this);
        railgunSystem = new RailgunSystem(this);
        
        // Visual Managers
        cinematicManager = new CinematicManager(this);
        effectManager = new EffectManager(this);
        hologramManager = new HologramManager(this);
        hudManager = new HudManager(this);
        particleSystemManager = new ParticleSystemManager(this);
        soundManager = new SoundManager(this);
    }
    
    private void registerCommands() {
        getCommand("lorfororl").setExecutor(new LorForOrlCommand(this));
        getCommand("achievements").setExecutor(new AchievementCommand(this));
    }
    
    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new CapsuleListener(this), this);
        getServer().getPluginManager().registerEvents(new CentrifugeListener(this), this);
        getServer().getPluginManager().registerEvents(new EquipmentModeListener(this), this);
        getServer().getPluginManager().registerEvents(new GeigerListener(this), this);
        getServer().getPluginManager().registerEvents(new GuiListener(this), this);
        getServer().getPluginManager().registerEvents(new LaboratoryListener(this), this);
        getServer().getPluginManager().registerEvents(new MouseWheelListener(this), this);
        getServer().getPluginManager().registerEvents(new RadiationListener(this), this);
        getServer().getPluginManager().registerEvents(new StructureCraftListener(this), this);
        getServer().getPluginManager().registerEvents(new StructureListener(this), this);
        getServer().getPluginManager().registerEvents(new WeaponListener(this), this);
    }
    
    private void initializeItems() {
        uraniumItems = new UraniumItems(this);
        laboratoryItems = new LaboratoryItems(this);
        structureItems = new StructureItems(this);
        advancedItems = new AdvancedItems(this);
    }
    
    public static LorForOrlPlugin getInstance() {
        return instance;
    }
    
    public long getStartTime() {
        return startTime;
    }
    
    // Getters для всех менеджеров
    public ConfigManager getConfigManager() { return configManager; }
    public DataManager getDataManager() { return dataManager; }
    public AchievementManager getAchievementManager() { return achievementManager; }
    public AutoMinerManager getAutoMinerManager() { return autoMinerManager; }
    public BalanceManager getBalanceManager() { return balanceManager; }
    public CentrifugeManager getCentrifugeManager() { return centrifugeManager; }
    public EnergyManager getEnergyManager() { return energyManager; }
    public SolarPanelManager getSolarPanelManager() { return solarPanelManager; }
    public EquipmentManager getEquipmentManager() { return equipmentManager; }
    public PowerArmorSystem getPowerArmorSystem() { return powerArmorSystem; }
    public GuiManager getGuiManager() { return guiManager; }
    public AnimatedGuiManager getAnimatedGuiManager() { return animatedGuiManager; }
    public LaboratoryManager getLaboratoryManager() { return laboratoryManager; }
    public NotificationManager getNotificationManager() { return notificationManager; }
    public RadiationManager getRadiationManager() { return radiationManager; }
    public RadiationZoneManager getRadiationZoneManager() { return radiationZoneManager; }
    public ReactorManager getReactorManager() { return reactorManager; }
    public ResearchManager getResearchManager() { return researchManager; }
    public AdvancedResearchManager getAdvancedResearchManager() { return advancedResearchManager; }
    public ResearchEconomy getResearchEconomy() { return researchEconomy; }
    public ResearchProgression getResearchProgression() { return researchProgression; }
    public SecurityManager getSecurityManager() { return securityManager; }
    public StructureBuilder getStructureBuilder() { return structureBuilder; }
    public NuclearBombManager getNuclearBombManager() { return nuclearBombManager; }
    public RailgunManager getRailgunManager() { return railgunManager; }
    public RailgunSystem getRailgunSystem() { return railgunSystem; }
    
    // Visual Managers Getters
    public CinematicManager getCinematicManager() { return cinematicManager; }
    public EffectManager getEffectManager() { return effectManager; }
    public HologramManager getHologramManager() { return hologramManager; }
    public HudManager getHudManager() { return hudManager; }
    public ParticleSystemManager getParticleSystemManager() { return particleSystemManager; }
    public SoundManager getSoundManager() { return soundManager; }
    
    // Items Getters
    public UraniumItems getUraniumItems() { return uraniumItems; }
    public LaboratoryItems getLaboratoryItems() { return laboratoryItems; }
    public StructureItems getStructureItems() { return structureItems; }
    public AdvancedItems getAdvancedItems() { return advancedItems; }
}
