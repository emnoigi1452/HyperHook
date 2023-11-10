package me.stella.Executor.Implement;

import com.darkevan.PreventHopperOre.Utils.PlayerData;
import me.stella.Application.Library;
import me.stella.Application.PluginBoot;
import me.stella.Discord.ElementBuilder;
import me.stella.Executor.DefaultPerms;
import me.stella.Executor.DiscordExecutor;
import me.stella.Minecraft.PreventHopper;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class CommandNotify extends DiscordExecutor {

    public static Map<UUID, BukkitTask> task = new HashMap<>();

    public static Map<UUID, Long> timer = new HashMap<>();

    public static void buildTask(final UUID uid, long period) {
        task.put(uid, (new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    PlayerData dataObject = Library.preventHopper.getPlayerData(uid);
                    final String host = Library.storage.getID(uid);
                    if(dataObject == null || host == null) {
                        this.cancel();
                        return;
                    }
                    Map<String, Integer> full = new LinkedHashMap<>();
                    for(String type: PreventHopper.keys) {
                        if(dataObject.getBlock(type) >= dataObject.getLimit(type))
                            full.put(type, dataObject.getLimit(type));
                    }
                    if(full.size() > 0) {
                        MessageEmbed fullEmbed = ElementBuilder.buildFullWarning(full, timer.getOrDefault(uid, -1L));
                        Library.application.sendPrivateMessage(host, Collections.singleton(fullEmbed), (message) -> {
                            timer.put(uid, System.currentTimeMillis());
                            (new BukkitRunnable() {
                                @Override
                                public void run() {
                                    message.delete().queue();
                                }
                            }).runTaskLaterAsynchronously(PluginBoot.main, (period - 5) * 20L);
                        });
                    }
                } catch(Exception err) { err.printStackTrace(); }
            }
        }).runTaskTimerAsynchronously(PluginBoot.main, 0L, (period * 20L)));
    }

    public static void shutdownTask(UUID uid) {
        if(task.containsKey(uid)) {
            task.get(uid).cancel();
            task.remove(uid);
            timer.remove(uid);
        }
    }

    public static final Map<String, Integer> TIME = new HashMap<>();


    public CommandNotify() {
        super("notify", Library.config.getCommand("notify"),
                Collections.singletonList(CompactParams.evaluate("duration", "Thời gian giữa mỗi lần kiểm tra kho", true, false)),
                DefaultPerms.PERMS);
        TIME.put("30s", 30);
        TIME.put("1m", 60);
        TIME.put("5m", 300);
        TIME.put("15m", 900);
        TIME.put("30m", 1800);
        TIME.put("1h", 3600);
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent e) {
        final String author = Objects.requireNonNull(e.getUser()).getId();
        if(!(Library.storage.userConnected(author))) {
            e.reply(Library.config.getMessage("not_connected"))
                    .setEphemeral(true).queue();
            return;
        }
        final UUID uid = Library.storage.getClientID(author);
        (new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    if(task.containsKey(uid)) {
                        shutdownTask(uid);
                        e.reply(Library.config.getMessage("notify-off"))
                                .setEphemeral(true).queue();
                    } else {
                        if(e.getOption("duration") == null) {
                            e.reply(Library.config.getMessage("no-time"))
                                    .setEphemeral(true).queue();
                            return;
                        }
                        String out = e.getOption("duration").getAsString()
                                .trim().toLowerCase();
                        if(out.length() > 0) {
                            int time = TIME.getOrDefault(out, 1800);
                            buildTask(uid, time);
                            e.reply(Library.config.getMessage("notify-on")
                                    .replace("{time}", ElementBuilder.formatTime(time)))
                                    .setEphemeral(true).queue();
                        }
                    }
                } catch(Exception err) {
                    err.printStackTrace();
                    e.reply(Library.config.getMessage("error")).queue();
                }
            }
        }).runTaskAsynchronously(PluginBoot.main);
    }

    @Override
    public void onChatCommand(MessageReceivedEvent e) {
        final String author = Objects.requireNonNull(e.getAuthor()).getId();
        final TextChannel channel = Objects.requireNonNull(e.getChannel()).asTextChannel();
        if(!(Library.storage.userConnected(author))) {
            channel.sendMessage(Library.config.getMessage("not_connected"))
                    .setMessageReference(e.getMessageId()).queue();
            return;
        }
        final String content = e.getMessage().getContentRaw();
        final long id = e.getMessageIdLong();
        final UUID uid = Library.storage.getClientID(author);
        (new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    if(task.containsKey(uid)) {
                        shutdownTask(uid);
                        channel.sendMessage(Library.config.getMessage("notify-off"))
                                .setMessageReference(id).queue();
                    } else {
                        String[] components = content.split(" ");
                        if(components.length < 2) {
                            channel.sendMessage(Library.config.getMessage("no-time"))
                                    .setMessageReference(id).queue();
                            return;
                        }
                        String out = components[1].trim().toLowerCase();
                        if(out.length() > 0) {
                            int time = TIME.getOrDefault(out, 1800);
                            buildTask(uid, time);
                            channel.sendMessage(Library.config.getMessage("notify-on")
                                    .replace("{time}", ElementBuilder.formatTime(time)))
                                    .setMessageReference(id).queue();
                        }
                    }
                } catch(Exception err) {
                    err.printStackTrace();
                    channel.sendMessage(Library.config.getMessage("error"))
                            .setMessageReference(id).queue();
                }
            }
        }).runTaskAsynchronously(PluginBoot.main);
    }

    @Override
    public String buildSuccessReply(Object... params) {
        throw new UnsupportedOperationException("Abandoned feature basically.");
    }
}
