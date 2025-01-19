package com.github.cinnaio.animalscontrol.listeners;

import com.github.cinnaio.animalscontrol.AnimalsControl;
import org.bukkit.Material;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityBreedEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.Random;

public class AnimalControlListener implements Listener {
    private final AnimalsControl plugin;
    private final Random random = new Random();
    
    public AnimalControlListener(AnimalsControl plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBreedAttempt(EntityBreedEvent event) {
        if (!(event.getBreeder() instanceof Player player)) return;
        
        if (!plugin.getAnimalData().getBreedingAnimals().contains(event.getEntity().getType().name())) return;

        PlayerInventory inventory = player.getInventory();
        int wheatRequired = plugin.getAnimalData().getBreedingWheatRequired();
        
        if (getWheatCount(inventory) < wheatRequired) {
            event.setCancelled(true);
            player.sendMessage(plugin.getAnimalData().getMessage("not_enough_wheat_breeding", 
                "amount", wheatRequired));
            return;
        }

        consumeWheat(inventory, wheatRequired);
        player.sendMessage(plugin.getAnimalData().getMessage("breed_success"));
    }

    @EventHandler
    public void onFeedAnimal(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof Animals animal)) return;

        Player player = event.getPlayer();
        ItemStack heldItem = player.getInventory().getItemInMainHand();
        
        if (heldItem.getType() != Material.WHEAT) return;
        if (!plugin.getAnimalData().getBreedingAnimals().contains(animal.getType().name())) return;

        event.setCancelled(true);

        long currentTime = System.currentTimeMillis() / 1000;
        PersistentDataContainer pdc = animal.getPersistentDataContainer();
        Long oldTime = pdc.get(plugin.getLastFeedKey(), PersistentDataType.LONG);

        // 消耗一个小麦
        heldItem.setAmount(heldItem.getAmount() - 1);

