package me.stella.Application;

import me.stella.Bridge.BridgeHook;
import me.stella.Bridge.StorageHook;
import me.stella.Discord.BotImpl;
import me.stella.Executor.CommandManager;
import me.stella.Internal.BungeeEcho;
import me.stella.Internal.DataConfig;
import me.stella.Minecraft.PreventHopper;
import net.milkbowl.vault.economy.Economy;

public class Library {
	
	public static DataConfig config;
	
	public static BridgeHook bridge;
	
	public static StorageHook storage;
	
	public static CommandManager manager;
	
	public static BotImpl application;
	
	public static BungeeEcho echo;
	
	public static PreventHopper preventHopper;
	
	public static Economy economy;
	
}
