package ch.njol.skript.lang.cache;

public interface ParsedTrigger {
	
	BitCode event();
	
	BitCode trigger();
}
