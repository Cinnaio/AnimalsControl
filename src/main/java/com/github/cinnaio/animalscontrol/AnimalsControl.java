package com.github.cinnaio.animalscontrol;

import com.github.cinnaio.animalscontrol.data.AnimalControlData;
import com.github.cinnaio.animalscontrol.handlers.AnimalHandler;
import com.github.cinnaio.animalscontrol.handlers.CommandHandler;
import com.github.cinnaio.animalscontrol.handlers.TaskManager;
import com.github.cinnaio.animalscontrol.listeners.AnimalControlListener;
import com.github.cinnaio.animalscontrol.listeners.BabyAnimalListener;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;

public final class AnimalsControl extends JavaPlugin {
    private NamespacedKey wheatKey;
    private NamespacedKey lastFeedKey;
    private AnimalControlData animalData;
    private TaskManager taskManager;
    private AnimalHandler animalHandler;
    private CommandHandler commandHandler;

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
        commandHandler = new CommandHandler(this);
        
        // 注册监听器
        getServer().getPluginManager().registerEvents(new AnimalControlListener(this), this);
        getServer().getPluginManager().registerEvents(new BabyAnimalListener(this), this);
        
        // 注册命令
        getCommand("acreload").setExecutor(commandHandler::onCommand);
        getCommand("toggleRemainingTime").setExecutor(commandHandler::onCommand);

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

}
