package com.github.cinnaio.animalscontrol.listeners;

import com.github.cinnaio.animalscontrol.AnimalsControl;
import org.bukkit.Material;
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

        // 处理繁殖逻辑
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

        // 获取新生幼崽
        Animals baby = (Animals) event.getEntity();

        // 设置幼崽的喂养时间
        long currentTime = System.currentTimeMillis() / 1000;
        baby.getPersistentDataContainer().set(plugin.getLastFeedKey(), PersistentDataType.LONG, currentTime);
        
        // 发送消息给玩家
        player.sendMessage(plugin.getAnimalData().getMessage("wild_animal_first_feed"));
        String timeRemaining = plugin.getAnimalData().formatTimeRemaining(plugin.getAnimalData().getStarvationTime());
        player.sendMessage(plugin.getAnimalData().getMessage("time_info", "time", timeRemaining));
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

        // 记录喂养者信息
        pdc.set(plugin.getWheatKey(), PersistentDataType.STRING, player.getUniqueId().toString());

        // 检查动物的生存时间
        if (oldTime != null) {
            // 检查动物是否已经饥饿
            if ((plugin.getAnimalData().getStarvationTime() - (currentTime - oldTime)) <= 0) {
                // 如果已经饥饿，立即处理死亡
                handleStarvation(plugin, animal, player);
                return;
            }

            // 使用配置中的 breed_chance 进行繁殖概率检查
            double breedChance = plugin.getAnimalData().getBreedChance();
            if (random.nextDouble() < breedChance) {
                animal.setLoveModeTicks(plugin.getAnimalData().getBreedDuration());
                player.sendMessage(plugin.getAnimalData().getMessage("breed_success"));
            }
        } else {
            // 如果是第一次喂养，100%进入发情状态
            animal.setLoveModeTicks(plugin.getAnimalData().getBreedDuration());
            player.sendMessage(plugin.getAnimalData().getMessage("breed_success"));
        }

        if (oldTime == null) {
            handleFirstFeeding(animal, currentTime, player);
        } else {
            handleNormalFeeding(animal, currentTime, player);
        }
    }

    private void handleFirstFeeding(Animals animal, long currentTime, Player player) {
        animal.getPersistentDataContainer().set(
            plugin.getLastFeedKey(), 
            PersistentDataType.LONG, 
            currentTime
        );

        // 直接使用传入的 player 参数
        player.sendMessage(plugin.getAnimalData().getMessage("wild_animal_first_feed"));
        // 显示饥饿时间
        String timeRemaining = plugin.getAnimalData().formatTimeRemaining(
            plugin.getAnimalData().getStarvationTime()
        );
        player.sendMessage(plugin.getAnimalData().getMessage("time_info", "time", timeRemaining));

        // 检查动物是否成年
        if (!animal.isAdult()) return;

        // 检查是否允许野生动物立即进入繁殖状态
        if (plugin.getAnimalData().isWildAnimalInstantBreed()) {
            // 设置动物的繁殖模式持续时间
            animal.setLoveModeTicks(plugin.getAnimalData().getBreedDuration());
        }
    }

    public static void handleStarvation(AnimalsControl plugin, Animals animal, Player player) {
        double radius = plugin.getAnimalData().getCheckRadius();
        
        // 获取指定范围内的动物
        List<Animals> nearbyAnimals = animal.getNearbyEntities(radius, radius, radius).stream()
            .filter(entity -> entity instanceof Animals)
            .map(entity -> (Animals) entity)
            .toList();

        // 如果有附近动物，分配时间
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

        // 发送消息给指定的玩家
        player.sendMessage(plugin.getAnimalData().getMessage("animal_starved"));
        if (!nearbyAnimals.isEmpty()) {
            player.sendMessage(plugin.getAnimalData().getMessage("time_shared"));
        }
        
        // 移除死亡动物
        animal.remove();
    }

    private void handleNormalFeeding(Animals animal, long currentTime, Player player) {
        animal.getPersistentDataContainer().set(
            plugin.getLastFeedKey(),
            PersistentDataType.LONG,
            currentTime
        );

        // 直接使用传入的 player 参数
        player.sendMessage(plugin.getAnimalData().getMessage("animal_fed"));

        // 计算并显示剩余时间
        long remainingTime = plugin.getAnimalData().getStarvationTime() - 
            (System.currentTimeMillis() / 1000 - currentTime);
        String timeRemaining = plugin.getAnimalData().formatTimeRemaining(remainingTime);
        
        // 仅在 show_remaining_time 为 true 时显示剩余时间
        if (plugin.getAnimalData().isShowRemainingTime()) {
            player.sendMessage(plugin.getAnimalData().getMessage("time_info", "time", timeRemaining));
        }

        // 检查动物是否成年
        if (!animal.isAdult()) return; // 如果动物不是成年，直接返回，不执行后续逻辑

        double chance = plugin.getAnimalData().getBreedChance();
        if (random.nextDouble() < chance) {
            animal.setLoveModeTicks(plugin.getAnimalData().getBreedDuration());

            // 发送调试消息
            if (plugin.getAnimalData().isDebugEnabled()) {
                player.sendMessage(plugin.getAnimalData().getMessage("debug.breed_chance", "chance", String.format("%.1f%%", chance * 100)));
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