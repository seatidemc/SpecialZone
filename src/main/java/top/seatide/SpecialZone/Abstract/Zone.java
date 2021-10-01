package top.seatide.SpecialZone.Abstract;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import top.seatide.SpecialZone.Utils.Files;

public class Zone {
    public String name;
    public double x1, x2, y1, y2, z1, z2;

    public Zone(String name) {
        this.name = name;
        var zone = Files.zones.getConfigurationSection(name);
        this.x1 = zone.getDouble("x1");
        this.x2 = zone.getDouble("x2");
        this.y1 = zone.getDouble("y1");
        this.y2 = zone.getDouble("y2");
        this.z1 = zone.getDouble("z1");
        this.z2 = zone.getDouble("z2");
    }

    public Zone(String name, double[] loc1, double[] loc2) {
        this.name = name;
        this.x1 = loc1[0];
        this.x2 = loc2[0];
        this.y1 = loc1[1];
        this.y2 = loc2[1];
        this.z1 = loc1[2];
        this.z2 = loc2[2];
    }

    public void create() {
        var zones = Files.zones;
        zones.set(this.name + ".x1", this.x1);
        zones.set(this.name + ".x2", this.x2);
        zones.set(this.name + ".y1", this.y1);
        zones.set(this.name + ".y2", this.y2);
        zones.set(this.name + ".z1", this.z1);
        zones.set(this.name + ".z2", this.z2);
        Files.save(zones, "zones.yml");
    }

    public void setProperty(String name, boolean value) {
        var zones = Files.zones;
        zones.set(this.name + ".properties." + name, value);
        Files.save(zones, "zones.yml");
    }

    public boolean getProperty(String name) {
        return Files.zones.getBoolean(this.name + ".properties." + name);
    }

    public boolean setException(String property, String name) {
        var zones = Files.zones;
        var origin = zones.getStringList(this.name + ".exceptions." + property);
        if (origin.contains(name)) {
            return false;
        }
        origin.add(name);
        zones.set(this.name + ".exceptions." + property, origin);
        Files.save(zones, "zones.yml");
        return true;
    }

    public boolean delException(String property, String name) {
        var zones = Files.zones;
        var origin = zones.getStringList(this.name + ".exceptions." + property);
        if (!origin.contains(name)) {
            return false;
        }
        origin.remove(name);
        zones.set(this.name + ".exceptions." + property, origin);
        Files.save(zones, "zones.yml");
        return true;
    }

    public List<String> getExceptions(String property) {
        return Files.zones.getStringList(this.name + ".exceptions." + property);
    }

    public boolean isInZone(Player p) {
        return isLocationInZone(this.name, p.getLocation());
    }

    public boolean hasEffectsOn(Player p, String property) {
        return getProperty(property) && !getExceptions(property).contains(p.getName());
    }

    public static boolean isLocationInZone(String zone, Location loc) {
        var zones = Files.zones.getConfigurationSection(zone);
        double x = loc.getX(), y = loc.getY(), z = loc.getZ(), x1 = zones.getDouble("x1"), x2 = zones.getDouble("x2"),
                y1 = zones.getDouble("y1"), y2 = zones.getDouble("y2"), z1 = zones.getDouble("z1"),
                z2 = zones.getDouble("z2");
        boolean ignoreY = zones.getBoolean("properties.ignoreY");
        return (x1 - x) * (x2 - x) < 0 && (ignoreY ? true : (y1 - y) * (y2 - y) < 0) && (z1 - z) * (z2 - z) < 0;
    }

    public static boolean isLocationInZoneGlobal(Location loc) {
        var keys = Files.zones.getKeys(false);
        for (var key : keys) {
            return isLocationInZone(key, loc);
        }
        return false;
    }

    public static String getLocationInZone(Location loc) {
        var keys = Files.zones.getKeys(false);
        for (var key : keys) {
            if (isLocationInZone(key, loc)) {
                return key;
            }
        }
        return null;
    }

    public static void delete(String zoneName) {
        var zone = Files.zones;
        zone.set(zoneName, null);
        Files.save(zone, "zones.yml");
    }

    public static boolean contains(String zoneName) {
        return Files.zones.getKeys(false).contains(zoneName);
    }
}
