package me.stella.Discord;

import me.stella.Application.Library;
import me.stella.Application.PluginBoot;
import me.stella.Internal.BungeeEcho;
import me.stella.Main.PluginImpl;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.logging.Level;

public class BotImpl {

	private String token;
	private JDA jda;
	private Guild guild;
	
	public BotImpl(String token) {
		this.token = token;
		run();
		this.jda = null;
		this.guild = null;
	}
	
	public void run() {
		new Thread(() -> {
			JDABuilder builder = JDABuilder.createDefault(token);
			builder.enableIntents(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_PRESENCES);
			builder.setActivity(BotSetup.buildActivity(Library.config.getActivityType(), Library.config.getActivityTitle()));
			List<Object> listeners = new ArrayList<Object>(); listeners.add(new GuildListener());
			if(Library.config.useSlashCommands())
				listeners.add(new SlashListener());
			builder.addEventListeners(listeners.toArray());
			try {
				JDA buildUnready = builder.build();
				this.jda = buildUnready.awaitReady();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			Library.echo = new BungeeEcho(Library.config.getServerName());
			BotSetup.handleChannels(PluginBoot.main, Library.echo);
			BungeeEcho.sendTempPacket("write", Library.echo.getProxyName());
			assert (this.guild != null);
		}).start();
	}

	public void initGuild(@NotNull Guild guild) {
		this.guild = guild;
		(new BukkitRunnable() {
			@Override
			public void run() {
				guild.loadMembers().onError(Throwable::printStackTrace)
						.onSuccess(list -> {
							PluginImpl.console.log(Level.INFO, "Loaded " + list.size() + " members of guild to cache!");
						});
			}
		}).runTaskAsynchronously(PluginBoot.main);
	}
	
	public void shutdown() {
		new Thread(() -> {
			try {
				this.jda.shutdown();
			} catch(Exception e) { e.printStackTrace(); }
		}).start();
	}

	public CompletableFuture<Member> user(String id) {
		return user(Long.parseLong(id.trim()));
	}

	public CompletableFuture<Member> user(long id) {
		return getGuild().retrieveMemberById(id).submit();
	}
	
	public void sendPrivateMessage(String discordUserID, String message) {
		(new BukkitRunnable() {
			@Override
			public void run() {
				user(discordUserID).thenAccept(member -> {
					if(member == null)
						return;
					final User userFromID = member.getUser();
					try {
						(userFromID.openPrivateChannel().submit())
								.thenAccept(channel -> {
									try {
										assert (channel != null);
										channel.sendMessage(message).queue();
									} catch(Exception err) { err.printStackTrace(); }
								});
					} catch(Exception err2) { err2.printStackTrace(); }
				});
			}
		}).runTaskAsynchronously(PluginBoot.main);
	}

	public void sendPrivateMessageOnThread(String discordUserID, String message) {
		user(discordUserID).thenAccept(member -> {
			if(member == null)
				return;
			final User userFromID = member.getUser();
			try {
				(userFromID.openPrivateChannel().submit())
						.thenAccept(channel -> {
							try {
								assert (channel != null);
								channel.sendMessage(message).queue();
							} catch(Exception err) { err.printStackTrace(); }
						});
			} catch(Exception err2) { err2.printStackTrace(); }
		});
	}

	public void sendPrivateMessageOnThread(String discordUserID, Collection<? extends MessageEmbed> embeds, Consumer<Message> consumer) {
		user(discordUserID).thenAccept(member -> {
			if(member == null)
				return;
			final User userFromID = member.getUser();
			try {
				(userFromID.openPrivateChannel().submit())
						.thenAccept(channel -> {
							try {
								assert (channel != null);
								channel.sendMessageEmbeds(embeds).queue(consumer);
							} catch(Exception err) { err.printStackTrace(); }
						});
			} catch(Exception err2) { err2.printStackTrace(); }
		});
	}

	public void sendPrivateMessage(String discordUserID, Collection<? extends MessageEmbed> embeds, Consumer<Message> consumer) {
		(new BukkitRunnable() {
			@Override
			public void run() {
				user(discordUserID).thenAccept(member -> {
					if(member == null)
						return;
					final User userFromID = member.getUser();
					try {
						(userFromID.openPrivateChannel().submit())
								.thenAccept(channel -> {
									try {
										assert (channel != null);
										channel.sendMessageEmbeds(embeds).queue(consumer);
									} catch(Exception err) { err.printStackTrace(); }
								});
					} catch(Exception err2) { err2.printStackTrace(); }
				});
			}
		}).runTaskAsynchronously(PluginBoot.main);
	}
	public Guild getGuild() {
		return this.guild;
	}
	
	public JDA getApplication() {
		return this.jda;
	}
	
	public String getToken() {
		return this.token;
	}

}
