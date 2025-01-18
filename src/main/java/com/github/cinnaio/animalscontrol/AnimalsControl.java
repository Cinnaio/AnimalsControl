package com.github.cinnaio.animalscontrol;

import com.github.cinnaio.animalscontrol.data.AnimalControlData;
import com.github.cinnaio.animalscontrol.listeners.AnimalControlListener;
import com.github.cinnaio.animalscontrol.handlers.TaskManager;
import com.github.cinnaio.animalscontrol.handlers.AnimalHandler;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.entity.Player;

public final class AnimalsControl extends JavaPlugin {
    private NamespacedKey wheatKey;
    private NamespacedKey lastFeedKey;
    private AnimalControlData animalData;
    private int updateTask;
    private int checkStarvationTask;
    private TaskManager taskManager;
    private AnimalHandler animalHandler;

    @Override
    public void onEnable() {
        // 保存默认配置
        saveDefaultConfig();
        
        // 初始化数据管理器
        animalData = new AnimalControlData(this);
        
        // 初始化 NamespacedKey
        wheatKey = new NamespacedKey(this, "fed_wheat");
        lastFeedKey = new NamespacedKey(this, "last_feed_time");
        
        // 初始化处理器和任务管理器
        animalHandler = new AnimalHandler(this);
        taskManager = new TaskManager(this, animalHandler);
        
        // 注册监听器
        getServer().getPluginManager().registerEvents(new AnimalControlListener(this), this);
        
        // 注册重载命令
        getCommand("acreload").setExecutor((sender, command, label, args) -> {
            if (sender.hasPermission("animalscontrol.reload")) {
                animalData.reload();
                sender.sendMessage("§a配置已重载！");
            } else {
                sender.sendMessage("§c你没有权限执行此命令！");
            }
            return true;
        });

        // 启动定时任务
        taskManager.startTasks();
    }

    @Override
    public void onDisable() {
        taskManager.stopTasks();
    }

    public NamespacedKey getWheatKey() {
        return wheatKey;
    }

    public NamespacedKey getLastFeedKey() {
        return lastFeedKey;
    }

    public AnimalControlData getAnimalData() {
        return animalData;
    }

    public AnimalHandler getAnimalHandler() {
        return animalHandler;
    }
}
