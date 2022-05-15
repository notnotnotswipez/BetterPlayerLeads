package me.swipez.betterplayerleads;

import org.bukkit.plugin.java.JavaPlugin;

public final class BetterPlayerLeads extends JavaPlugin {

    private static BetterPlayerLeads plugin;
    public static CollisionTeam team;

    @Override
    public void onEnable() {
        plugin = this;
        team = new CollisionTeam();
        new LeadRunnable().runTaskTimer(this, 1, 1);
        getServer().getPluginManager().registerEvents(new LeadInteractionListener(), this);
        getCommand("ropelength").setExecutor(new RopeLengthCommand());
    }

    @Override
    public void onDisable() {
        for (LeadData leadData : LeadRunnable.data.values()){
            leadData.removeLead(true);
        }
    }

    public static BetterPlayerLeads getPlugin() {
        return plugin;
    }

    public static CollisionTeam getTeam() {
        return team;
    }
}
