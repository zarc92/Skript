package ch.njol.skript.lang.cache.load;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.command.Argument;
import ch.njol.skript.command.ScriptCommand;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.lang.VariableString;
import ch.njol.skript.lang.cache.BitCode;
import ch.njol.skript.lang.cache.ParsedCommand;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Timespan;

public class ParsedCommandLoader implements ParsedCommand, LoadableElement<ScriptCommand> {
	
	private String name;
	private String pattern;
	
	private List<Argument<?>> arguments;
	@Nullable
	private String usage;
	@Nullable
	private String description;
	private List<String> aliases;
	
	@Nullable
	private String permission;
	@Nullable
	private String permissionMessage;
	private int executableBy;
	
	@Nullable
	private Timespan cooldown;
	@Nullable
	private BitCodeLoader cooldownMessage;
	@Nullable
	private String cooldownBypass;
	@Nullable
	private BitCodeLoader cooldownStorage;
	
	private BitCodeLoader trigger;
	
	public ParsedCommandLoader() {
		this.name = "";
		this.pattern = "";
		this.arguments = new ArrayList<>();
		this.aliases = new ArrayList<>();
		this.trigger = new BitCodeLoader();
	}
	
	@Override
	public void name(String name) {
		this.name = name;
	}
	
	@Override
	public void pattern(String pattern) {
		this.pattern = pattern;
	}

	@Override
	public void argument(@Nullable String name, Class<?> type, @Nullable String def, boolean single, boolean optional) {
		ClassInfo<?> info = Classes.getExactClassInfo(type);
		assert info != null;
		arguments.add(Argument.newInstance(name, info, def, arguments.size(), single, optional));
	}

	@Override
	public void usage(String usage) {
		this.usage = usage;
	}

	@Override
	public void description(String desc) {
		this.description = desc;
	}

	@Override
	public void aliases(List<String> aliases) {
		this.aliases = aliases;
	}

	@Override
	public void permission(String permission) {
		this.permission = permission;
	}

	@Override
	public void permissionMessage(String message) {
		this.permissionMessage = message;
	}

	@Override
	public void executableBy(int executable) {
		this.executableBy = executable;
	}

	@Override
	public void cooldown(Timespan time) {
		this.cooldown = time;
	}

	@Override
	public BitCode cooldownMessage() {
		if (cooldownMessage == null)
			cooldownMessage = new BitCodeLoader();
		assert cooldownMessage != null;
		return cooldownMessage;
	}

	@Override
	public void cooldownBypass(String bypass) {
		this.cooldownBypass = bypass;
	}

	@Override
	public BitCode cooldownStorage() {
		if (cooldownStorage == null)
			cooldownStorage = new BitCodeLoader();
		assert cooldownStorage != null;
		return cooldownStorage;
	}

	@Override
	public BitCode trigger() {
		return trigger;
	}

	@Override
	public ScriptCommand load() {
		VariableString cooldownMsg = null;
		if (cooldownMessage != null)
			cooldownMsg = (VariableString) cooldownMessage.load().pop();
		VariableString storageVar = null;
		if (cooldownStorage != null)
			storageVar = (VariableString) cooldownStorage.load().pop();
		
		List<TriggerItem> items; // TODO ouch, this needs large-scale Skript changes
	}
	
}
