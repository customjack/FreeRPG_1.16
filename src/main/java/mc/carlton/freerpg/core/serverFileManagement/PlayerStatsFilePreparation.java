package mc.carlton.freerpg.core.serverFileManagement;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;
import mc.carlton.freerpg.FreeRPG;
import mc.carlton.freerpg.config.ConfigLoad;
import org.apache.logging.log4j.Level;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;


public class PlayerStatsFilePreparation {

  FileConfiguration playerData;

  public void initializePlayerDataBase() {
    Plugin plugin = FreeRPG.getPlugin(FreeRPG.class);
    File userdata = new File(plugin.getDataFolder(), File.separator + "PlayerDatabase");
    if (!userdata.exists()) {
      userdata.mkdir();
    }
  }

  public void playJoinConditions(Player p) {
    String pName = p.getName();
    UUID pUUID = p.getUniqueId();
    preparePlayerFile(pName, pUUID, true);
  }

  public void preparePlayerFile(String pName, UUID playerUUID, boolean isRealLogin) {
    //When player file is created for the first time...
    PlayerFilesManager playerFilesManager = new PlayerFilesManager();
    File f = playerFilesManager.getPlayerFile(playerUUID);
    if (!f.exists()) {
      asyncCreatePlayerFile(f, pName);
    } else { //If the player's file already exists
      asyncUpdatePlayerFile(f, pName, isRealLogin);
    }

  }

  public void addIfMissing(String key, Object value) {
    if (!playerData.contains(key)) {
      playerData.set(key, value);
    }
  }

  public void createPlayerYAML(String pName) {
    //Load some file creation config
    ConfigLoad loadConfig = new ConfigLoad();
    String defaultLanguage = loadConfig.getDefaultLanguage();
    ArrayList<Double> tokensInfo = loadConfig.getTokensInfo();
    ArrayList<Integer> soulsInfo = loadConfig.getSoulsInfo();
    int passiveTokens0 = (int) Math.round(tokensInfo.get(4));
    int skillTokens0 = (int) Math.round(tokensInfo.get(5));
    int globalTokens0 = (int) Math.round(tokensInfo.get(6));
    int souls0 = (int) soulsInfo.get(0);
    String[] labels = {"digging", "woodcutting", "mining", "farming", "fishing", "archery",
        "beastMastery", "swordsmanship", "defense", "axeMastery", "repair", "agility", "alchemy",
        "smelting", "enchanting"};
    long unixTime = Instant.now().getEpochSecond();
    Date now = new Date();
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

    //General Player information
    playerData.createSection("general");
    playerData.set("general.username", pName);
    playerData.set("general.firstLogin", "\"" + simpleDateFormat.format(now) + "\"");
    playerData.set("general.lastLogin", unixTime);
    playerData.set("general.lastLogout", unixTime);
    playerData.set("general.playTime", 0);
    playerData.set("general.language", defaultLanguage);

    //Global stats data
    playerData.createSection("globalStats");
    playerData.set("globalStats.totalLevel", 0);
    playerData.set("globalStats.globalTokens", globalTokens0);
    playerData.set("globalStats.skill_1a", 0);
    playerData.set("globalStats.skill_1b", 0);
    playerData.set("globalStats.skill_1c", 0);
    playerData.set("globalStats.skill_2a", 0);
    playerData.set("globalStats.skill_2b", 0);
    playerData.set("globalStats.skill_2c", 0);
    playerData.set("globalStats.skill_3a", 0);
    playerData.set("globalStats.skill_3b", 0);
    playerData.set("globalStats.skill_3c", 0);
    playerData.set("globalStats.skill_M", 0);
    playerData.set("globalStats.flintToggle", 1);
    playerData.set("globalStats.oreToggle", 1);
    playerData.set("globalStats.speedToggle", 1);
    playerData.set("globalStats.potionToggle", 1);
    playerData.set("globalStats.grappleToggle", 1);
    playerData.set("globalStats.hotRodToggle", 1);
    playerData.set("globalStats.veinMinerToggle", 1);
    playerData.set("globalStats.megaDigToggle", 1);
    playerData.set("globalStats.souls", souls0);
    playerData.set("globalStats.levelUpMessageToggle", 1);
    playerData.set("globalStats.abilityPrepareMessageToggle", 1);
    playerData.set("globalStats.personalEXPMultiplier", 1.0);
    playerData.set("globalStats.triggerAbilitiesToggle", 1);
    playerData.set("globalStats.showEXPBarToggle", 1);
    playerData.set("globalStats.leafBlowerToggle", 1);
    playerData.set("globalStats.holyAxeToggle", 1);
    playerData.set("globalStats.numberOfCooldownBars", 1);
    playerData.set("globalStats.totalExperience", 0);
    playerData.set("globalStats.heartyToggle", 0);

    // Skill Type Data
    for (int i = 0; i < labels.length; i++) {
      playerData.createSection(labels[i] + "");
      playerData.set(labels[i] + ".level", 0);
      playerData.set(labels[i] + ".experience", 0);
      playerData.set(labels[i] + ".passiveTokens", passiveTokens0);
      playerData.set(labels[i] + ".skillTokens", skillTokens0);
      playerData.set(labels[i] + ".passive1", 0);
      playerData.set(labels[i] + ".passive2", 0);
      playerData.set(labels[i] + ".passive3", 0);
      playerData.set(labels[i] + ".skill_1a", 0);
      playerData.set(labels[i] + ".skill_1b", 0);
      playerData.set(labels[i] + ".skill_2a", 0);
      playerData.set(labels[i] + ".skill_2b", 0);
      playerData.set(labels[i] + ".skill_3a", 0);
      playerData.set(labels[i] + ".skill_3b", 0);
      playerData.set(labels[i] + ".skill_M", 0);
      playerData.set(labels[i] + ".triggerAbilityToggle", 1);
      playerData.set(labels[i] + ".showEXPBarToggle", 1);
    }
  }