        if (oldTime == null) {
            handleFirstFeeding(animal, currentTime);
        } else {
            handleNormalFeeding(animal, currentTime);
        }
    }

    private void handleFirstFeeding(Animals animal, long currentTime) {
        animal.getPersistentDataContainer().set(
            plugin.getLastFeedKey(), 
            PersistentDataType.LONG, 
            currentTime
        );
        
        // 获取附近的玩家
        animal.getWorld().getNearbyPlayers(animal.getLocation(), 16).forEach(player -> {
            player.sendMessage(plugin.getAnimalData().getMessage("wild_animal_first_feed"));
            // 显示饥饿时间
            String timeRemaining = plugin.getAnimalData().formatTimeRemaining(
                plugin.getAnimalData().getStarvationTime()
            );
            player.sendMessage(plugin.getAnimalData().getMessage("time_info", "time", timeRemaining));
        });

        if (!animal.isAdult()) return;
        if (plugin.getAnimalData().isWildAnimalInstantBreed()) {
            animal.setLoveModeTicks(plugin.getAnimalData().getBreedDuration());
        }
    }

    public static void handleStarvation(AnimalsControl plugin, Animals animal) {
        double radius = plugin.getAnimalData().getCheckRadius();
        String animalType = animal.getType().name();
        
        // 只处理配置中允许的动物类型
        if (!plugin.getAnimalData().getBreedingAnimals().contains(animalType)) {
            animal.remove();
            return;
        }

        // 获取指定范围内的同类动物
        List<Animals> nearbyAnimals = animal.getNearbyEntities(radius, radius, radius).stream()
            .filter(entity -> entity instanceof Animals)
            .map(entity -> (Animals) entity)
            .filter(nearbyAnimal -> 
                nearbyAnimal.getType().name().equals(animalType) &&
                nearbyAnimal != animal &&
                nearbyAnimal.getPersistentDataContainer().has(plugin.getLastFeedKey(), PersistentDataType.LONG)
            )
            .map(entity -> (Animals) entity)
            .toList();

        // 如果有同类动物，分配时间
        if (!nearbyAnimals.isEmpty()) {
            long timeShare = plugin.getAnimalData().getShareTime() / nearbyAnimals.size();
            
            // 分配时间给附近动物
            for (Animals nearbyAnimal : nearbyAnimals) {
                PersistentDataContainer nearbyPdc = nearbyAnimal.getPersistentDataContainer();
                Long oldTime = nearbyPdc.get(plugin.getLastFeedKey(), PersistentDataType.LONG);
                
                if (oldTime != null) {
                    nearbyPdc.set(
                        plugin.getLastFeedKey(),
                        PersistentDataType.LONG,
                        oldTime + timeShare
                    );
                }
            }
        }

        // 获取死亡动物的生存时间用于调试信息
        PersistentDataContainer pdc = animal.getPersistentDataContainer();
        Long birthTime = pdc.get(plugin.getLastFeedKey(), PersistentDataType.LONG);
        long lifetime = birthTime != null ? (System.currentTimeMillis() / 1000) - birthTime : 0;
        String lifetimeStr = plugin.getAnimalData().formatTimeRemaining(lifetime);
        
        // 发送消息给附近的玩家
        animal.getWorld().getNearbyPlayers(animal.getLocation(), 
            plugin.getAnimalData().getDeathEventRadius()
        ).forEach(player -> {
            player.sendMessage(plugin.getAnimalData().getMessage("animal_starved"));
            if (!nearbyAnimals.isEmpty()) {
                player.sendMessage(plugin.getAnimalData().getMessage("time_shared"));
            }
            
            // 如果启用了死亡事件显示
            if (plugin.getAnimalData().isShowDeathEvent()) {
                plugin.getAnimalData().sendDebugMessage(player, "death_event",
                    "type", animalType,
                    "location", plugin.getAnimalData().formatLocation(animal.getLocation()),
                    "lifetime", lifetimeStr,
                    "nearby_count", nearbyAnimals.size()
                );
            }
        });
        
        // 移除死亡动物
        animal.remove();
    }

    private void handleNormalFeeding(Animals animal, long currentTime) {
        animal.getPersistentDataContainer().set(
            plugin.getLastFeedKey(),
            PersistentDataType.LONG,
            currentTime
        );
        
        // 获取附近的玩家
        animal.getWorld().getNearbyPlayers(animal.getLocation(), 16).forEach(player -> {
            player.sendMessage(plugin.getAnimalData().getMessage("animal_fed"));
            
            // 计算并显示剩余时间
            long remainingTime = plugin.getAnimalData().getStarvationTime() - 
                (System.currentTimeMillis() / 1000 - currentTime);
            String timeRemaining = plugin.getAnimalData().formatTimeRemaining(remainingTime);
            player.sendMessage(plugin.getAnimalData().getMessage("time_info", "time", timeRemaining));
        });
        
        if (!animal.isAdult()) return;
        double chance = plugin.getAnimalData().getBreedChance();
        if (random.nextDouble() < chance) {
            animal.setLoveModeTicks(plugin.getAnimalData().getBreedDuration());
            
            // 发送调试消息
            if (plugin.getAnimalData().isDebugEnabled()) {
                animal.getWorld().getNearbyPlayers(animal.getLocation(), 16).forEach(player -> 
                    plugin.getAnimalData().sendDebugMessage(player, "breed_chance", 
                        "chance", String.format("%.1f%%", chance * 100)));
            }
        }
    }

    private int getWheatCount(PlayerInventory inventory) {
        int count = 0;
        for (ItemStack item : inventory.getContents()) {
            if (item != null && item.getType() == Material.WHEAT) {
                count += item.getAmount();
            }
        }
        return count;
    }

    private void consumeWheat(PlayerInventory inventory, int amount) {
        int remaining = amount;
        for (int i = 0; i < inventory.getSize() && remaining > 0; i++) {
            ItemStack item = inventory.getItem(i);
            if (item != null && item.getType() == Material.WHEAT) {
                if (item.getAmount() <= remaining) {
                    remaining -= item.getAmount();
                    inventory.setItem(i, null);
                } else {
                    item.setAmount(item.getAmount() - remaining);
                    remaining = 0;
                }
            }
        }
    }
} 