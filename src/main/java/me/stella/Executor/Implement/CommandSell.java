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
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class CommandSell extends DiscordExecutor {

    public static final List<CompactParams> params = Arrays.asList(
            CompactParams.evaluate("type", "Loại khoáng sản muốn bán", true),
            CompactParams.evaluate("amount", "Số lượng muốn bán", true, false)
    );

    public CommandSell() {
        super("sell", Library.config.getCommand("sell"), params, DefaultPerms.PERMS);
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
                        PlayerData data = Library.preventHopper.getPlayerData(playerID);
                        if(data == null)
                            throw new RuntimeException("What the fuck. Why is the data null");
                        else {
                            OptionMapping amountOption = e.getOption("amount"); int amount;
                            if(amountOption == null)
                                amount = -1;
                            else {
                                String strRep = amountOption.getAsString();
                                if(strRep.equalsIgnoreCase("ALL"))
                                    amount = -1;
                                else {
                                    amount = readInt(strRep);
                                    if(amount == -2) {
                                        e.reply(Library.config.getMessage("invalid_num"))
                                                .setEphemeral(true).queue();
                                        return;
                                    }
                                }
                            }
                            initSlashSellProtocol(e, playerID, data, storageKey, amount);
                        }
                    }
                } catch(Exception err) {
                    err.printStackTrace();
                    e.reply(Library.config.getMessage("error")).queue();
                }
            }
        }).runTaskAsynchronously(PluginBoot.main);
    }

    private int readInt(String strRep) {
        try {
            return Integer.parseInt(strRep);
        } catch(Exception err) { return -2; }
    }

    private void initSlashSellProtocol(SlashCommandInteractionEvent e, UUID uid, PlayerData data, String storageKey, int amount) {
        (new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    if(amount == 0) {
                        e.reply(Library.config.getMessage("invalid_num"))
                                .setEphemeral(true).queue();
                        return;
                    }
                    Economy economy = Library.economy; double earning = 0; int quantity = 0;
                    if(amount == -1) {
                        int count = data.getBlock(storageKey); quantity = count;
                        earning = Library.preventHopper.getPrice(storageKey) * count;
                        data.setBlock(storageKey, 0);
                    } else {
                        int balance = data.getBlock(storageKey);
                        if(balance < amount) {
                            e.reply(Library.config.getMessage("insufficient_storage")
                                            .replace("{amount}", ElementBuilder.formatter.format(amount))
                                            .replace("{type}", Library.config.getEmote(storageKey)))
                                    .setEphemeral(true).queue();
                            return;
                        }
                        quantity = amount;
                        earning = Library.preventHopper.getPrice(storageKey) * amount;
                        data.setBlock(storageKey, (balance - amount));
                    }
                    economy.depositPlayer(Bukkit.getOfflinePlayer(uid), earning);
                    e.reply(Library.config.getMessage("sell_output")
                                    .replace("{amount}", ElementBuilder.formatter.format(quantity))
                                    .replace("{type}", Library.config.getEmote(storageKey))
                                    .replace("{price}", ElementBuilder.formatter.format(earning)))
                            .setEphemeral(true).queue();
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
                        final UUID uid = Library.storage.getClientID(author);
                        PlayerData data = Library.preventHopper.getPlayerData(uid);
                        if(data == null)
                            throw new RuntimeException("What the fuck happened, why is ur data null");
                        else {
                            int count = 0;
                            if(components.length < 3)
                                count = -1;
                            else {
                                String rep = components[2].trim().toUpperCase();
                                if(rep.equals("ALL"))
                                    count = -1;
                                else {
                                    count = readInt(rep);
                                    if(count == -2) {
                                        channel.sendMessage(Library.config.getMessage("invalid_num"))
                                                .setMessageReference(id).queue();
                                        return;
                                    }
                                }
                            }
                            initChatSellProtocol(channel, id, uid, data, type, count);
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

    private void initChatSellProtocol(TextChannel t, long reference, UUID uid, PlayerData data, String storageKey, int amount) {
        (new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    if(amount == 0) {
                        t.sendMessage(Library.config.getMessage("invalid_num"))
                                .setMessageReference(reference).queue();
                        return;
                    }
                    Economy economy = Library.economy; double earning = 0; int quantity = 0;
                    if(amount == -1) {
                        int count = data.getBlock(storageKey); quantity = count;
                        earning = Library.preventHopper.getPrice(storageKey) * count;
                        data.setBlock(storageKey, 0);
                    } else {
                        int balance = data.getBlock(storageKey);
                        if(balance < amount) {
                            t.sendMessage(Library.config.getMessage("insufficient_storage")
                                            .replace("{amount}", ElementBuilder.formatter.format(amount))
                                            .replace("{type}", Library.config.getEmote(storageKey)))
                                    .setMessageReference(reference).queue();
                            return;
                        }
                        quantity = amount;
                        earning = Library.preventHopper.getPrice(storageKey) * amount;
                        data.setBlock(storageKey, (balance - amount));
                    }
                    economy.depositPlayer(Bukkit.getOfflinePlayer(uid), earning);
                    t.sendMessage(Library.config.getMessage("sell_output")
                                    .replace("{amount}", ElementBuilder.formatter.format(quantity))
                                    .replace("{type}", Library.config.getEmote(storageKey))
                                    .replace("{price}", ElementBuilder.formatter.format(earning)))
                            .setMessageReference(reference).queue();
                } catch(Exception err) {
                    err.printStackTrace();
                    t.sendMessage(Library.config.getMessage("error")).queue();
                }
            }
        }).runTaskAsynchronously(PluginBoot.main);
    }

    @Override
    public String buildSuccessReply(Object... params) {
        throw new UnsupportedOperationException("This command also does not use this");
    }
}
