package io.github.jisaacs1207.CompanyCraft;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.StringTokenizer;

public class Stock
{
  ConfigWriter c;
  CompanyCraft cc;
  
  public Stock(CompanyCraft cc, ConfigWriter c)
  {
    this.cc = cc;
    this.c = c;
  }
  
  public void fetchStock()
  {
    URL url = null;
    URLConnection urlConn = null;
    InputStreamReader inStream = null;
    BufferedReader buff = null;
    for (int i = 0; i <= 3; i++) {
      try
      {
        if (i == 0) {
          url = new URL("http://quote.yahoo.com/d/quotes.csv?s=XLI&f=sl1d1t1c1ohgv&e=.csv");
        }
        if (i == 1) {
          url = new URL("http://quote.yahoo.com/d/quotes.csv?s=MON&f=sl1d1t1c1ohgv&e=.csv");
        }
        if (i == 2) {
          url = new URL("http://quote.yahoo.com/d/quotes.csv?s=XLE&f=sl1d1t1c1ohgv&e=.csv");
        }
        if (i == 3) {
          url = new URL("http://quote.yahoo.com/d/quotes.csv?s=XLB&f=sl1d1t1c1ohgv&e=.csv");
        }
        urlConn = url.openConnection();
        inStream = 
          new InputStreamReader(urlConn.getInputStream());
        buff = new BufferedReader(inStream);
        String csvString = buff.readLine();
        
        StringTokenizer tokenizer = 
          new StringTokenizer(csvString, ",");
        tokenizer.nextToken();
        String price = tokenizer.nextToken();
        tokenizer.nextToken();
        tokenizer.nextToken();
        String trend = tokenizer.nextToken();
        if (i == 0)
        {
          this.c.industrial = Double.valueOf(price);
          this.cc.getConfig().set("RealTimeStocks.Industrial.Trend", Double.valueOf(trend));
        }
        if (i == 1)
        {
          this.c.textiles = Double.valueOf(price);
          this.cc.getConfig().set("RealTimeStocks.Textiles.Trend", Double.valueOf(trend));
        }
        if (i == 2)
        {
          this.c.energy = Double.valueOf(price);
          this.cc.getConfig().set("RealTimeStocks.Energy.Trend", Double.valueOf(trend));
        }
        if (i == 3)
        {
          this.c.raw_materials = Double.valueOf(price);
          this.cc.getConfig().set("RealTimeStocks.Raw_Materials.Trend", Double.valueOf(trend));
        }
      }
      catch (MalformedURLException e)
      {
        System.out.println("Please check the spelling of the URL:" + e.toString());
        



        this.cc.saveConfig();
        try
        {
          inStream.close();
          buff.close();
        }
        catch (Exception e2)
        {
          this.cc.logger.severe("Cannot connect to the Internet!");
        }
      }
      catch (IOException e1)
      {
        System.out.println("Can't read from the Internet: " + e1.toString());
        

        this.cc.saveConfig();
        try
        {
          inStream.close();
          buff.close();
        }
        catch (Exception e)
        {
          this.cc.logger.severe("Cannot connect to the Internet!");
        }
      }
      finally
      {
        this.cc.saveConfig();
        try
        {
          inStream.close();
          buff.close();
        }
        catch (Exception e)
        {
          this.cc.logger.severe("Cannot connect to the Internet!");
        }
      }
    }
  }
  
  public void getStock()
  {
    this.cc.getConfig().set("RealTimeStocks.Industrial", this.c.industrial);
    this.cc.getConfig().set("RealTimeStocks.Textiles", this.c.textiles);
    this.cc.getConfig().set("RealTimeStocks.Raw_Materials", this.c.raw_materials);
    this.cc.getConfig().set("RealTimeStocks.Energy", this.c.energy);
    
    this.c.industrialPercent = Double.valueOf(this.cc.getConfig().getDouble("RealTimeStocks.Industrial.Trend") / this.c.industrial.doubleValue());
    this.c.textilePercent = Double.valueOf(this.cc.getConfig().getDouble("RealTimeStocks.Textiles.Trend") / this.c.textiles.doubleValue());
    this.c.raw_materialPercent = Double.valueOf(this.cc.getConfig().getDouble("RealTimeStocks.Raw_Materials.Trend") / this.c.raw_materials.doubleValue());
    this.c.energyPercent = Double.valueOf(this.cc.getConfig().getDouble("RealTimeStocks.Energy.Trend") / this.c.energy.doubleValue());
    
    this.cc.saveConfig();
    fetchStock();
  }
}

