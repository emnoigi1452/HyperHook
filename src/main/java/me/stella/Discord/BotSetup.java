package me.stella.Discord;

import me.stella.Application.Library;
import me.stella.Executor.DiscordExecutor;
import me.stella.Executor.Implement.*;
import me.stella.Internal.BungeeEcho;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Activity.ActivityType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.Messenger;

import java.util.ArrayList;
import java.util.List;

public class BotSetup {
	
	public static Activity buildActivity(String type, String title) {
		ActivityType activityType = null;
		try {
			activityType = ActivityType.valueOf(type);
		} catch(Throwable t) { activityType = ActivityType.PLAYING; }
		return Activity.of(activityType, title);
	}
	
	public static List<String> getWhitelistedChannels() {
		List<String> whitelist = Library.config.getAllowedChannels();
		whitelist.remove("CHANNEL_1"); return whitelist;
	}
	
	public static boolean isLimited() {
		return getWhitelistedChannels().size() > 0;
	}
	
	public static char[] buildCharacterMap(boolean useNormal, boolean useUpcase, boolean useNumber) {
		StringBuilder appender = new StringBuilder();
		if(useNormal) {
			for(int i = 97; i < 123; i++)
				appender.append((char)i);
		}
		if(useUpcase) {
			for(int i = 65; i < 91; i++)
				appender.append((char)i);
		}
		if(useNumber) {
			for(int i = 48; i < 58; i++)
				appender.append((char)i);
		}
		return appender.toString().toCharArray();
	}
	
	public static String[] buildChatCommand(String raw) {
		assert (raw.startsWith(Library.config.getChatPrefix()));
		String[] params = raw.split(" ");
		params[0] = params[0].replace(Library.config.getChatPrefix(), new String());
		return params;
	}

	public static void buildInternalCommands() {
		List<DiscordExecutor> commands = new ArrayList<DiscordExecutor>();
		commands.add(new CommandConnect());
		commands.add(new CommandView());
		commands.add(new CommandNotify());
		commands.add(new CommandEcho());
		commands.add(new CommandSell());
		commands.add(new CommandCraft());
		commands.stream().forEach(command -> {
			Library.manager.putCommands(command.getName(), command);
		});
	}

	public static void handleChannels(JavaPlugin plugin, BungeeEcho echo) {
		Messenger messenger = plugin.getServer().getMessenger();
		if(!(messenger.isOutgoingChannelRegistered(plugin, "BungeeCord")))
			messenger.registerOutgoingPluginChannel(plugin, "BungeeCord");
		if(!(messenger.isIncomingChannelRegistered(plugin, "BungeeCord")))
			messenger.registerIncomingPluginChannel(plugin, "BungeeCord", echo);
	}

}
