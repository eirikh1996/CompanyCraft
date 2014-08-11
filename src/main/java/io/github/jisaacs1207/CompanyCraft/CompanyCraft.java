package io.github.jisaacs1207.CompanyCraft;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.bukkit.selections.Selection;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class CompanyCraft
  extends JavaPlugin
  implements Listener
{
  public final Logger logger = Logger.getLogger("Minecraft");
  public static CompanyCraft plugin;
  public double startingMoney = 30.0D;
  public double startCompMoney = 200.0D;
  public double pricePerBlock = 1.0D;
  public Economy econ = null;
  Boolean newPlayer = Boolean.valueOf(true);
  Boolean vaultEnabled = Boolean.valueOf(false);
  ConfigWriter c;
  private FileConfiguration customConfig = null;
  private File customConfigFile = null;
  Stock s;
  private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
  
  public void onDisable()
  {
    save();
    PluginDescriptionFile pdfFile = getDescription();
    this.logger.info(pdfFile.getName() + " Has Been Disabled!");
  }
  
  public void onEnable()
  {
    this.c = new ConfigWriter(this);
    try
    {
      this.c.init();
    }
    catch (InvalidConfigurationException e1)
    {
      this.logger.info("[CompanyCraft] Save/Load Error!");
    }
    if (!setupEconomy())
    {
      this.logger.severe("[CompanyCraft] Vault/Economy Plugin not found! Defaulting to regular money system!");
    }
    else
    {
      this.logger.info("[CompanyCraft] Successfully hooked into Vault!");
      this.vaultEnabled = Boolean.valueOf(true);
      loadEconomy();
    }
    Bukkit.getPluginManager().registerEvents(new Listeners(this.c, this), this);
    Bukkit.getPluginManager().registerEvents(new ClaimChunkListener(this.c, this), this);
    this.s = new Stock(this, this.c);
    if ((getConfig().getDouble("CompanyCraft.Starting Money") != 0.0D) && (getConfig().getDouble("CompanyCraft.Money to Start a Company") != 0.0D))
    {
      try
      {
        this.s.getStock();
      }
      catch (Exception localException) {}
    }
    else
    {
      saveFirstConfig();
      this.s.fetchStock();
      this.s.getStock();
    }
    startTimers();
    reloadConfig();
    if (!new File("plugins/Company Craft/Prices.yml").exists()) {
      try
      {
        new File("plugins/Company Craft/Prices.yml").createNewFile();
        this.logger.info("[CompanyCraft] Creating Prices.yml...");
      }
      catch (IOException e)
      {
        this.logger.severe("[CompanyCraft] Failed to create Prices.yml!");
      }
    }
    getCustomConfig().options().copyDefaults(true);
    reloadCustomConfig();
    saveDefaultCustomConfig();
    saveCustomConfig();
  }
  
  public void save()
  {
    try
    {
      this.c.save();
    }
    catch (InvalidConfigurationException e)
    {
      e.printStackTrace();
    }
    PluginDescriptionFile pdfFile = getDescription();
    this.logger.info(pdfFile.getName() + " Has Been Enabled! Created by Gcflames5");
  }
  
  public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
  {
    return new Commands(this.c, this).onCommand(sender, cmd, commandLabel, args);
  }
  
  public ArrayList<String> getCompanyList()
  {
    ArrayList<String> players = this.c.players;
    ArrayList<String> companies = new ArrayList<String>();
    for (String w : players) {
      companies.add((String)this.c.playerCompany.get(w));
    }
    return companies;
  }
  
  public String getCompany(Player playerName)
  {
    return (String)this.c.playerCompany.get(playerName.getName());
  }
  
  public String getCompanyByName(String name)
  {
    return (String)this.c.playerCompany.get(name);
  }
  
  public boolean checkHasItem(Player p, ItemStack i)
  {
    if (p.getInventory().contains(i)) {
      return true;
    }
    return false;
  }
  
  public String getPlayerPosition(Player p)
  {
    return (String)this.c.position.get(p.getName());
  }
  
  public String getOwner(String company)
  {
    for (String w : this.c.players) {
      if ((((String)this.c.position.get(w)).equalsIgnoreCase("Owner")) && (getCompanyByName(w).equalsIgnoreCase(company))) {
        return w;
      }
    }
    this.logger.severe("ERROR id87x: getOwner()");
    return null;
  }
  
  public void sendCompanyInfo(Player p, String company)
  {
    String desc;
    if (this.c.description.get(company) != null) {
      desc = (String)this.c.description.get(company);
    } else {
      desc = "Default Description :(";
    }
    p.sendMessage(ChatColor.BLUE + company + ": " + ChatColor.GOLD + desc);
    p.sendMessage(ChatColor.BLUE + "Owner: " + ChatColor.GOLD + getOwner(company));
    String execBuffer = "";
    for (String w : this.c.players) {
      if ((getCompanyByName(w).equalsIgnoreCase(company)) && (((String)this.c.position.get(p.getName())).equalsIgnoreCase("Executive"))) {
        execBuffer = execBuffer + w + ", ";
      }
    }
    p.sendMessage(ChatColor.BLUE + "Executives: " + ChatColor.GOLD + execBuffer.toString());
    String cfoBuffer = "";
    for (String w : this.c.players) {
      if ((getCompanyByName(w).equalsIgnoreCase(company)) && (((String)this.c.position.get(p.getName())).equalsIgnoreCase("CFO"))) {
        cfoBuffer = cfoBuffer + w + ", ";
      }
    }
    p.sendMessage(ChatColor.BLUE + "CFOs: " + ChatColor.GOLD + cfoBuffer.toString());
    String managerBuffer = "";
    for (String w : this.c.players) {
      if ((getCompanyByName(w).equalsIgnoreCase(company)) && (((String)this.c.position.get(p.getName())).equalsIgnoreCase("Manager"))) {
        managerBuffer = managerBuffer + w + ", ";
      }
    }
    p.sendMessage(ChatColor.BLUE + "Managers: " + ChatColor.GOLD + managerBuffer.toString());
    String employeeBuffer = "";
    for (String w : this.c.players) {
      if ((getCompanyByName(w).equalsIgnoreCase(company)) && (((String)this.c.position.get(p.getName())).equalsIgnoreCase("Employee"))) {
        employeeBuffer = employeeBuffer + w + ", ";
      }
    }
    p.sendMessage(ChatColor.BLUE + "Employees: " + ChatColor.GOLD + employeeBuffer.toString());
    String internBuffer = "";
    for (String w : this.c.players) {
      if ((getCompanyByName(w).equalsIgnoreCase(company)) && (((String)this.c.position.get(p.getName())).equalsIgnoreCase("Intern"))) {
        internBuffer = internBuffer + w + ", ";
      }
    }
    p.sendMessage(ChatColor.BLUE + "Interns: " + ChatColor.GOLD + internBuffer.toString());
  }
  
  public void saveFirstConfig()
  {
    getConfig().set("CompanyCraft.Starting Money", Integer.valueOf(30));
    getConfig().set("CompanyCraft.Money to Start a Company", Integer.valueOf(200));
    getConfig().set("CompanyCraft.Amount of Money to protect 1 Block", Double.valueOf(1.0D));
    
    getConfig().options().copyDefaults(true);
    saveDefaultConfig();
  }
  
  public Double getItemPrice(String s)
  {
    Double tuck = Double.valueOf(getConfig().getDouble("Prices." + s.toLowerCase()));
    return tuck;
  }
  
  public void updateSigns()
  {
    if (this.c.signs != null) {
      for (SerializableLocation sl : this.c.signs)
      {
        Location l = SerializableLocation.returnLocation(sl);
        if ((l.getBlock().getState() instanceof Sign))
        {
          Sign s = (Sign)l.getBlock().getState();
          this.c.industrialPercent = Double.valueOf(getConfig().getDouble("RealTimeStocks.Industrial.Trend") / this.c.industrial.doubleValue());
          this.c.textilePercent = Double.valueOf(getConfig().getDouble("RealTimeStocks.Textiles.Trend") / this.c.textiles.doubleValue());
          this.c.raw_materialPercent = Double.valueOf(getConfig().getDouble("RealTimeStocks.Raw_Materials.Trend") / this.c.raw_materials.doubleValue());
          this.c.energyPercent = Double.valueOf(getConfig().getDouble("RealTimeStocks.Energy.Trend") / this.c.energy.doubleValue());
          if (this.c.industrialPercent.doubleValue() >= 0.0D) {
            s.setLine(0, "I: " + ChatColor.DARK_GREEN + round(this.c.industrialPercent.doubleValue() * 1000.0D) + "%");
          } else if (this.c.industrialPercent.doubleValue() < 0.0D) {
            s.setLine(0, "I: " + ChatColor.DARK_RED + round(this.c.industrialPercent.doubleValue() * 1000.0D) + "%");
          }
          if (this.c.textilePercent.doubleValue() >= 0.0D) {
            s.setLine(1, "T: " + ChatColor.DARK_GREEN + round(this.c.textilePercent.doubleValue() * 1000.0D) + "%");
          } else if (this.c.textilePercent.doubleValue() < 0.0D) {
            s.setLine(1, "T: " + ChatColor.DARK_RED + round(this.c.textilePercent.doubleValue() * 1000.0D) + "%");
          }
          if (this.c.raw_materialPercent.doubleValue() >= 0.0D) {
            s.setLine(2, "RM: " + ChatColor.DARK_GREEN + round(this.c.raw_materialPercent.doubleValue() * 1000.0D) + "%");
          } else if (this.c.raw_materialPercent.doubleValue() < 0.0D) {
            s.setLine(2, "RM: " + ChatColor.DARK_RED + round(this.c.raw_materialPercent.doubleValue() * 1000.0D) + "%");
          }
          if (this.c.energyPercent.doubleValue() >= 0.0D) {
            s.setLine(3, "E: " + ChatColor.DARK_GREEN + round(this.c.energyPercent.doubleValue() * 1000.0D) + "%");
          } else if (this.c.energyPercent.doubleValue() < 0.0D) {
            s.setLine(3, "E: " + ChatColor.DARK_RED + round(this.c.energyPercent.doubleValue() * 1000.0D) + "%");
          }
          s.update();
        }
      }
    }
  }
  
  public void startTimers()
  {
    Runnable saveTimer = new Runnable()
    {
      public void run()
      {
        CompanyCraft.this.s.getStock();
      }
    };
    this.scheduler.scheduleAtFixedRate(saveTimer, 10L, 10L, TimeUnit.MINUTES);
    
    Runnable stockTimer = new Runnable()
    {
      public void run()
      {
        CompanyCraft.this.save();
      }
    };
    this.scheduler.scheduleAtFixedRate(stockTimer, 5L, 5L, TimeUnit.MINUTES);
  }
  
  public String getCompanyType(String company)
  {
    return (String)this.c.companyType.get(company);
  }
  
  double round(double d)
  {
    DecimalFormat twoDForm = new DecimalFormat("##.##");
    return Double.valueOf(twoDForm.format(d)).doubleValue();
  }
  
  public void fillShopSelection(String company, Selection sel, World w)
  {
    BlockVector min = sel.getNativeMinimumPoint().toBlockVector();
    BlockVector max = sel.getNativeMaximumPoint().toBlockVector();
    int minx = min.getBlockX();
    int maxx = max.getBlockX();
    int miny = min.getBlockY();
    int maxy = max.getBlockY();
    int minz = min.getBlockZ();
    int maxz = max.getBlockZ();
    for (int x = minx; x <= maxx; x++) {
      for (int y = miny; y <= maxy; y++) {
        for (int z = minz; z <= maxz; z++)
        {
          Location l = new Location(w, x, y, z);
          if (company != "none") {
            this.c.shops.put(l, company);
          }
        }
      }
    }
  }
  
  public void reloadCustomConfig()
  {
    if (this.customConfigFile == null) {
      this.customConfigFile = new File("plugins/Company Craft/Prices.yml");
    }
    if (!this.customConfigFile.exists()) {
      try
      {
        this.customConfigFile.createNewFile();
      }
      catch (IOException e)
      {
        e.printStackTrace();
      }
    }
    this.customConfig = YamlConfiguration.loadConfiguration(this.customConfigFile);
    


    getCustomConfig().options().copyDefaults(true);
    
    InputStream defConfigStream = getResource("plugins/Company Craft/Prices.yml");
    if (defConfigStream != null)
    {
      YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
      this.customConfig.setDefaults(defConfig);
    }
  }
  
  public FileConfiguration getCustomConfig()
  {
    if (this.customConfig == null) {
      reloadCustomConfig();
    }
    return this.customConfig;
  }
  
  public void saveCustomConfig()
  {
    if ((this.customConfig == null) || (this.customConfigFile == null)) {
      return;
    }
    try
    {
      getCustomConfig().save(this.customConfigFile);
    }
    catch (IOException ex)
    {
      getLogger().log(Level.SEVERE, "Could not save config to " + this.customConfigFile, ex);
    }
  }
  
  public void saveDefaultCustomConfig()
  {
    if (!this.customConfigFile.exists()) {
      saveResource(this.customConfigFile.getName(), false);
    }
  }
  
  private boolean setupEconomy()
  {
    if (getServer().getPluginManager().getPlugin("Vault") == null) {
      return false;
    }
    RegisteredServiceProvider<Economy> rsp = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
    if (rsp == null)
    {
      this.logger.severe("error: id-2");
      return false;
    }
    this.econ = ((Economy)rsp.getProvider());
    return this.econ != null;
  }
  
  public boolean playerIsRegistered(String n)
  {
    for (String w : this.c.players) {
      if (w.equalsIgnoreCase(n)) {
        return true;
      }
    }
    return false;
  }
  
  public void loadEconomy()
  {
    if (this.vaultEnabled.booleanValue()) {
      for (String w : this.c.players) {
        this.c.personalMoney.put(w, Double.valueOf(this.econ.getBalance(w)));
      }
    }
  }
  
  public void saveEconomy() {}
}
