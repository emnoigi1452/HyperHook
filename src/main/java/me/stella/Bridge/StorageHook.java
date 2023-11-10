package me.stella.Bridge;

import me.stella.Application.Library;
import me.stella.Application.PluginBoot;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class StorageHook {
	
	private final Map<String, UUID> storage;
	
	public StorageHook() {
		this.storage = Collections.synchronizedMap(new HashMap<String, UUID>());
	}
	
	public synchronized void connect(String id, UUID uuid) {
		this.storage.put(id, uuid);
	}
	
	public synchronized void disconnect(String id) {
		this.storage.remove(id);
	}
	
	public synchronized void disconnect(UUID id) {
		String userID = getID(id);
		if(userID == null || userID.length() < 1)
			return;
		this.storage.remove(userID);
	}

	public synchronized String getID(UUID uid) {
		for(Map.Entry<String, UUID> set: storage.entrySet()) {
			if(set.getValue().equals(uid))
				return set.getKey();
		}
		return null;
	}
	
	public boolean userConnected(String id) {
		return this.storage.containsKey(id);
	}
	
	public boolean clientConnected(UUID id) {
		return this.storage.values().contains(id);
	}
	
	public UUID getClientID(String id) {
		return this.storage.get(id);
	}
	
	public void close() {
		(new Thread(() -> {
			storage.keySet().forEach((id) -> {
				Library.application.sendPrivateMessageOnThread(id,
						Library.config.getMessage("disconnect").replace("{reason}", "SYSTEM_SHUTDOWN"));
			});
		})).start();
	}

}
