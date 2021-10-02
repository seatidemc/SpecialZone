package top.seatide.SpecialZone;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import top.seatide.SpecialZone.Abstract.Zone;
import top.seatide.SpecialZone.Utils.Files;
import top.seatide.SpecialZone.Utils.LogUtil;

public class Commands implements TabExecutor {
    public static List<String> supportedProperties;
    public final static String[] ARGS_1ST = { "set", "reload", "create", "delete", "addex", "delex" };
    public final static String[] BOOLEAN_OPTIONS = { "true", "false" };
    public static List<String> zoneNames;

    public List<String> getResult(String arg, List<String> commands) {
        List<String> result = new ArrayList<>();
        StringUtil.copyPartialMatches(arg, commands, result);
        Collections.sort(result);
        return result;
    }

    public Commands() {
        supportedProperties = Arrays.asList("keepInv", "keepExp", "noBreak", "ignoreY", "noPlace", "noIgnite", "noContainer");
        zoneNames = new ArrayList<>(Files.zones.getKeys(false));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        var result = new ArrayList<String>();
        if (cmd.getName().equalsIgnoreCase("specialzone")) {
            if (args.length == 1) {
                return getResult(args[0], Arrays.asList(ARGS_1ST));
            }
            if (args.length == 2) {
                if (args[0].equals("set") || args[0].equals("delete") || args[0].endsWith("ex")) {
                    return getResult(args[1], zoneNames);
                }
            }
            if (args.length == 3) {
                if (args[0].equals("set") || args[0].equals("delex") || args[0].equals("setex")) {
                    return getResult(args[2], supportedProperties);
                }
                if (args[0].equals("claim")) {
                    return getResult(args[2], Arrays.asList(BOOLEAN_OPTIONS));
                }
            }
            if (args.length == 4) {
                if (args[0].equals("set")) {
                    return getResult(args[3], Arrays.asList(BOOLEAN_OPTIONS));
                }
            }
        }
        return result;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("specialzone")) {
            if (args.length == 0) {
                LogUtil.send(sender, "参数不足。");
                return true;
            }
            switch (args[0]) {
                case "create": {
                    if (!(sender instanceof Player)) {
                        LogUtil.send(sender, "该指令仅支持玩家执行。");
                        return true;
                    }
                    if (args.length < 2) {
                        LogUtil.send(sender, "参数不足。");
                        return true;
                    }
                    if (Zone.contains(args[1])) {
                        LogUtil.send(sender, "&c创建区域失败，同名区域已存在。");
                        return true;
                    }
                    Player p = (Player) sender;
                    UUID u = p.getUniqueId();
                    var result = Events.selectionState.get(u);
                    if (result == null) {
                        LogUtil.send(sender, "&c创建区域失败，请先使用&e箭&c选择区域的范围。");
                        return true;
                    }
                    if (result[0].length != 3 || result[1].length != 3) {
                        LogUtil.send(sender, "&c创建区域失败，区域范围不完整。");
                        return true;
                    }
                    if (args.length < 2) {
                        LogUtil.send(sender, "&c参数不足。");
                        return true;
                    }
                    var zone = new Zone(args[1], result[0], result[1]);
                    zone.create();
                    LogUtil.send(sender, "&a成功创建区域！");
                    Events.selectionState.remove(u);
                    Files.reload();
                    break;
                }

                case "reload": {
                    Files.reload();
                    LogUtil.send(sender, "成功重载配置文件。");
                    break;
                }

                case "delete": {
                    if (args.length < 2) {
                        LogUtil.send(sender, "参数不足。");
                        return true;
                    }
                    var name = args[1];
                    if (!Zone.contains(name)) {
                        LogUtil.send(sender, "删除失败，区域不存在。");
                        return true;
                    }
                    Zone.delete(name);
                    LogUtil.send(sender, "成功删除区域 &c" + name + "&r。");
                    Files.reload();
                    break;
                }

                case "claim": {
                    if (args.length < 2) {
                        LogUtil.send(sender, "&c参数不足。");
                        return true;
                    }
                    var name = args[1];
                    if (!Zone.contains(name)) {
                        LogUtil.send(sender, "&c区域不存在。");
                        return true;
                    }
                    var zone = new Zone(name);
                    var value = true;
                    if (args.length >= 3)
                        value = Boolean.parseBoolean(args[2]);
                    zone.setProperty("noBreak", value);
                    zone.setProperty("noPlace", value);
                    zone.setProperty("noContainer", value);
                    zone.setProperty("noIgnite", value);
                    zone.setProperty("noExplosion", value);
                    if (value) {
                        LogUtil.send(sender, "成功将区域 &e" + name + "&r 标记为领地。");
                    } else {
                        LogUtil.send(sender, "成功取消标记为领地。");
                    }
                    break;
                }

                case "set": {
                    if (args.length < 3) {
                        LogUtil.send(sender, "&c参数不足。");
                        return true;
                    }
                    var name = args[1];
                    var property = args[2];
                    var value = true;
                    if (!Zone.contains(name)) {
                        LogUtil.send(sender, "&c区域不存在。");
                        return true;
                    }
                    if (args.length >= 4)
                        value = Boolean.parseBoolean(args[3]);
                    if (!supportedProperties.contains(property)) {
                        LogUtil.send(sender, "无效参数。参数只能为 " + StringUtils.join(supportedProperties, '、') + " 之一。");
                        return true;
                    }
                    var zone = new Zone(name);
                    zone.setProperty(property, value);
                    LogUtil.send(sender, "成功将区域 &e" + name + "&r 的 &a" + property + "&r 属性设置为 &"
                            + (value ? "atrue" : "cfalse") + "&r。");
                    Files.reload();
                    break;
                }

                case "addex": {
                    if (args.length < 4) {
                        LogUtil.send(sender, "&c参数不足。");
                        return true;
                    }

                    var zoneName = args[1];
                    var property = args[2];
                    var player = args[3];
                    if (!Zone.contains(zoneName)) {
                        LogUtil.send(sender, "&c区域不存在。");
                        return true;
                    }
                    var zone = new Zone(zoneName);
                    if (zone.setException(property, player)) {
                        LogUtil.send(sender,
                                "成功将 &a" + player + "&r 加入区域 &e" + zoneName + "&r 的 &b" + property + "&r 属性排除项。");
                    } else {
                        LogUtil.send(sender, "添加排除项失败，玩家已存在。");
                    }
                    Files.reload();
                    break;
                }

                case "delex": {
                    if (args.length < 4) {
                        LogUtil.send(sender, "&c参数不足。");
                        return true;
                    }
                    var zoneName = args[1];
                    var property = args[2];
                    var player = args[3];
                    if (!Zone.contains(zoneName)) {
                        LogUtil.send(sender, "&c区域不存在。");
                        return true;
                    }
                    var zone = new Zone(zoneName);
                    if (zone.delException(property, player)) {
                        LogUtil.send(sender,
                                "成功从区域 &e" + zoneName + "&r 的 &b" + property + "&r 属性排除项移除 &a" + player + "&r。");
                    } else {
                        LogUtil.send(sender, "删除排除项失败，玩家不存在。");
                    }
                    Files.reload();
                    break;
                }

                case "getex": {
                    if (args.length < 3) {
                        LogUtil.send(sender, "&c参数不足。");
                        return true;
                    }
                    var zoneName = args[1];
                    var property = args[2];
                    if (!Zone.contains(zoneName)) {
                        LogUtil.send(sender, "&c区域不存在。");
                        return true;
                    }
                    var zone = new Zone(zoneName);
                    var result = zone.getExceptions(property);
                    if (result.size() == 0) {
                        LogUtil.send(sender, "&e" + zoneName + "&r 没有 &b" + property + "&r 属性的排除项。");
                    } else {
                        LogUtil.send(sender, "&e" + zoneName + "&r 的 &b" + property + "&r 属性排除项：");
                        LogUtil.send(sender, "&e" + StringUtils.join(result, '、'));
                    }
                    Files.reload();
                    break;
                }

                default: {
                    LogUtil.send(sender, "未知指令参数。");
                }
            }
        }
        return true;
    }
}
