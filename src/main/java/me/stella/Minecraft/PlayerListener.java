package me.stella.Minecraft;

import me.stella.Application.Library;
import me.stella.Application.PluginBoot;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

public class PlayerListener implements Listener {
	
	@EventHandler
	public void onQuit(PlayerQuitEvent e) {
		final UUID uid = e.getPlayer().getUniqueId();
		(new BukkitRunnable() {
			@Override
			public void run() {
				if(Library.storage.clientConnected(uid)) {
					String id = Library.storage.getID(uid);
					Library.storage.disconnect(uid);
					Library.application.sendPrivateMessage(id, Library.config.getMessage("disconnect")
							.replace("{reason}", "ACCOUNT_LOGOUT"));
				}
			}
		}).runTaskAsynchronously(PluginBoot.main);
	}

}
