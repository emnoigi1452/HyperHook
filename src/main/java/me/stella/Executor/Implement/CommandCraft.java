package me.stella.Executor.Implement;

import com.darkevan.PreventHopperOre.Utils.PlayerData;
import me.stella.Application.Library;
import me.stella.Application.PluginBoot;
import me.stella.Discord.ElementBuilder;
import me.stella.Executor.DefaultPerms;
import me.stella.Executor.DiscordExecutor;
import me.stella.Minecraft.PreventHopper;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collections;
import java.util.Objects;
import java.util.UUID;

public class CommandCraft extends DiscordExecutor {

    public CommandCraft() {
        super("craft", Library.config.getCommand("craft"),
                Collections.singletonList(CompactParams.evaluate("type", "Loại khoáng sản muốn craft", true)),
                DefaultPerms.PERMS);
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent e) {
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
                    final String type = Objects.requireNonNull(e.getOption("type")).getAsString();
                    String storageKey = PreventHopper.fromKey(type, false);
                    if(storageKey.equals("AIR"))
                        e.reply(Library.config.getMessage("invalid_type")).setEphemeral(true).queue();
                    else {
                        final UUID playerID = Library.storage.getClientID(author);
                        Player player = (Player) Bukkit.getOfflinePlayer(playerID);
                        if(!player.hasPermission("prevent-block.discord")) {
                            e.reply(Library.config.getMessage("host_no_perm"))
                                    .setEphemeral(true).queue();
                            return;
                        }
                        PlayerData data = Library.preventHopper.getPlayerData(player);
                        if(data == null)
                            throw new RuntimeException("Dafug is going on brev");
                        else initCraftTask(e, player.getName(), storageKey, data);
                    }
                } catch(Exception err) {
                    err.printStackTrace();
                    e.reply(Library.config.getMessage("error")).queue();
                }
            }
        }).runTask(PluginBoot.main);
    }

    private void initCraftTask(SlashCommandInteractionEvent e, String name, String storageKey, PlayerData data) {
        int blocks = data.getBlock(storageKey) / 9; int count = blocks * 9;
        String param = "%javascript_prevent-block_condense," + PreventHopper.toKey(storageKey, false).toLowerCase() + "%";
        String idBlock = PreventHopper.fromKey(PreventHopper.toKey(storageKey, false), true);
        e.reply(Library.config.getMessage("crafted")
                .replace("{count}", ElementBuilder.formatter.format(count))
                .replace("{type}", Library.config.getEmote(storageKey))
                .replace("{blocks}", ElementBuilder.formatter.format(blocks))
                .replace("{block}", Library.config.getEmote(idBlock)))
                .setEphemeral(true).queue();
        (new BukkitRunnable() {
            @Override
            public void run() {
                Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "papi parse " + name + " " + param);
            }
        }).runTaskLater(PluginBoot.main, 10L);
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
        final String content = e.getMessage().getContentRaw();
        (new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    String[] components = content.split(" ");
                    if(components.length < 2) {
                        channel.sendMessage(Library.config.getMessage("type_unspecified"))
                                .setMessageReference(id).queue();
                        return;
                    }
                    String type = PreventHopper.fromKey(components[1], false);
                    if(type.equals("AIR"))
                        channel.sendMessage(Library.config.getMessage("invalid_type"))
                                .setMessageReference(id).queue();
                    else {
                        final UUID playerID = Library.storage.getClientID(author);
                        Player player = (Player) Bukkit.getOfflinePlayer(playerID);
                        if(!player.hasPermission("prevent-block.discord")) {
                            channel.sendMessage(Library.config.getMessage("host_no_perm"))
                                    .setMessageReference(id).queue();
                            return;
                        }
                        PlayerData data = Library.preventHopper.getPlayerData(player);
                        if(data == null)
                            throw new RuntimeException("Dafug is going on brev");
                        else initCraftTask(channel, id, player.getName(), type, data);
                    }
                } catch(Exception err) {
                    err.printStackTrace();
                    channel.sendMessage(Library.config.getMessage("error"))
                            .setMessageReference(id).queue();
                }
            }
        }).runTask(PluginBoot.main);
    }

    private void initCraftTask(TextChannel channel, long id, String name, String storageKey, PlayerData data) {
        int blocks = data.getBlock(storageKey) / 9; int count = blocks * 9;
        String param = "%javascript_prevent-block_condense," + PreventHopper.toKey(storageKey, false).toLowerCase() + "%";
        String idBlock = PreventHopper.fromKey(PreventHopper.toKey(storageKey, false), true);
        channel.sendMessage(Library.config.getMessage("crafted")
                        .replace("{count}", ElementBuilder.formatter.format(count))
                        .replace("{type}", Library.config.getEmote(storageKey))
                        .replace("{blocks}", ElementBuilder.formatter.format(blocks))
                        .replace("{block}", Library.config.getEmote(idBlock)))
                .setMessageReference(id).queue();
        (new BukkitRunnable() {
            @Override
            public void run() {
                Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "papi parse " + name + " " + param);
            }
        }).runTaskLater(PluginBoot.main, 10L);
    }

    @Override
    public String buildSuccessReply(Object... params) {
        throw new UnsupportedOperationException("Yea this thing is still not used");
    }
}
