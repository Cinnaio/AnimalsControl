package com.github.cinnaio.animalscontrol.listeners;

import com.github.cinnaio.animalscontrol.AnimalsControl;
import org.bukkit.entity.Animals;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityBreedEvent;
import org.bukkit.persistence.PersistentDataType;

public class BabyAnimalListener implements Listener {
    private final AnimalsControl plugin;

    public BabyAnimalListener(AnimalsControl plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBabyAnimalBorn(EntityBreedEvent event) {
        // 获取繁殖者（父母）动物
        Animals parent = (Animals) event.getBreeder();

        // 获取新生幼崽
        Animals baby = (Animals) event.getEntity();

        // 设置幼崽的喂养时间
        long currentTime = System.currentTimeMillis() / 1000;
        baby.getPersistentDataContainer().set(plugin.getLastFeedKey(), PersistentDataType.LONG, currentTime);
        
        // 不发送任何消息，只需确保幼崽的存活时间被正确设置
    }
}