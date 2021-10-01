package top.seatide.SpecialZone;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import top.seatide.SpecialZone.Utils.Files;
import top.seatide.SpecialZone.Utils.LogUtil;

public class Main extends JavaPlugin {
    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        Files.init(this);
        var commands = new Commands();
        this.getCommand("specialzone").setExecutor(commands);
        this.getCommand("specialzone").setTabCompleter(commands);
        Bukkit.getPluginManager().registerEvents(new Events(), this);
        LogUtil.success("SpecialZone 已启用。");
    }

    @Override
    public void onDisable() {
        LogUtil.success("SpecialZone 已禁用。");
    }
}