package mc.carlton.freerpg.customContainers;

import mc.carlton.freerpg.utils.globalVariables.StringsAndOtherData;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class OldCustomPotion {

  public PotionEffectType potionEffectType;
  public Material ingredient;
  public int potionDuration;
  public Color color;
  public String potionName;

  public OldCustomPotion() {
    this.potionEffectType = null;
    this.ingredient = null;
    this.potionDuration = 9;
    this.color = null;
    this.potionName = null;
  }

  public Color getColor() {
    return color;
  }

  public void setColor(Color color) {
    this.color = color;
  }

  public int getPotionDuration() {
    return potionDuration;
  }

  public void setPotionDuration(int potionDuration) {
    this.potionDuration = potionDuration;
  }

  public Material getIngredient() {
    return ingredient;
  }

  public void setIngredient(Material ingredient) {
    this.ingredient = ingredient;
  }

  public PotionEffectType getPotionEffectType() {
    return potionEffectType;
  }

  public void setPotionEffectType(PotionEffectType potionEffectType) {
    this.potionEffectType = potionEffectType;
  }

  public String getPotionName() {
    return potionName;
  }

  public void setPotionName() {
    StringsAndOtherData stringsAndOtherData = new StringsAndOtherData();
    this.potionName = stringsAndOtherData.getPotionNameFromEffect(potionEffectType);
  }

  public ItemStack getPotionItemStack() {
    ItemStack potion = new ItemStack(Material.POTION, 1);
    potion.addUnsafeEnchantment(Enchantment.LOYALTY, 1);
    PotionMeta meta = (PotionMeta) potion.getItemMeta();
    meta.addCustomEffect(new PotionEffect(potionEffectType, potionDuration * 20, 0), true);
    meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
    meta.setColor(color);
    meta.setDisplayName(ChatColor.RESET + potionName);
    potion.setItemMeta(meta);
    return potion;
  }
}
