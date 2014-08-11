package io.github.jisaacs1207.CompanyCraft;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class ClaimChunkListener
  implements Listener
{
  ConfigWriter c;
  CompanyCraft cc;
  int valuePerChunk = 100;
  
  public ClaimChunkListener(ConfigWriter c, CompanyCraft cc)
  {
    this.c = c;
    this.cc = cc;
  }
  
  @EventHandler(priority=EventPriority.LOW, ignoreCancelled=true)
  public void onBlockBreak(BlockBreakEvent e)
  {
    String company = (String)this.c.companyChunks.get(e.getBlock().getChunk().toString());
    if (company != null) {
      if (!company.equalsIgnoreCase("none"))
      {
        Player p = e.getPlayer();
        String playerComp = this.cc.getCompany(p);
        if (!company.equalsIgnoreCase(playerComp))
        {
          p.sendMessage(ChatColor.DARK_RED + "This is " + company + "'s land! No trespassing!");
          e.setCancelled(true);
        }
      }
      else
      {
        e.getPlayer().sendMessage(ChatColor.DARK_RED + "This is " + company + "'s land! No trespassing!");
        e.setCancelled(true);
      }
    }
  }
  
  public void claimCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
  {
    if ((sender instanceof Player))
    {
      Player p = (Player)sender;
      String company = this.cc.getCompany(p);
      if ((company != null) && (!company.equalsIgnoreCase("none"))) {
        this.cc.getPlayerPosition(p).equalsIgnoreCase("Owner");
      }
    }
  }
}
