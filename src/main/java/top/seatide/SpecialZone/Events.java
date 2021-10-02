package top.seatide.SpecialZone;

import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
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
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class Events implements Listener {
    public static Map<UUID, double[][]> selectionState;

    public Events() {
        selectionState = new HashMap<>();
    }

    public boolean isPlayer(Entity ent) {
        return ent.getType() == EntityType.PLAYER;
    }

    public boolean isInteractiveBlock(Material type) {
        Material[] match = {
            Material.CHEST,
            Material.ENDER_CHEST,
            Material.FURNACE,
            Material.CRAFTING_TABLE,
            Material.STONECUTTER,
            Material.DISPENSER,
            Material.SMITHING_TABLE,
            Material.LOOM,
            Material.CARTOGRAPHY_TABLE,
            Material.SMOKER,
            Material.BLAST_FURNACE,
            Material.COMPOSTER,
            Material.BARREL,
            Material.FLETCHING_TABLE,
            Material.ENCHANTING_TABLE
        };
        return List.of(match).contains(type);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.getPlayer().hasPermission("specialzone.exception"))
            return;
        if (event.getPlayer().getGameMode() == GameMode.CREATIVE)
            return;
        var zoneName = Zone.getLocationInZone(event.getBlock().getLocation());
        if (zoneName != null) {
            var zone = new Zone(zoneName);
            if (zone.hasEffectsOn(event.getPlayer(), "noBreak"))
                event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (event.getEntity().hasPermission("specialzone.exception"))
            return;
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
        if (event.getPlayer().hasPermission("specialzone.exception"))
            return;
        if (event.getPlayer().getGameMode() == GameMode.CREATIVE)
            return;
        Player p = event.getPlayer();
        var zoneName = Zone.getLocationInZone(event.getBlock().getLocation());
        if (zoneName != null) {
            var zone = new Zone(zoneName);
            if (zone.hasEffectsOn(p, "noPlace"))
                event.setCancelled(true);
        }
    }

    @EventHandler
    public void onHangingBreak(HangingBreakByEntityEvent event) {
        var p = event.getRemover();
        if (isPlayer(p)) {
            if (((Player) p).getGameMode() == GameMode.CREATIVE)
                return;
            if (p.hasPermission("specialzone.exception"))
                return;
        }
        var hang = event.getEntity();
        var zoneName = Zone.getLocationInZone(hang.getLocation());
        if (zoneName != null) {
            var zone = new Zone(zoneName);
            if (isPlayer(p)) {
                if (zone.hasEffectsOn((Player) p, "noBreak")) {
                    event.setCancelled(true);
                }
            } else {
                if (zone.hasEffects("noBreak")) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        var p = event.getPlayer();
        var clickedBlock = event.getClickedBlock();
        if (clickedBlock == null)
            return;
        var zoneName = Zone.getLocationInZone(clickedBlock.getLocation());
        if (zoneName != null) {
            if (p.hasPermission("specialzone.exception"))
                return;
            if (p.getGameMode() == GameMode.CREATIVE)
                return;
            var item = event.getItem();
            var zone = new Zone(zoneName);
            if (zone.hasEffectsOn(p, "noIgnite") && item != null) {
                if (item.getType() == Material.FLINT_AND_STEEL)
                    event.setCancelled(true);
            }
            if (zone.hasEffectsOn(p, "noContainer")) {
                var clickedType = clickedBlock.getType();
                if (isInteractiveBlock(clickedType)) {
                    event.setCancelled(true);
                }
            }
            if (zone.hasEffectsOn(p, "noPlace") && item != null) {
                var clickedType = clickedBlock.getType();
                if (isInteractiveBlock(clickedType)) {
                    if (event.getPlayer().isSneaking() && event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                        event.setCancelled(true);
                    }
                } else {
                    var itemType = item.getType();
                    // TODO: paintings, item_frames, etc in mod.
                    if ((itemType == Material.PAINTING || itemType == Material.ITEM_FRAME)
                            && event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                        event.setCancelled(true);
                    }
                }
            }
        }

        if (event.getHand().name().equals("HAND")) {
            ItemStack item = event.getItem();
            UUID u = p.getUniqueId();
            if (item != null) {
                if (event.getAction() == Action.RIGHT_CLICK_BLOCK && item.getType() == Material.ARROW) {
                    if (!p.hasPermission("specialzone.select"))
                        return;
                    Location location = clickedBlock.getLocation();
                    if (Zone.isLocationInZoneGlobal(location)) {
                        LogUtil.send(p, "&c不能在已存在的区域内划定新区域。");
                        selectionState.remove(u);
                        return;
                    }
                    if (selectionState.containsKey(u)) {
                        double[][] locations = selectionState.get(u);
                        double[] loc2 = { location.getX(), location.getY(), location.getZ() };
                        locations[1] = loc2;
                        selectionState.replace(u, locations);
                        LogUtil.send(p, "&e已选择第二个区域点 &a(" + location.getX() + ", " + location.getY() + ", "
                                + location.getZ() + ")&e。");
                        LogUtil.send(p, "&e执行 &a/specialzone create <名称>&e 以创建新的区域。");
                        LogUtil.send(p, "&8&o左键单击可取消选择。");
                    } else {
                        double[] loc1 = { location.getX(), location.getY(), location.getZ() };
                        double[][] locations = { loc1, {} };
                        selectionState.put(u, locations);
                        LogUtil.send(p, "&e已选择第一个区域点 &a(" + location.getX() + ", " + location.getY() + ", "
                                + location.getZ() + ")&e。");
                        LogUtil.send(p, "&8&o左键单击可取消选择。");
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