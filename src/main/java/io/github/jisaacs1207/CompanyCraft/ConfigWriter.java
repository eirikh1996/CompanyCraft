package io.github.jisaacs1207.CompanyCraft;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;

public class ConfigWriter
  extends Config
{
  public ConfigWriter(Plugin plugin)
  {
    this.CONFIG_FILE = new File(plugin.getDataFolder(), "SaveDat.yml");
    this.CONFIG_HEADER = "COMPANY CRAFT SAVE FILE DO NOT MESS WITH FILE UNLESS YOU KNOW WHAT YOU ARE DOING!!!    created by Gcflames5";
  }
  
  public ArrayList<String> players = new ArrayList();
  HashMap<String, String> playerCompany = new HashMap();
  HashMap<SerializableLocation, String> protectedBlocks = new HashMap();
  HashMap<String, String> invites = new HashMap();
  HashMap<String, String> position = new HashMap();
  HashMap<String, String> forSale = new HashMap();
  HashMap<String, String> description = new HashMap();
  HashMap<String, Double> personalMoney = new HashMap();
  HashMap<String, Double> companyMoney = new HashMap();
  HashMap<String, Integer> stockForSale = new HashMap();
  HashMap<String, Integer> pricePerShare = new HashMap();
  ArrayList<SerializableLocation> signs = new ArrayList();
  HashMap<String, String> companyType = new HashMap();
  HashMap<String, SerializableLocation> companyHome = new HashMap();
  HashMap<Location, String> shops = new HashMap();
  ArrayList<Location> inventoryChest = new ArrayList();
  ArrayList<Location> shopSigns = new ArrayList();
  HashMap<String, String> companyChunks = new HashMap();
  Double industrial = Double.valueOf(0.0D);
  Double textiles = Double.valueOf(0.0D);
  Double raw_materials = Double.valueOf(0.0D);
  Double energy = Double.valueOf(0.0D);
  public Double industrialPercent;
  public Double textilePercent;
  public Double raw_materialPercent;
  public Double energyPercent;
}

