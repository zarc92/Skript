package ch.njol.skript.lang.cache.load;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.command.Argument;
import ch.njol.skript.lang.cache.BitCode;
import ch.njol.skript.lang.cache.ParsedCommand;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Timespan;

public class ParsedCommandLoader implements ParsedCommand {
	
	private String name;
	private List<Argument<?>> arguments;
	private String usage;
	private String description;
	private List<String> aliases;
	private String permission;
	private String permissionMessage;
	
	
	public ParsedCommandLoader() {
		name = "";
		arguments = new ArrayList<>();
	}
	
	@Override
	public void name(String name) {
		this.name = name;
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
		// TODO Auto-generated method stub
		
	}

	@Override
	public void cooldown(Timespan time) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public BitCode cooldownMessage() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void cooldownBypass(String bypass) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public BitCode cooldownStorage() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BitCode trigger() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
