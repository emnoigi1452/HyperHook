package me.stella.Discord;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import me.stella.Main.PluginImpl;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitRunnable;

import me.stella.Application.Library;
import me.stella.Application.PluginBoot;
import me.stella.Executor.DiscordExecutor;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class GuildListener extends ListenerAdapter {
	
	@Override
	public void onMessageReceived(final MessageReceivedEvent e) {
		if(!(e.getChannel().getType().equals(ChannelType.TEXT)))
			return;
		try {
			Guild guild = e.getGuild();
			if(guild.getId().equalsIgnoreCase(Library.config.getGuildID())) {
				MessageChannel channel = e.getChannel();
				if(BotSetup.isLimited()) {
					if(!BotSetup.getWhitelistedChannels().contains(channel.getId()))
						return;
				}
				final String message = e.getMessage().getContentRaw().toLowerCase();
				if(message.startsWith(Library.config.getChatPrefix())) {
					(new BukkitRunnable() {
						public void run() {
							String[] nodes = BotSetup.buildChatCommand(message);
							DiscordExecutor command = Library.manager.getCommand(nodes[0]);
							command.onChatCommand(e);
						}
					}).runTaskAsynchronously(PluginBoot.main);
				}
			}
		} catch(Exception err) { err.printStackTrace(); }
	}

	@Override
	public void onGuildMemberRemove(GuildMemberRemoveEvent e) {
		final Guild guild = e.getGuild();
		if(guild.getId().equals(Library.config.getGuildID())) {
			(new BukkitRunnable() {
				@Override
				public void run() {
					guild.unloadMember(e.getUser().getIdLong());
				}
			}).runTaskAsynchronously(PluginBoot.main);
		}
	}

	@Override
	public void onGuildMemberJoin(GuildMemberJoinEvent e) {
		final Guild guild = e.getGuild();
		if(guild.getId().equals(Library.config.getGuildID())) {
			(new BukkitRunnable() {
				@Override
				public void run() {
					e.getUser();
				}
			}).runTaskAsynchronously(PluginBoot.main);
		}
	}
	
	@Override
	public void onGuildReady(GuildReadyEvent e) {
		final Guild guild = e.getGuild();
		if(guild.getId().equals(Library.config.getGuildID())) {
			PluginImpl.console.log(Level.INFO, ChatColor.GOLD + "Guild with ID = " + e.getGuild().getId() + " is ready!");
			Library.application.initGuild(guild);
			(new BukkitRunnable() {
				@Override
				public void run() {
					try {
						if(Library.config.useSlashCommands()) {
							PluginImpl.console.log(Level.INFO, ChatColor.GREEN + "Attempting slash injection... Setting up stuff <3 ...");
							final List<SlashCommandData> slashes = new ArrayList<>();
							for(DiscordExecutor cmd: Library.manager.executors()) {
								SlashCommandData data = Commands.slash(cmd.getName(), cmd.getDescription());
								List<DiscordExecutor.CompactParams> params = cmd.getParameters();
								if(params != null) {
									for(DiscordExecutor.CompactParams option: params)
										data.addOption(OptionType.STRING, option.getName(), option.getDescription(), option.isRequired(), option.canAutoComplete());
								}
								data.setDefaultPermissions(DefaultMemberPermissions.enabledFor(cmd.getPermissions()));
								data.setGuildOnly(true); slashes.add(data);
							}
							PluginImpl.console.log(Level.INFO, "Attempting to inject " + slashes.size() + " slash commands.");
							guild.updateCommands()
									.addCommands(slashes).queue(e -> {
										PluginImpl.console.log(Level.INFO, "Injected " + slashes.size() + " command! Pool size = " + e.size());
									});
						}
					} catch(Exception err) {
						PluginImpl.console.log(Level.INFO, ChatColor.RED + "Error encountered! " + err.toString());
						err.printStackTrace();
					}
				}
			}).runTaskLaterAsynchronously(PluginBoot.main, 20L);
		}
	}

}
