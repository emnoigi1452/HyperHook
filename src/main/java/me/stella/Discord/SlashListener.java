package me.stella.Discord;

import me.stella.Application.Library;
import me.stella.Application.PluginBoot;
import me.stella.Executor.Implement.CommandNotify;
import me.stella.Minecraft.PreventHopper;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SlashListener extends ListenerAdapter {
	
	@Override
	public void onSlashCommandInteraction(final SlashCommandInteractionEvent e) {
		if(!Library.config.useSlashCommands())
			return;
		if(!(e.getChannel().getType().equals(ChannelType.TEXT)) || !(verifyInteraction(e)))
			return;
		MessageChannel channel = e.getChannel();
		if(BotSetup.isLimited()) {
			if(!BotSetup.getWhitelistedChannels().contains(channel.getId())) {
				e.reply(Library.config.getMessage("not_permitted"))
						.setEphemeral(true).queue();
			}
		}
		try {
			if(Library.manager.hasCommands(e.getName()))
				(new BukkitRunnable() {
					public void run() {
						Library.manager.getCommand(e.getName()).onSlashCommand(e);
					}
				}).runTaskAsynchronously(PluginBoot.main);
		} catch(Exception err) { err.printStackTrace(); }
	}
	
	@Override
	public void onCommandAutoCompleteInteraction(@NotNull final CommandAutoCompleteInteractionEvent e) {
		(new BukkitRunnable() {
			@Override
			public void run() {
				try {
					switch(e.getName().trim().toLowerCase()) {
						case "notify":
							if(e.getFocusedOption().getName().equals("duration")) {
								List<Command.Choice> choices = CommandNotify.TIME.entrySet().stream()
										.filter(time -> time.getKey().startsWith(e.getFocusedOption().getValue()))
										.sorted(Map.Entry.comparingByValue())
										.map(time -> new Command.Choice(ElementBuilder.parseTimeName(time.getKey()), time.getKey()))
										.collect(Collectors.toList());
								e.replyChoices(choices).queue();
							}
							break;
						case "sell":
							if(e.getFocusedOption().getName().equals("type")) {
								List<Command.Choice> choices = Stream.of(PreventHopper.input)
										.filter(type -> type.startsWith(e.getFocusedOption().getValue()))
										.map(type -> new Command.Choice(Library.config.getName(PreventHopper.fromKey(type, false)), type))
										.collect(Collectors.toList());
								e.replyChoices(choices).queue(); break;
							} else if(e.getFocusedOption().getName().equals("amount"))
								e.replyChoices(Collections.singletonList(new Command.Choice("Bán hết toàn bộ khoáng sản", "ALL"))).queue();
							break;
						case "craft":
							if(e.getFocusedOption().getName().equals("type")) {
								List<Command.Choice> choices = Stream.of(PreventHopper.input)
										.filter(type -> type.startsWith(e.getFocusedOption().getValue()) && !type.equals("STONE"))
										.map(type -> new Command.Choice(Library.config.getName(PreventHopper.fromKey(type, false)), type))
										.collect(Collectors.toList());
								e.replyChoices(choices).queue();
							}
							break;
						default:
							e.replyChoices(Collections.emptyList()).queue(); break;
					}
				} catch(Exception err) { e.replyChoices(Collections.emptyList()).queue(); }
			}
		}).runTaskAsynchronously(PluginBoot.main);
	}
	
	private boolean verifyInteraction(SlashCommandInteractionEvent e) {
		return e.getJDA().equals(Library.application.getApplication());
	}

}
