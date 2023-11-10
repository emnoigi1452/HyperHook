package me.stella.Executor.Implement;

import com.darkevan.PreventHopperOre.Utils.PlayerData;
import me.stella.Application.Library;
import me.stella.Application.PluginBoot;
import me.stella.Discord.ElementBuilder;
import me.stella.Executor.DefaultPerms;
import me.stella.Executor.DiscordExecutor;
import me.stella.Main.PluginImpl;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;

public class CommandView extends DiscordExecutor {

	private static final Map<String, InteractionHook> interactions = new HashMap<>();

	private static final Map<String, ButtonHandler> buttons = new HashMap<>();

	private static final Map<String, BukkitTask> janitor = new HashMap<>();

	public CommandView() {
		super("view", Library.config.getCommand("view"), null, DefaultPerms.PERMS);
	}

	private static void killInteractions(String user) {
		interactions.remove(user);
		ButtonHandler handler = buttons.get(user);
		if(handler != null)
			Library.application.getApplication().removeEventListener(handler);
		BukkitTask task = janitor.get(user);
		if(task != null && !task.isCancelled())
			task.cancel();
		buttons.remove(user); janitor.remove(user);
	}

	private static void buildInteractions(final String user, Player player, PlayerData data, InteractionHook hook) {
		interactions.put(user, hook);
		ButtonHandler handler = new ButtonHandler(user, player, data);
		Library.application.getApplication().addEventListener(handler);
		buttons.put(user, handler);
		janitor.put(user, (new BukkitRunnable() {
			@Override
			public void run() {
				hook.editOriginal("> **Lưu ý:** Bạn không còn có thể tương tác với tin nhắn này!").queue();
				killInteractions(user);
			}
		}).runTaskLaterAsynchronously(PluginBoot.main, Library.config.getButtonExpiration() * 20L));
	}

	@Override
	public void onSlashCommand(final SlashCommandInteractionEvent e) {
		final String author = Objects.requireNonNull(e.getMember()).getId();
		if(!(Library.storage.userConnected(author))) {
			e.reply(Library.config.getMessage("not_connected"))
					.setEphemeral(true).queue();
			return;
		}
		(new BukkitRunnable() {
			@Override
			public void run() {
				try {
					Player player = (Player) Bukkit.getServer().getOfflinePlayer(Library.storage.getClientID(author));
					PlayerData data = Library.preventHopper.getPlayerData(player);
					MessageEmbed embed = ElementBuilder.buildEmbed(player, data);
					String gen = String.valueOf(System.currentTimeMillis());
					killInteractions(author);
					ReplyCallbackAction reply = e.replyEmbeds(embed).setEphemeral(true)
							.addActionRow(
									Button.primary(author + "_normal_" + gen, "Xem dạng thường")
											.withEmoji(Emoji.fromFormatted(Library.config.getEmote("normal"))),
									Button.primary(author + "_blocks_" + gen, "Xem số khối")
											.withEmoji(Emoji.fromFormatted(Library.config.getEmote("blocks"))),
									Button.primary(author + "_money_" + gen, "Xem giá trị")
											.withEmoji(Emoji.fromFormatted(Library.config.getEmote("money"))));
					reply.queue(hook -> {
						try {
							interactions.put(author, hook);
							buildInteractions(author, player, data, hook);
						} catch(Exception err) { err.printStackTrace(); }
					});
				} catch(Exception err) {
					err.printStackTrace();
					e.reply(Library.config.getMessage("error")).queue();
				}
            }
		}).runTaskAsynchronously(PluginBoot.main);
	}

	@Override
	public void onChatCommand(MessageReceivedEvent e) {
		final String author = Objects.requireNonNull(e.getMember()).getId();
		final TextChannel channel = e.getChannel().asTextChannel();
		long id = e.getMessageIdLong();
		if(!(Library.storage.userConnected(author))) {
			channel.sendMessage(Library.config.getMessage("not_connected"))
					.setMessageReference(id).queue();
			return;
		}
		(new BukkitRunnable() {
			@Override
			public void run() {
				try {
					Player player = (Player) Bukkit.getServer().getOfflinePlayer(Library.storage.getClientID(author));
					PlayerData data = Library.preventHopper.getPlayerData(player);
					MessageEmbed embed = ElementBuilder.buildEmbed(player, data);
					channel.sendMessageEmbeds(embed).setMessageReference(id).queue();
				} catch(Exception err) {
					err.printStackTrace();
					channel.sendMessage(Library.config.getMessage("error"))
							.setMessageReference(id).queue();
				}
			}
		}).runTaskAsynchronously(PluginBoot.main);
	}