  public void updatePlayerYAML(String pName, boolean isRealLogin) {
    String[] labels = {"digging", "woodcutting", "mining", "farming", "fishing", "archery",
        "beastMastery", "swordsmanship", "defense", "axeMastery", "repair", "agility", "alchemy",
        "smelting", "enchanting"};
    long unixTime = Instant.now().getEpochSecond();
    Date now = new Date();
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
    //General Player information
    if (!playerData.contains("general")) {
      playerData.createSection("general");
    }
    if (!playerData.contains("general.username")) {
      playerData.set("general.username", pName);
    } else {
      if (isRealLogin) {
        String registeredName = playerData.getString("general.username");
        if (!registeredName.equalsIgnoreCase(pName)) {
          playerData.set("general.username", pName);
        }
      }
    }
    addIfMissing("general.firstLogin", "\"" + simpleDateFormat.format(now) + "\"");

    //Whether it exists or not, the last login will be set to the current unix timestamp
    if (isRealLogin) {
      playerData.set("general.lastLogin", unixTime);
    } else {
      addIfMissing("general.lastLogin", unixTime);
    }

    addIfMissing("general.lastLogout", unixTime);
    addIfMissing("general.playTime", 0);

    if (!playerData.contains("general.language")) {
      ConfigLoad loadConfig = new ConfigLoad();
      String defaultLanguage = loadConfig.getDefaultLanguage();
      playerData.set("general.language", defaultLanguage);
    }

    //Global stats data
    if (!playerData.contains("globalStats")) {
      playerData.createSection("globalStats");
    }
    addIfMissing("globalStats.totalLevel", 0);
    addIfMissing("globalStats.globalTokens", 0);
    addIfMissing("globalStats.skill_1a", 0);
    addIfMissing("globalStats.skill_1b", 0);
    addIfMissing("globalStats.skill_1c", 0);
    addIfMissing("globalStats.skill_2a", 0);
    addIfMissing("globalStats.skill_2b", 0);
    addIfMissing("globalStats.skill_2c", 0);
    addIfMissing("globalStats.skill_3a", 0);
    addIfMissing("globalStats.skill_3b", 0);
    addIfMissing("globalStats.skill_3c", 0);
    addIfMissing("globalStats.skill_M", 0);
    addIfMissing("globalStats.flintToggle", 1);
    addIfMissing("globalStats.oreToggle", 1);
    addIfMissing("globalStats.speedToggle", 1);
    addIfMissing("globalStats.potionToggle", 1);
    addIfMissing("globalStats.grappleToggle", 1);
    addIfMissing("globalStats.hotRodToggle", 1);
    addIfMissing("globalStats.veinMinerToggle", 1);
    addIfMissing("globalStats.megaDigToggle", 1);
    addIfMissing("globalStats.souls", 0);
    addIfMissing("globalStats.levelUpMessageToggle", 1);
    addIfMissing("globalStats.abilityPrepareMessageToggle", 1);
    addIfMissing("globalStats.personalEXPMultiplier", 1.0);
    addIfMissing("globalStats.triggerAbilitiesToggle", 1);
    addIfMissing("globalStats.showEXPBarToggle", 1);
    addIfMissing("globalStats.leafBlowerToggle", 1);
    addIfMissing("globalStats.holyAxeToggle", 1);
    addIfMissing("globalStats.numberOfCooldownBars", 1);
    addIfMissing("globalStats.totalExperience", 0);
    addIfMissing("globalStats.heartyToggle", 0);

    // Skill Type Data
    for (int i = 0; i < labels.length; i++) {
      if (!playerData.contains(labels[i] + "")) {
        playerData.createSection(labels[i] + "");
      }
      addIfMissing(labels[i] + ".level", 0);
      addIfMissing(labels[i] + ".experience", 0);
      addIfMissing(labels[i] + ".passiveTokens", 0);
      addIfMissing(labels[i] + ".skillTokens", 0);
      addIfMissing(labels[i] + ".passive1", 0);
      addIfMissing(labels[i] + ".passive2", 0);
      addIfMissing(labels[i] + ".passive3", 0);
      addIfMissing(labels[i] + ".skill_1a", 0);
      addIfMissing(labels[i] + ".skill_1b", 0);
      addIfMissing(labels[i] + ".skill_2a", 0);
      addIfMissing(labels[i] + ".skill_2b", 0);
      addIfMissing(labels[i] + ".skill_3a", 0);
      addIfMissing(labels[i] + ".skill_3b", 0);
      addIfMissing(labels[i] + ".skill_M", 0);
      addIfMissing(labels[i] + ".triggerAbilityToggle", 1);
      addIfMissing(labels[i] + ".showEXPBarToggle", 1);
    }
  }

