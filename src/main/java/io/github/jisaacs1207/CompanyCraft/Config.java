package io.github.jisaacs1207.CompanyCraft;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

public abstract class Config
  extends ConfigObject
{
  protected transient File CONFIG_FILE = null;
  protected transient String CONFIG_HEADER = null;
  
  public Config()
  {
    this.CONFIG_HEADER = null;
  }
  
  public Config load(File file)
    throws InvalidConfigurationException
  {
    if (file == null) {
      throw new InvalidConfigurationException(new NullPointerException());
    }
    if (!file.exists()) {
      throw new InvalidConfigurationException(new IOException("File doesn't exist"));
    }
    this.CONFIG_FILE = file;
    return reload();
  }
  
  public Config reload()
    throws InvalidConfigurationException
  {
    if (this.CONFIG_FILE == null) {
      throw new InvalidConfigurationException(new NullPointerException());
    }
    if (!this.CONFIG_FILE.exists()) {
      throw new InvalidConfigurationException(new IOException("File doesn't exist"));
    }
    YamlConfiguration yamlConfig = YamlConfiguration.loadConfiguration(this.CONFIG_FILE);
    try
    {
      onLoad(yamlConfig);
      yamlConfig.save(this.CONFIG_FILE);
    }
    catch (Exception ex)
    {
      throw new InvalidConfigurationException(ex);
    }
    return this;
  }
  
  public Config save(File file)
    throws InvalidConfigurationException
  {
    if (file == null) {
      throw new InvalidConfigurationException(new NullPointerException());
    }
    this.CONFIG_FILE = file;
    return save();
  }
  
  public Config save()
    throws InvalidConfigurationException
  {
    if (this.CONFIG_FILE == null) {
      throw new InvalidConfigurationException(new NullPointerException());
    }
    if (!this.CONFIG_FILE.exists()) {
      try
      {
        if (this.CONFIG_FILE.getParentFile() != null) {
          this.CONFIG_FILE.getParentFile().mkdirs();
        }
        this.CONFIG_FILE.createNewFile();
        if (this.CONFIG_HEADER != null)
        {
          Writer newConfig = new BufferedWriter(new FileWriter(this.CONFIG_FILE));
          for (String line : this.CONFIG_HEADER.split("\n")) {
            newConfig.write("# " + line + "\n");
          }
          newConfig.close();
        }
      }
      catch (Exception ex)
      {
        throw new InvalidConfigurationException(ex);
      }
    }
    YamlConfiguration yamlConfig = YamlConfiguration.loadConfiguration(this.CONFIG_FILE);
    try
    {
      onSave(yamlConfig);
      yamlConfig.save(this.CONFIG_FILE);
    }
    catch (Exception ex)
    {
      throw new InvalidConfigurationException(ex);
    }
    return this;
  }
  
  public Config init(File file)
    throws InvalidConfigurationException
  {
    if (file == null) {
      throw new InvalidConfigurationException(new NullPointerException());
    }
    this.CONFIG_FILE = file;
    return init();
  }
  
  public Config init()
    throws InvalidConfigurationException
  {
    if (this.CONFIG_FILE == null) {
      throw new InvalidConfigurationException(new NullPointerException());
    }
    if (this.CONFIG_FILE.exists()) {
      return reload();
    }
    return save();
  }
}
