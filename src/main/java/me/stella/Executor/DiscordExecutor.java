package me.stella.Executor;

import java.util.List;
import java.util.Map;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public abstract class DiscordExecutor {
	
	private String name;
	private String description;
	private List<CompactParams> params;
	private Permission[] permissions;
	
	public DiscordExecutor(String name, String description, List<CompactParams> params, Permission[] permissions) {
		this.name = name;
		this.description = description;
		this.params = params;
		this.permissions = permissions;
	}
	
	public String getName() {
		return this.name;
	}
	
	public String getDescription() {
		return this.description;
	}
	
	public List<CompactParams> getParameters() {
		return this.params;
	}

	public Permission[] getPermissions() {
		return this.permissions;
	}
	
	public abstract void onSlashCommand(SlashCommandInteractionEvent e);
	
	public abstract void onChatCommand(MessageReceivedEvent e);
	
	public abstract String buildSuccessReply(Object... params);

	public static class CompactParams {
		private String name;
		private String description;
		private boolean autocomplete;
		private boolean required;

		public CompactParams(String name, String description, boolean autocomplete, boolean required) {
			this.name = name;
			this.description = description;
			this.autocomplete = autocomplete;
			this.required = required;
		}

		public String getName() {
			return this.name;
		};

		public String getDescription() {
			return this.description;
		}

		public boolean isRequired() {
			return this.required;
		}

		public boolean canAutoComplete() { return this.autocomplete; }

		public static CompactParams evaluate(String a, String b, boolean c, boolean d) {
			return new CompactParams(a, b, c, d);
		}

		public static CompactParams evaluate(String a, String b, boolean c) {
			return evaluate(a, b, c,true);
		}

		public static CompactParams evaluate(String a, String b) { return evaluate(a, b, false, true); }

	}
	
}