  public void createPlayerFile(File f, String pName) {
    try {
      playerData = YamlConfiguration.loadConfiguration(f);
      createPlayerYAML(pName);
      playerData.save(f);
    } catch (IOException exception) {
      FreeRPG.log(Level.ERROR, exception.getMessage());
    }
  }

  public void updatePlayerFile(File f, String pName, boolean isRealLogin) {
    try {
      playerData = YamlConfiguration.loadConfiguration(f);
      updatePlayerYAML(pName, isRealLogin);
      playerData.save(f);
    } catch (IOException exception) {
      FreeRPG.log(Level.ERROR, exception.getMessage());
    }
  }

  public void asyncCreatePlayerFile(File f, String pName) {
    Plugin plugin = FreeRPG.getPlugin(FreeRPG.class);
    new BukkitRunnable() {
      @Override
      public void run() {
        createPlayerFile(f, pName);
      }
    }.runTaskAsynchronously(plugin);
  }

  public void asyncUpdatePlayerFile(File f, String pName, boolean isRealLogin) {
    Plugin plugin = FreeRPG.getPlugin(FreeRPG.class);
    new BukkitRunnable() {
      @Override
      public void run() {
        updatePlayerFile(f, pName, isRealLogin);
      }
    }.runTaskAsynchronously(plugin);
  }
}
