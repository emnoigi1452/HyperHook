package me.stella.Executor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import me.stella.Application.Library;
import me.stella.Application.PluginBoot;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.bukkit.scheduler.BukkitRunnable;

public class CommandManager {
	
	private Map<String, DiscordExecutor> commands;
	
	public CommandManager() {
		this.commands = new HashMap<String, DiscordExecutor>();
	}
	
	public void close() {
		this.commands.clear();
	}
	
	public synchronized void putCommands(String name, DiscordExecutor cmd) {
		this.commands.put(name, cmd);
	}
	
	public DiscordExecutor getCommand(String name) {
		return this.commands.getOrDefault(name, null);
	}

	public static String processHeader(String message) {
		return message.replace(Library.config.getChatPrefix(), "");
	}
	
	public List<DiscordExecutor> executors() {
		return new ArrayList<DiscordExecutor>(this.commands.values());
	}
	
	public boolean hasCommands(String name) {
		return this.commands.keySet().contains(name);
	}
	
	public Set<String> getNames() {
		return this.commands.keySet();
	}

}
