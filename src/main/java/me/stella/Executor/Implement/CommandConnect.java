package me.stella.Executor.Implement;

import me.stella.Application.Library;
import me.stella.Application.PluginBoot;
import me.stella.Bridge.BridgeHook;
import me.stella.Discord.BotSetup;
import me.stella.Executor.DefaultPerms;
import me.stella.Executor.DiscordExecutor;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class CommandConnect extends DiscordExecutor {

	public CommandConnect() {
		super("connect", Library.config.getCommand("connect"), null, DefaultPerms.PERMS);
	}

	@Override
	public void onSlashCommand(SlashCommandInteractionEvent e) {
		final String author = e.getMember().getId();
		(new BukkitRunnable() {
			@Override
			public void run() {
				try {
					final String code = generate();
					Library.bridge.write(code, author);
					final String reply = buildSuccessReply(code, parseExpiration());
					e.reply(reply).setEphemeral(true).queue(hook -> {
						(new BukkitRunnable() {
							@Override
							public void run() {
								if(!Library.bridge.keyExisted(code))
									return;
								Library.bridge.delete(code);
								hook.editOriginal(reply.concat("\n> **Lưu ý:** Mã kết nối này đã hết hạn! :x:"))
										.queue();
							}
						}).runTaskLaterAsynchronously(PluginBoot.main, Library.config.getCodeTimeout() * 20L);
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
		final MessageChannel channel = e.getChannel();
		final Message message = e.getMessage();
		final String author = e.getMember().getId();
		(new BukkitRunnable() {
			@Override
			public void run() {
				try {
					final String code = generate();
					Library.bridge.write(code, author);
					String reply = buildSuccessReply(code, parseExpiration());
					channel.sendMessage(reply).setMessageReference(message)
							.queue(message -> {
								(new BukkitRunnable() {
									@Override
									public void run() {
										if(!Library.bridge.keyExisted(code))
											return;
										Library.bridge.delete(code);
										message.editMessage(reply.concat("\n> **Lưu ý:** Mã kết nối này đã hết hạn! :x:"))
												.queue();
									}
								}).runTaskLaterAsynchronously(PluginBoot.main, Library.config.getCodeTimeout() * 20L);
							});
				} catch(Exception err) {
					err.printStackTrace();
					channel.sendMessage(Library.config.getMessage("error")).setMessageReference(message).queue();
				}
			}
		}).runTaskAsynchronously(PluginBoot.main);
	}

	@Override
	public String buildSuccessReply(Object... params) {
		return Library.config.getMessage("code")
				.replace("{code}", String.valueOf(params[0]))
				.replace("{expire}", String.valueOf(params[1]));
	}
	
	private String generate() {
		char[] mapping = BotSetup.buildCharacterMap(
				Library.config.useNormalCharacters(),
				Library.config.useUpcaseCharacters(),
				Library.config.useNumbers());
		return BridgeHook.generateKey(Library.config.getCodeLength(), mapping);
	}
	
	private String parseExpiration() {
		int len = Library.config.getCodeTimeout();
		StringBuilder timeBuilder = new StringBuilder();
		int m = len / 60; String ms = String.valueOf(m);
		if(m < 10)
			ms = "0" + ms;
		timeBuilder.append(ms).append("m");
		int s = len % 60; String ss = String.valueOf(s);
		if(s < 10)
			ss = "0" + ss;
		timeBuilder.append(ss).append("s");
		return timeBuilder.toString();
	}

}
