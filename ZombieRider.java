package me.prostedeni.goodcraft.zombierider;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.spigotmc.event.entity.EntityDismountEvent;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public final class ZombieRider extends JavaPlugin implements Listener {

    public static int timer;
    ArrayList<String> ridingPlayers = new ArrayList<String>();

    @Override
    public void onEnable() {
        // Plugin startup logic
        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    @EventHandler
    public void onPlayerDismount(EntityDismountEvent e){
        String player = e.getEntity().getName();
        if (ridingPlayers.contains(player)) {
            ridingPlayers.remove(player);
        }
    }
    
    @EventHandler
    public void onLeavePlayer(PlayerQuitEvent e){
        String player = e.getPlayer().getName();
        if (ridingPlayers.contains(player)) {
            ridingPlayers.remove(player);
        }
    }
    
    @EventHandler
    public void onRightClick(PlayerInteractEntityEvent e) {
        String entity = e.getRightClicked().getName();
        if ((entity.equalsIgnoreCase(String.valueOf(EntityType.ZOMBIE))) || (entity.equalsIgnoreCase(String.valueOf(EntityType.ENDERMAN))) || (entity.equalsIgnoreCase(String.valueOf(EntityType.SKELETON))) || (entity.equalsIgnoreCase(String.valueOf(EntityType.DROWNED))) || (entity.equalsIgnoreCase(String.valueOf(EntityType.BLAZE))) || (entity.equalsIgnoreCase(String.valueOf(EntityType.WITHER_SKELETON))) || (entity.equalsIgnoreCase(String.valueOf(EntityType.WITCH))) || (entity.equalsIgnoreCase(String.valueOf(EntityType.SPIDER))) || (entity.equalsIgnoreCase(String.valueOf(EntityType.CAVE_SPIDER))) || (entity.equalsIgnoreCase(String.valueOf(EntityType.CREEPER)))) {
            Player player = e.getPlayer();
            Entity ent = e.getRightClicked();
            ent.addPassenger(player);
            ridingPlayers.add(e.getPlayer().getName());
        }
    }

    @EventHandler
    public void onMovePlayer(PlayerInteractEvent e) {
        if (ridingPlayers.contains(e.getPlayer().getName())) {
            if (!(e.getPlayer().isOnGround())) {
                for (Entity ent : e.getPlayer().getNearbyEntities(1, 3, 1)) {
                    if (ent instanceof Zombie || ent instanceof Skeleton || ent instanceof Enderman || ent instanceof Creeper || ent instanceof Drowned || ent instanceof Blaze || ent instanceof Witch || ent instanceof Spider || ent instanceof CaveSpider || ent instanceof WitherSkeleton) {
                        AtomicInteger processId = new AtomicInteger();
                        timer = 5;
                        int taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
                            public void run() {
                                Location loc = new Location(e.getPlayer().getWorld(), e.getPlayer().getLocation().getBlockX(), (e.getPlayer().getLocation().getBlockY() - 3), e.getPlayer().getLocation().getBlockZ());
                                if (loc.getBlock().getType() != Material.AIR){
                                    Vector pos = ent.getLocation().toVector();
                                    Vector target = e.getPlayer().getTargetBlock(null, 50).getLocation().toVector();
                                    Vector velocity = target.subtract(pos);
                                    ent.setVelocity(velocity.normalize().multiply(0.30));
                                } else {
                                    e.setCancelled(true);
                                }
                                //prevents from flying

                                timer--;
                                if (timer <= 0) {
                                    Bukkit.getScheduler().cancelTask(processId.get());
                                }
                                //5 second timer
                            }
                        }, 0, 20);
                        processId.set(taskId);
                    }
                }
            }
    }
}
}
