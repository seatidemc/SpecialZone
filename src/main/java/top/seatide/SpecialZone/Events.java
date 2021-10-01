package top.seatide.SpecialZone;

import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import top.seatide.SpecialZone.Abstract.Zone;
import top.seatide.SpecialZone.Utils.LogUtil;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class Events implements Listener {
    public static Map<UUID, double[][]> selectionState;

    public Events() {
        selectionState = new HashMap<>();
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        var zoneName = Zone.getLocationInZone(event.getBlock().getLocation());
        if (zoneName != null) {
            var zone = new Zone(zoneName);
            if (zone.hasEffectsOn(event.getPlayer(), "noBreak"))
                event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player p = event.getEntity();
        var zoneName = Zone.getLocationInZone(p.getLocation());
        if (zoneName != null) {
            var zone = new Zone(zoneName);
            if (zone.hasEffectsOn(p, "keepInv")) {
                event.setKeepInventory(true);
                LogUtil.send(p, "&e你的背包物品已保留。");
            }
            if (zone.hasEffectsOn(p, "keepExp")) {
                event.setKeepLevel(true);
                event.getDrops().clear();
                event.setDroppedExp(0);
                LogUtil.send(p, "&e你的经验已保留。");
            }
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player p = event.getPlayer();
        var zoneName = Zone.getLocationInZone(event.getBlock().getLocation());
        if (zoneName != null) {
            var zone = new Zone(zoneName);
            if (zone.hasEffectsOn(p, "noPlace"))
                event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player p = event.getPlayer();
        var zoneName = Zone.getLocationInZone(p.getLocation());
        if (zoneName != null) {
            var item = event.getItem();
            var zone = new Zone(zoneName);
            if (zone.hasEffectsOn(p, "noIgnite") && item != null) {
                if (item.getType() == Material.FLINT_AND_STEEL) event.setCancelled(true);
            }
            if (zone.hasEffectsOn(p, "noContainer")) {
                var type = event.getClickedBlock().getType();
                if (type == Material.CHEST || type == Material.ENDER_CHEST || type == Material.FURNACE || type == Material.CRAFTING_TABLE || type == Material.STONECUTTER) {
                    event.setCancelled(true);
                }
            }
        }
        
        if (event.getHand().name().equals("HAND")) {
            ItemStack item = event.getItem();
            
            UUID u = p.getUniqueId();
            if (item != null) {
                if (event.getAction() == Action.RIGHT_CLICK_BLOCK && item.getType() == Material.ARROW) {
                    Block block = event.getClickedBlock();
                    Location location = block.getLocation();
                    if (selectionState.containsKey(u)) {
                        double[][] locations = selectionState.get(u);
                        double[] loc2 = { location.getX(), location.getY(), location.getZ() };
                        locations[1] = loc2;
                        selectionState.replace(u, locations);
                        LogUtil.send(p, "&e已选择第二个区域点 &a(" + location.getX() + ", " + location.getY() + ", "
                                + location.getZ() + ")&e。");
                        LogUtil.send(p, "&e执行 &a/specialzone create <名称>&e 以创建新的区域。");
                    } else {
                        double[] loc1 = { location.getX(), location.getY(), location.getZ() };
                        double[][] locations = { loc1, {} };
                        selectionState.put(u, locations);
                        LogUtil.send(p, "&e已选择第一个区域点 &a(" + location.getX() + ", " + location.getY() + ", "
                                + location.getZ() + ")&e。");
                    }
                } else {
                    if (selectionState.containsKey(u)) {
                        selectionState.remove(u);
                        LogUtil.send(p, "&c已取消区域点的选择。");
                    }
                }
            }
        }
    }
}