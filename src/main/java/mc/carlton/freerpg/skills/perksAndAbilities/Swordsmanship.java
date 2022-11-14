package mc.carlton.freerpg.skills.perksAndAbilities;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import mc.carlton.freerpg.config.ConfigLoad;
import mc.carlton.freerpg.core.info.player.AbilityTimers;
import mc.carlton.freerpg.core.info.player.AbilityTracker;
import mc.carlton.freerpg.core.info.player.ChangeStats;
import mc.carlton.freerpg.core.info.player.PlayerStats;
import mc.carlton.freerpg.utils.game.ActionBarMessages;
import mc.carlton.freerpg.utils.game.ExpFarmTracker;
import mc.carlton.freerpg.utils.game.LanguageSelector;
import mc.carlton.freerpg.utils.game.TrackItem;
import mc.carlton.freerpg.utils.globalVariables.EntityGroups;
import mc.carlton.freerpg.utils.globalVariables.ItemGroups;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attributable;
import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class Swordsmanship extends Skill {

  Random rand = new Random(); //Random class Import
  private String skillName = "swordsmanship";
  private boolean runMethods;

  public Swordsmanship(Player p) {
    super(p);
    this.p = p;
    this.pName = p.getDisplayName();
    this.itemInHand = p.getInventory().getItemInMainHand();
    this.increaseStats = new ChangeStats(p);
    this.abilities = new AbilityTracker(p);
    this.timers = new AbilityTimers(p);
    this.pStatClass = new PlayerStats(p);
    this.actionMessage = new ActionBarMessages(p);
    this.lang = new LanguageSelector(p);
    ConfigLoad configLoad = new ConfigLoad();
    this.runMethods = configLoad.getAllowedSkillsMap().get(skillName);
    expMap = configLoad.getExpMapForSkill(skillName);
  }

  public void initiateAbility() {
    if (!runMethods) {
      return;
    }
    if (!p.hasPermission("freeRPG.swordsmanshipAbility")) {
      return;
    }
    Map<String, ArrayList<Number>> pStat = pStatClass.getPlayerData();
    if ((int) pStat.get("global").get(24) < 1 || !pStatClass.isPlayerSkillAbilityOn(skillName)) {
      return;
    }
    Integer[] pTimers = timers.getPlayerCooldownTimes();
    Integer[] pAbilities = abilities.getPlayerAbilities();
    if (pAbilities[7] == -1) {
      int cooldown = pTimers[7];
      if (cooldown < 1) {
        int prepMessages = (int) pStatClass.getPlayerData().get("global")
            .get(22); //Toggle for preparation messages
        if (prepMessages > 0) {
          actionMessage.sendMessage(
              ChatColor.GRAY + ">>>" + lang.getString("prepare") + " " + lang.getString("sword")
                  + "...<<<");
        }
        int taskID = new BukkitRunnable() {
          @Override
          public void run() {
            if (prepMessages > 0) {
              actionMessage.sendMessage(
                  ChatColor.GRAY + ">>>..." + lang.getString("rest") + " " + lang.getString("sword")
                      + "<<<");
            }
            try {
              abilities.setPlayerAbility(skillName, -1);
            } catch (Exception e) {

            }
          }
        }.runTaskLater(plugin, 20 * 4).getTaskId();
        abilities.setPlayerAbility(skillName, taskID);
      } else {
        actionMessage.sendMessage(
            ChatColor.RED + lang.getString("swiftStrikes") + " " + lang.getString("cooldown") + ": "
                + ChatColor.WHITE + cooldown + ChatColor.RED + "s");
      }
    }
  }

  public void enableAbility() {
    if (!runMethods) {
      return;
    }
    Integer[] pAbilities = abilities.getPlayerAbilities();
    Map<String, ArrayList<Number>> pStat = pStatClass.getPlayerData();
    actionMessage.sendMessage(
        ChatColor.GREEN + ChatColor.BOLD.toString() + ">>>" + lang.getString("swiftStrikes") + " "
            + lang.getString("activated") + "<<<");
    int durationLevel = (int) pStat.get(skillName).get(4);
    double duration0 = Math.ceil(durationLevel * 0.4) + 40;
    long duration = (long) duration0;
    int sharperLevel = (int) pStat.get(skillName).get(12);
    int sharpLevel = itemInHand.getEnchantmentLevel(Enchantment.DAMAGE_ALL);
    if (sharperLevel > 0) {
      itemInHand.removeEnchantment(Enchantment.DAMAGE_ALL);
      itemInHand.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, sharpLevel + 1);
    }

    //Mark the item
    long unixTime = Instant.now().getEpochSecond();
    String keyName =
        p.getUniqueId().toString() + "-frpg-" + skillName + "-" + String.valueOf(unixTime);
    NamespacedKey key = new NamespacedKey(plugin, keyName);
    ItemMeta itemMeta = itemInHand.getItemMeta();
    itemMeta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "frpg-swordsmanship");
    itemInHand.setItemMeta(itemMeta);

    ((Attributable) p).getAttribute(Attribute.GENERIC_ATTACK_SPEED).setBaseValue(1024.0);
    Bukkit.getScheduler().cancelTask(pAbilities[7]);
    abilities.setPlayerAbility(skillName, -2);
    String coolDownEndMessage =
        ChatColor.GREEN + ">>>" + lang.getString("swiftStrikes") + " " + lang.getString(
            "readyToUse") + "<<<";
    String endMessage =
        ChatColor.RED + ChatColor.BOLD.toString() + ">>>" + lang.getString("swiftStrikes") + " "
            + lang.getString("ended") + "<<<";
    timers.abilityDurationTimer(skillName, duration, endMessage, coolDownEndMessage, key,
        itemInHand, sharpLevel, sharperLevel);
  }

  public void preventLogoutTheft(int taskID_swordsmanship, ItemStack itemInHand_swords,
      NamespacedKey key, boolean isDisabling) {
    if (!runMethods) {
      return;
    }
    Integer[] pAbilities = abilities.getPlayerAbilities();
    Map<String, ArrayList<Number>> pStat = pStatClass.getPlayerData();
    int sharperLevel = (int) pStat.get(skillName).get(12);
    ((Attributable) p).getAttribute(Attribute.GENERIC_ATTACK_SPEED).setBaseValue(4.0);

    if (sharperLevel > 0) {
      if (pAbilities[7] == -2) {
        Bukkit.getScheduler().cancelTask(taskID_swordsmanship);
        int sharpLevel = itemInHand_swords.getEnchantmentLevel(Enchantment.DAMAGE_ALL) - 1;
        String endMessage =
            ChatColor.RED + ChatColor.BOLD.toString() + ">>>" + lang.getString("magicForce")
                + "<<<";
        String coolDownEndMessage =
            ChatColor.GREEN + ">>>" + lang.getString("swiftStrikes") + " " + lang.getString(
                "readyToUse") + "<<<";
        timers.endAbility(skillName, endMessage, coolDownEndMessage, key, itemInHand_swords,
            sharpLevel, sharperLevel, isDisabling);
        TrackItem trackItem = new TrackItem();
        trackItem.removeItemKey(itemInHand_swords, key);
      }
    }
  }

  public void thirstForBlood(Entity entity) {
    if (!runMethods) {
      return;
    }
    EntityGroups entityGroups = new EntityGroups();
    List<EntityType> thirstMobs = entityGroups.getThirstMobs();
    if (!(thirstMobs.contains(entity.getType()))) {
      return;
    }
    ItemGroups itemGroups = new ItemGroups();
    List<Material> swords = itemGroups.getSwords();
    if (!(swords.contains(itemInHand.getType()))) {
      return;
    }
    Map<String, ArrayList<Number>> pStat = pStatClass.getPlayerData();
    if ((int) pStat.get(skillName).get(11) < 1) {
      return;
    }
    int foodLevel = p.getFoodLevel();
    float saturation = p.getSaturation();
    p.setFoodLevel(Math.min(foodLevel + 2, 20));
    p.setSaturation(Math.min(saturation + 2, p.getFoodLevel()));
    increaseStats.changeEXP(skillName, expMap.get("thirstForBloodActivate"));
  }

  public void doubleHit(Entity entity, double damage) {
    if (!runMethods) {
      return;
    }
    Map<String, ArrayList<Number>> pStat = pStatClass.getPlayerData();
    int doubleHitLevel = (int) pStat.get(skillName).get(5);
    if (!(entity instanceof LivingEntity)) {
      return;
    }
    if (doubleHitLevel * 0.0002 > rand.nextDouble()) {
      new BukkitRunnable() {
        @Override
        public void run() {
          if (!entity.isDead()) {
            LivingEntity aliveEntity = (LivingEntity) entity;
            double hpRemaining = aliveEntity.getHealth();
            Vector knockback = aliveEntity.getVelocity();
            aliveEntity.setNoDamageTicks(0);
            if (aliveEntity instanceof Player) {
              aliveEntity.damage(2);
              increaseStats.changeEXP(skillName, expMap.get("doubleHitActivate"));
            } else {
              aliveEntity.damage(Math.min(damage * 0.5, hpRemaining - 1));
              increaseStats.changeEXP(skillName,
                  (int) Math.round(damage * expMap.get("doubleHit_EXPperDamagePointDone")));
            }
            double multiplier;
            if (knockback.length() > 0.1) {
              double newKnockback = Math.min(knockback.length() * 2, 80.0);
              multiplier = newKnockback / knockback.length();
            } else {
              multiplier = 2.0;
            }
            aliveEntity.setVelocity(knockback.multiply(multiplier));
            aliveEntity.setNoDamageTicks(20);

          }
        }
      }.runTaskLater(plugin, 4);
    }
  }

  public void killBuffs(Entity entity) { //This is written very inefficiently and is very messy
    if (!runMethods) {
      return;
    }
    EntityGroups entityGroups = new EntityGroups();
    List<EntityType> hostileMobs = entityGroups.getHostileMobs();
    if (!(hostileMobs.contains(entity.getType()))) {
      return;
    }
    ItemGroups itemGroups = new ItemGroups();
    List<Material> swords = itemGroups.getSwords();
    if (!(swords.contains(itemInHand.getType()))) {
      return;
    }

    Map<String, ArrayList<Number>> pStat = pStatClass.getPlayerData();
    int adrenaline1Level = Math.min((int) pStat.get(skillName).get(7), 5);
    int adrenaline2Level = Math.min((int) pStat.get(skillName).get(9), 5);
    int killingSpree1Level = Math.min((int) pStat.get(skillName).get(8), 5);
    int kilingSpree2Level = Math.min((int) pStat.get(skillName).get(10), 5);
    int totalSpeedTime = adrenaline1Level * 2 * 20;
    int totalStrengthtime = killingSpree1Level * 2 * 20;
    int speedIItime = (int) Math.round(totalSpeedTime * adrenaline2Level * 0.2);
    int strengthIItime = (int) Math.round(totalStrengthtime * kilingSpree2Level * 0.2);
    int speedItime = totalSpeedTime - speedIItime;
    int strengthItime = totalStrengthtime - strengthIItime;

    if (totalStrengthtime == 0 && totalSpeedTime == 0) {
      return;
    }

    //Speed
    boolean addEffect = true;
    boolean hasEffect = false;
    potionEffectLoop:
    for (PotionEffect effect : p.getActivePotionEffects()) {
      if (effect.getType().equals(PotionEffectType.SPEED)) {
        hasEffect = true;
        if ((effect.getDuration() > totalSpeedTime) || (effect.getAmplifier() > 1
            && effect.getDuration() > totalSpeedTime * 0.2) || (effect.getAmplifier() == 1
            && effect.getDuration() > speedIItime && effect.getDuration() > totalSpeedTime * 0.2)
            || totalSpeedTime == 0) {
          addEffect = false;
        }
        break potionEffectLoop;
      }
    }
    // TODO check for code duplication, check if code can be extracted to separate method
    if (addEffect) {
      if (hasEffect) {
        p.removePotionEffect(PotionEffectType.SPEED);
      }
      if (speedIItime == 0) {
        p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, totalSpeedTime, 0));
      } else {
        p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, speedIItime + 2, 1));
        int taskID = new BukkitRunnable() {
          @Override
          public void run() {
            boolean addEffect = true;
            boolean hasEffect = false;
            potionEffectLoop:
            for (PotionEffect effect : p.getActivePotionEffects()) {
              if (effect.getType().equals(PotionEffectType.SPEED)) {
                hasEffect = true;
                if ((effect.getDuration() > totalSpeedTime) || (effect.getAmplifier() > 1
                    && effect.getDuration() > speedItime * 0.2)) {
                  addEffect = false;
                }
                break potionEffectLoop;
              }
            }
            if (addEffect && hasEffect) {
              p.removePotionEffect(PotionEffectType.SPEED);
              p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, speedItime, 0));
            }

          }
        }.runTaskLater(plugin, speedIItime).getTaskId();
      }
    }

    //Strength
    addEffect = true;
    hasEffect = false;
    potionEffectLoop:
    for (PotionEffect effect : p.getActivePotionEffects()) {
      if (effect.getType().equals(PotionEffectType.INCREASE_DAMAGE)) {
        hasEffect = true;
        if ((effect.getDuration() > totalStrengthtime) || (effect.getAmplifier() > 1
            && effect.getDuration() > totalStrengthtime * 0.2) || (effect.getAmplifier() == 1
            && effect.getDuration() > strengthIItime
            && effect.getDuration() > totalStrengthtime * 0.2) || totalStrengthtime == 0) {
          addEffect = false;
        }
        break potionEffectLoop;
      }
    }
    // TODO check for code duplication, check if code can be extracted to separate method
    if (addEffect) {
      if (hasEffect) {
        p.removePotionEffect(PotionEffectType.INCREASE_DAMAGE);
      }
      if (strengthIItime == 0) {
        p.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, totalStrengthtime, 0));
      } else {
        p.addPotionEffect(
            new PotionEffect(PotionEffectType.INCREASE_DAMAGE, strengthIItime + 2, 1));
        int taskID = new BukkitRunnable() {
          @Override
          public void run() {
            boolean addEffect = true;
            boolean hasEffect = false;
            potionEffectLoop:
            for (PotionEffect effect : p.getActivePotionEffects()) {
              if (effect.getType().equals(PotionEffectType.INCREASE_DAMAGE)) {
                hasEffect = true;
                if ((effect.getDuration() > totalStrengthtime) || (effect.getAmplifier() > 1
                    && effect.getDuration() > strengthItime * 0.2)) {
                  addEffect = false;
                }
                break potionEffectLoop;
              }
            }
            if (addEffect && hasEffect) {
              p.removePotionEffect(PotionEffectType.INCREASE_DAMAGE);
              p.addPotionEffect(
                  new PotionEffect(PotionEffectType.INCREASE_DAMAGE, strengthItime, 0));
            }

          }
        }.runTaskLater(plugin, strengthIItime).getTaskId();
      }

    }

  }

  public void giveHitEXP(double finalDamage, Entity entity) {
    if (!runMethods || entity.getType().equals(EntityType.ARMOR_STAND)) {
      return;
    }
    ExpFarmTracker expFarmTracker = new ExpFarmTracker();
    double multiplier = expFarmTracker.getExpFarmAndSpawnerCombinedMultiplier(entity, skillName);
    increaseStats.changeEXP(skillName, (int) Math.round(
        (finalDamage * expMap.get("dealDamage_EXPperDamagePointDone") + expMap.get("dealDamage"))
            * multiplier));
  }

  public void giveKillEXP(Entity entity) {
    if (!runMethods) {
      return;
    }
    ItemGroups itemGroups = new ItemGroups();
    List<Material> swords = itemGroups.getSwords();
    if (!(swords.contains(itemInHand.getType()))) {
      return;
    }
    EntityGroups entityGroups = new EntityGroups();
    entityGroups.killEntity(entity, skillName, expMap, increaseStats);
  }


}
