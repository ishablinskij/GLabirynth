package glabirynth.glabirynth;

import glabirynth.glabirynth.BossBar.BossBar;
import glabirynth.glabirynth.Commands.CommandExecutor;
import glabirynth.glabirynth.Files.JewelryFile;
import glabirynth.glabirynth.Files.LabyrinthConfig;
import glabirynth.glabirynth.Files.SoundsFile;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;


public final class GLabirynth extends JavaPlugin implements Listener {

    private BossBar bar = new BossBar(this);
    private  HashMap<Location, Boolean> visited = new HashMap();

    private List<Player> escapedPlayers = new ArrayList<>();

    private HashMap<UUID, Location> freeze = new HashMap<>();
    private File directory = Path.of("plugins/labyrinth").toFile();


    private boolean isStarted = false;


    @Override
    public void onEnable() {
        this.getServer().getPluginManager().registerEvents(this, this);
        createFilesOfLabyrinth();
        LabyrinthConfig.createConfigFile();
        LabyrinthConfig.fillConfig();
        JewelryFile.createJewelryFile();
        JewelryFile.fillJewelryFile();
        SoundsFile.createSoundsFile();
        SoundsFile.fillSoundsFile();
        this.getCommand("labyrinth").setExecutor(new CommandExecutor());
    }

