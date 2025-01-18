package com.github.cinnaio.animalscontrol.handlers;

import com.github.cinnaio.animalscontrol.AnimalsControl;
import com.github.cinnaio.animalscontrol.listeners.AnimalControlListener;
import org.bukkit.entity.Animals;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class AnimalHandler {
    private final AnimalsControl plugin;

    public AnimalHandler(AnimalsControl plugin) {
        this.plugin = plugin;
    }

    public void updateAnimalNameTag(Animals animal) {
        PersistentDataContainer pdc = animal.getPersistentDataContainer();
        Long lastFeedTime = pdc.get(plugin.getLastFeedKey(), PersistentDataType.LONG);
        
        if (lastFeedTime != null) {
            long currentTime = System.currentTimeMillis() / 1000;
            long remainingTime = plugin.getAnimalData().getStarvationTime() - (currentTime - lastFeedTime);
            
            if (remainingTime <= 0) {
                // 如果已经饥饿，立即处理死亡
                AnimalControlListener.handleStarvation(plugin, animal);
                return;
            }
            
            // 只有当动物在玩家视野范围内时才更新名称
            if (!animal.getWorld().getNearbyPlayers(animal.getLocation(), 48).isEmpty()) {
                animal.setCustomName(plugin.getAnimalData().formatDisplayName(remainingTime));
                animal.setCustomNameVisible(true);
            } else {
                animal.setCustomNameVisible(false);
            }
        }
    }

    public void checkAndHandleStarvation(Animals animal) {
        PersistentDataContainer pdc = animal.getPersistentDataContainer();
        Long lastFeedTime = pdc.get(plugin.getLastFeedKey(), PersistentDataType.LONG);
        
        if (lastFeedTime != null) {
            long currentTime = System.currentTimeMillis() / 1000;
            long timeDelta = currentTime - lastFeedTime;
            
            if (timeDelta > plugin.getAnimalData().getStarvationTime()) {
                // 如果超过饥饿时间，调用饥饿处理
                AnimalControlListener.handleStarvation(plugin, animal);
            }
        }
    }
} 