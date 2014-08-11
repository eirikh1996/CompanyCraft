package io.github.jisaacs1207.CompanyCraft;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.CuboidSelection;
import com.sk89q.worldedit.bukkit.selections.Selection;

import java.util.ArrayList;
import java.util.Iterator;

import net.milkbowl.vault.economy.EconomyResponse;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Commands
  implements CommandExecutor
{
  CompanyCraft cc;
  ConfigWriter c;
  
  public Commands(ConfigWriter configWriter, CompanyCraft companyCraft)
  {
    this.c = configWriter;
    this.cc = companyCraft;
  }
  
  public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
  {
    if (cmd.getName().equalsIgnoreCase("createCompany"))
    {
      if (args.length == 2)
      {
        ArrayList<String> players = this.c.players;
        boolean nameAllowed = true;
        for (String i : players) {
          if ((this.c.playerCompany.get(i) != null) && 
            (((String)this.c.playerCompany.get(i)).equalsIgnoreCase(args[0].toString()))) {
            nameAllowed = false;
          }
        }
        if (!nameAllowed) {
          sender.sendMessage(ChatColor.DARK_RED + "This name already exists, please choose another.");
        }
        if (!((String)this.c.playerCompany.get(sender.getName())).equalsIgnoreCase("none"))
        {
          nameAllowed = false;
          sender.sendMessage(ChatColor.DARK_RED + "You are already in a company, leave with /ccLeave!");
        }
        if (args[0].equalsIgnoreCase("none"))
        {
          sender.sendMessage(ChatColor.DARK_RED + "Congratulations, you sir, have managed to find the ONE name out of billions of others that will mess with the code, try again!");
          nameAllowed = false;
        }
        String type = "Industrial";
        if ((args[1].equalsIgnoreCase("Industrial")) || (args[1].equalsIgnoreCase("Raw_Materials")) || (args[1].equalsIgnoreCase("Energy")) || (args[1].equalsIgnoreCase("Textile")))
        {
          if (nameAllowed) {
            if (args[1].equalsIgnoreCase("Industrial"))
            {
              this.c.companyType.put(args[0], "Industrial");
              type = "Industrial";
            }
            else if (args[1].equalsIgnoreCase("Raw_Materials"))
            {
              this.c.companyType.put(args[0], "Raw_Materials");
              type = "Raw_Materials";
            }
            else if (args[1].equalsIgnoreCase("Energy"))
            {
              this.c.companyType.put(args[0], "Energy");
              type = "Energy";
            }
            else if (args[1].equalsIgnoreCase("Textile"))
            {
              this.c.companyType.put(args[0], "Textile");
              type = "Textile";
            }
          }
        }
        else
        {
          nameAllowed = false;
          sender.sendMessage(ChatColor.DARK_RED + "Valid Company Types: Industrial, Raw_Materials, Energy, Textile");
        }
        if (nameAllowed)
        {
          this.c.playerCompany.put(sender.getName(), args[0].toString());
          sender.sendMessage(ChatColor.GRAY + "Congrats! " + args[0] + " has been created!");
          this.c.position.put(sender.getName(), "Owner");
          this.c.companyMoney.put(args[0].toString(), Double.valueOf(0.0D));
          this.cc.getConfig().set("StockHolders." + sender.getName() + "." + args[0], Integer.valueOf(100));
          
          this.cc.saveConfig();
          this.cc.reloadConfig();
        }
        return true;
      }
      sender.sendMessage(ChatColor.DARK_RED + "Command Usage: /createCompany <companyname> <companyType>");
      return true;
    }
    String s1;
    if ((cmd.getName().equalsIgnoreCase("debug")) && 
      ((sender instanceof Player)))
    {
      Player p = (Player)sender;
      String s = p.getItemInHand().getType().toString();
      s1 = String.valueOf(p.getItemInHand().getDurability());
      
      sender.sendMessage(s + " : " + s1);
      
      this.cc.logger.info((String)this.c.playerCompany.get(sender.getName()) + " ");
      this.cc.logger.info(this.c.players + " ");
      return true;
    }
    if (cmd.getName().equalsIgnoreCase("ccProtect"))
    {
      if ((sender instanceof Player))
      {
        Player p = (Player)sender;
        SerializableLocation l = new SerializableLocation(p.getTargetBlock(null, 50).getLocation());
        if (this.cc.getCompany(p) != "none")
        {
          this.c.protectedBlocks.put(l, this.cc.getCompany(p));
          p.sendMessage(ChatColor.GRAY + "Protection successfully added");
        }
        else
        {
          sender.sendMessage(ChatColor.DARK_RED + "You cannot protect cc. block because you are not in an company! Create one with /createCompany");
        }
        return true;
      }
      this.cc.logger.info("Only players can use /ccProtect");
      return true;
    }
    if (cmd.getName().equalsIgnoreCase("ccLeave")) {
      if ((sender instanceof Player))
      {
        Player p = (Player)sender;
        if (this.cc.getPlayerPosition(p) != null)
        {
          if (!this.cc.getPlayerPosition(p).equalsIgnoreCase("Owner"))
          {
            if (!this.cc.getCompany(p).equalsIgnoreCase("none"))
            {
              String name = sender.getName();
              sender.sendMessage(ChatColor.GRAY + "You have left " + this.cc.getCompany(p));
              this.c.playerCompany.put(name, "none");
              this.c.position.put(sender.getName(), "none");
              
              return true;
            }
            sender.sendMessage(ChatColor.DARK_RED + "You are not in a company! Therefore you cannot leave!");
          }
          else
          {
            sender.sendMessage("You are the owner! You must /ccDisband or pass the ownership to someone else");
            return true;
          }
        }
        else {
          this.cc.logger.severe("Please notify the creator of cc. plugin of cc. error: Error ID x88");
        }
      }
      else
      {
        this.cc.logger.info("Only a player can use /ccLeave!");
        return true;
      }
    }
    if (cmd.getName().equalsIgnoreCase("ccList"))
    {
      StringBuffer sb = new StringBuffer(400);
      for (String w : this.cc.getCompanyList()) {
        sb = sb.append(w + ", ");
      }
      sender.sendMessage(sb.toString());
      return true;
    }
    if (cmd.getName().equalsIgnoreCase("ccInvite")) {
      if (args.length == 1)
      {
        if ((sender instanceof Player))
        {
          Player p = (Player)sender;
          if (((String)this.c.position.get(p.getName())).equalsIgnoreCase("Owner"))
          {
            if (p.isOnline())
            {
              Player target = Bukkit.getServer().getPlayer(args[0]);
              if (target != null)
              {
                if (target.isOnline())
                {
                  this.c.invites.put(target.getName(), this.cc.getCompany(p));
                  sender.sendMessage(ChatColor.DARK_GREEN + args[0] + " has been invited to " + this.cc.getCompany(p));
                  return true;
                }
                sender.sendMessage(ChatColor.DARK_RED + target.getName() + " is not online! Invite not sent!");
                return true;
              }
              sender.sendMessage(ChatColor.DARK_RED + " is not a valid name! Invite not sent!");
              return true;
            }
          }
          else {
            p.sendMessage("You are not the owner!");
          }
        }
        else
        {
          sender.sendMessage(ChatColor.DARK_RED + "That person does not exist/is not online!");
          return true;
        }
      }
      else
      {
        sender.sendMessage(ChatColor.DARK_RED + "Command Usage: /ccInvite <playername>");
        return true;
      }
    }
    if (cmd.getName().equalsIgnoreCase("ccInfo")) {
      if ((sender instanceof Player))
      {
        Player p = (Player)sender;
        if ((args.length == 0) && 
          (!this.cc.getCompany(p).equalsIgnoreCase("none"))) {
          this.cc.sendCompanyInfo(p, this.cc.getCompany(p));
        }
      }
      else
      {
        this.cc.logger.info("Only players can use /ccInfo!");
      }
    }
    if (cmd.getName().equalsIgnoreCase("ccSetDesc")) {
      if ((sender instanceof Player))
      {
        Player p = (Player)sender;
        if (!this.cc.getCompany(p).equalsIgnoreCase("none"))
        {
          if (this.cc.getPlayerPosition(p).equalsIgnoreCase("Owner"))
          {
            StringBuffer desc = new StringBuffer(400);
            for (String w : args) {
              desc = desc.append(w + " ");
            }
            this.c.description.put(this.cc.getCompany(p), desc.toString());
            sender.sendMessage(ChatColor.GRAY + "Description Set!");
          }
          else
          {
            sender.sendMessage(ChatColor.DARK_RED + "You do not have permission to change the description of your company!");
          }
        }
        else {
          sender.sendMessage(ChatColor.DARK_RED + "You are not in a company! Create one with /createCompany or join one with /ccJoin");
        }
      }
      else
      {
        this.cc.logger.info("Only players can use /ccDesc!");
      }
    }
    if (cmd.getName().equalsIgnoreCase("ccJoin"))
    {
      if ((sender instanceof Player))
      {
        if (args.length == 1)
        {
          Player p = (Player)sender;
          String company = args[0];
          if (this.c.invites.get(p.getName()) != null)
          {
            if (((String)this.c.invites.get(p.getName())).equalsIgnoreCase(company))
            {
              this.c.playerCompany.put(p.getName(), company);
              sender.sendMessage(ChatColor.DARK_GREEN + "You have sucessfully joined: " + company);
              this.c.position.put(p.getName(), "Intern");
              return true;
            }
            sender.sendMessage(ChatColor.DARK_RED + "You have no pending invitations for " + company + "!");
            return true;
          }
          sender.sendMessage(ChatColor.DARK_RED + "You have no pending invitations for " + company + "!");
          return true;
        }
        sender.sendMessage(ChatColor.DARK_RED + "Command Usage: /ccJoin <companyname>");
        return true;
      }
      this.cc.logger.info("Only players can use /ccJoin!");
    }
    if ((cmd.getName().equalsIgnoreCase("ccMoney")) || (cmd.getName().equalsIgnoreCase("ccBalance")))
    {
      if ((sender instanceof Player))
      {
        Player p = (Player)sender;
        this.cc.loadEconomy();
        sender.sendMessage(ChatColor.GRAY + "Money: " + this.c.personalMoney.get(p.getName()));
        return true;
      }
      this.cc.logger.info("Only players can use /ccMoney and /ccBalance!");
      return true;
    }
    if ((cmd.getName().equalsIgnoreCase("ccDisband")) && 
      ((sender instanceof Player)))
    {
      Player p = (Player)sender;
      String company = this.cc.getCompany(p);
      if (this.cc.getPlayerPosition(p).equalsIgnoreCase("Owner")) {
        for (String w : this.c.players)
        {
          if (w != null)
          {
            int stock = this.cc.getConfig().getInt("StockHolders." + w + "." + company);
            if ((stock != 0) && (stock != 0.0D))
            {
              this.cc.loadEconomy();
              Double money = (Double)this.c.personalMoney.get(w);
              Double compMoney = (Double)this.c.companyMoney.get(company);
              this.c.personalMoney.put(w, Double.valueOf(money.doubleValue() + compMoney.doubleValue() * stock));
              if (this.cc.vaultEnabled.booleanValue()) {
                EconomyResponse localEconomyResponse1 = this.cc.econ.depositPlayer(w, compMoney.doubleValue() * stock);
              }
              Player targetP = Bukkit.getServer().getPlayer(w);
              targetP.sendMessage(ChatColor.GRAY + company + "has been disbanded! You got $" + money + compMoney.doubleValue() * stock);
            }
          }
          if (this.cc.getCompanyByName(w).equals(this.cc.getCompany(p)))
          {
            this.c.playerCompany.put(w, "none");
            this.c.position.put(w, "none");
            this.cc.getConfig().set("StockHolders." + w + "." + company, Integer.valueOf(0));
          }
        }
      }
    }
    Object company;
    if ((cmd.getName().equalsIgnoreCase("ccSellStock")) && 
      ((sender instanceof Player)))
    {
      Player p = (Player)sender;
      if (this.cc.getPlayerPosition(p).equalsIgnoreCase("Owner"))
      {
        if (args.length == 2)
        {
          int numberOf = Integer.valueOf(args[0]).intValue();
          int pricePer = Integer.valueOf(args[1]).intValue();
          int currentStock = this.cc.getConfig().getInt("StockHolders." + p.getName() + "." + this.cc.getCompany(p));
          if (currentStock - Integer.valueOf(args[0]).intValue() < 51)
          {
            p.sendMessage(ChatColor.DARK_RED + "You cannot give away more than half of your company!");
            return true;
          }
          company = this.cc.getCompany(p);
          if (this.c.stockForSale.get(company) != null)
          {
            this.c.stockForSale.put(String.valueOf(company), Integer.valueOf(numberOf + ((Integer)this.c.stockForSale.get(company)).intValue()));
            p.sendMessage("Selling " + numberOf + " shares for " + pricePer + " per share!");
            p.sendMessage("In total, you are now selling " + numberOf + this.c.stockForSale.get(company) + " stocks!");
          }
          else
          {
            this.c.stockForSale.put(String.valueOf(company), Integer.valueOf(numberOf));
            p.sendMessage("Selling " + numberOf + " shares for " + pricePer + " per share!");
          }
          this.c.pricePerShare.put(String.valueOf(company), Integer.valueOf(pricePer));
          this.cc.getConfig().set("StockHolders." + p.getName() + "." + this.cc.getCompany(p), 
            Integer.valueOf(currentStock - Integer.valueOf(args[0]).intValue()));
          this.cc.saveConfig();
          this.cc.reloadConfig();
          return true;
        }
        p.sendMessage(ChatColor.DARK_RED + "Command Usage /ccSellStock <NumberOfStock> <PricePerShare>");
        return true;
      }
      if (this.cc.getPlayerPosition(p).equalsIgnoreCase("none"))
      {
        p.sendMessage(ChatColor.DARK_RED + "You cannot sell stock because you are not in a company! Create one with /ccCreateCompany");
        return true;
      }
      p.sendMessage(ChatColor.DARK_RED + "You are not the owner of your company! You cannot sell stock!");
      return true;
    }
    if ((cmd.getName().equalsIgnoreCase("ccStockList")) && 
      ((sender instanceof Player)))
    {
      ArrayList<String> companies = new ArrayList<String>();
      ArrayList<Integer> amountSelling = new ArrayList<Integer>();
      ArrayList<Integer> amountPerStock = new ArrayList<Integer>();
      for (company = this.cc.getCompanyList().iterator(); ((Iterator<?>)company).hasNext();)
      {
        String w = (String)((Iterator<?>)company).next();
        if (this.c.stockForSale.get(w) != null)
        {
          companies.add(w);
          amountSelling.add((Integer)this.c.stockForSale.get(w));
          amountPerStock.add((Integer)this.c.pricePerShare.get(w));
        }
      }
      for (int i = 0; i < companies.size(); i++) {
        sender.sendMessage("Company: " + (String)companies.get(i) + " Amount For Sale: " + amountSelling.get(i) + " Price Per Share: " + 
          amountPerStock.get(i));
      }
      return true;
    }
    if ((cmd.getName().equalsIgnoreCase("ccBuyStock")) && 
      ((sender instanceof Player))) {
      if (this.c.stockForSale.get(args[0]) != null)
      {
        Player p = (Player)sender;
        if (args.length == 2)
        {
          String companyName = args[0];
          int numberOfStock = Integer.valueOf(args[1]).intValue();
          int totalCost = ((Integer)this.c.pricePerShare.get(companyName)).intValue() + numberOfStock;
          this.cc.loadEconomy();
          if ((this.c.stockForSale.get(companyName) != null) && (((Integer)this.c.stockForSale.get(companyName)).intValue() != 0) && (((Integer)this.c.stockForSale.get(companyName)).intValue() >= numberOfStock))
          {
            if (((Double)this.c.personalMoney.get(p.getName())).doubleValue() >= totalCost)
            {
              this.c.stockForSale.put(companyName, Integer.valueOf(((Integer)this.c.stockForSale.get(companyName)).intValue() - numberOfStock));
              if (((Integer)this.c.stockForSale.get(companyName)).intValue() == 0) {
                this.c.stockForSale.put(companyName, null);
              }
              this.cc.getConfig().set("StockHolders." + p.getName() + "." + companyName, Integer.valueOf(numberOfStock));
              Double money = (Double)this.c.personalMoney.get(p.getName());
              this.c.personalMoney.put(p.getName(), Double.valueOf(money.doubleValue() - totalCost));
              if (this.cc.vaultEnabled.booleanValue())
              {
                EconomyResponse r = this.cc.econ.withdrawPlayer(p.getName(), totalCost);
                if (r.transactionSuccess()) {
                  sender.sendMessage(String.format("You were given %s and now have %s", new Object[] { this.cc.econ.format(r.amount), this.cc.econ.format(r.balance) }));
                } else {
                  sender.sendMessage(String.format("An error occured: %s", new Object[] { r.errorMessage }));
                }
              }
              Double ownerMoney = (Double)this.c.personalMoney.get(this.cc.getOwner(companyName));
              this.c.personalMoney.put(this.cc.getOwner(companyName), Double.valueOf(ownerMoney.doubleValue() + totalCost));
              if (this.cc.vaultEnabled.booleanValue())
              {
                EconomyResponse r1 = this.cc.econ.depositPlayer(this.cc.getOwner(companyName), totalCost);
                if (r1.transactionSuccess()) {
                  sender.sendMessage(String.format("You were given %s and now have %s", new Object[] { this.cc.econ.format(r1.amount), this.cc.econ.format(r1.balance) }));
                } else {
                  sender.sendMessage(String.format("An error occured: %s", new Object[] { r1.errorMessage }));
                }
              }
              p.sendMessage(ChatColor.DARK_GREEN + "You have bought " + numberOfStock + " stocks of " + companyName + " at " + this.c.pricePerShare.get(companyName) + " dollars per share!");
            }
            else
            {
              p.sendMessage(ChatColor.DARK_RED + "You only have " + this.c.personalMoney.get(p.getName()) + 
                " dollars! " + numberOfStock + " shares in " + companyName + "costs" + totalCost + "!");
            }
          }
          else {
            p.sendMessage(ChatColor.DARK_RED + companyName + " is not selling " + numberOfStock + " stocks!");
          }
        }
        else
        {
          p.sendMessage(ChatColor.DARK_RED + "Command Usage: /ccBuyStock <Company Name> <# of stock>");
        }
      }
      else
      {
        sender.sendMessage(ChatColor.DARK_RED + args[0] + " is not selling any stock!");
      }
    }
    Double totalMoney;
    if ((cmd.getName().equalsIgnoreCase("ccSellItem")) && 
      ((sender instanceof Player)))
    {
      Player p = (Player)sender;
      company = this.cc.getCompany(p);
      if (args.length == 2)
      {
        String itemName = args[0];
        String manipulatedItemName1 = itemName.replace("_", "");
        String manipulatedItemName = manipulatedItemName1.toLowerCase();
        if (this.cc.getConfig().getDouble("Prices." + itemName) != 0.0D)
        {
          Double price = Double.valueOf(this.cc.getConfig().getDouble("Prices." + manipulatedItemName));
          int itemAmount = Integer.valueOf(args[1]).intValue();
          ItemStack item = null;
          Boolean cont = Boolean.valueOf(true);
          try
          {
            item = new ItemStack(Material.matchMaterial(itemName.toUpperCase()), itemAmount);
          }
          catch (Exception e)
          {
            e.printStackTrace();
            sender.sendMessage(ChatColor.DARK_RED + "Item does not exist! Instead of ironsword, try iron_sword.");
            cont = Boolean.valueOf(false);
          }
          if ((this.cc.checkHasItem(p, item)) && (cont.booleanValue()))
          {
            p.getInventory().remove(item);
            totalMoney = Double.valueOf(price.doubleValue() * itemAmount * 0.75D);
            
            Double moneyForSeller = Double.valueOf(totalMoney.doubleValue() * 0.25D);
            totalMoney = Double.valueOf(totalMoney.doubleValue() - moneyForSeller.doubleValue());
            this.cc.loadEconomy();
            this.c.personalMoney.put(p.getName(), Double.valueOf(((Double)this.c.personalMoney.get(p.getName())).doubleValue() + moneyForSeller.doubleValue()));
            if (this.cc.vaultEnabled.booleanValue())
            {
              EconomyResponse r = this.cc.econ.depositPlayer(p.getName(), moneyForSeller.doubleValue());
              if (r.transactionSuccess()) {
                sender.sendMessage(String.format("You were given %s and now have %s", new Object[] { this.cc.econ.format(r.amount), this.cc.econ.format(r.balance) }));
              } else {
                sender.sendMessage(String.format("An error occured: %s", new Object[] { r.errorMessage }));
              }
            }
            p.sendMessage(ChatColor.GRAY + "You have earned " + moneyForSeller + " dollars!");
            for (String w : this.c.players) {
              if (this.cc.getConfig().getDouble("StockHolders." + p.getName() + "." + company) != 0.0D)
              {
                Double moneyBefore = (Double)this.c.personalMoney.get(w);
                Double moneyDue = Double.valueOf(this.cc.getConfig().getDouble("StockHolders." + p.getName() + "." + company));
                Double moneyNow = Double.valueOf(moneyBefore.doubleValue() + moneyDue.doubleValue());
                this.c.personalMoney.put(w, moneyNow);
                if (this.cc.vaultEnabled.booleanValue())
                {
                  EconomyResponse r1 = this.cc.econ.depositPlayer(p.getName(), moneyDue.doubleValue());
                  if (r1.transactionSuccess()) {
                    sender.sendMessage(String.format("You were given %s and now have %s", new Object[] { this.cc.econ.format(r1.amount), this.cc.econ.format(r1.balance) }));
                  } else {
                    sender.sendMessage(String.format("An error occured: %s", new Object[] { r1.errorMessage }));
                  }
                }
                this.cc.logger.info(p.getName() + " has been paid " + moneyDue);
              }
            }
          }
          else
          {
            sender.sendMessage(ChatColor.DARK_RED + "You do not have enough of cc. item!");
          }
        }
        else
        {
          sender.sendMessage(ChatColor.DARK_RED + "Item: " + itemName + " does not exist! If you are experiencing problems, try /ccSellItemInHand");
        }
      }
      else
      {
        sender.sendMessage(ChatColor.DARK_RED + "Command Usage: /ccSellItem <ItemName> <# of Item>");
      }
    }
    if ((cmd.getName().equalsIgnoreCase("ccSellItemInHand")) && 
      ((sender instanceof Player)))
    {
      Player p = (Player)sender;
      company = (String)this.c.playerCompany.get(p.getName());
      ItemStack item = p.getItemInHand();
      int itemAmount = item.getAmount();
      String itemName = item.getType().toString().toLowerCase().replace("_", "");
      Double price = Double.valueOf(this.cc.getConfig().getDouble("Prices." + itemName));
      this.cc.logger.info(itemAmount + "  " + itemName + "  " + price);
      p.getInventory().remove(item);
      totalMoney = Double.valueOf(price.doubleValue() * itemAmount * 0.75D);
      
      Double moneyForSeller = Double.valueOf(totalMoney.doubleValue() * 0.25D);
      totalMoney = Double.valueOf(totalMoney.doubleValue() - moneyForSeller.doubleValue());
      this.cc.loadEconomy();
      this.c.personalMoney.put(p.getName(), Double.valueOf(((Double)this.c.personalMoney.get(p.getName())).doubleValue() + moneyForSeller.doubleValue()));
      if (this.cc.vaultEnabled.booleanValue())
      {
        EconomyResponse r = this.cc.econ.depositPlayer(p.getName(), moneyForSeller.doubleValue());
        if (r.transactionSuccess()) {
          sender.sendMessage(String.format("You were given %s and now have %s", new Object[] { this.cc.econ.format(r.amount), this.cc.econ.format(r.balance) }));
        } else {
          sender.sendMessage(String.format("An error occured: %s", new Object[] { r.errorMessage }));
        }
      }
      p.sendMessage(ChatColor.GRAY + "You have earned " + moneyForSeller + " dollars!");
      for (String w : this.c.players) {
        if (this.cc.getCustomConfig().getDouble("StockHolders." + p.getName() + "." + company) != 0.0D)
        {
          Double moneyBefore = (Double)this.c.personalMoney.get(w);
          Double moneyDue = Double.valueOf(this.cc.getConfig().getDouble("StockHolders." + p.getName() + "." + company));
          Double moneyNow = Double.valueOf(moneyBefore.doubleValue() + moneyDue.doubleValue());
          this.c.personalMoney.put(w, moneyNow);
          if (this.cc.vaultEnabled.booleanValue())
          {
            EconomyResponse r1 = this.cc.econ.depositPlayer(w, moneyDue.doubleValue());
            if (r1.transactionSuccess()) {
              sender.sendMessage(String.format("You were given %s and now have %s", new Object[] { this.cc.econ.format(r1.amount), this.cc.econ.format(r1.balance) }));
            } else {
              sender.sendMessage(String.format("An error occured: %s", new Object[] { r1.errorMessage }));
            }
          }
          this.cc.logger.info(p.getName() + " has been paid " + moneyDue);
        }
      }
    }
    if ((cmd.getName().equalsIgnoreCase("ccProtectArea")) && ((sender instanceof Player)))
    {
      try
      {
        Player p = (Player)sender;
        

        WorldEditPlugin worldEditPlugin = (WorldEditPlugin)Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");
        Selection sel = worldEditPlugin.getSelection(p);
        if ((sel instanceof CuboidSelection))
        {
          BlockVector min = sel.getNativeMinimumPoint().toBlockVector();
          BlockVector max = sel.getNativeMaximumPoint().toBlockVector();
          
          Double totalCost = Double.valueOf(sel.getArea() * this.cc.pricePerBlock);
          this.cc.loadEconomy();
          if (((Double)this.c.personalMoney.get(sender.getName())).doubleValue() >= totalCost.doubleValue())
          {
            this.c.personalMoney.put(sender.getName(), Double.valueOf(((Double)this.c.personalMoney.get(sender.getName())).doubleValue() - totalCost.doubleValue()));
            if (this.cc.vaultEnabled.booleanValue())
            {
              EconomyResponse r = this.cc.econ.withdrawPlayer(p.getName(), totalCost.doubleValue());
              if (r.transactionSuccess()) {
                sender.sendMessage(String.format("You were given %s and now have %s", new Object[] { this.cc.econ.format(r.amount), this.cc.econ.format(r.balance) }));
              } else {
                sender.sendMessage(String.format("An error occured: %s", new Object[] { r.errorMessage }));
              }
            }
            sender.sendMessage(totalCost + " has been removed from your account! Protection Added!");
            
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
                  Location l = new Location(p.getWorld(), x, y, z);
                  SerializableLocation sl = new SerializableLocation(l);
                  if (this.cc.getCompany(p) != "none") {
                    this.c.protectedBlocks.put(sl, this.cc.getCompany(p));
                  }
                }
              }
            }
          }
        }
      }
      catch (Exception localException1) {}
      return true;
    }
    if (cmd.getName().equalsIgnoreCase("stock"))
    {
      this.c.industrialPercent = Double.valueOf(this.cc.getConfig().getDouble("RealTimeStocks.Industrial.Trend") / this.c.industrial.doubleValue());
      this.c.textilePercent = Double.valueOf(this.cc.getConfig().getDouble("RealTimeStocks.Textiles.Trend") / this.c.textiles.doubleValue());
      this.c.raw_materialPercent = Double.valueOf(this.cc.getConfig().getDouble("RealTimeStocks.Raw_Materials.Trend") / this.c.raw_materials.doubleValue());
      this.c.energyPercent = Double.valueOf(this.cc.getConfig().getDouble("RealTimeStocks.Energy.Trend") / this.c.energy.doubleValue());
      if (this.c.industrialPercent.doubleValue() >= 0.0D) {
        sender.sendMessage("Industrial: " + ChatColor.DARK_GREEN + this.c.industrialPercent.doubleValue() * 100.0D + "%");
      } else if (this.c.industrialPercent.doubleValue() < 0.0D) {
        sender.sendMessage("Industrial: " + ChatColor.DARK_RED + this.c.industrialPercent.doubleValue() * 100.0D + "%");
      }
      if (this.c.textilePercent.doubleValue() >= 0.0D) {
        sender.sendMessage("Textiles: " + ChatColor.DARK_GREEN + this.c.textilePercent.doubleValue() * 100.0D + "%");
      } else if (this.c.textilePercent.doubleValue() < 0.0D) {
        sender.sendMessage("Textiles: " + ChatColor.DARK_RED + this.c.textilePercent.doubleValue() * 100.0D + "%");
      }
      if (this.c.raw_materialPercent.doubleValue() >= 0.0D) {
        sender.sendMessage("Raw Materials: " + ChatColor.DARK_GREEN + this.c.raw_materialPercent.doubleValue() * 100.0D + "%");
      } else if (this.c.raw_materialPercent.doubleValue() < 0.0D) {
        sender.sendMessage("Raw Materials: " + ChatColor.DARK_RED + this.c.raw_materialPercent.doubleValue() * 100.0D + "%");
      }
      if (this.c.energyPercent.doubleValue() >= 0.0D) {
        sender.sendMessage("Energy: " + ChatColor.DARK_GREEN + this.c.energyPercent.doubleValue() * 100.0D + "%");
      } else if (this.c.energyPercent.doubleValue() < 0.0D) {
        sender.sendMessage("Energy: " + ChatColor.DARK_RED + this.c.energyPercent.doubleValue() * 100.0D + "%");
      }
      return true;
    }
    if (cmd.getName().equalsIgnoreCase("ccSetHome")) {
      if ((sender instanceof Player))
      {
        Player p = (Player)sender;
        if (this.c.position.get(p.getName()) != null)
        {
          if ((((String)this.c.position.get(p.getName())).equalsIgnoreCase("Owner")) && (!this.cc.getCompany(p).equalsIgnoreCase("none")))
          {
            String compName = this.cc.getCompany(p);
            this.c.companyHome.put(compName, new SerializableLocation(p.getLocation()));
            p.sendMessage(ChatColor.DARK_GREEN + "Company Home Successfuly Set!");
            return true;
          }
          p.sendMessage(ChatColor.DARK_RED + "You cannot set the home becuase you are not an owner!");
        }
      }
      else
      {
        this.cc.logger.info("Only players can use /ccSetHome!");
      }
    }
    if (cmd.getName().equalsIgnoreCase("ccHome")) {
      if ((sender instanceof Player))
      {
        Player p = (Player)sender;
        if (!this.cc.getCompany(p).equalsIgnoreCase("none"))
        {
          String compName = this.cc.getCompany(p);
          if (this.c.companyHome.get(compName) != null)
          {
            p.teleport(SerializableLocation.returnLocation((SerializableLocation)this.c.companyHome.get(compName)));
            for (int i = 0; i <= 5; i++)
            {
              p.getWorld().playEffect(p.getLocation(), Effect.SMOKE, 0);
              p.getWorld().playEffect(p.getLocation(), Effect.MOBSPAWNER_FLAMES, 0);
            }
          }
          else
          {
            p.sendMessage(ChatColor.DARK_RED + "There is no home set for your company! Tell your superiors to set one!");
          }
        }
        else
        {
          p.sendMessage(ChatColor.DARK_RED + "You are not in a company! Join one with /ccJoin!");
        }
      }
      else
      {
        this.cc.logger.info("Only players can use ccHome!");
      }
    }
    Block b;
    if ((cmd.getName().equalsIgnoreCase("ccSign")) && 
      ((sender instanceof Player)))
    {
      Player p = (Player)sender;
      b = p.getTargetBlock(null, 50);
      if ((b.getState() instanceof Sign))
      {
        Sign s = (Sign)b.getState();
        this.c.signs.add(new SerializableLocation(s.getLocation()));
        this.cc.updateSigns();
        p.sendMessage(ChatColor.DARK_GREEN + "Succesfully Added Real Time Stock Sign!");
      }
      else
      {
        p.sendMessage(ChatColor.DARK_RED + "Block is not a sign!");
      }
    }
    if ((cmd.getName().equalsIgnoreCase("ccMyStock")) && 
      ((sender instanceof Player))) {
      for (String w : this.cc.getCompanyList())
      {
        Double d = Double.valueOf(this.cc.getConfig().getDouble("StockHolders." + sender.getName() + "." + w));
        if ((d.doubleValue() != 0.0D) && (d != null)) {
          sender.sendMessage(w + ": " + d);
        }
      }
    }
    if ((cmd.getName().equalsIgnoreCase("ccCreateShop")) && 
      ((sender instanceof Player)))
    {
      Player p = (Player)sender;
      try
      {
        WorldEditPlugin worldEditPlugin = (WorldEditPlugin)Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");
        Selection sel = worldEditPlugin.getSelection(p);
        if (sel != null)
        {
          if (!this.cc.getCompany(p).equalsIgnoreCase("none"))
          {
            if (((String)this.c.position.get(p.getName())).equalsIgnoreCase("Owner"))
            {
              Double totalBlocks = Double.valueOf(sel.getArea());
              this.cc.loadEconomy();
              Double pMoney = (Double)this.c.personalMoney.get(p.getName());
              if (totalBlocks.doubleValue() * this.cc.pricePerBlock <= pMoney.doubleValue())
              {
                this.c.personalMoney.put(p.getName(), Double.valueOf(pMoney.doubleValue() - totalBlocks.doubleValue() * this.cc.pricePerBlock));
                if (this.cc.vaultEnabled.booleanValue())
                {
                  EconomyResponse r = this.cc.econ.depositPlayer(p.getName(), totalBlocks.doubleValue() * this.cc.pricePerBlock);
                  if (r.transactionSuccess()) {
                    sender.sendMessage(String.format("You were given %s and now have %s", new Object[] { this.cc.econ.format(r.amount), this.cc.econ.format(r.balance) }));
                  } else {
                    sender.sendMessage(String.format("An error occured: %s", new Object[] { r.errorMessage }));
                  }
                }
                this.cc.fillShopSelection(this.cc.getCompany(p), sel, p.getWorld());
                p.sendMessage(ChatColor.DARK_GREEN + this.cc.getCompany(p) + "'s shop has been created!");
              }
              else
              {
                p.sendMessage(ChatColor.DARK_RED + "You don't have enough money to create cc. shop!");
              }
            }
            else
            {
              p.sendMessage(ChatColor.DARK_RED + "Only owners can create shops!");
            }
          }
          else {
            p.sendMessage(ChatColor.DARK_RED + "You are not in a company!");
          }
        }
        else {
          p.sendMessage(ChatColor.DARK_RED + "No region selected!");
        }
      }
      catch (Exception e)
      {
        this.cc.logger.severe("WORLD EDIT PLUGIN NOT FOUND! PLEASE INSTALL WORLDEDIT FOR ADDED FUNCTIONALITY!");
        p.sendMessage(ChatColor.DARK_RED + "Request not processed, World Edit Not Found!");
      }
    }
    if (cmd.getName().equalsIgnoreCase("ccShopSign")) {
      if ((sender instanceof Player))
      {
        Player p = (Player)sender;
        b = p.getTargetBlock(null, 50);
        if ((b.getState() instanceof Sign))
        {
          Sign s = (Sign)b.getState();
          
          String line2 = s.getLine(1);
          

          Boolean materialCheck = Boolean.valueOf(true);
          if (s.getLocation().getBlock().getRelative(BlockFace.DOWN).getType() == Material.CHEST)
          {
            if (this.cc.getConfig().getDouble("Prices." + line2.toLowerCase().replace("_", "").replace(" ", "")) != 0.0D)
            {
              try
              {
                Material.getMaterial(line2.toUpperCase());
              }
              catch (Exception e)
              {
                materialCheck = Boolean.valueOf(false);
                p.sendMessage(ChatColor.DARK_RED + "Sign not registered! Item does not exist!");
              }
              try
              {
                Double.valueOf(s.getLine(2));
              }
              catch (Exception e)
              {
                materialCheck = Boolean.valueOf(false);
                p.sendMessage(ChatColor.DARK_RED + "Price parse fail! Make sure line 3 is your price (Example: 23.12)");
              }
              try
              {
                Integer.valueOf(s.getLine(3));
              }
              catch (Exception e)
              {
                materialCheck = Boolean.valueOf(false);
                p.sendMessage(ChatColor.DARK_RED + "Quantity parse fail! Make sure line 4 is your quantity (Example: 64)");
              }
              if (materialCheck.booleanValue())
              {
                s.setLine(0, this.cc.getCompany(p));
                s.update();
                this.c.shopSigns.add(s.getLocation());
                this.c.inventoryChest.add(s.getLocation().getBlock().getRelative(BlockFace.DOWN).getLocation());
                p.sendMessage(ChatColor.DARK_GREEN + "Sign Successfully Registered!");
              }
            }
            else
            {
              p.sendMessage(ChatColor.DARK_RED + "Sign not registered! Item does not exist! Try iron_sword instead of ironsword!");
            }
          }
          else {
            p.sendMessage(ChatColor.DARK_RED + "Block under the sign is not a chest!");
          }
        }
      }
      else
      {
        sender.sendMessage(ChatColor.DARK_RED + "You do not have an inventory chest set! Use /ccSetShopChest first!");
      }
    }
    if ((cmd.getName().equalsIgnoreCase("ccSetPosition")) && 
      ((sender instanceof Player)))
    {
      Player p = (Player)sender;
      if ((this.c.position.get(p.getName()) != "Owner") && (this.c.position.get(p.getName()) != "Executive") && (this.c.position.get(p.getName()) != "CFO"))
      {
        if (args.length == 2)
        {
          String playerName = args[0].toString();
          String newPosition = args[1].toString();
          if (!p.getName().equals(playerName))
          {
            if (!newPosition.equalsIgnoreCase("Owner"))
            {
              if ((newPosition.equalsIgnoreCase("Executive")) || (newPosition.equalsIgnoreCase("CFO")) || (newPosition.equalsIgnoreCase("Manager")) || (newPosition.equalsIgnoreCase("Employee")) || (newPosition.equalsIgnoreCase("Intern")))
              {
                if (this.cc.playerIsRegistered(playerName))
                {
                  this.c.position.put(p.getName(), newPosition);
                  p.sendMessage(ChatColor.DARK_GREEN + playerName + " has been prmoted to the position of: " + newPosition + "!");
                }
                else
                {
                  p.sendMessage(ChatColor.DARK_RED + playerName + " does not exist on cc. server!");
                }
              }
              else {
                p.sendMessage(ChatColor.DARK_RED + "Valid Positions: Executive, CFO, Manager, Employee, Intern");
              }
            }
            else {
              p.sendMessage(ChatColor.DARK_RED + "You cannot promote somebody to the position of Owner!");
            }
          }
          else {
            p.sendMessage(ChatColor.DARK_RED + "You cannot change your own position!");
          }
        }
        else
        {
          p.sendMessage(ChatColor.DARK_RED + "Command Usage: /ccSetPosition <playerName> <position>");
        }
      }
      else {
        p.sendMessage(ChatColor.DARK_RED + "You must be the owner, executive, or the CFO to change positions!");
      }
    }
    if (cmd.getName().equalsIgnoreCase("ccClaim"))
    {
      if ((sender instanceof Player))
      {
        Player p = (Player)sender;
        if ((this.cc.getCompany(p) != null) && (!this.cc.getCompany(p).equalsIgnoreCase("none")))
        {
          String pos = this.cc.getPlayerPosition(p);
          if ((pos.equalsIgnoreCase("Owner")) || (pos.equalsIgnoreCase("CFO")) || (pos.equalsIgnoreCase("Executive")))
          {
            Double compMoney = (Double)this.c.companyMoney.get(this.cc.getCompany(p));
            Double requiredMoney = Double.valueOf(this.cc.getConfig().getDouble("CompanyCraft.Chunk_Protection_Price"));
            if (requiredMoney == null) {
              requiredMoney = Double.valueOf(50.0D);
            }
            if (compMoney.doubleValue() >= requiredMoney.doubleValue())
            {
              this.c.companyMoney.put(this.cc.getCompany(p), Double.valueOf(compMoney.doubleValue() - requiredMoney.doubleValue()));
              p.sendMessage(ChatColor.DARK_GREEN + "You have bought 1 Chunk of Real Estate for $" + requiredMoney);
            }
            else
            {
              p.sendMessage(ChatColor.DARK_RED + "You do not have enough company money! Your current company balance is: " + compMoney + ". A chunk is priced at " + requiredMoney + ". You can add some of your personal money to the company total with /addToCompFunds <Amount>. The Owner will be informed of you giving money.");
            }
          }
          else
          {
            p.sendMessage(ChatColor.DARK_RED + "Only the Owner, CFO, or Executive can protect land!");
          }
        }
        else
        {
          p.sendMessage(ChatColor.DARK_RED + "You must be in a company to buy land!");
          this.c.playerCompany.put(p.getName(), "none");
        }
      }
    }
    else {
      return true;
    }
    return true;
  }
}

