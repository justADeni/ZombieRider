package me.prostedeni.goodcraft.zombierider;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.util.Vector;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.spigotmc.event.entity.EntityDismountEvent;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.HashMap;

public final class ZombieRider extends JavaPlugin implements Listener {

    public static int timer;
    ArrayList<String> ridingPlayers = new ArrayList<String>();
    HashMap<String, Integer> map = new HashMap<>();

    @EventHandler
    public void onCreeperOrSkeletonTarget(EntityTargetEvent b){
        if (b.getEntity() instanceof Creeper || b.getEntity() instanceof Skeleton) {
            if (map.containsValue(b.getEntity().getEntityId())) {
                b.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageByEntityEvent d){
        if (getConfig().getBoolean("NoRiderDamage")) {
            if (d.getEntity() instanceof Player) {
                if (ridingPlayers.contains(((Player) d.getEntity()).getPlayer().getName())) {
                    if (d.getDamager().getEntityId() == map.get(((Player) d.getEntity()).getPlayer().getName())) {
                        d.setCancelled(true);
                    }
                }
            }
        }
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        saveDefaultConfig();

        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        saveConfig();
    }

    @EventHandler
    public void onPlayerDismount(EntityDismountEvent e){
        String player = e.getEntity().getName();
        if (ridingPlayers.contains(player)) {
            ridingPlayers.remove(player);
            if (map.containsKey(player)) {
                map.remove(player);
            }
        }
    }

    @EventHandler
    public void onLeavePlayer(PlayerQuitEvent e){
        String player = e.getPlayer().getName();
        if (ridingPlayers.contains(player)) {
            ridingPlayers.remove(player);
            if (map.containsKey(player)) {
                map.remove(player);
            }
        }
    }

    @EventHandler
    public void onRightClick(PlayerInteractEntityEvent e) {
        String entity = e.getRightClicked().getName();
        if (e.getPlayer().hasPermission("zombierider.use")) {
            if ((entity.equalsIgnoreCase(String.valueOf(EntityType.ZOMBIE))) || (entity.equalsIgnoreCase(String.valueOf(EntityType.ENDERMAN))) || (entity.equalsIgnoreCase(String.valueOf(EntityType.SKELETON))) || (entity.equalsIgnoreCase(String.valueOf(EntityType.DROWNED))) || (entity.equalsIgnoreCase(String.valueOf(EntityType.BLAZE))) || (entity.equalsIgnoreCase(String.valueOf(EntityType.WITHER_SKELETON))) || (entity.equalsIgnoreCase(String.valueOf(EntityType.WITCH))) || (entity.equalsIgnoreCase(String.valueOf(EntityType.SPIDER))) || (entity.equalsIgnoreCase(String.valueOf(EntityType.CAVE_SPIDER))) || (entity.equalsIgnoreCase(String.valueOf(EntityType.CREEPER)))) {
                Player player = e.getPlayer();
                Entity ent = e.getRightClicked();
                if (ent.getPassenger() == null) {
                    ent.addPassenger(player);
                    ridingPlayers.add(e.getPlayer().getName());
                    map.put(e.getPlayer().getName(), ent.getEntityId());
                }
            }
        }
    }

    @EventHandler
    public void onClickPlayer(PlayerInteractEvent e) {
        if (ridingPlayers.contains(e.getPlayer().getName())) {
            if (!(e.getPlayer().isOnGround())) {
                for (Entity ent : e.getPlayer().getNearbyEntities(1, 3, 1)) {
                    if (ent.getEntityId() == map.get(e.getPlayer().getName())){
                        if (ent instanceof Zombie || ent instanceof Skeleton || ent instanceof Enderman || ent instanceof Creeper || ent instanceof Drowned || ent instanceof Blaze || ent instanceof Witch || ent instanceof Spider || ent instanceof CaveSpider || ent instanceof WitherSkeleton || ent instanceof ZombieVillager) {
                            AtomicInteger processId = new AtomicInteger();
                            timer = 24;
                            int taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
                                public void run() {
                                    Location loc1 = new Location(e.getPlayer().getWorld(), e.getPlayer().getLocation().getBlockX(), (e.getPlayer().getLocation().getBlockY() - 1), e.getPlayer().getLocation().getBlockZ());
                                    Location loc2 = new Location(e.getPlayer().getWorld(), e.getPlayer().getLocation().getBlockX(), (e.getPlayer().getLocation().getBlockY() - 2), e.getPlayer().getLocation().getBlockZ());
                                    Location loc3 = new Location(e.getPlayer().getWorld(), e.getPlayer().getLocation().getBlockX(), (e.getPlayer().getLocation().getBlockY() - 3), e.getPlayer().getLocation().getBlockZ());
                                    if (loc1.getBlock().getType() != Material.AIR || loc2.getBlock().getType() != Material.AIR || loc3.getBlock().getType() != Material.AIR) {
                                        Vector pos = ent.getLocation().toVector();
                                        Vector target = e.getPlayer().getTargetBlock(null, 50).getLocation().toVector();
                                        Vector velocity = target.subtract(pos);
                                        ent.setVelocity(velocity.normalize().multiply(0.40));
                                        ent.setRotation(e.getPlayer().getLocation().getYaw(), e.getPlayer().getLocation().getPitch());
                                    } else {
                                        e.setCancelled(true);
                                    }
                                    //prevents from flying

                                    timer--;
                                    if (timer <= 0) {
                                        Bukkit.getScheduler().cancelTask(processId.get());
                                    }
                                    //timer cancels movement
                                }
                            }, 0, 8);
                            processId.set(taskId);
                            ((Monster) ent).setAI(true);
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player){
            if (sender.hasPermission("zombierider.admin")){
                if (command.getName().equals("zombierider")){
                    if (args.length == 0){
                        sender.sendMessage(ChatColor.DARK_RED + "No arguments detected");
                    } else if (args.length == 1){
                        if (args[0].equalsIgnoreCase("reload")) {
                            reloadConfig();
                            getConfig();
                            saveConfig();
                            sender.sendMessage(ChatColor.DARK_GREEN + "Config reloaded");
                        } else if (!(args[0].equalsIgnoreCase("reload"))){
                            sender.sendMessage(ChatColor.DARK_RED + "Invalid arguments");
                        }
                    } else if (args.length > 1){
                        sender.sendMessage(ChatColor.DARK_RED + "Invalid arguments");
                    }
                }
            }
        } else {
            if (command.getName().equals("zombierider")){
                if (args.length == 0){
                    sender.sendMessage(ChatColor.DARK_RED + "No arguments detected");
                } else if (args.length == 1){
                    if (args[0].equalsIgnoreCase("reload")) {
                        reloadConfig();
                        getConfig();
                        saveConfig();
                        sender.sendMessage(ChatColor.DARK_GREEN + "Config reloaded");
                    } else if (!(args[0].equalsIgnoreCase("reload"))){
                        sender.sendMessage(ChatColor.DARK_RED + "Invalid arguments");
                    }
                } else if (args.length > 1){
                    sender.sendMessage(ChatColor.DARK_RED + "Invalid arguments");
                }
            }
        }
        return false;
    }

}
