package ch.njol.skript.lang.cache.load;

import java.util.ArrayList;
import java.util.List;

import ch.njol.skript.command.Argument;
import ch.njol.skript.lang.cache.BitCode;
import ch.njol.skript.lang.cache.ParsedCommand;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Timespan;

public class ParsedCommandLoader implements ParsedCommand {
	
	private String name;
	private List<Argument<?>> arguments;
	
	public ParsedCommandLoader() {
		arguments = new ArrayList<>();
	}
	
	@Override
	public void name(String name) {
		this.name = name;
	}

	@Override
	public void argument(String name, Class<?> type, boolean single, boolean optional) {
		// TODO handle default arguments (expression...) somehow
	}

	@Override
	public void usage(String usage) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void description(String desc) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void aliases(List<String> aliases) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void permission(String permission) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void permissionMessage(String message) {
		// TODO Auto-generated method stub
		
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
