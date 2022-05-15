package me.swipez.betterplayerleads;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class LeadData {

    static ScoreboardManager scoreboardManager = Bukkit.getScoreboardManager();
    static Scoreboard scoreboard = scoreboardManager.getNewScoreboard();

    private final UUID dataMain;
    private UUID owner = null;
    private List<UUID> victims = new ArrayList<>();
    private UUID leadRepresentation = null;
    private UUID hitchEntity = null;
    private Location postLocation = null;

    private boolean leaded = false;
    private boolean posted = false;
    private boolean bouncingBack = false;

    private int ropeLength = 5;

    public LeadData(Player player) {
        this.dataMain = player.getUniqueId();
        player.setScoreboard(BetterPlayerLeads.team.getBoard());
    }

    public int getRopeLength() {
        return ropeLength;
    }

    public void setRopeLength(int ropeLength) {
        this.ropeLength = ropeLength;
    }

    public void tick(){
        if (isVictim()){
            if (isLeaded()){
                if (getRepresentation() != null && getOwner() != null){
                    if (!getDataMain().getWorld().getUID().equals(getOwner().getWorld().getUID())){
                        removeLead(true);
                        return;
                    }
                    double distance = 0.5;
                    getRepresentation().teleport(getDataMain().getEyeLocation().subtract(0,0.5,0).subtract(getDataMain().getEyeLocation().getDirection().normalize().subtract(getDataMain().getEyeLocation().getDirection().normalize().multiply(distance))));
                    if (isPosted()){
                        if (getHitchEntity() == null){
                            removeLead(true);
                            return;
                        }
                        getRepresentation().setLeashHolder(getHitchEntity());
                    }
                    else {
                        getRepresentation().setLeashHolder(getOwner());
                    }

                    double pullRange = ropeLength;
                    double power = 0.5;

                    Location desiredLocation;
                    if (isPosted()){
                        desiredLocation = getHitchEntity().getLocation();
                    }
                    else {
                        desiredLocation = getOwner().getEyeLocation();
                    }


                    if (getDataMain().getLocation().distance(desiredLocation) >= pullRange){
                        if (!bouncingBack){
                            bouncingBack = true;
                            Vector velocity = desiredLocation.toVector().subtract(getDataMain().getLocation().toVector()).normalize().multiply(power);
                            getDataMain().setVelocity(getDataMain().getVelocity().add(velocity));
                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    bouncingBack = false;
                                }
                            }.runTaskLater(BetterPlayerLeads.getPlugin(), 1);
                        }

                    }
                }
                else {
                    removeLead(true);
                }
            }
        }
    }

    public void setPosted(Location location){
        postLocation = location;
        posted = true;
        makeHitchAttempt();
    }

    public void setLeaded(Player owner){
        leaded = true;
        setOwner(owner);
        makeRepresentationAttempt();
    }

    public void removePost(boolean dropItem){
        if (isVictim()){
            if (dropItem){
                getDataMain().getWorld().dropItemNaturally(postLocation.getBlock().getLocation(), new ItemStack(Material.LEAD));
            }
            posted = false;
            if (getHitchEntity() != null){
                getHitchEntity().remove();
            }
        }
    }

    public void removeLead(boolean dropItem) {
        if (isVictim()){
            if (dropItem){
                getDataMain().getWorld().dropItemNaturally(getOwner().getLocation(), new ItemStack(Material.LEAD));
            }
            leaded = false;
            posted = false;
            if (getRepresentation() != null){
                getRepresentation().setLeashHolder(null);
                getRepresentation().remove();
            }
            if (getHitchEntity() != null){
                getHitchEntity().remove();
            }
            LeadRunnable.data.get(owner).removeVictim(getDataMain());
            owner = null;
        }
    }

    private void makeHitchAttempt(){
        if (getHitchEntity() != null){
            return;
        }
        LeashHitch leashHitch = (LeashHitch) getDataMain().getWorld().spawnEntity(postLocation.getBlock().getLocation().add(0.5, 0.5, 0.5), EntityType.LEASH_HITCH);
        hitchEntity = leashHitch.getUniqueId();
    }

    private void makeRepresentationAttempt(){
        if (getRepresentation() == null){
            Slime slime = getDataMain().getWorld().spawn(getDataMain().getEyeLocation(), Slime.class, (entity) -> {
                entity.setSize(1);
                entity.setAI(false);
                entity.setSilent(true);
                entity.setInvisible(true);
                entity.setInvulnerable(true);
                entity.setVisualFire(false);
                entity.setCollidable(false);
                entity.getCollidableExemptions().clear();
            });
            leadRepresentation = slime.getUniqueId();
            BetterPlayerLeads.getTeam().getTeam().addEntry(leadRepresentation.toString());
        }
    }

    public Slime getRepresentation(){
        if (leadRepresentation == null){
            return null;
        }
        return (Slime) Bukkit.getEntity(leadRepresentation);
    }

    public LeashHitch getHitchEntity(){
        if (hitchEntity == null){
            return null;
        }
        return (LeashHitch) Bukkit.getEntity(hitchEntity);
    }

    public boolean isVictim(){
        return owner != null;
    }

    public Player getDataMain() {
        if (dataMain == null){
            return null;
        }
        return Bukkit.getPlayer(dataMain);
    }

    public Player getOwner() {
        if (owner == null){
            return null;
        }
        return Bukkit.getPlayer(owner);
    }

    public void removeVictim(Player player){
        victims.remove(player.getUniqueId());
    }

    public List<UUID> getVictims() {
        return victims;
    }

    public boolean isLeaded() {
        return leaded;
    }

    public boolean isPosted() {
        return posted;
    }

    public void addVictim(Player victim){
        if (victim == null){
            return;
        }
        victims.add(victim.getUniqueId());
    }

    public void setOwner(Player owner) {
        if (owner == null){
            this.owner = null;
            return;
        }
        this.owner = owner.getUniqueId();
        LeadData data;
        if (!LeadRunnable.data.containsKey(owner.getUniqueId())){
            data = new LeadData(owner);
            LeadRunnable.data.put(owner.getUniqueId(), data);
        }
        else {
            data = LeadRunnable.data.get(owner.getUniqueId());
        }
        data.addVictim(getDataMain());
    }
}
