package glabirynth.glabirynth.Commands;

import glabirynth.glabirynth.BossBar.BossBar;
import glabirynth.glabirynth.Files.LabyrinthConfig;
import glabirynth.glabirynth.Files.SoundsFile;
import glabirynth.glabirynth.GLabirynth;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public class CommandExecutor implements org.bukkit.command.CommandExecutor {


    private BossBar bar = new BossBar(GLabirynth.getPlugin(GLabirynth.class));

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender.isOp() && sender instanceof Player) {
            if (args.length == 0) {
                sender.sendMessage("/labyrinth start - начать лабиринт");
                return true;
            }

            if (args[0].equalsIgnoreCase("start")) {
                GLabirynth.getPlugin(GLabirynth.class).setStarted(true);

                File teams = new File("plugins/Games/teams/teams.yml");
                FileConfiguration teamsCfg = YamlConfiguration.loadConfiguration(teams);
                String[] teamName = new String[]{"red", "blue", "pink", "green", "yellow", "orange", "aqua", "purple"};
                int offset = 0;
                for(int i = 0; i < teamName.length; ++i) {
                    List<String> memlist = teamsCfg.getStringList("Teams." + teamName[i] + ".players");
                    int count = 0;
                    for (String s : memlist) {
                        if (Bukkit.getPlayer(UUID.fromString(s)) != null) {
                            Player player = Bukkit.getPlayer(UUID.fromString(s));
                            HashMap<String, Double> locationOfStartOfLabyrinth = (HashMap<String, Double>) LabyrinthConfig.getProperty("locationOfStartSpawnInLabyrinth");
                            player.getInventory().clear();
                            player.teleport(new Location(player.getWorld(), locationOfStartOfLabyrinth.get("x"),  locationOfStartOfLabyrinth.get("y"), (double) (1000 + offset)));
                            player.setGameMode(GameMode.ADVENTURE);
                            Location locationOfClock = new Location(Bukkit.getWorld("maze"), LabyrinthConfig.getLocation("clockSpawning").getX(), LabyrinthConfig.getLocation("clockSpawning").getY(), LabyrinthConfig.getLocation("clockSpawning").getZ() + offset);
                            createArmorStand(locationOfClock);
                            if (SoundsFile.getSoundProperty("soundOfAllGame") != null) {
                                Bukkit.dispatchCommand(GLabirynth.getPlugin(GLabirynth.class).getServer().getConsoleSender(), "playsound" + SoundsFile.getSoundProperty("soundOfAllGame"));
                            }

                            if (count == 0) {
                                ItemStack compass = new ItemStack(Material.COMPASS);
                                ItemMeta compassMeta = compass.getItemMeta();
                                compassMeta.setDisplayName("Выход");
                                compass.setItemMeta(compassMeta);
                                player.getInventory().addItem(compass);
                                Location locationOfCompass = new Location(Bukkit.getWorld("maze") , LabyrinthConfig.getLocation("locationOfExitFromLabyrinth").getX(), LabyrinthConfig.getLocation("locationOfExitFromLabyrinth").getY(), LabyrinthConfig.getLocation("locationOfExitFromLabyrinth").getBlockZ() + offset);
                                player.setCompassTarget(locationOfCompass);
                                count++;
                            }

                        }
                    }
                    offset += 1000;
                }
                initializeBar();
            }
        }
        return true;
    }

    private void initializeBar() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            bar.createBar();
            bar.addPlayer(p);
        }
    }

    private void createArmorStand(Location location) {
        ArmorStand stand = (ArmorStand) location.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);
        stand.setVisible(false); // Make it invisible
        stand.setGravity(false); // Make it not falling
        // Put the diamond on its head
        stand.getEquipment().setHelmet(new ItemStack(Material.CLOCK));
        new BukkitRunnable() {
            Location l = stand.getLocation();
            int i = -360;
            @Override
            public void run() {
                l.setPitch(i);
                i++;
            }
        }.runTaskTimer(GLabirynth.getPlugin(GLabirynth.class), 20*0L, 20*5L);
    }
}
