package me.stella.Bridge;

import java.security.SecureRandom;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import me.stella.Application.PluginBoot;

public class BridgeHook {
	
	public static String generateKey(int length, char[] charMap) {
		Random random = new SecureRandom();
		StringBuilder keyBuilder = new StringBuilder();
		for(int i = 0; i < length; i++)
			keyBuilder.append(charMap[random.nextInt(charMap.length)]);
		return keyBuilder.toString();
	}
	
	private final Map<String, String> bridge;
	private final long tickTimeout;
	
	public BridgeHook(long timeout) {
		this.bridge = Collections.synchronizedMap(new HashMap<String, String>());
		this.tickTimeout = timeout;
	}
	
	public long getTimeOut() {
		return this.tickTimeout;
	}
	
	public long getTimeOutSeconds() {
		return Math.floorDiv(tickTimeout, 20);
	}
	
	public synchronized void write(String key, String id) {
		this.bridge.put(key, id);
	}
	
	public String getID(String key) {
		return this.bridge.getOrDefault(key, null);
	}
	
	public synchronized void delete(String key) {
		this.bridge.remove(key);
	}
	
	public boolean keyExisted(String key) {
		return this.bridge.containsKey(key);
	}
	
	public void close() {
		bridge.clear();
	}

}
