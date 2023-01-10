package glabirynth.glabirynth.BossBar;


import glabirynth.glabirynth.Events.BossBarProgressEvent;
import glabirynth.glabirynth.GLabirynth;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class BossBar {

    private int taskId = -1;
    private final GLabirynth plugin;
    private org.bukkit.boss.BossBar bar;

    private double progress = 1.0;

    /*
       Start time in ticks (20 ticks = 1 second)
     */
    private int time = 600;

    public BossBar(GLabirynth plugin) {
        this.plugin = plugin;
    }

    public void addPlayer(Player player) {
        bar.addPlayer(player);
    }

    public int getTime() {
        return time;
    }

    public void cast() {
        taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
                bar.setProgress(progress);
                for (int i = 0; i < time; i++) {
                    int finalI = i;
                    Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                        if (finalI <= time) {
                            bar.setTitle("§6Оставшееся время: " + timeToString(time - finalI));
                        } else {
                            bar.setVisible(false);
                            BossBarProgressEvent event = new BossBarProgressEvent(bar.getPlayers());
                            Bukkit.getServer().getPluginManager().callEvent(event);
                        }
                    }, i * 20L);
            }
        }, 20, (getTime() * 20L));
    }


    public void createBar() {
        bar = Bukkit.createBossBar(format("§6Оставшееся время: " + timeToString(time)), BarColor.RED, BarStyle.SOLID);
        bar.setVisible(true);
        cast();

    }

    private String format(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public void setTime(int time) {
        this.time = time;
    }

    public int getTaskId() {
        return taskId;
    }

    public org.bukkit.boss.BossBar getBar() {
        return bar;
    }

    public void addTime() {
        setTime(getTime() + 30);
    }

    private String timeToString(int time) {
        int minutes = time / 60;
        int seconds = time % 60;
        return " " + minutes + "мин" + ":" + seconds + "сек";
    }
}
