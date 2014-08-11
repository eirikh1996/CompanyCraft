package io.github.jisaacs1207.CompanyCraft;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class Listeners
  implements Listener
{
  CompanyCraft cc = null;
  ConfigWriter c = null;
  
  public Listeners(ConfigWriter configWriter, CompanyCraft companyCraft)
  {
    this.c = configWriter;
    this.cc = companyCraft;
  }
  
  @EventHandler
  public void checkShopSign(PlayerInteractEvent e)
  {
    Block b = e.getClickedBlock();
    if ((b != null) && 
      ((b.getState() instanceof Sign)))
    {
      Sign s = (Sign)b.getState();
      Location l = s.getLocation();
      s.update();
      Boolean signRegistered = Boolean.valueOf(false);
      for (Location w : this.c.shopSigns) {
        if (w.toString().equalsIgnoreCase(l.toString()))
        {
          signRegistered = Boolean.valueOf(true);
          break;
        }
      }
      if (signRegistered.booleanValue())
      {
        int amount = Integer.valueOf(s.getLine(3)).intValue();
        ItemStack i = new ItemStack(Material.matchMaterial(s.getLine(1).toUpperCase()), amount);
        Double price = Double.valueOf(Double.valueOf(s.getLine(2)).doubleValue());
        String company = s.getLine(0);
        Location chestLocation = new Location(s.getWorld(), s.getX(), s.getY() - 1, s.getZ());
        Chest chest = (Chest)chestLocation.getBlock().getState();
        Inventory chestContents = chest.getInventory();
        this.cc.reloadConfig();
        this.cc.loadEconomy();
        Double playerMoney = (Double)this.c.personalMoney.get(e.getPlayer().getName());
        this.cc.saveEconomy();
        if (playerMoney.doubleValue() >= price.doubleValue() * amount)
        {
          if (chestContents.contains(Material.matchMaterial(s.getLine(1).toUpperCase()), amount))
          {
            chestContents.removeItem(new ItemStack[] { i });
            chest.update();
            this.cc.loadEconomy();
            this.c.personalMoney.put(e.getPlayer().getName(), Double.valueOf(playerMoney.doubleValue() - price.doubleValue() * Integer.valueOf(s.getLine(3)).intValue()));
            this.cc.saveEconomy();
            e.getPlayer().sendMessage(price.doubleValue() * Integer.valueOf(s.getLine(3)).intValue() + " has been deducted from your account!");
            e.getPlayer().getInventory().addItem(new ItemStack[] { i });
            e.getPlayer().updateInventory();
            
            this.cc.loadEconomy();
            for (String w : this.c.players) {
              if (this.cc.getConfig().getDouble("StockHolders." + e.getPlayer().getName() + "." + company) != 0.0D)
              {
                Double moneyBefore = (Double)this.c.personalMoney.get(w);
                Double totalStock = Double.valueOf(this.cc.getConfig().getDouble("StockHolders." + e.getPlayer().getName() + "." + company));
                this.c.personalMoney.put(w, Double.valueOf(moneyBefore.doubleValue() + price.doubleValue() * Integer.valueOf(s.getLine(3)).intValue() * (totalStock.doubleValue() / 100.0D)));
              }
            }
            this.cc.saveEconomy();
          }
          else
          {
            e.getPlayer().sendMessage(ChatColor.DARK_RED + "cc shop is out of stock!");
          }
        }
        else {
          e.getPlayer().sendMessage(ChatColor.DARK_RED + "You do not have enough money!");
        }
      }
    }
  }
  
  @EventHandler
  public void checkShopPVP(EntityDamageByEntityEvent e)
  {
    Entity victim = e.getEntity();
    Location l = victim.getLocation();
    if ((victim instanceof Player))
    {
      Player p = (Player)victim;
      if (this.c.shops.get(l) != null)
      {
        p.sendMessage(ChatColor.DARK_RED + "PVP is disabled in shops!");
        e.setCancelled(true);
      }
    }
  }
  
  @EventHandler
  public void checkShopInteract(PlayerInteractEvent e)
  {
    Block b = e.getClickedBlock();
    if (b != null)
    {
      Location l = b.getLocation();
      String company = this.cc.getCompany(e.getPlayer());
      if ((this.c.shops.get(l) != null) && 
        (!((String)this.c.shops.get(l)).equalsIgnoreCase(company)))
      {
        e.getPlayer().sendMessage(ChatColor.DARK_RED + "cc is not your shop!");
        e.setCancelled(true);
      }
    }
    if (b != null)
    {
      String comp = this.cc.getCompany(e.getPlayer());
      Boolean match = Boolean.valueOf(false);
      for (Location w : this.c.inventoryChest) {
        if (w.toString().equalsIgnoreCase(b.getLocation().toString()))
        {
          Location signLocation = new Location(b.getWorld(), b.getX(), b.getY() + 1, b.getZ());
          if ((signLocation.getBlock().getState() instanceof Sign))
          {
            Sign sign = (Sign)signLocation.getBlock().getState();
            String signComp = sign.getLine(0);
            if (comp.equalsIgnoreCase(signComp)) {
              break;
            }
            match = Boolean.valueOf(true);
            break;
          }
        }
      }
      if (match.booleanValue()) {
        e.setCancelled(true);
      }
    }
  }
  
  @EventHandler
  public void checkShopBlockPlace(BlockPlaceEvent e)
  {
    Block b = e.getBlock();
    Location l = b.getLocation();
    String company = this.cc.getCompany(e.getPlayer());
    if ((this.c.shops.get(l) != null) && 
      (!((String)this.c.shops.get(l)).equalsIgnoreCase(company)))
    {
      e.getPlayer().sendMessage(ChatColor.DARK_RED + "cc is not your shop!");
      e.setCancelled(true);
    }
  }
  
  @EventHandler(priority=EventPriority.LOW)
  public void checkShopBlockBreak(BlockBreakEvent e)
  {
    Block b = e.getBlock();
    Boolean match;
    if (b != null)
    {
      Location l = b.getLocation();
      String company = this.cc.getCompany(e.getPlayer());
      if ((this.c.shops.get(l) != null) && 
        (!((String)this.c.shops.get(l)).equalsIgnoreCase(company)))
      {
        e.getPlayer().sendMessage(ChatColor.DARK_RED + "cc is not your shop!");
        e.setCancelled(true);
      }
      if (b != null)
      {
        String comp = this.cc.getCompany(e.getPlayer());
        match = Boolean.valueOf(false);
        for (Location w : this.c.inventoryChest) {
          if (w.toString().equalsIgnoreCase(b.getLocation().toString()))
          {
            Location signLocation = new Location(b.getWorld(), b.getX(), b.getY() + 1, b.getZ());
            try
            {
              Sign sign = (Sign)signLocation.getBlock().getState();
              String signComp = sign.getLine(0);
              if (comp.equalsIgnoreCase(signComp)) {
                break;
              }
              match = Boolean.valueOf(true);
            }
            catch (Exception localException) {}
          }
        }
        if (match.booleanValue()) {
          e.setCancelled(true);
        }
      }
    }
    if (b != null)
    {
      String comp = this.cc.getCompany(e.getPlayer());
      match = Boolean.valueOf(false);
      for (Location w : this.c.shopSigns) {
        if (w.toString().equalsIgnoreCase(b.getLocation().toString()))
        {
          Location signLocation = new Location(b.getWorld(), b.getX(), b.getY() + 1, b.getZ());
          try
          {
            Sign sign = (Sign)signLocation.getBlock().getState();
            String signComp = sign.getLine(0);
            if (comp.equalsIgnoreCase(signComp)) {
              break;
            }
            match = Boolean.valueOf(true);
            e.getPlayer().sendMessage(ChatColor.DARK_RED + "cc is someone else's shop sign!");
          }
          catch (Exception localException2) {}
        }
      }
      if (match.booleanValue()) {
        e.setCancelled(true);
      }
    }
  }
  
  @EventHandler
  public void checkRegisteredSign(BlockBreakEvent e)
  {
    Location l = e.getBlock().getLocation();
    ArrayList<Location> locations = this.c.shopSigns;
    for (int i = 0; i < locations.size(); i++) {
      if (((Location)locations.get(i)).toString().equalsIgnoreCase(l.toString()))
      {
        this.c.shopSigns.remove(i);
        e.getPlayer().sendMessage(ChatColor.GRAY + "Shop Sign Deregistered!");
      }
    }
  }
  
  @EventHandler(priority=EventPriority.HIGH)
  public void onEntityDamage(EntityDamageByEntityEvent event)
  {
    Entity attacker = event.getDamager();
    Entity victim = event.getEntity();
    if (((attacker instanceof Player)) && ((victim instanceof Player)))
    {
      Player attackerP = (Player)attacker;
      Player victimP = (Player)victim;
      if (this.cc.getCompany(attackerP).equalsIgnoreCase(this.cc.getCompany(victimP)))
      {
        attackerP.sendMessage(ChatColor.DARK_RED + "You cannot hurt members of your company!");
        event.setCancelled(true);
      }
    }
  }
  
  @EventHandler
  public void onPlayerJoin(PlayerJoinEvent e)
    throws IOException
  {
    Boolean newPlayer = Boolean.valueOf(true);
    if (this.c.players != null) {
      for (String w : this.c.players) {
        if (w.equals(e.getPlayer().getName()))
        {
          newPlayer = Boolean.valueOf(false);
          break;
        }
      }
    }
    if (newPlayer.booleanValue())
    {
      Bukkit.getServer().broadcastMessage(ChatColor.YELLOW + "Welcome " + e.getPlayer().getName() + " to the server!");
      this.c.players.add(e.getPlayer().getName());
      
      this.c.playerCompany.put(e.getPlayer().getName(), "none");
      this.c.position.put(e.getPlayer().getName(), "none");
      if (this.cc.vaultEnabled.booleanValue()) {
        this.cc.econ.createPlayerAccount(e.getPlayer().getName());
      }
    }
  }
  
  @EventHandler(priority=EventPriority.HIGH)
  public void onBlockBreak(BlockBreakEvent e)
  {
    Location blockBroken = e.getBlock().getLocation();
    Iterator<Map.Entry<SerializableLocation, String>> iterator = this.c.protectedBlocks.entrySet().iterator();
    while (iterator.hasNext())
    {
      Map.Entry<SerializableLocation, String> sl = (Map.Entry)iterator.next();
      Location savedBlock = SerializableLocation.returnBlockLocation((SerializableLocation)sl.getKey());
      if (savedBlock.toString().equals(blockBroken.toString()))
      {
        Player p = e.getPlayer();
        String company = this.cc.getCompany(p);
        if (((SerializableLocation)sl.getKey()).equals(company))
        {
          e.getPlayer().sendMessage(ChatColor.GRAY + "Protection Removed");
        }
        else if (sl.getValue() != company)
        {
          e.getPlayer().sendMessage(ChatColor.DARK_RED + "Your company does not own cc block!");
          e.setCancelled(true);
        }
      }
    }
  }
  
  @EventHandler
  public void onPlayerInteract(PlayerInteractEvent e)
  {
    if (e.getAction() == Action.RIGHT_CLICK_BLOCK)
    {
      Location blockBroken = e.getClickedBlock().getLocation();
      Iterator<Map.Entry<SerializableLocation, String>> iterator = this.c.protectedBlocks.entrySet().iterator();
      while (iterator.hasNext())
      {
        Map.Entry<SerializableLocation, String> sl = (Map.Entry)iterator.next();
        Location savedBlock = SerializableLocation.returnBlockLocation((SerializableLocation)sl.getKey());
        if (savedBlock.toString().equalsIgnoreCase(blockBroken.toString()))
        {
          Player p = e.getPlayer();
          String company = this.cc.getCompany(p);
          if (((SerializableLocation)sl.getKey()).equals(company))
          {
            e.getPlayer().sendMessage(ChatColor.GRAY + "Protection Removed");
          }
          else if (sl.getValue() != company)
          {
            e.getPlayer().sendMessage(ChatColor.DARK_RED + "Your company does not own cc chest/furnace!");
            e.setCancelled(true);
          }
        }
      }
    }
  }
}

