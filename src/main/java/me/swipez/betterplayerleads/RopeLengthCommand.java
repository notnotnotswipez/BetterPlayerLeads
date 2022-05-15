package me.swipez.betterplayerleads;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RopeLengthCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player player){
            if (args.length != 2){
                player.sendMessage(ChatColor.YELLOW+"Invalid Usage: /ropelength <player> <length>!");
                return true;
            }
            Player selectedPlayer = Bukkit.getPlayer(args[0]);
            int selectedLength = Integer.parseInt(args[1]);

            if (!LeadRunnable.data.containsKey(player.getUniqueId())){
                player.sendMessage(ChatColor.YELLOW+"No players leashed!");
                return true;
            }
            LeadData data = LeadRunnable.data.get(player.getUniqueId());

            if (data.getVictims().isEmpty()){
                player.sendMessage(ChatColor.YELLOW+"No players leashed!");
                return true;
            }

            if (selectedPlayer == null){
                player.sendMessage(ChatColor.YELLOW+"That is not a valid player!");
                return true;
            }

            if (!LeadRunnable.data.containsKey(selectedPlayer.getUniqueId())){
                player.sendMessage(ChatColor.YELLOW+"That player is not under your leash!");
                return true;
            }

            LeadData victimData = LeadRunnable.data.get(selectedPlayer.getUniqueId());

            if (!victimData.isLeaded() || !victimData.getOwner().getUniqueId().equals(player.getUniqueId())){
                player.sendMessage(ChatColor.YELLOW+"That player is not under your leash!");
                return true;
            }

            int prevLength = victimData.getRopeLength();
            if (selectedLength > prevLength){
                selectedPlayer.sendMessage(ChatColor.YELLOW+"Your rope got looser!");
            }
            else if (selectedLength < prevLength){
                selectedPlayer.sendMessage(ChatColor.YELLOW+"Your rope got tighter!");
            }
            victimData.setRopeLength(selectedLength);
            player.sendMessage(ChatColor.YELLOW+"Changed rope length for "+ChatColor.GREEN+selectedPlayer.getName()+ChatColor.YELLOW+" to "+ChatColor.BLUE+selectedLength+ChatColor.YELLOW+"!");

        }
        return true;
    }
}
