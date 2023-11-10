package me.stella.Executor.Implement;

import com.google.common.collect.ImmutableSet;
import me.stella.Application.Library;
import me.stella.Application.PluginBoot;
import me.stella.Discord.ElementBuilder;
import me.stella.Executor.DefaultPerms;
import me.stella.Executor.DiscordExecutor;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;

public class CommandEcho extends DiscordExecutor {
    public CommandEcho() {
        super("echo", Library.config.getCommand("echo"), null, DefaultPerms.PERMS);
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent e) {
        (new BukkitRunnable() {
            @Override
            public void run() {
                ImmutableSet<String> proxies = Library.echo.getProxies();
                if(proxies.size() > 1) {
                    Map<String, String> pool = new HashMap<>();
                    proxies.forEach(e -> {
                        String id = Library.echo.getProxyID(e);
                        if(id != null) {
                            String mention = "<@" + id + ">";
                            pool.put(e, mention);
                        }
                    });
                    MessageEmbed embed = ElementBuilder.buildEchoDisplay(pool);
                    e.replyEmbeds(embed).setEphemeral(true).queue();
                } else e.reply(Library.config.getMessage("only_proxy").replace("{proxy}", Library.echo.getProxyName()))
                        .setEphemeral(true).queue();
            }
        }).runTaskAsynchronously(PluginBoot.main);
    }

    @Override
    public void onChatCommand(MessageReceivedEvent e) {
        final TextChannel channel = e.getChannel().asTextChannel();
        final String reference = e.getMessageId();
        (new BukkitRunnable() {
            @Override
            public void run() {
                ImmutableSet<String> proxies = Library.echo.getProxies();
                if(proxies.size() > 1) {
                    Map<String, String> pool = new HashMap<>();
                    proxies.forEach(e -> {
                        String id = Library.echo.getProxyID(e);
                        if(id != null) {
                            String mention = "<@" + id + ">";
                            pool.put(e, mention);
                        }
                    });
                    MessageEmbed embed = ElementBuilder.buildEchoDisplay(pool);
                    channel.sendMessageEmbeds(embed).setMessageReference(reference).queue();
                } else channel.sendMessage(Library.config.getMessage("only_proxy").replace("{proxy}", Library.echo.getProxyName()))
                        .setMessageReference(reference).queue();
            }
        }).runTaskAsynchronously(PluginBoot.main);
    }

    @Override
    public String buildSuccessReply(Object... params) {
        throw new UnsupportedOperationException("Yea im still not using this");
    }
}
