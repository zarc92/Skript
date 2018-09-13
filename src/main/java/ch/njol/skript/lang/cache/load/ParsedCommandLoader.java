package ch.njol.skript.lang.cache.load;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.ScriptLoader;
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
	private String usage;
	private String description;
	private List<String> aliases;
	
	private String permission;
	@Nullable
	private BitCodeLoader permissionMessage;
	private int executableBy;
	
	@Nullable
	private Timespan cooldown;
	@Nullable
	private BitCodeLoader cooldownMessage;
	private String cooldownBypass;
	@Nullable
	private BitCodeLoader cooldownStorage;
	
	private BitCodeLoader trigger;
	
	public ParsedCommandLoader() {
		this.name = "";
		this.pattern = "";
		this.arguments = new ArrayList<>();
		this.usage = "";
		this.description = "";
		this.aliases = new ArrayList<>();
		this.permission = "";
		this.cooldownBypass = "";
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
	public BitCode permissionMessage() {
		if (permissionMessage == null)
			permissionMessage = new BitCodeLoader();
		assert permissionMessage != null;
		return permissionMessage;
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
		VariableString permissionMsg = null;
		if (permissionMessage != null)
			permissionMsg = (VariableString) permissionMessage.load().pop();
		VariableString cooldownMsg = null;
		if (cooldownMessage != null)
			cooldownMsg = (VariableString) cooldownMessage.load().pop();
		VariableString storageVar = null;
		if (cooldownStorage != null)
			storageVar = (VariableString) cooldownStorage.load().pop();
		
		List<TriggerItem> items = ScriptLoader.loadTriggerStack(trigger.load());
		return new ScriptCommand(new File("unknown"), name, pattern, arguments, description, usage, aliases, permission, permissionMsg, cooldown, cooldownMsg, cooldownBypass, storageVar, executableBy, items);
	}
	
}
