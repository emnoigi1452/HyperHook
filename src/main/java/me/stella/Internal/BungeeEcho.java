package me.stella.Internal;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

import com.google.common.collect.ImmutableSet;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import me.stella.Application.Library;
import me.stella.Application.PluginBoot;
import org.bukkit.scheduler.BukkitRunnable;

public class BungeeEcho implements PluginMessageListener {
	
	public static final String ECHO_CHANNEL = "HyperHook.Connect.Bungee";
	
	private String proxyName;
	private Map<String, String> proxies;
	
	public BungeeEcho(String name) {
		this.proxyName = name;
		this.proxies = Collections.synchronizedMap(new HashMap<String, String>());
		this.proxies.put(this.proxyName, Library.application.getApplication().getSelfUser().getId());
	}
	
	public synchronized void pushUpdate(String oldProxy, String newProxy) {
		BungeeEcho.sendEchoPacket("edit", oldProxy, newProxy);
	}
	
	public String getProxyName() {
		return this.proxyName;
	}
	
	public ImmutableSet<String> getProxies() {
		return ImmutableSet.copyOf(this.proxies.keySet());
	}
	
	public String getProxyID(String proxyName) {
		return this.proxies.getOrDefault(proxyName, null);
	}
	
	public synchronized void writeProxy(String proxy, String botID) {
		if(this.proxies.containsKey(proxy))
			return;
		this.proxies.put(proxy, botID);
	}
	
	public synchronized void deleteProxy(String proxy) {
		this.proxies.remove(proxy);
	}
	
	public synchronized void changeProxy(String oldProxy, String newProxy, String botID) {
		this.proxies.remove(oldProxy);
		this.proxies.put(newProxy, botID);
	}

	@Override
	public void onPluginMessageReceived(final String channel, final Player player, final byte[] message) {
		if(!(channel.equals("BungeeCord")))
			return;
		try {
			new Thread(() -> {
				ByteArrayDataInput input = ByteStreams.newDataInput(message);
				if(!(input.readUTF().equals(BungeeEcho.ECHO_CHANNEL)))
					return;
				short payloadSize = input.readShort();
				byte[] payload = new byte[(int)payloadSize];
				input.readFully(payload, 0, (int)payloadSize);
				ByteArrayDataInput packet = ByteStreams.newDataInput(payload);
				String requestType = packet.readUTF();
				String botID = packet.readUTF();
				int paramLen = packet.readShort();
				String[] params = new String[paramLen];
				for(int i = 0; i < paramLen; i++)
					params[i] = packet.readUTF();
				// not accepting request from your own echo
				if(params[0].equals(Library.echo.getProxyName()))
					return;
				switch(requestType.toLowerCase()) {
					case "write":
						Library.echo.writeProxy(params[0], botID);
						BungeeEcho.sendEchoPacket("write", getProxyName());
						break;
					case "delete":
						Library.echo.deleteProxy(params[0]);
						break;
					case "edit":
						Library.echo.changeProxy(params[0], params[1], botID);
						break;
				}
			}).start();
		} catch(Exception err) { err.printStackTrace(); }
	}
	
	public static void sendEchoPacket(final String type, final String... name) {
		final ByteArrayDataOutput output = ByteStreams.newDataOutput(), payload = ByteStreams.newDataOutput();
		(new BukkitRunnable() {
			@Override
			public void run() {
				try {
					output.writeUTF("Forward");
					output.writeUTF("ONLINE");
					output.writeUTF(BungeeEcho.ECHO_CHANNEL);
					payload.writeUTF(type);
					payload.writeUTF(Library.application.getApplication().getSelfUser().getId());
					payload.writeShort(name.length);
					for(String param: name)
						payload.writeUTF(param);
					byte[] payloadBytes = payload.toByteArray();
					output.writeShort(payloadBytes.length);
					output.write(payloadBytes);
				} catch(Exception err) { err.printStackTrace(); }
				PluginBoot.main.getServer().sendPluginMessage(PluginBoot.random(), "BungeeCord", output.toByteArray());
			}
		}).runTask(PluginBoot.random());
	}

	public static void sendTempPacket(final String type, final String... name) {
		final ByteArrayDataOutput output = ByteStreams.newDataOutput(), payload = ByteStreams.newDataOutput();
		final Plugin ran = PluginBoot.random();
		Bukkit.getMessenger().registerOutgoingPluginChannel(ran, "BungeeCord");
		(new BukkitRunnable() {
			@Override
			public void run() {
				try {
					output.writeUTF("Forward");
					output.writeUTF("ONLINE");
					output.writeUTF(BungeeEcho.ECHO_CHANNEL);
					payload.writeUTF(type);
					payload.writeUTF(Library.application.getApplication().getSelfUser().getId());
					payload.writeShort(name.length);
					for(String param: name)
						payload.writeUTF(param);
					byte[] payloadBytes = payload.toByteArray();
					output.writeShort(payloadBytes.length);
					output.write(payloadBytes);
				} catch(Exception err) { err.printStackTrace(); }
				PluginBoot.main.getServer().sendPluginMessage(ran, "BungeeCord", output.toByteArray());
				Bukkit.getMessenger().unregisterOutgoingPluginChannel(ran, "BungeeCord");
			}
		}).runTask(PluginBoot.random());
	}
	
	
}
