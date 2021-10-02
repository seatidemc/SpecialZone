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
    public final static String[] ARGS_1ST = { "set", "reload", "create", "delete", "addex", "delex", "getex", "info" };
    public final static String[] BOOLEAN_OPTIONS = { "true", "false" };
    public static List<String> zoneNames;

    public List<String> getResult(String arg, List<String> commands) {
        List<String> result = new ArrayList<>();
        StringUtil.copyPartialMatches(arg, commands, result);
        Collections.sort(result);
        return result;
    }

    /**
     * 将一个布尔值转换为 "&atrue" 或者 "&cfalse"。
     */
    public String tgfr(boolean input) {
        return "&" + (input ? "atrue" : "cfalse");
    }

    public String tgfrx(boolean input, String... custom) {
        return "&" + (input ? "a" + custom[0] : "c" + custom[1]);
    }

    public String tgfrx(boolean input) {
        return "&" + (input ? "a已开启" : "c未开启");
    }

    public Commands() {
        supportedProperties = Arrays.asList("keepInv", "keepExp", "noBreak", "ignoreY", "noPlace", "noIgnite",
                "noContainer");
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
                if (args[0].equals("set") || args[0].endsWith("ex")) {
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
                    LogUtil.send(sender, "成功将区域 &e" + name + "&r 的 &a" + property + "&r 属性设置为 " + tgfr(value) + "&r。");
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

                case "info": {
                    if (!(sender instanceof Player)) {
                        LogUtil.send(sender, "该指令仅支持玩家执行。");
                        return true;
                    }
                    var p = (Player) sender;
                    var zoneName = Zone.getLocationInZone(p.getLocation());
                    if (zoneName == null) {
                        LogUtil.send(sender, "&c当前所处位置没有已划定的区域。");
                        return true;
                    }
                    var zone = new Zone(zoneName);
                    String[] msgs = { "&e区域 &a" + zoneName + "&e 的详细信息：",
                            "&e忽略 Y 轴 — " + tgfrx(zone.getProperty("ignoreY")),
                            "&e禁止破坏 — " + tgfrx(zone.getProperty("noBreak")),
                            "&e禁止放置 — " + tgfrx(zone.getProperty("noPlace")),
                            "&e禁止打开容器 — " + tgfrx(zone.getProperty("noContainer")),
                            "&e禁止点火 — " + tgfrx(zone.getProperty("noIgnite")),
                            "&e保留物品栏 — " + tgfrx(zone.getProperty("keepInv")),
                            "&e保留经验 — " + tgfrx(zone.getProperty("keepExp")), "&e区域范围 — &a(" + zone.x1 + ", " + zone.y1
                                    + ", " + zone.z1 + ") &b→&a (" + zone.x2 + ", " + zone.y2 + ", " + zone.z2 + ")" };
                    LogUtil.sendAll(sender, msgs);
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
