package me.stella.Minecraft;

import java.util.Arrays;
import java.util.UUID;

import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;

import com.darkevan.PreventHopperOre.Utils.PlayerData;
import com.darkevan.PreventHopperOre.Variables.GlobalVars;

import me.stella.Application.PluginBoot;

public class PreventHopper {
	
	public static final String[] keys = new String[]{
			"COAL",
			"LAPIS_LAZULI",
			"REDSTONE",
			"IRON_INGOT",
			"GOLD_INGOT",
			"DIAMOND",
			"EMERALD",
			"STONE"
	};

	public static final String[] input = new String[] {
			"COAL",
			"LAPIS",
			"REDSTONE",
			"IRON",
			"GOLD",
			"DIAMOND",
			"EMERALD",
			"STONE"
	};
	
	public static final String[] blockKeys = new String[]{
			"COAL_BLOCK",
			"LAPIS_BLOCK",
			"REDSTONE_BLOCK",
			"IRON_BLOCK",
			"GOLD_BLOCK",
			"DIAMOND_BLOCK",
			"EMERALD_BLOCK"
	};

	public static String fromKey(String key, boolean b) {
		switch(key.toUpperCase()) {
			case "COAL": return b ? blockKeys[0] : keys[0];
			case "LAPIS": return b ? blockKeys[1] : keys[1];
			case "REDSTONE": return b ? blockKeys[2] : keys[2];
			case "IRON": return b ? blockKeys[3] : keys[3];
			case "GOLD": return b ? blockKeys[4] : keys[4];
			case "DIAMOND": return b ? blockKeys[5] : keys[5];
			case "EMERALD": return b ? blockKeys[6] : keys[6];
			case "STONE": return keys[7];
		}
		return "AIR";
	}

	public static String toKey(String name, boolean b) {
		if(name == null || name.length() < 1)
			return null;
		return !b ? name.split("_")[0].toUpperCase() : name.toUpperCase();
	}
	
	private Plugin plugin;
	
	public PreventHopper(Plugin plugin) throws Exception {
		PluginDescriptionFile file = plugin.getDescription();
		if(!assertMainClass(file) || !assertName(file))
			throw (new IllegalStateException("Invalid version of the plugin \'PreventHopper-ORE\'!"));
		this.plugin = plugin;
		try {
			ClassLoader loader = plugin.getClass().getClassLoader();
			Class.forName("com.darkevan.PreventHopperOre.Main.mainClass", true, loader);
		} catch(Exception err) { err.printStackTrace(); }
	}
	
	public Plugin getPlugin() {
		return this.plugin;
	}
	
	private boolean assertMainClass(PluginDescriptionFile description) {
		return description.getMain().equals("com.darkevan.PreventHopperOre.Main.mainClass");
	}
	
	private boolean assertName(PluginDescriptionFile description) {
		return description.getName().equals(PluginBoot.depends[0]);
	}
	
	public PlayerData getPlayerData(Player player) {
		if(player == null)
			return null;
		return ((PlayerData)player.getMetadata("playerData").get(0).value());
	}
	
	public PlayerData getPlayerData(UUID uid) {
		OfflinePlayer player = PluginBoot.main.getServer().getOfflinePlayer(uid);
		if(!(player.isOnline()))
			return null;
		return getPlayerData((Player) player);
	}
	
	public boolean isStorageActive(Player player) {
		return isStorageActive(player.getUniqueId());
	}
	
	public boolean isStorageActive(UUID uid) {
		return GlobalVars.getTogglePlayer().contains(uid);
	}
	
	public double getPrice(String type) {
		if(!(Arrays.asList(type).contains(type)))
			return 0.0D;
		FileConfiguration config = getPlugin().getConfig();
		return config.getDouble("price." + type);
	}

}
