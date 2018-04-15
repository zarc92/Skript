package ch.njol.skript.lang.cache;

import java.util.List;

import ch.njol.skript.util.Timespan;

public interface ParsedCommand {
	
	void name(String name);
	
	void argument(String name, Class<?> type, boolean single, boolean forceOptional);
	
	void usage(String usage);
	
	void description(String desc);
	
	void aliases(List<String> aliases);
	
	int executableBy(int executable);
	
	void cooldown(Timespan time);
	
	BitCode cooldownMessage();
	
	void cooldownBypass(String bypass);
	
	BitCode cooldownStorage();
	
	BitCode trigger();
}
