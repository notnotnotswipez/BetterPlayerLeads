package me.swipez.betterplayerleads;

import org.bukkit.ChatColor;
import org.bukkit.Difficulty;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LeashHitch;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerUnleashEntityEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class LeadInteractionListener implements Listener {

    List<UUID> interactionTickDelay = new ArrayList<>();

    @EventHandler
    public void onRepDamage(EntityDamageEvent event){
        if (event.getEntity().getType().equals(EntityType.SLIME)){
            if (event.getEntity().isInvulnerable()){
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event){
        Player player = event.getEntity();
        if (LeadRunnable.data.containsKey(player.getUniqueId())){
            LeadData data = LeadRunnable.data.get(player.getUniqueId());
            data.getVictims().clear();
            data.removeLead(true);
        }
    }

    @EventHandler
    public void onLeadDamage(HangingBreakByEntityEvent event){
        if (event.getEntity() instanceof LeashHitch hitch){
            if (event.getRemover() instanceof Player player){
                if (LeadRunnable.data.containsKey(player.getUniqueId())){
                    LeadData data = LeadRunnable.data.get(player.getUniqueId());
                    if (data.isPosted()){
                        if (data.getHitchEntity().getUniqueId().equals(hitch.getUniqueId())){
                            event.setCancelled(true);
                        }
                    }
                    else {
                        if (!data.getVictims().isEmpty()){
                            for (UUID uuid : data.getVictims()){
                                LeadData victimData = LeadRunnable.data.get(uuid);
                                if (victimData.getHitchEntity().getUniqueId().equals(hitch.getUniqueId())){
                                    event.setCancelled(true);
                                    victimData.removePost(false);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event){
        Player player = event.getPlayer();
        if (LeadRunnable.data.containsKey(player.getUniqueId())){
            LeadData data = LeadRunnable.data.get(player.getUniqueId());
            if (data.isPosted()){
                if (data.getHitchEntity().getLocation().getBlock().equals(event.getBlock())){
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void playerUnleashEvent(PlayerUnleashEntityEvent event){
        Player player = event.getPlayer();
        if (LeadRunnable.data.containsKey(player.getUniqueId())) {
            LeadData ownerData = LeadRunnable.data.get(player.getUniqueId());
            if (ownerData.isPosted()) {
                if (ownerData.getRepresentation().getUniqueId().equals(event.getEntity().getUniqueId())) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onLeadInteracted(PlayerInteractAtEntityEvent event) {
        if (event.getRightClicked() instanceof LeashHitch hitch){
            Player player = event.getPlayer();
            if (LeadRunnable.data.containsKey(player.getUniqueId())){
                LeadData ownerData = LeadRunnable.data.get(player.getUniqueId());
                if (!ownerData.getVictims().isEmpty()){
                    for (UUID uuid : ownerData.getVictims()){
                        LeadData victimData = LeadRunnable.data.get(uuid);
                        if (victimData.isPosted()){
                            if (victimData.getHitchEntity().getUniqueId().equals(hitch.getUniqueId())){
                                event.setCancelled(true);
                            }
                        }
                    }
                }
            }
        }
        if (interactionTickDelay.contains(event.getPlayer().getUniqueId())){
            return;
        }
        interactionTickDelay.add(event.getPlayer().getUniqueId());
        new BukkitRunnable() {
            @Override
            public void run() {
                interactionTickDelay.remove(event.getPlayer().getUniqueId());
            }
        }.runTaskLater(BetterPlayerLeads.getPlugin(), 10);
        if (event.getRightClicked() instanceof LeashHitch hitch){
            Player player = event.getPlayer();
            if (LeadRunnable.data.containsKey(player.getUniqueId())){
                LeadData ownerData = LeadRunnable.data.get(player.getUniqueId());
                if (!ownerData.getVictims().isEmpty()){
                    for (UUID uuid : ownerData.getVictims()){
                        LeadData victimData = LeadRunnable.data.get(uuid);
                        if (victimData.isPosted()){
                            if (victimData.getHitchEntity().getUniqueId().equals(hitch.getUniqueId())){
                                hitch.remove();
                                victimData.removePost(false);
                            }
                        }
                    }
                }
            }
        }
        if (event.getRightClicked() instanceof Player clicked) {
            if (!clicked.hasPermission("betterleads.lead")){
                return;
            }
            if (clicked.getWorld().getDifficulty().equals(Difficulty.PEACEFUL)){
                if (event.getPlayer().getInventory().getItemInMainHand().getType().equals(Material.LEAD)){
                    event.getPlayer().sendMessage(ChatColor.RED+"This world is in peaceful! You cannot lead players.");
                }
                return;
            }
            LeadData data = null;
            if (LeadRunnable.data.containsKey(clicked.getUniqueId())){
                data = LeadRunnable.data.get(clicked.getUniqueId());
            }
            else {
                data = new LeadData(clicked);
                LeadRunnable.data.put(clicked.getUniqueId(), data);
            }

            if (!data.isLeaded()){
                if (!event.getPlayer().getInventory().getItemInMainHand().getType().equals(Material.LEAD)){
                    return;
                }
                data.setLeaded(event.getPlayer());
                int amount = event.getPlayer().getInventory().getItemInMainHand().getAmount();
                event.getPlayer().getInventory().getItemInMainHand().setAmount(amount-1);
            }
            else {
                if (data.getOwner().getUniqueId().equals(event.getPlayer().getUniqueId())){
                    if (!data.isPosted()){
                        data.removeLead(true);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onLeadPosted(PlayerInteractEvent event){
        if (!event.hasBlock()){
            return;
        }
        Player player = event.getPlayer();
        if (LeadRunnable.data.containsKey(player.getUniqueId())){
            LeadData ownerData = LeadRunnable.data.get(player.getUniqueId());
            if (!ownerData.getVictims().isEmpty()){
                for (UUID uuid : ownerData.getVictims()){
                    LeadData victimData = LeadRunnable.data.get(uuid);
                    if (!victimData.isPosted()){
                        if (event.getClickedBlock().getType().toString().toLowerCase().contains("fence")){
                            event.setCancelled(true);
                        }
                    }
                }
            }
        }
        if (interactionTickDelay.contains(event.getPlayer().getUniqueId())){
            return;
        }
        if (LeadRunnable.data.containsKey(player.getUniqueId())){
            LeadData ownerData = LeadRunnable.data.get(player.getUniqueId());
            if (!ownerData.getVictims().isEmpty()){
                for (UUID uuid : ownerData.getVictims()){
                    LeadData victimData = LeadRunnable.data.get(uuid);
                    if (!victimData.isPosted()){
                        if (event.getClickedBlock().getType().toString().toLowerCase().contains("fence")){
                            victimData.setPosted(event.getClickedBlock().getLocation());
                        }
                    }
                }
                interactionTickDelay.add(event.getPlayer().getUniqueId());
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        interactionTickDelay.remove(event.getPlayer().getUniqueId());
                    }
                }.runTaskLater(BetterPlayerLeads.getPlugin(), 10);
            }
        }
    }
}
