package me.stella.Application;

import java.util.Objects;
import java.util.logging.Level;

import me.stella.Minecraft.PlayerListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import me.stella.Bridge.BridgeHook;
import me.stella.Bridge.StorageHook;
import me.stella.Discord.BotImpl;
import me.stella.Discord.BotSetup;
import me.stella.Executor.CommandManager;
import me.stella.Internal.BungeeEcho;
import me.stella.Internal.DataConfig;
import me.stella.Main.PluginImpl;
import me.stella.Minecraft.CommandHyperHook;
import me.stella.Minecraft.PreventHopper;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.scheduler.BukkitRunnable;

public class PluginBoot {
	
	public static final String[] depends = new String[] { "PreventHopper-ORE", "Vault" };
	
	public static String config;
	public static JavaPlugin main;
	
	public static void start(JavaPlugin plugin) {
		main = plugin;
		plugin.saveDefaultConfig();
		Library.config = new DataConfig(plugin.getDataFolder() + "/config.yml");
		config = Library.config.getConfigFile().getAbsolutePath();
		if(Library.config.getBotToken().trim().isEmpty())
			PluginBoot.forceShutdown("no_token");
		if(Library.config.getGuildID().trim().isEmpty())
			PluginBoot.forceShutdown("no_guild");
		for(String dependency: PluginBoot.depends) {
			if(main.getServer().getPluginManager().getPlugin(dependency) == null) {
				PluginBoot.forceShutdown("dependency_error");
				break;
			}
		}
		try {
			Library.preventHopper = new PreventHopper(Objects.requireNonNull(main.getServer().getPluginManager().getPlugin(depends[0])));
		} catch(Exception err) { PluginBoot.forceShutdown("invalid_plugin"); }
		RegisteredServiceProvider<Economy> service = main.getServer().getServicesManager().getRegistration(Economy.class);
        assert service != null;
        Library.economy = service.getProvider();
		Library.bridge = new BridgeHook((long)Library.config.getCodeTimeout()*20);
		Library.storage = new StorageHook();
		Library.manager = new CommandManager();
		BotSetup.buildInternalCommands();
		Library.application = new BotImpl(Library.config.getBotToken());
		PluginImpl.console.log(Level.INFO, "Finished setting up all components on Discord!");
		Objects.requireNonNull(main.getCommand("hyperhook")).setExecutor(new CommandHyperHook());
		main.getServer().getPluginManager().registerEvents(new PlayerListener(), main);
	}
	
	public static void stop() {
		Library.storage.close();
		Library.bridge.close();
		Bukkit.getScheduler().cancelTasks(PluginBoot.main);
		PluginBoot.main.getServer().getMessenger().unregisterIncomingPluginChannel(PluginBoot.main, "BungeeCord");
		PluginBoot.main.getServer().getMessenger().unregisterOutgoingPluginChannel(PluginBoot.main, "BungeeCord");
		BungeeEcho.sendTempPacket("delete", Library.echo.getProxyName());
		(new Thread(() -> {
			try {
				Thread.sleep(5000L);
				Library.application.shutdown();
				PluginImpl.console.log(Level.INFO, PluginImpl.color("&4Discord Application has successfully shutdown!"));
			} catch(Exception err) { err.printStackTrace(); }
		})).start();
	}
	
	public static void forceShutdown(String message) {
		PluginImpl.error(PluginImpl.color(Library.config.getMessage(message)));
		main.getServer().getPluginManager().disablePlugin(main); return;
	}

	public static Plugin random() {
		return Bukkit.getPluginManager().getPlugins()[0];
	}
}
