package mc.carlton.freerpg.core.leaveAndJoin;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import mc.carlton.freerpg.FreeRPG;
import mc.carlton.freerpg.core.info.player.AbilityLogoutTracker;
import mc.carlton.freerpg.core.info.player.AbilityTimers;
import mc.carlton.freerpg.core.info.player.AbilityTracker;
import mc.carlton.freerpg.core.info.player.ChangeStats;
import mc.carlton.freerpg.core.info.player.Leaderboards;
import mc.carlton.freerpg.core.info.player.PlayerStats;
import mc.carlton.freerpg.core.info.player.PlayerStatsLoadIn;
import mc.carlton.freerpg.core.serverFileManagement.PlayerStatsFilePreparation;
import mc.carlton.freerpg.skills.perksAndAbilities.Agility;
import mc.carlton.freerpg.skills.perksAndAbilities.Defense;
import mc.carlton.freerpg.skills.perksAndAbilities.Farming;
import mc.carlton.freerpg.skills.perksAndAbilities.Fishing;
import mc.carlton.freerpg.utils.game.BossBarStorage;
import mc.carlton.freerpg.utils.game.FurnaceUserTracker;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attributable;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class LoginProcedure {

  Player p;
  Plugin plugin = FreeRPG.getPlugin(FreeRPG.class);
  private UUID uuid;


  public LoginProcedure(Player p) {
    this.p = p;
    this.uuid = p.getUniqueId();

  }

  public LoginProcedure(UUID playerUUID) {
    this.p = Bukkit.getPlayer(playerUUID);
    this.uuid = playerUUID;

  }

  public void addStatsToPlayerMap(boolean onlinePlayer) {
    //Read in player's past stats stats
    PlayerStatsLoadIn loadInPlayer = new PlayerStatsLoadIn(uuid);
    long loginTime = loadInPlayer.getLoginTime();
    long playTime = loadInPlayer.getPlayTime();
    String language = loadInPlayer.getPlayerLanguage();
    Map<String, ArrayList<Number>> playerStats0 = loadInPlayer.getPlayerStatsMapFromFile();
    Map<String, Integer> playerSkillExpBarToggles = loadInPlayer.getSkillExpBarToggles();
    Map<String, Integer> playerSkillAbilityToggles = loadInPlayer.getSkillAbilityToggles();

    //Combine the player's stats with everyone on the server's
    PlayerStats pStatsClass = new PlayerStats(uuid);
    Map<UUID, Map<String, ArrayList<Number>>> allStats = pStatsClass.getData();
    allStats.put(uuid, playerStats0);
    pStatsClass.setData(allStats);
    pStatsClass.addPlayerTimes(loginTime, playTime);
    pStatsClass.setPlayerLanguage(language);
    pStatsClass.addPlayerSkillToggleAbility(playerSkillAbilityToggles);
    pStatsClass.addPlayerSkillToggleExpBar(playerSkillExpBarToggles);
    pStatsClass.setPlayerAreStatsSaved(!onlinePlayer);
  }

  public void updatePlayerLoginTime() {
    PlayerStats pStatsClass = new PlayerStats(uuid);
    long playTime = pStatsClass.getPlayerPlayTime();
    pStatsClass.addPlayerTimes(Instant.now().getEpochSecond(), playTime);
    pStatsClass.setPlayerAreStatsSaved(false);
  }

  public void playerLogin() {
    PlayerStats playerStats = new PlayerStats(uuid);
    if (!playerStats.isPlayerRegistered()) { //If the player is registered, we assume this has already been done
      //If the player is new, creates a new stats file for them,
      //If the player's stat file is not properly formatted, this also fixes that
      PlayerStatsFilePreparation playerStatsFilePreparation = new PlayerStatsFilePreparation();
      playerStatsFilePreparation.playJoinConditions(p);

      //Read in player's past stats into an the playerStats Class
      addStatsToPlayerMap(true);
    } else {
      updatePlayerLoginTime();
    }

    //Loads player into leaderboard stat tracking if it's their first time logging in/they have no player profile
    Leaderboards leaderboards = new Leaderboards();
    leaderboards.initializeNewPlayer(p);

    //Makes sure the player's stats are consistent with the defined EXP curve
    ChangeStats changeStats = new ChangeStats(p);
    changeStats.checkPlayerLevelEXPCurveConsistency();
    changeStats.setTotalLevel();
    changeStats.setTotalExperience();

    //Makes sure player's attack speed is normal
    ((Attributable) p).getAttribute(Attribute.GENERIC_ATTACK_SPEED).setBaseValue(4.0);

    //Initiates player abilities
    Integer[] initAbils = {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1};
    AbilityTracker abilitiesClass = new AbilityTracker(p);
    Map<UUID, Integer[]> allAbilities = abilitiesClass.getAbilities();
    allAbilities.put(uuid, initAbils);
    abilitiesClass.setAbilities(allAbilities);

    //Initiates player timers
    AbilityTimers timersClass = new AbilityTimers(p);
    Map<UUID, Integer[]> allTimers = timersClass.getCooldownTimes();
    if (!allTimers.containsKey(uuid)) {
      Integer[] initTimers = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
      allTimers.put(uuid, initTimers);
      timersClass.setTimes(allTimers);
    }

    //Initiates player abiliyLogoutTracker
    AbilityLogoutTracker abilityLogout = new AbilityLogoutTracker(p);
    abilityLogout.initializePlayer();

    //Initializes Abilities
    Farming farmingClass = new Farming(p);
    farmingClass.oneWithNatureStart();

    Fishing fishingClass = new Fishing(p);
    fishingClass.fishPersonStart();

    Agility agilityClass = new Agility(p);
    agilityClass.gracefulFeetStart();

    Defense defenseClass = new Defense(p);
    defenseClass.hearty();

    //Sets up bossbar
    BossBarStorage bossBarStorage = new BossBarStorage();
    bossBarStorage.initializeNewPlayer(p);

    //Checks if player recently logged out with active furnaces
    FurnaceUserTracker furnaceUserTracker = new FurnaceUserTracker();
    furnaceUserTracker.playerLogin(p);
  }
}
