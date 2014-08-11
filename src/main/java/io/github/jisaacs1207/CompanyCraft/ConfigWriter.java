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
  
  public ArrayList<String> players = new ArrayList<String>();
  HashMap<String, String> playerCompany = new HashMap<String, String>();
  HashMap<SerializableLocation, String> protectedBlocks = new HashMap<SerializableLocation, String>();
  HashMap<String, String> invites = new HashMap<String, String>();
  HashMap<String, String> position = new HashMap<String, String>();
  HashMap<String, String> forSale = new HashMap<String, String>();
  HashMap<String, String> description = new HashMap<String, String>();
  HashMap<String, Double> personalMoney = new HashMap<String, Double>();
  HashMap<String, Double> companyMoney = new HashMap<String, Double>();
  HashMap<String, Integer> stockForSale = new HashMap<String, Integer>();
  HashMap<String, Integer> pricePerShare = new HashMap<String, Integer>();
  ArrayList<SerializableLocation> signs = new ArrayList<SerializableLocation>();
  HashMap<String, String> companyType = new HashMap<String, String>();
  HashMap<String, SerializableLocation> companyHome = new HashMap<String, SerializableLocation>();
  HashMap<Location, String> shops = new HashMap<Location, String>();
  ArrayList<Location> inventoryChest = new ArrayList<Location>();
  ArrayList<Location> shopSigns = new ArrayList<Location>();
  HashMap<String, String> companyChunks = new HashMap<String, String>();
  Double industrial = Double.valueOf(0.0D);
  Double textiles = Double.valueOf(0.0D);
  Double raw_materials = Double.valueOf(0.0D);
  Double energy = Double.valueOf(0.0D);
  public Double industrialPercent;
  public Double textilePercent;
  public Double raw_materialPercent;
  public Double energyPercent;
}

