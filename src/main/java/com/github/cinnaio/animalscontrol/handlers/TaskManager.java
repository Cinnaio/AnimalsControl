package com.github.cinnaio.animalscontrol.handlers;

import com.github.cinnaio.animalscontrol.AnimalsControl;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Player;

public class TaskManager {
    private final AnimalsControl plugin;
    private final AnimalHandler animalHandler;
    private int updateTask;
    private int checkStarvationTask;

    public TaskManager(AnimalsControl plugin, AnimalHandler animalHandler) {
        this.plugin = plugin;
        this.animalHandler = animalHandler;
    }

    public void startTasks() {
        startUpdateTask();
        startStarvationCheckTask();
    }

    public void stopTasks() {
        Bukkit.getScheduler().cancelTask(updateTask);
        Bukkit.getScheduler().cancelTask(checkStarvationTask);
    }

    private void startUpdateTask() {
        int interval = plugin.getAnimalData().getUpdateInterval();
        updateTask = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            if (plugin.getAnimalData().isOnlyNearPlayers()) {
                // 只更新玩家附近的动物
                for (Player player : Bukkit.getOnlinePlayers()) {
                    double range = plugin.getAnimalData().getUpdateRange();
                    player.getNearbyEntities(range, range, range).stream()
                        .filter(entity -> entity instanceof Animals)
                        .map(entity -> (Animals) entity)
                        .forEach(animal -> {
                            animalHandler.updateAnimalNameTag(animal); // 更新动物名称
                            if (!plugin.getAnimalData().isShowRemainingTime()) {
                                animal.setCustomNameVisible(false); // 立即隐藏名称
                            }
                        });
                }
            } else {
                // 更新所有世界的动物
                for (World world : Bukkit.getWorlds()) {
                    world.getEntities().stream()
                        .filter(entity -> entity instanceof Animals)
                        .map(entity -> (Animals) entity)
                        .forEach(animal -> {
                            animalHandler.updateAnimalNameTag(animal); // 更新动物名称
                            if (!plugin.getAnimalData().isShowRemainingTime()) {
                                animal.setCustomNameVisible(false); // 立即隐藏名称
                            }
                        });
                }
            }
        }, interval, interval);
    }

    private void startStarvationCheckTask() {
        checkStarvationTask = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            if (plugin.getAnimalData().isOnlyCheckNearPlayers()) {
                // 只检查在线玩家附近的动物
                double range = plugin.getAnimalData().getStarvationCheckRange();
                for (Player player : Bukkit.getOnlinePlayers()) {
                    player.getNearbyEntities(range, range, range).stream()
                        .filter(entity -> entity instanceof Animals)
                        .map(entity -> (Animals) entity)
                        .forEach(animalHandler::checkAndHandleStarvation);
                }
            } else {
                // 检查所有世界的动物
                for (World world : Bukkit.getWorlds()) {
                    world.getEntities().stream()
                        .filter(entity -> entity instanceof Animals)
                        .map(entity -> (Animals) entity)
                        .forEach(animalHandler::checkAndHandleStarvation);
                }
            }
        }, plugin.getAnimalData().getStarvationCheckInterval(), plugin.getAnimalData().getStarvationCheckInterval());
    }
} 