package me.stella.Internal;

import java.io.File;
import java.util.List;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import me.stella.Main.PluginImpl;

public class DataConfig {
	
	private File file;
	private FileConfiguration pluginConfig;
	
	public DataConfig(String directory) {
		loadConfig(directory);
	}
	
	public void loadConfig(String directory) {
		this.file = new File(directory);
		this.pluginConfig = YamlConfiguration.loadConfiguration(this.file);
	}
	
	public File getConfigFile() {
		return this.file;
	}
	
	public String getMessage(String key) {
		if(!(this.pluginConfig.contains("messages." + key)))
			return null;
		return this.pluginConfig.getString("messages." + key).replace("{prefix}", getPrefix());
	}
	
	public String getColoredMessage(String key) {
		return PluginImpl.color(getMessage(key));
	}
	
	public String getCommand(String command) {
		if(!(this.pluginConfig.contains("commands." + command)))
			return "This is a HyperHook bot command!";
		return this.pluginConfig.getString("commands." + command);
	}

	public String getName(String type) {
		if(!(this.pluginConfig.contains("translate." + type)))
			return type;
		return this.pluginConfig.getString("translate." + type);
	}
	
	public String getEmote(String emote) {
		if(!(this.pluginConfig.contains("emotes." + emote)))
			return ":x:";
		return this.pluginConfig.getString("emotes." + emote);
	}
	
	public String getPrefix() {
		return this.pluginConfig.getString("config.prefix");
	}
	
	public String getServerName() {
		return this.pluginConfig.getString("config.proxy_name");
	}
	
	public String getBotToken() {
		return this.pluginConfig.getString("config.discord.bot_token");
	}
	
	public String getChatPrefix() {
		return this.pluginConfig.getString("config.discord.chat_command_prefix");
	}
	
	public String getGuildID() {
		return this.pluginConfig.getString("config.discord.guild_id");
	}
	
	public List<String> getAllowedChannels() {
		return this.pluginConfig.getStringList("config.discord.allowed_channels");
	}
	
	public boolean useSlashCommands() {
		return this.pluginConfig.getBoolean("config.discord.use_slash_commands");
	}
	
	public int getCodeLength() {
		return this.pluginConfig.getInt("config.bot.code_length");
	}
	
	public int getCodeTimeout() {
		return this.pluginConfig.getInt("config.bot.code_expire");
	}
	
	public boolean useNormalCharacters() {
		return this.pluginConfig.getBoolean("config.bot.characters.normal");
	}
	
	public boolean useUpcaseCharacters() {
		return this.pluginConfig.getBoolean("config.bot.characters.upcase");
	}
	
	public boolean useNumbers() {
		return this.pluginConfig.getBoolean("config.bot.characters.numbers");
	}
	
	public String getActivityType() {
		return this.pluginConfig.getString("config.bot.rich_presence.type");
	}
	
	public String getActivityTitle() {
		return this.pluginConfig.getString("config.bot.rich_presence.title");
	}

	public long getButtonExpiration() { return this.pluginConfig.getLong("interactions.view.max-time"); }

}
