package me.stella.Minecraft;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.stella.Application.Library;
import org.jetbrains.annotations.NotNull;

public class CommandHyperHook implements CommandExecutor {
	
	public static final String hookPermission = "hyperhook.use.hook";
	public static final String reloadPermission = "hyperhook.use.reload";

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, String label, String[] args) {
		if(args.length < 1)
			sender.sendMessage(Library.config.getColoredMessage("syntax-connect"));
		else CommandHyperHook.hookCommand(sender, args[0]);
		return true;
	}
	
	public static void hookCommand(CommandSender sender, String code) {
		if(!(sender instanceof Player))
			sender.sendMessage(Library.config.getColoredMessage("only_player"));
		else {
			Player player = ((Player)sender);
			if(!(Library.bridge.keyExisted(code)))
				sender.sendMessage(Library.config.getColoredMessage("invalid_code"));
			else {
				String id = Library.bridge.getID(code.trim());
				Library.storage.connect(id, player.getUniqueId());
				Library.bridge.delete(code);
				player.sendMessage(Library.config.getColoredMessage("account_connected"));
				String note = Library.config.getMessage("discord_connected").replace("{player}", player.getName());
				Library.application.sendPrivateMessage(id, note);
			}
		}
	}
}
