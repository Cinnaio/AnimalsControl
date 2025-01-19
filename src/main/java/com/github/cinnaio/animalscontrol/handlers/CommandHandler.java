package com.github.cinnaio.animalscontrol.handlers;

import com.github.cinnaio.animalscontrol.AnimalsControl;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandHandler {
    private final AnimalsControl plugin;

    public CommandHandler(AnimalsControl plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("acreload")) {
            if (sender.hasPermission("animalscontrol.reload")) {
                plugin.getAnimalData().reload();
                sender.sendMessage("§a配置已重载！");
            } else {
                sender.sendMessage("§c你没有权限执行此命令！");
            }
            return true;
        } else if (command.getName().equalsIgnoreCase("toggleRemainingTime")) {
            if (sender instanceof Player player) {
                boolean currentSetting = plugin.getAnimalData().isShowRemainingTime();
                plugin.getAnimalData().setShowRemainingTime(!currentSetting);
                String message = currentSetting ? "§c已关闭剩余时间显示！" : "§a已开启剩余时间显示！";
                player.sendMessage(message);
            } else {
                sender.sendMessage("§c此命令只能由玩家执行！");
            }
            return true;
        }
        return false;
    }
} 