    @Override
    public void onDisable() {

    }
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (isStarted) {
            if (event.getPlayer().getWorld() == Bukkit.getWorld("maze")) {
                if (event.getPlayer().getInventory().getItemInMainHand().getType().equals(Material.CLOCK)) {
                    Player player = event.getPlayer();
                    ItemStack clock = player.getInventory().getItemInMainHand();
                    if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR) {
                        int amount = clock.getAmount();
                        amount--;
                        clock.setAmount(amount);
                        bar.addTime();
                        Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.YELLOW + "" + player.getPlayer() + " активировал дополнительное время! +30сек");
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 100.0F, 1.0F);
                    }
                }
                if (event.getClickedBlock() != null && event.getClickedBlock().getBlockData().getMaterial().equals(Material.BIRCH_BUTTON ) && event.getAction() == Action.RIGHT_CLICK_BLOCK && !event.getPlayer().getGameMode().equals(GameMode.SPECTATOR)) {
                    Player player = event.getPlayer();
                    Inventory inv = player.getInventory();
                    String[] jewelry = JewelryFile.getArrayOfJewelry();
                    int amount = 0;
                    for (int i = 0; i < inv.getSize(); i++) {
                        for (String s : jewelry) {
                            Material material = Material.getMaterial(s);
                            ItemStack item = inv.getItem(i);
                            if (item != null) {
                                if (item.getType() == material) {
                                    amount += item.getAmount() * JewelryFile.getValueOfJewel(s);
                                    item.setAmount(0);
                                }
                            }
                        }
                    }
                    player.sendMessage(ChatColor.WHITE + "[" + ChatColor.YELLOW + "+" + amount + ChatColor.WHITE + "] Собранные монеты");
                    Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "games coins give " + player.getName() + " " + amount);
                    player.teleport(LabyrinthConfig.getLocation( "locationOfStartSpawn"));
                    Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.YELLOW + "" + player.getName() + " сбежал из лабиринта!");
                    player.sendTitle(ChatColor.GREEN + "" + ChatColor.BOLD + "ВЫ СБЕЖАЛИ", "");
                    player.playSound(event.getPlayer().getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 100.0F, 1.0F);
                    escapedPlayers.add(event.getPlayer());
                }
            }
        }
    }

    private void bfs(Location loc) {
        if (!this.visited.containsKey(loc) && (loc.getBlock().getType() == Material.OAK_PLANKS || loc.getBlock().getType() == Material.OAK_STAIRS)) {
            this.visited.put(loc, true);
            this.bfs(new Location(loc.getWorld(), (loc.getBlockX() + 1), loc.getBlockY(), loc.getBlockZ()));
            this.bfs(new Location(loc.getWorld(), loc.getBlockX(), (loc.getBlockY() + 1), loc.getBlockZ()));
            this.bfs(new Location(loc.getWorld(), loc.getBlockX(), loc.getBlockY(), (loc.getBlockZ() + 1)));
            this.bfs(new Location(loc.getWorld(), (loc.getBlockX() - 1), loc.getBlockY(), loc.getBlockZ()));
            this.bfs(new Location(loc.getWorld(), loc.getBlockX(), (loc.getBlockY() - 1), loc.getBlockZ()));
            this.bfs(new Location(loc.getWorld(), loc.getBlockX(), loc.getBlockY(), (loc.getBlockZ() - 1)));
            loc.getBlock().setType(Material.AIR);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        if (isStarted) {
            if (e.getBlock().getLocation().getWorld() == Bukkit.getWorld("maze") && (e.getBlock().getType().equals(Material.OAK_PLANKS) || e.getBlock().getType().equals(Material.OAK_STAIRS))) {
                this.visited.clear();
                Location loc = e.getBlock().getLocation();
                this.visited.put(loc, true);
                this.bfs(new Location(loc.getWorld(), (loc.getBlockX() + 1), loc.getBlockY(), loc.getBlockZ()));
                this.bfs(new Location(loc.getWorld(), loc.getBlockX(), (double) (loc.getBlockY() + 1), loc.getBlockZ()));
                this.bfs(new Location(loc.getWorld(), loc.getBlockX(), loc.getBlockY(), (loc.getBlockZ() + 1)));
                this.bfs(new Location(loc.getWorld(), (loc.getBlockX() - 1), loc.getBlockY(), loc.getBlockZ()));
                this.bfs(new Location(loc.getWorld(), loc.getBlockX(), (loc.getBlockY() - 1), loc.getBlockZ()));
                this.bfs(new Location(loc.getWorld(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ() - 1));
            }
        }
    }

    public List<Player> getEscapedPlayers() {
        return escapedPlayers;
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        if (isStarted) {
            Player player = event.getPlayer();
            if (player.getWorld().equals(Bukkit.getWorld("maze"))) {
                event.setCancelled(true);
                freeze(player);
                player.teleport(freeze.get(player.getUniqueId()));
                for(int i = 0; i <= 60; ++i) {
                    int finalI = i;
                    Bukkit.getScheduler().scheduleSyncDelayedTask(this, () -> {
                        if (freeze.containsKey(player.getUniqueId())) {
                            if (finalI != 60) {
                                player.sendTitle(ChatColor.RED + "" + (60 - finalI), "Время до смерти");
                            } else {
                                player.getInventory().clear();
                                unfreeze(player);
                                player.teleport(LabyrinthConfig.getLocation("locationOfPlaceForLooser"));
                                player.sendTitle(ChatColor.RED + "" + ChatColor.BOLD + "ПОТРАЧЕНО", "");
                                player.playSound(player.getLocation(), Sound.ENTITY_ELDER_GUARDIAN_DEATH, 100.0F, 1.45F);
                            }
                        }

                    }, (i * 20));
                }
            }
        }
    }

    @EventHandler
    public void onInteractWithDeathPlayer(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        if (isStarted) {
            if (player.getWorld().equals(Bukkit.getWorld("maze"))) {
                if (event.getRightClicked().getType().equals(EntityType.PLAYER)) {
                    if (freeze.containsKey(event.getRightClicked().getUniqueId())) {
                        if (event.getPlayer().getItemInHand().getType().equals(Material.GOLDEN_APPLE)) {
                            int cnt = player.getItemInHand().getAmount();
                            --cnt;
                            if (cnt == 0) {
                                player.getInventory().setItemInMainHand(new ItemStack(Material.AIR, 1));
                            } else {
                                player.getInventory().setItemInMainHand(new ItemStack(Material.GOLDEN_APPLE, cnt));
                            }
                            unfreeze((Player) event.getRightClicked());
                        }
                    }
                }
            }
        }
    }
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (isStarted) {
            Player player = event.getPlayer();
            UUID uuid = event.getPlayer().getUniqueId();
            if (freeze.containsKey(uuid)) {
                Location startLocation = freeze.get(uuid);
                if (player.getLocation() != freeze.get(uuid)) {
                    player.teleport(startLocation);
                    player.spawnParticle(Particle.ENCHANTMENT_TABLE, player.getLocation(), 2);
                    player.playSound(player.getLocation(), Sound.BLOCK_BAMBOO_HIT, 100.0F, 1.0F);
                    player.sendMessage("Ну ка встал на место! Низя двигаться, представь, что ты не существуешь и жди своего тимейта.");
                }
            }
        }
    }


    private void createFilesOfLabyrinth() {
        try {
            if (!directory.exists()) directory = Files.createDirectory(Path.of("plugins/labyrinth")).toFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void freeze(Player p){
        Location startLocation = p.getLocation();
        p.setAllowFlight(true);
        p.teleport(startLocation);
        p.setFlying(true);
        p.setFlySpeed(0);
        freeze.put(p.getUniqueId(), startLocation);
    }

    private void unfreeze(Player p){
        p.setAllowFlight(false);
        p.setFlying(false);
        p.setFlySpeed(0.1F);
        freeze.remove(p.getUniqueId());
    }

    public void setStarted(boolean started) {
        isStarted = started;
    }
}
