package me.swipez.betterplayerleads;

import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.UUID;

public class LeadRunnable extends BukkitRunnable {

    public static HashMap<UUID, LeadData> data = new HashMap<>();

    @Override
    public void run() {
        for (LeadData leadData : data.values()){
            leadData.tick();
        }
    }
}