	private static boolean isButtonExpired(long expiration) {
		return (expiration - System.currentTimeMillis()) > (Library.config.getButtonExpiration() * 1000L);
	}

	private static class ButtonHandler extends ListenerAdapter {

		private final String host;
		private final Player player;
		private final PlayerData data;

		public ButtonHandler(String host, Player player, PlayerData data) {
			this.host = host;
			this.player = player;
			this.data = data;
		}

		public String getHost() {
			return this.host;
		}

		public Player getPlayer() {
			return this.player;
		}

		public PlayerData getData() {
			return this.data;
		}

		@Override
		public void onButtonInteraction(ButtonInteractionEvent e) {
			final String interaction = e.getUser().getId();
			if(interaction.equals(getHost())) {
				final Button button = e.getButton();
				final String id = Objects.requireNonNull(button).getId();
                assert id != null;
                if(isValidButton(id)) {
					String[] compo = id.split("_");
					long expiry = Long.parseLong(compo[2]);
					if(!isButtonExpired(expiry)) {
						String module = compo[1];
						final InteractionHook hook = Objects.requireNonNull(interactions.get(interaction));
						switch(module.toLowerCase()) {
							case "blocks":
								(new BukkitRunnable() {
									@Override
									public void run() {
										hook.editOriginalEmbeds(
												ElementBuilder.buildEmbed(getPlayer(), getData(), ElementBuilder.ViewerLayout.BLOCKS))
												.queue();
										e.reply("> Chế độ hiển thị: **Đếm khối**").setEphemeral(true).queue(message -> {
											(new BukkitRunnable() {
												@Override
												public void run() {
													message.deleteOriginal().queue();
												}
											}).runTaskLaterAsynchronously(PluginBoot.main, 20L);
										});
									}
								}).runTaskAsynchronously(PluginBoot.main); break;
							case "money":
								(new BukkitRunnable() {
									@Override
									public void run() {
										hook.editOriginalEmbeds(
														ElementBuilder.buildEmbed(getPlayer(), getData(), ElementBuilder.ViewerLayout.PRICE))
												.queue();
										e.reply("> Chế độ hiển thị: **Giá trị**").setEphemeral(true).queue(msg -> {
											(new BukkitRunnable() {
												@Override
												public void run() {
													msg.deleteOriginal().queue();
												}
											}).runTaskLaterAsynchronously(PluginBoot.main, 20L);
										});
									}
								}).runTaskAsynchronously(PluginBoot.main); break;
							case "normal":
								(new BukkitRunnable() {
									@Override
									public void run() {
										hook.editOriginalEmbeds(
														ElementBuilder.buildEmbed(getPlayer(), getData(), ElementBuilder.ViewerLayout.NORMAL))
												.queue();
										e.reply("> Chế độ hiển thị: **Thường**").setEphemeral(true).queue(msg -> {
											(new BukkitRunnable() {
												@Override
												public void run() {
													msg.deleteOriginal().queue();
												}
											}).runTaskLaterAsynchronously(PluginBoot.main, 20L);
										});
									}
								}).runTaskAsynchronously(PluginBoot.main); break;
							default:
								e.reply(Library.config.getMessage("error")).queue();
								throw new RuntimeException("What da fuq happened? Key = " + module);
						}
					}
				}
			}
		}

		private boolean isValidButton(String id) {
			return id.contains("_blocks_") || id.contains("_money_") || id.contains("_normal_");
		}

	}

	@Override
	public String buildSuccessReply(Object... params) {
		throw new UnsupportedOperationException("This command does not utilize this function!");
	}

}
