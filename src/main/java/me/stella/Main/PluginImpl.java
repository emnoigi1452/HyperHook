package me.stella.Main;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.plugin.java.JavaPlugin;

import me.stella.Application.PluginBoot;
import net.md_5.bungee.api.ChatColor;

public class PluginImpl extends JavaPlugin {
	
	public static Logger console = Logger.getLogger("Minecraft");
	
	@Override
	public void onEnable() {
		PluginBoot.start(this);
	}
	
	@Override
	public void onDisable() {
		PluginBoot.stop();
	}
	
	public static void info(String message) {
		console.log(Level.INFO, message);
	}
	
	public static void warn(String message) {
		console.log(Level.WARNING, message);
	}
	
	public static void error(String message) {
		console.log(Level.SEVERE, message);
	}
	
	public static String color(String message) {
		return ChatColor.translateAlternateColorCodes('&', message);
	}

}
