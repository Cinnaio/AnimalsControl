package com.github.cinnaio.animalscontrol.handlers;

import com.github.cinnaio.animalscontrol.AnimalsControl;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandHandler implements CommandExecutor {
    private final AnimalsControl plugin;

    public CommandHandler(AnimalsControl plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("animalcontrol")) {
            if (args.length == 0) {
                sender.sendMessage("§c请提供一个操作参数: reload 或 toggle.");
                return true;
            }

            switch (args[0].toLowerCase()) {
                case "reload":
                    if (sender.hasPermission("animalscontrol.reload")) {
                        plugin.getAnimalData().reload();
                        sender.sendMessage("§a配置已重载！");
                    } else {
                        sender.sendMessage("§c你没有权限执行此命令！");
                    }
                    return true;

                case "toggle":
                    if (sender instanceof Player player) {
                        boolean currentSetting = plugin.getAnimalData().isShowRemainingTime();
                        plugin.getAnimalData().setShowRemainingTime(!currentSetting);
                        String message = currentSetting ? "§c已关闭剩余时间显示！" : "§a已开启剩余时间显示！";
                        player.sendMessage(message);
                    } else {
                        sender.sendMessage("§c此命令只能由玩家执行！");
                    }
                    return true;

                default:
                    sender.sendMessage("§c未知操作参数: " + args[0]);
                    return true;
            }
        }
        return false;
    }
} 