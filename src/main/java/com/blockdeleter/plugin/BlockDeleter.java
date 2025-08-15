package com.blockdeleter.plugin;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class BlockDeleter extends JavaPlugin implements Listener {

    private final Map<UUID, RegionInfo> playerRegions = new HashMap<>();
    private final Map<UUID, Set<BlockLocation>> trackedBlocks = new HashMap<>();
    private WorldEditPlugin worldEditPlugin;
    private FileConfiguration config;
    private File definesFile;
    private FileConfiguration definesConfig;
    private int deleteDelay;
    
    @Override
    public void onEnable() {
        saveDefaultConfig();
        config = getConfig();
        
        loadDefinesFile();
        
        deleteDelay = config.getInt("delete-delay", 300);
        
        getServer().getPluginManager().registerEvents(this, this);
        
        worldEditPlugin = (WorldEditPlugin) getServer().getPluginManager().getPlugin("WorldEdit");
        if (worldEditPlugin == null) {
            getLogger().severe("WorldEdit plugin not found! Disabling BlockDeleter...");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        
        loadRegions();
        
        getLogger().info("BlockDeleter plugin has been enabled!");
    }

    @Override
    public void onDisable() {
        saveRegions();
        getLogger().info("BlockDeleter plugin has been disabled!");
    }
    
    private void loadDefinesFile() {
        definesFile = new File(getDataFolder(), "defines.yml");
        if (!definesFile.exists()) {
            saveResource("defines.yml", false);
        }
        definesConfig = YamlConfiguration.loadConfiguration(definesFile);
    }
    
    private void saveDefinesFile() {
        try {
            definesConfig.save(definesFile);
        } catch (IOException e) {
            getLogger().severe("Could not save defines.yml: " + e.getMessage());
        }
    }
    
    private void loadRegions() {
        ConfigurationSection regionsSection = definesConfig.getConfigurationSection("regions");
        if (regionsSection == null) {
            return;
        }
        
        for (String regionId : regionsSection.getKeys(false)) {
            ConfigurationSection regionSection = regionsSection.getConfigurationSection(regionId);
            if (regionSection == null) continue;
            
            String worldName = regionSection.getString("world");
            UUID ownerUUID = UUID.fromString(regionSection.getString("owner"));
            List<Integer> min = regionSection.getIntegerList("min");
            List<Integer> max = regionSection.getIntegerList("max");
            
            if (worldName == null || min.size() != 3 || max.size() != 3) {
                getLogger().warning("Invalid region data for region: " + regionId);
                continue;
            }
            
            RegionInfo regionInfo = new RegionInfo(worldName, min.get(0), min.get(1), min.get(2), max.get(0), max.get(1), max.get(2));
            playerRegions.put(ownerUUID, regionInfo);
            
            if (!trackedBlocks.containsKey(ownerUUID)) {
                trackedBlocks.put(ownerUUID, new HashSet<>());
            }
        }
    }
    
    private void saveRegions() {
        definesConfig.set("regions", null);
        
        int regionCounter = 0;
        for (Map.Entry<UUID, RegionInfo> entry : playerRegions.entrySet()) {
            UUID ownerUUID = entry.getKey();
            RegionInfo regionInfo = entry.getValue();
            
            String regionId = "region_" + regionCounter++;
            definesConfig.set("regions." + regionId + ".world", regionInfo.worldName);
            definesConfig.set("regions." + regionId + ".owner", ownerUUID.toString());
            
            List<Integer> min = Arrays.asList(regionInfo.minX, regionInfo.minY, regionInfo.minZ);
            List<Integer> max = Arrays.asList(regionInfo.maxX, regionInfo.maxY, regionInfo.maxZ);
            definesConfig.set("regions." + regionId + ".min", min);
            definesConfig.set("regions." + regionId + ".max", max);
            definesConfig.set("regions." + regionId + ".created", System.currentTimeMillis());
        }
        
        saveDefinesFile();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(config.getString("messages.player-only", "§cThis command can only be used by players."));
            return true;
        }

        Player player = (Player) sender;

        if (!command.getName().equalsIgnoreCase("blockdeleter")) {
            return false;
        }

        if (args.length == 0) {
            player.sendMessage(config.getString("messages.command-usage", "§cUsage: /blockdeleter define"));
            return true;
        }

        if (args[0].equalsIgnoreCase("define")) {
            defineRegion(player);
            return true;
        }

        return false;
    }

    private void defineRegion(Player player) {
        try {
            Region region = worldEditPlugin.getSession(player).getSelection(BukkitAdapter.adapt(player.getWorld()));
            
            if (region == null) {
                player.sendMessage(config.getString("messages.no-selection", "§cYou must make a WorldEdit selection first!"));
                return;
            }
            
            BlockVector3 min = region.getMinimumPoint();
            BlockVector3 max = region.getMaximumPoint();
            
            UUID playerUUID = player.getUniqueId();
            RegionInfo regionInfo = new RegionInfo(
                player.getWorld().getName(),
                min.getBlockX(), min.getBlockY(), min.getBlockZ(),
                max.getBlockX(), max.getBlockY(), max.getBlockZ()
            );
            playerRegions.put(playerUUID, regionInfo);
            
            if (!trackedBlocks.containsKey(playerUUID)) {
                trackedBlocks.put(playerUUID, new HashSet<>());
            } else {
                trackedBlocks.get(playerUUID).clear();
            }
            
            saveRegions();
            
            String message = config.getString("messages.region-defined", "§aRegion defined successfully! Blocks placed in this region will be deleted after %time% seconds.");
            message = message.replace("%time%", String.valueOf(deleteDelay));
            player.sendMessage(message);
            
        } catch (Exception e) {
            String errorMsg = config.getString("messages.region-error", "§cError defining region: %error%");
            errorMsg = errorMsg.replace("%error%", e.getMessage());
            player.sendMessage(errorMsg);
            getLogger().warning("Error defining region: " + e.getMessage());
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();
        
        if (!playerRegions.containsKey(playerUUID)) {
            return;
        }
        
        RegionInfo regionInfo = playerRegions.get(playerUUID);
        Block placedBlock = event.getBlock();
        
        if (isInRegion(placedBlock, regionInfo)) {
            BlockLocation blockLocation = new BlockLocation(
                    placedBlock.getWorld().getName(),
                    placedBlock.getX(),
                    placedBlock.getY(),
                    placedBlock.getZ()
            );
            
            trackedBlocks.get(playerUUID).add(blockLocation);
            
            Bukkit.getScheduler().runTaskLater(this, () -> {
                if (trackedBlocks.containsKey(playerUUID) && 
                    trackedBlocks.get(playerUUID).contains(blockLocation)) {
                    
                    World world = Bukkit.getWorld(blockLocation.worldName);
                    if (world != null) {
                        Block blockToDelete = world.getBlockAt(blockLocation.x, blockLocation.y, blockLocation.z);
                        blockToDelete.setType(Material.AIR);
                        
                        trackedBlocks.get(playerUUID).remove(blockLocation);
                    }
                }
            }, 20L * deleteDelay);
        }
    }

    private boolean isInRegion(Block block, RegionInfo regionInfo) {
        if (!block.getWorld().getName().equals(regionInfo.worldName)) {
            return false;
        }
        
        int x = block.getX();
        int y = block.getY();
        int z = block.getZ();
        
        return x >= regionInfo.minX && x <= regionInfo.maxX &&
               y >= regionInfo.minY && y <= regionInfo.maxY &&
               z >= regionInfo.minZ && z <= regionInfo.maxZ;
    }

    private static class RegionInfo {
        private final String worldName;
        private final int minX, minY, minZ;
        private final int maxX, maxY, maxZ;
        private Region region;

        public RegionInfo(Region region, String worldName) {
            this.region = region;
            this.worldName = worldName;
            
            BlockVector3 min = region.getMinimumPoint();
            BlockVector3 max = region.getMaximumPoint();
            
            this.minX = min.getBlockX();
            this.minY = min.getBlockY();
            this.minZ = min.getBlockZ();
            this.maxX = max.getBlockX();
            this.maxY = max.getBlockY();
            this.maxZ = max.getBlockZ();
        }
        
        public RegionInfo(String worldName, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
            this.worldName = worldName;
            this.minX = minX;
            this.minY = minY;
            this.minZ = minZ;
            this.maxX = maxX;
            this.maxY = maxY;
            this.maxZ = maxZ;
            this.region = null;
        }
    }

    private static class BlockLocation {
        private final String worldName;
        private final int x;
        private final int y;
        private final int z;

        public BlockLocation(String worldName, int x, int y, int z) {
            this.worldName = worldName;
            this.x = x;
            this.y = y;
            this.z = z;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            BlockLocation that = (BlockLocation) o;
            return x == that.x && y == that.y && z == that.z && worldName.equals(that.worldName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(worldName, x, y, z);
        }
    }
}
