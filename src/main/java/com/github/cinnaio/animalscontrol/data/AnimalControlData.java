package com.github.cinnaio.animalscontrol.data;

import com.github.cinnaio.animalscontrol.AnimalsControl;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.List;

public class AnimalControlData {
    private final AnimalsControl plugin;
    private FileConfiguration config;

    // 时间相关配置
    private long starvationTime;
    private long shareTime;
    private double checkRadius;

    // 繁殖相关配置
    private double breedChance;
    private int breedDuration;
    private boolean wildAnimalInstantBreed;
    private List<String> breedingAnimals;
    private int breedingWheatRequired;

    private boolean debugEnabled;
    private boolean showDeathEvent;
    private double deathEventRadius;

    private boolean showRemainingTime;
    private String timeFormat;
    private String starvedFormat;

    private int updateInterval;
    private boolean onlyNearPlayers;
    private double updateRange;

    private int starvationCheckInterval;
    private boolean onlyCheckNearPlayers;
    private double starvationCheckRange;

    public AnimalControlData(AnimalsControl plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        plugin.reloadConfig();
        config = plugin.getConfig();
        loadData();
    }

    private void loadData() {
        // 加载时间相关数据
        starvationTime = config.getLong("time_settings.starvation_time", 604800L);
        shareTime = config.getLong("time_settings.share_time", 86400L);
        checkRadius = config.getDouble("time_settings.check_radius", 5.0);

        // 加载繁殖相关数据
        breedChance = config.getDouble("breeding_settings.breed_chance", 0.1);
        breedDuration = config.getInt("breeding_settings.breed_duration", 600);
        wildAnimalInstantBreed = config.getBoolean("breeding_settings.wild_animal_instant_breed", true);
        breedingAnimals = config.getStringList("breeding_animals");
        breedingWheatRequired = config.getInt("breeding_wheat_required", 2);

        // 加载调试设置
        debugEnabled = config.getBoolean("debug.enabled", false);
        showDeathEvent = config.getBoolean("debug.show_death_event", true);
        deathEventRadius = config.getDouble("debug.death_event_radius", 32.0);

        // 加载显示设置
        showRemainingTime = config.getBoolean("display.show_remaining_time", true);
        timeFormat = translateColors(config.getString("display.time_format", "&7剩余: {time}"));
        starvedFormat = translateColors(config.getString("display.starved_format", "&c已饥饿"));

        // 加载更新设置
        updateInterval = config.getInt("display.update.interval", 100);
        onlyNearPlayers = config.getBoolean("display.update.only_near_players", true);
        updateRange = config.getDouble("display.update.range", 48.0);

        // 加载饥饿检查设置
        starvationCheckInterval = config.getInt("starvation_check.interval", 1200);
        onlyCheckNearPlayers = config.getBoolean("starvation_check.only_near_players", true);
        starvationCheckRange = config.getDouble("starvation_check.range", 64.0);
    }

    // 将 & 转换为 §
    private String translateColors(String text) {
        return text.replace('&', '§');
    }

    // Getter 方法
    public long getStarvationTime() {
        return starvationTime;
    }

    public long getShareTime() {
        return shareTime;
    }

    public double getCheckRadius() {
        return checkRadius;
    }

    public double getBreedChance() {
        return breedChance;
    }

    public int getBreedDuration() {
        return breedDuration;
    }

    public boolean isWildAnimalInstantBreed() {
        return wildAnimalInstantBreed;
    }

    public List<String> getBreedingAnimals() {
        return breedingAnimals;
    }

    public int getBreedingWheatRequired() {
        return breedingWheatRequired;
    }

    public boolean isDebugEnabled() {
        return debugEnabled;
    }

    public boolean isShowDeathEvent() {
        return debugEnabled && showDeathEvent;
    }

    public double getDeathEventRadius() {
        return deathEventRadius;
    }

    public boolean isShowRemainingTime() {
        return showRemainingTime;
    }

    public int getUpdateInterval() {
        return updateInterval;
    }

    public boolean isOnlyNearPlayers() {
        return onlyNearPlayers;
    }

    public double getUpdateRange() {
        return updateRange;
    }

    public int getStarvationCheckInterval() {
        return starvationCheckInterval;
    }

    public boolean isOnlyCheckNearPlayers() {
        return onlyCheckNearPlayers;
    }

    public double getStarvationCheckRange() {
        return starvationCheckRange;
    }

    // 发送调试消息
    public void sendDebugMessage(Player player, String path, Object... args) {
        if (debugEnabled) {
            player.sendMessage(getMessage("debug." + path, args));
        }
    }

    // 格式化位置信息
    public String formatLocation(Location loc) {
        return String.format("(%.1f, %.1f, %.1f)", loc.getX(), loc.getY(), loc.getZ());
    }

    // 格式化时间显示（将秒转换为可读格式）
    public String formatTimeRemaining(long seconds) {
        if (seconds < 0) return "已过期";
        
        long days = seconds / (24 * 3600);
        seconds %= (24 * 3600);
        long hours = seconds / 3600;
        seconds %= 3600;
        long minutes = seconds / 60;
        seconds %= 60;

        StringBuilder result = new StringBuilder();
        if (days > 0) result.append(days).append("天");
        if (hours > 0) result.append(hours).append("小时");
        if (minutes > 0) result.append(minutes).append("分钟");
        if (seconds > 0 || result.isEmpty()) result.append(seconds).append("秒");

        return result.toString();
    }

    // 获取格式化的显示名称
    public String formatDisplayName(long remainingTime) {
        if (remainingTime <= 0) {
            return starvedFormat;
        }
        return timeFormat.replace("{time}", formatTimeRemaining(remainingTime));
    }

    // 获取格式化的消息
    public String getMessage(String path, Object... args) {
        String message = config.getString("messages." + path, "");
        message = translateColors(message);
        if (args != null && args.length > 0) {
            for (int i = 0; i < args.length; i += 2) {
                if (i + 1 < args.length) {
                    message = message.replace("{" + args[i] + "}", String.valueOf(args[i + 1]));
                }
            }
        }
        return message;
    }

    public void setShowRemainingTime(boolean showRemainingTime) {
        this.showRemainingTime = showRemainingTime;
    }
} 